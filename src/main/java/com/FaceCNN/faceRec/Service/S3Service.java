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

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.FolderContent;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.FolderContentRepository;
import com.FaceCNN.faceRec.Repository.FolderRepository;
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
    
            // Check if the folder already exists for the user and folderName
            Optional<Folder> existingFolder = user.getFolders().stream()
                    .filter(folder -> folder.getFolderPath().equals(buildFolderPath(user.getId(), folderName)))
                    .findFirst();
    
            Folder folder;
            if (existingFolder.isPresent()) {
                // If the folder already exists, use it
                folder = existingFolder.get();
            } else {
                // If the folder doesn't exist, create a new one
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
}
