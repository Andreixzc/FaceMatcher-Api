package com.FaceCNN.faceRec.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.FaceCNN.faceRec.Model.FolderContent;

public interface FolderContentRepository extends JpaRepository<FolderContent,UUID> {
    @Query("SELECT fc.originalFileName FROM FolderContent fc WHERE fc.pklFilename = :pklFilename")
    String findOriginalFileNameByPklFilename(String pklFilename);
    
}
