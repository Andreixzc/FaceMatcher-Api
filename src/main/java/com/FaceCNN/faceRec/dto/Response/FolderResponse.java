package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.Folder;

public record FolderResponse(String name, String path, String id, String userId){


    public static FolderResponse fromFolder(Folder folder) {

        return new FolderResponse(
                sanitize(folder.getFolderName()),
                folder.getFolderPath(),
                folder.getId().toString(),
                folder.userId().toString()
        );

    }


    private static String sanitize(String property){
        return property.replaceAll("/", "").replaceAll(" ", "").trim();

    }
}
