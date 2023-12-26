package com.FaceCNN.faceRec.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.FolderRepository;
import com.FaceCNN.faceRec.Repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequestMapping("/s3")
public class S3Service {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile multipartFile, UUID id, String folderName) {
        File file = convertMultiPartFileToFile(multipartFile);
        Optional<User> user = userRepository.findById(id);
        Folder folder = new Folder();
        folder.setFolderPath(user.get().getName() + "/" + folderName);
        folder.setUser(user.get());
        user.get().getFolders().add(folder);
        userRepository.save(user.get());
        String key = folder.getFolderPath() + "/" + file.getName();
        s3Client.putObject(new PutObjectRequest(bucketName, key, file));
        return "File uploaded : " + file.getAbsolutePath();
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
