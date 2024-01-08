package com.FaceCNN.faceRec.Service;

import java.util.Objects;

import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.FaceCNN.faceRec.Dto.UserLoginDto;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User create(User user) {
        if (!isEmailValid(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }
        return null;

    }

    
    public boolean isEmailValid(String email) {
        return Objects.isNull(userRepository.findByEmail(email));
    }

    public ResponseEntity<String> login(UserLoginDto userLoginDto){

        User user = userRepository.findByEmail(userLoginDto.login()).get(0);
        if (user!= null && user.getPassword().equals(passwordEncoder.encode(userLoginDto.password()))) {
            return new ResponseEntity<>("Success", HttpStatus.OK);
        }
        return new ResponseEntity<>("Incorrect credentials", HttpStatus.NOT_FOUND);
       
    }
}
