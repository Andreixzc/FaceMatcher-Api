package com.FaceCNN.faceRec.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MainControllerAdvice {

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleJWTVerificationException(TokenExpiredException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token! -> " + exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleRuntimeException(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleInternalServerError(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
