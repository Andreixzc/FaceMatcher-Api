package com.FaceCNN.faceRec.configuration.security;

import com.FaceCNN.faceRec.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("FaceRec")
                    .withSubject(user.getId().toString())
                    .withExpiresAt(expirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating JWT token", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        var algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("FaceRec")
                .build()
                .verify(tokenJWT)
                .getSubject();
    }

    private Instant expirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
