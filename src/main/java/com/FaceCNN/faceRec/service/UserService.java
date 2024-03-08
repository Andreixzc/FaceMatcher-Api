package com.FaceCNN.faceRec.service;

import com.FaceCNN.faceRec.dto.Response.CreatedUserResponse;
import com.FaceCNN.faceRec.model.User;
import com.FaceCNN.faceRec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CreatedUserResponse create(User user) {
        if (!isEmailValid(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return CreatedUserResponse.fromUser(userRepository.save(user));
        }
        return null;

    }

    public boolean isEmailValid(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
