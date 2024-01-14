package com.FaceCNN.faceRec.service;
import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.repository.FolderContentRepository;
import com.FaceCNN.faceRec.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class FolderContentService {

    private static final Logger log = LoggerFactory.getLogger(FolderContentService.class);

    private final FolderContentRepository folderContentRepository;
    private final FolderRepository folderRepository;

    public List<FolderContentResponse> findFolderContentByFolderId(UUID folderId) {

        List<FolderContent> result = this.folderContentRepository.findFolderContentByFolderId(folderId);
        result.sort(Comparator.comparing(FolderContent::getCreatedOn).reversed());

        return result.stream().map(FolderContentResponse::fromFolderContent).toList();

    }

    public List<FolderContentResponse> findFolderContentsByFilePaths(List<String> filePaths) {

        List<FolderContent> result = new ArrayList<>();

        for(String filePath : filePaths) {
            Optional<FolderContent> folderContent = this.folderContentRepository.findFolderContentByFilePath(filePath);
            if(folderContent.isEmpty()) {
                continue;
            }
            result.add(folderContent.get());
        }

        result.sort(Comparator.comparing(FolderContent::getCreatedOn).reversed());

        return result.stream().map(FolderContentResponse::fromFolderContent).toList();

    }

    public void deleteById(FolderContent folderContent) {

        try {

            Folder folder = folderContent.getFolder();
            int folderAmountOfFiles = folder.getFolderContents().size();

            // por padrão o s3 deleta a pasta do bucket se na hora da exclusão ele checar que só existe 1 arquivo na pasta.
            // Existem work arounds que inserem um arquivo temporário na pasta antes da exclusão, mas nesse caso achei melhor
            // somente deletar a pasta junto

            if(folderAmountOfFiles <= 1) {
                folderRepository.deleteById(folder.getId());
                return;
            }

            folderContentRepository.deleteFolderContentById(folderContent.getId());


        } catch(Exception ex) {
            log.error("Error during delete folder content operation", ex);
        }


    }


}
