package com.FaceCNN.faceRec.repository;

import com.FaceCNN.faceRec.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);

}
