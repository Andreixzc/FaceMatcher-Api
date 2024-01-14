package com.FaceCNN.faceRec.controller;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.FaceCNN.faceRec.service.S3Service;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<FolderResponse> upload(@RequestParam(value = "file") List<MultipartFile> files,
                                                 @RequestParam(value = "id") UUID id,
                                                 @RequestParam(value = "folderName") String folderName) {
        return (ResponseEntity.ok(s3Service.uploadFiles(files, id, folderName)));
    }

    @PostMapping("/ref")
    public ResponseEntity<List<FolderContentResponse>> uploadRef(@RequestParam(value = "file") MultipartFile file,
                                                                 @RequestParam(value = "folderPath") String pklFolderPath) {

        return ResponseEntity.ok(s3Service.checkMatch(file, pklFolderPath));
    }

}
