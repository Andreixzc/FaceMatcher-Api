package com.FaceCNN.faceRec.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Service.FolderService;
import com.amazonaws.Response;

@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;


    //Não sei se compensa fazer autenticação por um jwt por exemplo, talvez agnt podia deixar gambiarra e validar
    //se o cara ta logado e se o ID bate, passando o USER pelo body dessa req
    @GetMapping("/list/{id}")
    public ResponseEntity<?> listFolderByUser(@PathVariable UUID id) {
        try {
            List<Folder> folders = this.folderService.findFolderByUserId(id);
            if (folders != null) {
                return new ResponseEntity<>(folders, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Folder not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
