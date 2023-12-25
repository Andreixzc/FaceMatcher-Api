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

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequestMapping("/s3")
public class S3Service {

    @Autowired
    private UserRepository userRepository;

    public String uploadFile(MultipartFile multipartFile, UUID id, String folderName) {
        Optional<User> user = userRepository.findById(id);
        File file = convertMultiPartFileToFile(multipartFile);
        Folder folder = new Folder();
        folder.setFolderPath(user.get().getName() + "/" + folderName);
        folder.setUser(user.get());
        user.get().getFolders().add(folder);
        
        

        return "";
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
