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
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

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
    public String uploadSingleFile(MultipartFile multipartFile, UUID id, String folderName) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
    
            Folder folder = new Folder();
            FolderContent folderContent = new FolderContent();
            String originalFilename = multipartFile.getOriginalFilename();
            folder.setFolderContent(folderContent);
            folder.setFolderPath(buildFolderPath(user.getName(), folderName));
            folderContent.setOriginalFileName(folder.getFolderPath() + "/" + originalFilename);
            folderContent.setPklFilename(getPklFilename(folder.getFolderPath() + "pkl/" + originalFilename));
            folder.setUser(user);
            user.getFolders().add(folder);
            folderContent.setFolder(folder);

            userRepository.save(user);
    
            String key = folder.getFolderPath() + "/" + multipartFile.getOriginalFilename();
            File file = convertMultiPartFileToFile(multipartFile);
            s3Client.putObject(new PutObjectRequest(bucketName, key, file));
            file.delete();
    
            return "File uploaded successfully: " + key;
        } catch (Exception e) {
            return "Failed to upload file. Error: " + e.getMessage();
        }
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

    public String uploadMultiFiles(List<MultipartFile> files, UUID id, String folderName) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));

            Folder folder = new Folder();
            folder.setFolderPath(buildFolderPath(user.getName(), folderName));
            folder.setUser(user);
            user.getFolders().add(folder);

            userRepository.save(user);

            for (MultipartFile file : files) {
                String key = folder.getFolderPath() + "/" + file.getOriginalFilename();
                File file2 = convertMultiPartFileToFile(file);
                s3Client.putObject(new PutObjectRequest(bucketName, key, file2));
                file2.delete();
            }

            return "Files uploaded successfully";
        } catch (Exception e) {
            return "Failed to upload files. Error: " + e.getMessage();
        }
    }

    private String buildFolderPath(String userName, String folderName) {
        Path path = Paths.get(userName, folderName);
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
}
