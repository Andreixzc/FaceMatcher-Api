package com.FaceCNN.faceRec.dto.Response;

import com.FaceCNN.faceRec.model.User;

import java.util.UUID;

public record CreatedUser(UUID id, String name, String email) {
    public static CreatedUser fromUser(User user) {
        return new CreatedUser(user.getId(), user.getName(), user.getEmail());
    }
}
