package com.FaceCNN.faceRec.Dto.Response;

import com.FaceCNN.faceRec.Model.Folder;

public record FolderResponse(String name, String path, String id, String userId){


    public static FolderResponse fromFolder(Folder folder) {

        return new FolderResponse(
                Sanitize(folder.getFolderName()),
                folder.getFolderPath(),
                folder.getId().toString(),
                folder.userId().toString()
        );

    }


    public static String Sanitize(String property){
        return property.replaceAll("/", "").replaceAll(" ", "").trim();

    }
}
