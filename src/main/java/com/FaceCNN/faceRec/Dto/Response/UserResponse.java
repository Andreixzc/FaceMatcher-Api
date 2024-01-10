package com.FaceCNN.faceRec.Dto.Response;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Model.Folder;

public record UserResponse(
        UUID id,
        String name,
        String email,
        List<Folder> folders) {
    public UserResponse {

    }

}
