package com.FaceCNN.faceRec.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.FaceCNN.faceRec.model.FolderContent;
import org.springframework.data.repository.query.Param;

public interface FolderContentRepository extends JpaRepository<FolderContent,UUID> {
    @Query("SELECT fc.filePath FROM FolderContent fc WHERE fc.pklFilePath = :pklFilePath")
    String findFilePathByPKLFilePath(String pklFilePath);

    List<FolderContent> findFolderContentByFolderId(UUID folderId);

    Optional<FolderContent> findFolderContentByFilePath(String filePath);

    Optional<FolderContent> findFolderContentById(UUID id);

    @Modifying
    @Transactional
    @Query("DELETE FROM FolderContent WHERE id = :folderContentId")
    void deleteFolderContentById(@Param("folderContentId") UUID folderContentId);


    
}
