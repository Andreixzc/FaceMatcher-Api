package com.FaceCNN.faceRec.util;

import com.FaceCNN.faceRec.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class PrincipalUtils {

    private PrincipalUtils() {
    }

    public static User getLoggedUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            throw new RuntimeException("Invalid logged user");
        }
        return (User) principal;
    }
}
