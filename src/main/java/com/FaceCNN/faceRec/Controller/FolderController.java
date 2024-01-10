package com.FaceCNN.faceRec.Controller;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com.FaceCNN.faceRec.Service.FolderService;


@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;


    //Não sei se compensa fazer autenticação por um jwt por exemplo, talvez agnt podia deixar gambiarra e validar
    //se o cara ta logado e se o ID bate, passando o USER pelo body dessa req
    @GetMapping("/list/{id}")
    public ResponseEntity<List<FolderResponse>> listFolderByUser(@PathVariable UUID id) {
        try {
            List<FolderResponse> folders = this.folderService.findFolderByUserId(id);
            return new ResponseEntity<>(folders, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
