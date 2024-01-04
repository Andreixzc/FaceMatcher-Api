package com.FaceCNN.faceRec.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.FaceCNN.faceRec.Model.FolderContent;

public interface FolderContentRepository extends JpaRepository<FolderContent,UUID> {
    
}
