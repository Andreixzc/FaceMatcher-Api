package com.FaceCNN.faceRec.controller;

import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.service.FolderService;
import com.FaceCNN.faceRec.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.FaceCNN.faceRec.util.PrincipalUtils.getLoggedUser;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final S3Service s3Service;

    @GetMapping("/list")
    public List<FolderResponse> listFoldersFromLoggedUser() {
        var userId = getLoggedUser().getId();
        return folderService.findFoldersByUserId(userId);
    }

    @GetMapping("/{id}")
    public FolderResponse listFolderById(@PathVariable UUID id) {
        Folder folder = folderService.findFolderById(id);
        return FolderResponse.fromFolder(folder);
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
