package com.FaceCNN.faceRec.Controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.FaceCNN.faceRec.Service.S3Service;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "id") UUID id,
            @RequestParam(value = "folderName") String folderName) {

        // Your code logic here

        return ResponseEntity.ok("File uploaded successfully");
    }
}
