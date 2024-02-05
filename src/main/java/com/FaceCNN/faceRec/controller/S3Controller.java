package com.FaceCNN.faceRec.controller;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.FaceCNN.faceRec.util.PrincipalUtils.getLoggedUser;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public FolderResponse upload(@RequestParam(value = "file") List<MultipartFile> files,
                                 @RequestParam(value = "folderName") String folderName) {
        var userId = getLoggedUser().getId();
        return s3Service.uploadFiles(files, userId, folderName);
    }

    @PostMapping("/ref")
    public List<FolderContentResponse> uploadRef(@RequestParam(value = "file") MultipartFile file,
                                                 @RequestParam(value = "folderPath") String pklFolderName) {
        var userId = getLoggedUser().getId();
        String parsedId = pklFolderName.toString().split("/")[0];
        System.out.println(parsedId);
        if (userId.toString().equals(parsedId)) {
            return s3Service.checkMatch(file, pklFolderName);
            
        }
        throw new RuntimeException("Not folder owner");
    }

}
