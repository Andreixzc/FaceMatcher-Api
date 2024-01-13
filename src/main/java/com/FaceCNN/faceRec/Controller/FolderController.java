package com.FaceCNN.faceRec.Controller;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.FaceCNN.faceRec.Service.FolderService;


@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final S3Service s3Service;

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<FolderResponse>> listFolderByUser(@PathVariable UUID userId) {
        try {
            List<FolderResponse> folders = folderService.findFolderByUserId(userId);
            return new ResponseEntity<>(folders, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFolder(@PathVariable UUID id) {
        try {
            Folder folder = folderService.findFolderById(id);

            folderService.verifyFolderOwner(folder);

            s3Service.deleteFolder(folder.getFolderPath());
            folderService.deleteFolder(folder);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
