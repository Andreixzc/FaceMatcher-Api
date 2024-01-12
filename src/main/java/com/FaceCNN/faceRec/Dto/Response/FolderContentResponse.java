package com.FaceCNN.faceRec.Dto.Response;

import com.FaceCNN.faceRec.Model.FolderContent;

import java.util.Date;

public record FolderContentResponse(String folderContentId, String filePath, String URL,
                                    Date createdOn, String folderId, String fileName , String fileExtension){

    public static FolderContentResponse fromFolderContent(FolderContent folderContent) {

        return new FolderContentResponse
                (
                    folderContent.getId().toString(),
                    folderContent.getFilePath(),
                    folderContent.getURL(),
                    folderContent.getCreatedOn(),
                    folderContent.folderId().toString(),
                    SanitizeFileName(folderContent.getFileName()),
                    GetFileExtension(folderContent.getFileName()
                )
        );

    }

    public static String GetFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase().trim();
    }

    public static String SanitizeFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
