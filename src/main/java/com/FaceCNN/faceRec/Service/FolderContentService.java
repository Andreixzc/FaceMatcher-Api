package com.FaceCNN.faceRec.Service;
import com.FaceCNN.faceRec.Dto.Response.FolderContentResponse;
import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import com.FaceCNN.faceRec.Model.FolderContent;
import com.FaceCNN.faceRec.Repository.FolderContentRepository;
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
