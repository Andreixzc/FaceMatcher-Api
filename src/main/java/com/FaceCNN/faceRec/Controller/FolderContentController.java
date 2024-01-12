package com.FaceCNN.faceRec.Controller;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.FaceCNN.faceRec.Service.FolderContentService;


@RestController
@RequestMapping("/folder-content")
@RequiredArgsConstructor
public class FolderContentController {

    private final FolderContentService folderContentService;

    @GetMapping("/{folderId}")
    public ResponseEntity<List<FolderContentResponse>> listFolderContentByFolderId(@PathVariable UUID folderId) {
        try {
            List<FolderContentResponse> folderContent = folderContentService.findFolderContentByFolderId(folderId);
            return new ResponseEntity<>(folderContent, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}


