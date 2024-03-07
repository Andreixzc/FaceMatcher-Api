package com.FaceCNN.faceRec.controller;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.service.FolderContentService;
import com.FaceCNN.faceRec.service.FolderService;
import com.FaceCNN.faceRec.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/folder-content")
@RequiredArgsConstructor
public class FolderContentController {

    private final FolderContentService folderContentService;
    private final S3Service s3Service;
    private final FolderService folderService;

    @GetMapping("/list/{folderId}")
    public List<FolderContentResponse> listFolderContentByFolderId(@PathVariable UUID folderId) {
        return folderContentService.findFolderContentByFolderId(folderId);
    }

    @Transactional
    @DeleteMapping("/{folderContentId}")
    public ResponseEntity<Void> deleteFolderContentByFolderContentId(@PathVariable UUID folderContentId) {
        FolderContent folderContent = folderContentService.findFolderContentById(folderContentId);

        folderService.verifyFolderOwner(folderContent.getFolder());

        s3Service.deleteFolderContent(folderContent);

        folderContentService.deleteFolderContent(folderContent);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/downloadAllMatches")
    public List<byte[]> downloadAllMatches(@RequestBody String[] objKeys) throws Exception {
        return s3Service.downloadFowardByte(objKeys);
    }
}   


