package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.Folder;

public record FolderResponse(String name, String id, String userId, String folderPath, String folderPklPath){


    public static FolderResponse fromFolder(Folder folder) {

        return new FolderResponse(
                sanitize(folder.getFolderName()),
                folder.getId().toString(),
                folder.userId().toString(),
                folder.getFolderPath(),
                folder.getFolderPklPath()
        );

    }


    private static String sanitize(String property){
        return property.replaceAll("/", "").replaceAll(" ", "").trim();

    }
}
