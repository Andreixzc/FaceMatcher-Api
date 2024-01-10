package com.FaceCNN.faceRec.Service;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Repository.FolderRepository;

@Service
public class FolderService {
    
    @Autowired
    private FolderRepository folderRepository;


    public List<FolderResponse> findFolderByUserId(UUID id){

        List<Folder> result = this.folderRepository.findByUserId(id);
        return result.stream().map(FolderResponse::fromFolder).toList();

    }
}
