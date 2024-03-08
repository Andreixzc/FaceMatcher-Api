package com.FaceCNN.faceRec.service;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.model.User;
import com.FaceCNN.faceRec.repository.FolderContentRepository;
import com.FaceCNN.faceRec.repository.UserRepository;
import com.FaceCNN.faceRec.util.FolderUtils;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class S3Service extends FolderUtils {

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
                folder.setCreatedAt(new Date());
                folder.setFolderPath(buildFolderPath(user.getId(), folderName));
                folder.setFolderName(folderName);
                folder.setFolderPklPath(folder.getFolderPath() + "pkl");
                user.addFolder(folder);
            }

            for (MultipartFile multipartFile : multipartFiles) {

                FolderContent folderContent = new FolderContent();
                String originalFilename = multipartFile.getOriginalFilename();
                folderContent.setCreatedAt(new Date());
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

        // O resultado do listing pode ser truncado, então é necessário iterar sobre os
        // resultados
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

    public List<FolderContentResponse> checkMatch(MultipartFile multipartFile, String pklFolderToSearch) {

        try {

            log.info(
                    "starting the process of finding matches between the reference image and the user's bucket images");

            // -------------Upload da imagem de referência pro bucket---------------
            String key = "tmp/" + multipartFile.getOriginalFilename();
            sendMultipartFileToS3(multipartFile, key);
            ///////////////////////////////////////////////////////////////////////////////////////

            // ---------Invocando a função lambda que retorna o nome dos arquivos em PKL que
            // houveram matches--------
            String requestBody = buildLambdaRequestBody(key, pklFolderToSearch);
            ResponseEntity<String> response = httpService.post(LAMBDA_FUNCTION_URL, requestBody);
            ///////////////////////////////////////////////////////////////////////////////////////

            // ----------- ----------------Tratando resultado da
            // requisição:--------------------------------
            // Convertendo a resposta em Json, pra lista: que ficaria assim: [imagem1.pkl,
            // imagem2.pkl,imagem3.pkl...]
            List<String> resultList = parseMatchesJson(response.getBody());

            /////////////////////////////////////////////////////////////////
            // Construindo o caminho completo do arquivo Pkl:
            // [UUID/Evento1/imagem1.pkl,UUID/Evento1/imagem2.pkl,UUID/Evento1/imagem3.pkl]
            List<String> matchesKey = buildMatchesPath(resultList, pklFolderToSearch);

            // Extraindo path original do banco de dados das imagens, sem ser do arquivo em
            // PKL.
            List<String> originalMatchPath = getOriginalFileNames(matchesKey);

            return folderContentService.findFolderContentsByFilePaths(originalMatchPath);

        } catch (Exception ex) {
            log.error("Error during the process of finding image references", ex);
            throw ex;
        }

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

    private List<String> getOriginalFileNames(List<String> pklFilenames) {
        return pklFilenames.stream()
                .map(folderContentRepository::findFilePathByPKLFilePath)
                .toList();
    }

    public void sendMultipartFileToS3(MultipartFile multipartFile, String key) {
        File file = convertMultiPartFileToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        file.delete();
    }

    public void initializeS3Folder(String key) {
        byte[] emptyContent = new byte[0];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(emptyContent);

        s3Client.putObject(bucketName, key, inputStream, new ObjectMetadata());
    }

    

    public List<File> downloadFoward(String[] objectKeys) {
        String bucketName = this.bucketName;
        List<File> downloadedFiles = new ArrayList<>();

        for (String objectKey : objectKeys) {
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.SA_EAST_1).build();
            try {
                S3Object o = s3.getObject(bucketName, objectKey);
                S3ObjectInputStream s3is = o.getObjectContent();

                File downloadedFile = new File(objectKey);
                FileOutputStream fos = new FileOutputStream(downloadedFile);
                byte[] readBuf = new byte[1024];
                int readLen;
                while ((readLen = s3is.read(readBuf)) > 0) {
                    fos.write(readBuf, 0, readLen);
                }
                s3is.close();
                fos.close();

                downloadedFiles.add(downloadedFile);
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        for (int i = 0; i < downloadedFiles.size(); i++) {
            System.out.println("Printing downloaded files" + downloadedFiles.get(i).getName());
        }
        return downloadedFiles;
    }


    public List<byte[]> downloadFowardByte(String[] objectKeys) {
        List<byte[]> downloadedFiles = new ArrayList<>();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.SA_EAST_1).build();

        try {
            for (String objectKey : objectKeys) {
                S3Object object = s3.getObject(bucketName, objectKey);
                S3ObjectInputStream s3is = object.getObjectContent();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] readBuf = new byte[1024];
                int readLen;
                while ((readLen = s3is.read(readBuf)) > 0) {
                    outputStream.write(readBuf, 0, readLen);
                }
                outputStream.close();

                downloadedFiles.add(outputStream.toByteArray());
            }
        } catch (AmazonServiceException | IOException e) {
            e.printStackTrace();
            // Handle exception
        }

        return downloadedFiles;
    }


}
