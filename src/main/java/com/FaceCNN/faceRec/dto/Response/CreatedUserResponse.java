package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.User;

import java.util.UUID;

public record CreatedUserResponse(UUID id, String name, String email) {
    public static CreatedUserResponse fromUser(User user) {
        return new CreatedUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
