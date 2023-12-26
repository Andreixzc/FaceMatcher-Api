package com.FaceCNN.faceRec.Controller;

import java.util.List;
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

    @PostMapping("/upload/single")
    public ResponseEntity<String> uploadFile(
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "id") UUID id,
            @RequestParam(value = "folderName") String folderName) {
        s3Service.uploadSingleFile(file, id, folderName);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<String> uploadMultipleFiles(
            @RequestParam(value = "files") List<MultipartFile> files,
            @RequestParam(value = "id") UUID id,
            @RequestParam(value = "folderName") String folderName) {
        s3Service.uploadMultiFiles(files, id, folderName);
        return ResponseEntity.ok("Files uploaded successfully");
    }
}
