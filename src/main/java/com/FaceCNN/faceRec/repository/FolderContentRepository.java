package com.FaceCNN.faceRec.repository;

import com.FaceCNN.faceRec.model.FolderContent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FolderContentRepository extends JpaRepository<FolderContent, UUID> {

    @Query("SELECT fc.filePath FROM FolderContent fc WHERE fc.pklFilePath = :pklFilePath")
    String findFilePathByPKLFilePath(String pklFilePath);

    List<FolderContent> findFolderContentByFolderId(UUID folderId, Sort sort);

    List<FolderContent> findFolderContentByFilePathInOrderByCreatedAtDesc(List<String> filePaths);
}
