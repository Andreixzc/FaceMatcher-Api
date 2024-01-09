package com.FaceCNN.faceRec.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Repository.FolderRepository;

@Service
public class FolderService {
    
    @Autowired
    private FolderRepository folderRepository;


    public List<Folder> findFolderByUserId(UUID id){

        return this.folderRepository.findByUserId(id);
    }
}
