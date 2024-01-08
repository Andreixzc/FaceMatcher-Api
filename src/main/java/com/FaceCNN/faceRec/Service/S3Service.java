package com.FaceCNN.faceRec.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.FaceCNN.faceRec.Dto.FolderResponseDto;
import com.FaceCNN.faceRec.Dto.MatchesResponseDto;
import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.FolderContent;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.FolderContentRepository;
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private FolderContentRepository folderContentRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public FolderResponseDto uploadFiles(List<MultipartFile> multipartFiles, UUID userId, String folderName) {
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
                folderContent.setPklFilename(getPklFilename(folder.getFolderPath() + "pkl/" + originalFilename));
                folderContent.setFolder(folder);
                folder.getFolderContents().add(folderContent);

                String key = folderContent.getOriginalFileName();
                File file = convertMultiPartFileToFile(multipartFile);
                s3Client.putObject(new PutObjectRequest(bucketName, key, file));
                file.delete();
            }

            userRepository.save(user);

            return new FolderResponseDto(folder.getFolderPath() + "pkl", "Ok");
        } catch (Exception e) {
            return new FolderResponseDto(null, "Erro");
        }
    }

    private String buildFolderPath(UUID userId, String folderName) {
        Path path = Paths.get(userId.toString(), folderName);
        return path.toString().replace(File.separator, "/");
    }

    public MatchesResponseDto checkMatch(MultipartFile multipartFile, String pklfolderToSearch) {
        // upload ref picture:
        String folderName = "tmp";
        String key = folderName + "/" + multipartFile.getOriginalFilename();
        File file = convertMultiPartFileToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();

        // Send request to lambda:
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"ref_path\": \"" + key + "\", \"pickle_folder_key\": \"" + pklfolderToSearch
                + "\", \"bucket_name\": \"" + bucketName + "\"}";
        String lambdaFunctionUrl = "https://cixhwmjnywefsq3zi3m6aezwk40eojlm.lambda-url.sa-east-1.on.aws/";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(lambdaFunctionUrl, entity, String.class);
        List<String> resultList = parseMatchesJson(response.getBody());
        List<String> matchesKey = buildMatchesPath(resultList, pklfolderToSearch);
        List<String> originalMatchPath = getOriginalFileNames(matchesKey);
        List<String> imgUrlList = new ArrayList<>();

        for (String path : originalMatchPath) {
            imgUrlList.add(getImageUrl(bucketName, path, s3Client));
        }
        return new MatchesResponseDto(imgUrlList);
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

    public List<String> buildMatchesPath(List<String> matches, String pklFolderPath) {
        System.out.println(pklFolderPath);
        String sufix = "pkl";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pklFolderPath.lastIndexOf(sufix); i++) {
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
        List<String> output = new ArrayList<>();
        for (String item : pklFilenames) {
            output.add(this.folderContentRepository.findOriginalFileNameByPklFilename(item));
        }
        return output;
    }

    public static String getImageUrl(String bucketName, String filePath, AmazonS3 s3Client) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60; // 1 hour
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
                    filePath)
                    .withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            return url.toString();
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }

}
