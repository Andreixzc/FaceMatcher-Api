package com.FaceCNN.faceRec.service;

import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.FaceCNN.faceRec.util.PrincipalUtils.getLoggedUser;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    public List<FolderResponse> findFoldersByUserId(UUID userId) {

        List<Folder> result = folderRepository.findByUserId(userId);
        return result.stream().map(FolderResponse::fromFolder).toList();

    }

    public void deleteFolder(Folder folder) {
        folderRepository.deleteById(folder.getId());
    }

    public void verifyFolderOwner(Folder folder) {
        if (!folder.userId().equals(getLoggedUser().getId())) {
            throw new RuntimeException("You are not the owner of this folder");
        }
    }

    public Folder findFolderById(UUID id) {
        return folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Folder not found"));
    }
}
