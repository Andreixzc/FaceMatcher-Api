package com.FaceCNN.faceRec.Dto.Response;

import com.FaceCNN.faceRec.Model.Folder;

public record FolderResponse(String name, String path, String id, String userId){


    private static final String DEFAULT_FOLDER_NAME = "Folder";


    public static FolderResponse fromFolder(Folder folder) {

        return new FolderResponse(
                Sanitize(folder.getFolderPath()),
                folder.getFolderPath(),
                folder.getId().toString(),
                folder.userId().toString()
        );

    }


    public static String Sanitize(String folderPath){

        if(folderPath == null || folderPath.isEmpty()) {
            return FolderResponse.DEFAULT_FOLDER_NAME;
        }

        return folderPath.substring(folderPath.lastIndexOf("/")).replaceAll("/", "");

    }
}
