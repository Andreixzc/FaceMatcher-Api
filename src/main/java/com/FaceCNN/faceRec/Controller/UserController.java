package com.FaceCNN.faceRec.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.FaceCNN.faceRec.Model.User;
import com.FaceCNN.faceRec.Service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody @Valid User user){
        return ResponseEntity.ok(userService.create(user));
    }
    
}
