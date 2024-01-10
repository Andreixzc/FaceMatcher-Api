package com.FaceCNN.faceRec.Dto.Response;

import com.FaceCNN.faceRec.Model.User;

import java.util.UUID;

public record CreatedUser(UUID id, String name, String email) {
    public static CreatedUser fromUser(User user) {
        return new CreatedUser(user.getId(), user.getName(), user.getEmail());
    }
}
