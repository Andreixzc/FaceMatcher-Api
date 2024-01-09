package com.FaceCNN.faceRec.Controller;

import java.util.List;
import java.util.UUID;

import org.apache.http.protocol.ResponseServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.FaceCNN.faceRec.Dto.FolderResponseDto;
import com.FaceCNN.faceRec.Dto.MatchesResponseDto;
import com.FaceCNN.faceRec.Service.S3Service;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<FolderResponseDto> upload(
            @RequestParam(value = "file") List<MultipartFile> files,
            @RequestParam(value = "id") UUID id,
            @RequestParam(value = "folderName") String folderName) {
        return(ResponseEntity.ok(s3Service.uploadFiles(files, id, folderName)));
        // return ResponseEntity.ok("Files uploaded successfully");
    }

    @PostMapping("/ref")
    public ResponseEntity<MatchesResponseDto> uploadRef(@RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "folderPath") String pklFolderPath) {
            s3Service.checkMatch(file,pklFolderPath);
            return ResponseEntity.ok( s3Service.checkMatch(file,pklFolderPath));
        
    }
    

}
