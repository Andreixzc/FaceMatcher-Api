package com.FaceCNN.faceRec.controller;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.repository.FolderContentRepository;
import com.FaceCNN.faceRec.service.FolderService;
import com.FaceCNN.faceRec.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.FaceCNN.faceRec.service.FolderContentService;


@RestController
@RequestMapping("/folder-content")
@RequiredArgsConstructor
public class FolderContentController {

    private final FolderContentService folderContentService;
    private final S3Service s3Service;
    private final FolderService folderService;
    private final FolderContentRepository folderContentRepository;

    @GetMapping("/list/{folderId}")
    public ResponseEntity<List<FolderContentResponse>> listFolderContentByFolderId(@PathVariable UUID folderId) {
        try {
            List<FolderContentResponse> folderContent = folderContentService.findFolderContentByFolderId(folderId);
            return new ResponseEntity<>(folderContent, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{folderContentId}")
    public ResponseEntity<List<FolderContentResponse>> deleteFolderContentByFolderContentId(@PathVariable UUID folderContentId) {
        try {

            FolderContent folderContent = folderContentRepository.findFolderContentById(folderContentId).orElseThrow(
                    () -> new RuntimeException("Folder Content not found")
            );

            folderService.verifyFolderOwner(folderContent.getFolder());

            s3Service.deleteFolderContent(folderContent);

            folderContentService.deleteById(folderContent);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}


