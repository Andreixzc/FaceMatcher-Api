package com.FaceCNN.faceRec.service;

import com.FaceCNN.faceRec.dto.Request.FolderRequest;
import com.FaceCNN.faceRec.dto.Response.FolderResponse;
import com.FaceCNN.faceRec.model.Folder;
import com.FaceCNN.faceRec.model.User;
import com.FaceCNN.faceRec.repository.FolderRepository;
import com.FaceCNN.faceRec.repository.UserRepository;
import com.FaceCNN.faceRec.util.FolderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.FaceCNN.faceRec.util.PrincipalUtils.getLoggedUser;

@Service
@RequiredArgsConstructor
public class FolderService extends FolderUtils {

    private final FolderRepository folderRepository;

    private final UserRepository userRepository;

    private final S3Service s3Service;


    public List<FolderResponse> findFoldersByUserId(UUID userId) {

        List<Folder> result = folderRepository.findByUserId(userId);
        return result.stream().map(FolderResponse::fromFolder).toList();

    }

    @SuppressWarnings("null")
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

    public FolderResponse createFolder(FolderRequest folderRequest) {

        UUID userId = getLoggedUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        Optional<Folder> existingFolder = user.getFolders().stream()
                .filter(folder -> folder.getFolderPath().equals(buildFolderPath(user.getId(), folderRequest.folderName())))
                .findFirst();

        if(existingFolder.isPresent()) {
            throw new RuntimeException("There is already a folder with that name");
        }

        Folder folder = new Folder();

        folder.setCreatedAt(new Date());
        folder.setFolderPath(buildFolderPath(user.getId(), folderRequest.folderName()));
        folder.setFolderName(folderRequest.folderName());
        folder.setFolderPklPath(folder.getFolderPath() + "pkl");
        user.addFolder(folder);

    //    s3Service.initializeS3Folder(folder.getFolderPath() + "/");
    //    s3Service.initializeS3Folder(folder.getFolderPklPath() + "/");

        userRepository.save(user);

        return FolderResponse.fromFolder(folder);


    }
}
