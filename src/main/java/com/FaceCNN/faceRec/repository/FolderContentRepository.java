package com.FaceCNN.faceRec.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.FaceCNN.faceRec.model.FolderContent;

public interface FolderContentRepository extends JpaRepository<FolderContent,UUID> {
    @Query("SELECT fc.filePath FROM FolderContent fc WHERE fc.pklFilePath = :pklFilePath")
    String findFilePathByPKLFilePath(String pklFilePath);

    List<FolderContent> findFolderContentByFolderId(UUID folderId);
    
}
