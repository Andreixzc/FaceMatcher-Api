package com.FaceCNN.faceRec.Controller;

import java.net.URI;

import com.FaceCNN.faceRec.Configuration.security.TokenResponse;
import com.FaceCNN.faceRec.Configuration.security.TokenService;
import com.FaceCNN.faceRec.Dto.Response.CreatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager manager;

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) {
        CreatedUser createdUser = userService.create(user);
        if (createdUser != null) {
            return ResponseEntity.created(URI.create("/users/" + createdUser.id())).body(createdUser);
        }
        return ResponseEntity.badRequest().body("Email already registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest userDto) {
        try {
            var token = new UsernamePasswordAuthenticationToken(userDto.login(), userDto.password());
            var authentication = manager.authenticate(token);

            String tokenJWT = tokenService.generateToken((User) authentication.getPrincipal());

            return ResponseEntity.ok(new TokenResponse(tokenJWT));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
