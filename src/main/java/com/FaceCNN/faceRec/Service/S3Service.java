package com.FaceCNN.faceRec.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.FolderContent;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequestMapping("/s3")
public class S3Service {

    @Autowired
    private UserRepository userRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFiles(List<MultipartFile> multipartFiles, UUID userId, String folderName) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
            Optional<Folder> existingFolder = user.getFolders().stream()
                    .filter(folder -> folder.getFolderPath().equals(buildFolderPath(user.getId(), folderName)))
                    .findFirst();

            Folder folder;
            if (existingFolder.isPresent()) {
                folder = existingFolder.get();
            } else {
                folder = new Folder();
                folder.setFolderPath(buildFolderPath(user.getId(), folderName));
                folder.setUser(user);
                user.getFolders().add(folder);
            }

            for (MultipartFile multipartFile : multipartFiles) {
                FolderContent folderContent = new FolderContent();
                String originalFilename = multipartFile.getOriginalFilename();
                folderContent.setOriginalFileName(folder.getFolderPath() + "/" + originalFilename);
                folderContent.setPklFilename(getPklFilename(folder.getFolderPath() + "/pkl/" + originalFilename));
                folderContent.setFolder(folder);
                folder.getFolderContents().add(folderContent);

                String key = folderContent.getOriginalFileName();
                File file = convertMultiPartFileToFile(multipartFile);
                s3Client.putObject(new PutObjectRequest(bucketName, key, file));
                file.delete();
            }

            userRepository.save(user);

            return "Files uploaded successfully";
        } catch (Exception e) {
            return "Failed to upload files. Error: " + e.getMessage();
        }
    }

    private String buildFolderPath(UUID userId, String folderName) {
        Path path = Paths.get(userId.toString(), folderName);
        return path.toString().replace(File.separator, "/");
    }

    public String checkMatch(MultipartFile multipartFile, String pklfolderToSearch) {

        String folderName = "tmp";
        String key = folderName + "/" + multipartFile.getOriginalFilename();
        File file = convertMultiPartFileToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();
        // upa arquivo na pasta tmp

        // fazer req pra lambda passando o path da foto
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // lembrar de passar a key do pkl
        String requestBody = "{\"ref_path\": \"" + key + "\", \"pickle_folder_key\": \"" + pklfolderToSearch
                + "\", \"bucket_name\": \"" + bucketName + "\"}";
        String lambdaFunctionUrl = "https://cixhwmjnywefsq3zi3m6aezwk40eojlm.lambda-url.sa-east-1.on.aws/";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(lambdaFunctionUrl, entity, String.class);
        List<String> resultList = parseMatchesJson(response.getBody());
        List<String> matchesKey = buildMatchesPath(resultList, pklfolderToSearch);
        //Usar matches key para recuperar o filename original dos arquivos que deram match.
        //instanciar um folder repository? e dar um findby pklfilename? dunno
        if (response.getStatusCode() == HttpStatus.OK) {

            return "Uploaded and Lambda function invoked successfully";
        } else {

            return "Uploaded, but failed to invoke Lambda function. Response: " + response.getBody();
        }
    }

    public static List<String> parseMatchesJson(String jsonResponse) {
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
        String sufix = ".pkl";
        int posToInsert = str.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < posToInsert; i++) {
            sb.append(str.charAt(i));
        }
        sb.append(sufix);
        return sb.toString();

    }

    public List<String> buildMatchesPath(List<String> matches,String bucketPath){
        String sufix = "pkl";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bucketPath.lastIndexOf(sufix); i++) {
            sb.append(bucketPath.charAt(i));
        }
        String prefix = sb.toString();
        prefix = prefix + "/";
        
        
        for (int i = 0; i < matches.size(); i++) {
            String originalStr = matches.get(i);
            String concatenatedString = prefix + originalStr;
            matches.set(i, concatenatedString);
        }
        
        return matches;
        
    }

}
