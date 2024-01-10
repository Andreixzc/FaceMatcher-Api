package com.FaceCNN.faceRec.Service;

import com.FaceCNN.faceRec.Dto.Response.CreatedUser;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CreatedUser create(User user) {
        if (!isEmailValid(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return CreatedUser.fromUser(userRepository.save(user));
        }
        return null;

    }

    public boolean isEmailValid(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
