package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.Folder;

import java.util.Date;

public record FolderResponse(String name, String id, String userId, String folderPath, String folderPklPath, Date createdAt){


    public static FolderResponse fromFolder(Folder folder) {

        return new FolderResponse(
                sanitize(folder.getFolderName()),
                folder.getId().toString(),
                folder.userId().toString(),
                folder.getFolderPath(),
                folder.getFolderPklPath(),
                folder.getCreatedAt()
        );

    }


    private static String sanitize(String property){
        return property.replaceAll("/", "").replaceAll(" ", "").trim();

    }
}
