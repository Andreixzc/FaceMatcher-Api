package com.FaceCNN.faceRec.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.FaceCNN.faceRec.model.Folder;

public interface FolderRepository extends JpaRepository<Folder,UUID> {

    List<Folder> findByUserId(UUID userId);
    
}
