package com.FaceCNN.faceRec.Dto;

import java.util.List;
import java.util.UUID;

import com.FaceCNN.faceRec.Model.Folder;

public record UserDto(
        UUID id,
        String name,
        String email,
        List<Folder> folders) {
    public UserDto {

    }

}
