package com.FaceCNN.faceRec.Service;

import com.FaceCNN.faceRec.Dto.Response.FolderResponseOld;
import com.FaceCNN.faceRec.Dto.Response.MatchesResponse;
import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.FolderContent;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.FolderContentRepository;
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
@RequestMapping("/s3")
public class S3Service {

    private static final String PICKLE_FILE_SUFFIX = ".pkl";
    private static final String LAMBDA_FUNCTION_URL = "https://cixhwmjnywefsq3zi3m6aezwk40eojlm.lambda-url.sa-east-1.on.aws/";

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderContentRepository folderContentRepository;

    @Autowired
    private AmazonS3 s3Client;

    public FolderResponseOld uploadFiles(List<MultipartFile> multipartFiles, UUID userId, String folderName) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
            Optional<Folder> existingFolder = user.getFolders().stream()
                    .filter(folder -> folder.getFolderPath().equals(buildFolderPath(user.getId(), folderName)))
                    .findFirst();

            Folder folder = new Folder();
            if (existingFolder.isPresent()) {
                folder = existingFolder.get();
            } else {
                folder.setFolderPath(buildFolderPath(user.getId(), folderName));
                user.addFolder(folder);
            }

            for (MultipartFile multipartFile : multipartFiles) {
                FolderContent folderContent = new FolderContent();
                String originalFilename = multipartFile.getOriginalFilename();
                folderContent.setOriginalFileName(folder.getFolderPath() + "/" + originalFilename);
                folderContent.setPklFilename(getPklFilename(folder.getFolderPath() + "pkl/" + originalFilename));
                folder.addFolderContent(folderContent);

                String key = folderContent.getOriginalFileName();
                File file = convertMultiPartFileToFile(multipartFile);
                s3Client.putObject(new PutObjectRequest(bucketName, key, file));
                file.delete();
            }

            userRepository.save(user);

            return new FolderResponseOld(folder.getFolderPath() + "pkl", "Ok");
        } catch (Exception e) {
            return new FolderResponseOld(null, "Erro: " + e.getMessage());
        }
    }

    private String buildFolderPath(UUID userId, String folderName) {
        Path path = Paths.get(userId.toString(), folderName);
        return path.toString().replace(File.separator, "/");
    }

    public MatchesResponse checkMatch(MultipartFile multipartFile, String pklFolderToSearch) {
        //-------------Upload da imagem de referência pro bucket---------------
        String folderName = "tmp";
        String key = folderName + "/" + multipartFile.getOriginalFilename();
        File file = convertMultiPartFileToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();
        ///////////////////////////////////////////////////////////////////////////////////////

        //---------Invocando a função lambda que retorna o nome dos arquivos em PKL que houveram matches--------
        String requestBody = buildLambdaRequestBody(key, pklFolderToSearch);
        ResponseEntity<String> response = invokeLambda(requestBody);
        ///////////////////////////////////////////////////////////////////////////////////////

        //----------- ----------------Tratando resultado da requisição:--------------------------------
        // Convertendo a resposta em Json, pra lista: que ficaria assim: [imagem1.pkl, imagem2.pkl,imagem3.pkl...]
        List<String> resultList = parseMatchesJson(response.getBody());
        /////////////////////////////////////////////////////////////////
        //Construindo o caminho completo do arquivo Pkl:
        //[UUID/Evento1/imagem1.pkl,UUID/Evento1/imagem2.pkl,UUID/Evento1/imagem3.pkl]
        List<String> matchesKey = buildMatchesPath(resultList, pklFolderToSearch);

        //Extraindo path original do banco de dados das imagens, sem ser do arquivo em PKL.
        List<String> originalMatchPath = getOriginalFileNames(matchesKey);
        //originalMatchPath = [UUID/Evento1/imagem1.png,UUID/Evento1/imagem2.jpeg,UUID/Evento1/imagem3.png]

        List<String> imagesUrlList = originalMatchPath.stream()
                .map(this::getImageUrlByFilePath)
                .filter(Objects::nonNull)
                .toList();

        //Itero sobre essa lista, e vou pegando a url da imagem de cada um dos caminhos e retorno pro controller:
        return new MatchesResponse(imagesUrlList);
    }

    private ResponseEntity<String> invokeLambda(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return new RestTemplate().postForEntity(LAMBDA_FUNCTION_URL, entity, String.class);
    }

    private String buildLambdaRequestBody(String refPath, String pickleFolderKey) {
        // Cria corpo da requisição em JSON:
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(Map.of(
                    "ref_path", refPath,
                    "pickle_folder_key", pickleFolderKey,
                    "bucket_name", bucketName));
        } catch (JsonProcessingException e) {
            log.error("Error building Lambda request body", e);
            throw new RuntimeException("Error building Lambda request body", e);
        }
    }

    //Métodos auxiliares:
    private List<String> parseMatchesJson(String jsonResponse) {
        List<String> resultList = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            if (jsonNode.has("matching_photos") && jsonNode.get("matching_photos").isArray()) {
                JsonNode photosNode = jsonNode.get("matching_photos");
                for (JsonNode photo : photosNode) {
                    resultList.add(photo.asText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    public String getPklFilename(String str) {
        String suffix = ".pkl";
        int posToInsert = str.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < posToInsert; i++) {
            sb.append(str.charAt(i));
        }
        sb.append(suffix);
        return sb.toString();
    }

    public List<String> buildMatchesPath(List<String> matches, String pklFolderPath) {
        System.out.println(pklFolderPath);
        String suffix = "pkl";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pklFolderPath.lastIndexOf(suffix); i++) {
            sb.append(pklFolderPath.charAt(i));
        }
        String prefix = sb.toString();
        prefix = prefix + "pkl/";
        for (int i = 0; i < matches.size(); i++) {
            String originalStr = matches.get(i);
            String concatenatedString = prefix + originalStr;
            matches.set(i, concatenatedString);
        }

        return matches;
    }

    public List<String> getOriginalFileNames(List<String> pklFilenames) {
        return pklFilenames.stream()
                .map(item -> folderContentRepository.findOriginalFileNameByPklFilename(item))
                .toList();
    }

    private String getImageUrlByFilePath(String filePath) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime() + 1000 * 60 * 60; // 1 hour
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, filePath)
                    .withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            return url.toString();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }

}
