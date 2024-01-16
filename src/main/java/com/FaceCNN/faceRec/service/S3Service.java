package com.FaceCNN.faceRec.service;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.model.User;
import com.FaceCNN.faceRec.repository.FolderContentRepository;
import com.FaceCNN.faceRec.repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String PICKLE_FILE_SUFFIX = ".pkl";
    private static final String LAMBDA_FUNCTION_URL = "https://cixhwmjnywefsq3zi3m6aezwk40eojlm.lambda-url.sa-east-1.on.aws/";

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;
    private final UserRepository userRepository;
    private final FolderContentRepository folderContentRepository;
    private final FolderContentService folderContentService;
    private final HttpService httpService;
    private final ObjectMapper objectMapper;

    public FolderResponse uploadFiles(List<MultipartFile> multipartFiles, UUID userId, String folderName) {
        try {

            log.info("Starting the file upload process");

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
                folder.setFolderName(folderName);
                folder.setFolderPklPath(folder.getFolderPath() + "pkl");
                user.addFolder(folder);
            }

            for (MultipartFile multipartFile : multipartFiles) {

                FolderContent folderContent = new FolderContent();
                String originalFilename = multipartFile.getOriginalFilename();
                folderContent.setFileName(multipartFile.getOriginalFilename());
                folderContent.setFilePath(folder.getFolderPath() + "/" + originalFilename);
                folderContent.setPklFilePath(getPklFilename(folder.getFolderPath() + "pkl/" + originalFilename));

                String key = folderContent.getFilePath();

                sendMultipartFileToS3(multipartFile, key);

                folderContent.setUrl(s3Client.getUrl(bucketName, key).toString());
                folder.addFolderContent(folderContent);
            }

            userRepository.save(user);

            log.info("Files have been successfully uploaded");

            return FolderResponse.fromFolder(folder);

        } catch (Exception ex) {
            log.error("Error during file upload process", ex);
            throw ex;
        }
    }

    public void deleteFolder(String folderPath) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(folderPath);

        ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

        // O resultado do listing pode ser truncado, então é necessário iterar sobre os resultados
        while (true) {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, objectSummary.getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }

    public void deleteFolderContent(FolderContent folderContent) {

        s3Client.deleteObject(bucketName, folderContent.getFilePath());
        s3Client.deleteObject(bucketName, folderContent.getPklFilePath());
    }

    private String buildFolderPath(UUID userId, String folderName) {
        Path path = Paths.get(userId.toString(), folderName);
        return path.toString().replace(File.separator, "/");
    }

    public List<FolderContentResponse> checkMatch(MultipartFile multipartFile, String pklFolderToSearch) {

        try {

            log.info("starting the process of finding matches between the reference image and the user's bucket images");

            //-------------Upload da imagem de referência pro bucket---------------
            String key = "tmp/" + multipartFile.getOriginalFilename();
            sendMultipartFileToS3(multipartFile, key);
            ///////////////////////////////////////////////////////////////////////////////////////

            //---------Invocando a função lambda que retorna o nome dos arquivos em PKL que houveram matches--------
            String requestBody = buildLambdaRequestBody(key, pklFolderToSearch);
            ResponseEntity<String> response = httpService.post(LAMBDA_FUNCTION_URL, requestBody);
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

            return folderContentService.findFolderContentsByFilePaths(originalMatchPath);

        } catch (Exception ex) {
            log.error("Error during the process of finding image references", ex);
            throw ex;
        }

    }

    private void sendMultipartFileToS3(MultipartFile multipartFile, String key) {
        File file = convertMultiPartFileToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();
    }

    private String buildLambdaRequestBody(String refPath, String pickleFolderKey) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "ref_path", refPath,
                    "pickle_folder_key", pickleFolderKey,
                    "bucket_name", bucketName));
        } catch (JsonProcessingException ex) {
            log.error("Error building Lambda request body", ex);
            throw new RuntimeException("Error building Lambda request body", ex);
        }
    }

    //Métodos auxiliares:
    private List<String> parseMatchesJson(String jsonResponse) {
        List<String> resultList = new ArrayList<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            if (jsonNode.has("matching_photos") && jsonNode.get("matching_photos").isArray()) {
                JsonNode photosNode = jsonNode.get("matching_photos");
                for (JsonNode photo : photosNode) {
                    resultList.add(photo.asText());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return resultList;
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException ex) {
            log.error("Error converting multipartFile to file", ex);
        }
        return convertedFile;
    }

    public String getPklFilename(String str) {
        int posToInsert = str.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < posToInsert; i++) {
            sb.append(str.charAt(i));
        }
        sb.append(PICKLE_FILE_SUFFIX);
        return sb.toString();
    }

    private List<String> buildMatchesPath(List<String> matches, String pklFolderPath) {
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

    private List<String> getOriginalFileNames(List<String> pklFilenames) {
        return pklFilenames.stream()
                .map(folderContentRepository::findFilePathByPKLFilePath)
                .toList();
    }

}
