package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.FolderContent;

import java.util.Date;

public record FolderContentResponse(String folderContentId, String filePath, String URL,
                                    Date createdOn, String folderId, String fileName , String fileExtension){

    public static FolderContentResponse fromFolderContent(FolderContent folderContent) {

        return new FolderContentResponse
                (
                    folderContent.getId().toString(),
                    folderContent.getFilePath(),
                    folderContent.getUrl(),
                    folderContent.getCreatedOn(),
                    folderContent.folderId().toString(),
                    sanitizeFileName(folderContent.getFileName()),
                    getFileExtension(folderContent.getFileName()
                )
        );

    }

    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase().trim();
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
