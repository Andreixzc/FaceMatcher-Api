package com.FaceCNN.faceRec.Controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.FaceCNN.faceRec.Dto.Request.UserLoginRequest;
import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) {
        User createdUser = userService.create(user);
        if (createdUser != null) {
            return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                    .body(createdUser);
        }
        return ResponseEntity.badRequest().body("Email already registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest userDto) {

        return userService.login(userDto);
    }

}
