package com.FaceCNN.faceRec.service;
import com.FaceCNN.faceRec.dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.model.FolderContent;
import com.FaceCNN.faceRec.repository.FolderContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
public class FolderContentService {

    @Autowired
    private FolderContentRepository folderContentRepository;

    public List<FolderContentResponse> findFolderContentByFolderId(UUID folderId) {

        List<FolderContent> result = this.folderContentRepository.findFolderContentByFolderId(folderId);
        result.sort(Comparator.comparing(FolderContent::getCreatedOn).reversed());

        return result.stream().map(FolderContentResponse::fromFolderContent).toList();

    }


}
