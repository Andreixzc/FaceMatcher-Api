package com.FaceCNN.faceRec.Service;

import com.FaceCNN.faceRec.Dto.Response.FolderResponse;
import com.FaceCNN.faceRec.Model.Folder;
import com.FaceCNN.faceRec.Repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.FaceCNN.faceRec.util.PrincipalUtils.getLoggedUser;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    public List<FolderResponse> findFolderByUserId(UUID id) {

        List<Folder> result = this.folderRepository.findByUserId(id);
        return result.stream().map(FolderResponse::fromFolder).toList();

    }

    public void deleteFolder(UUID id) {
        Folder folder = findFolderById(id);

        if (!folder.userId().equals(getLoggedUser().getId())) {
            throw new RuntimeException("Invalid user to delete this folder");
        }

        folderRepository.deleteById(id);
    }

    private Folder findFolderById(UUID id) {
        return folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Folder not found"));
    }
}
