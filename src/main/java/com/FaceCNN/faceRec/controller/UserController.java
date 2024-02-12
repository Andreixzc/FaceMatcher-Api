package com.FaceCNN.faceRec.controller;

import java.net.URI;

import com.FaceCNN.faceRec.configuration.security.TokenResponse;
import com.FaceCNN.faceRec.configuration.security.TokenService;
import com.FaceCNN.faceRec.dto.Response.CreatedUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.FaceCNN.faceRec.dto.Request.UserLoginRequest;
import com.FaceCNN.faceRec.model.User;
import com.FaceCNN.faceRec.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager manager;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) {
        CreatedUserResponse createdUser = userService.create(user);
        if (createdUser != null) {
            return ResponseEntity.created(URI.create("/users/" + createdUser.id())).body(createdUser);
        }
        return ResponseEntity.badRequest().body("Email already registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest userDto) {
        try {
            var token = new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password());
            var authentication = manager.authenticate(token);

            String tokenJWT = tokenService.generateToken((User) authentication.getPrincipal());

            return ResponseEntity.ok(new TokenResponse(tokenJWT));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
