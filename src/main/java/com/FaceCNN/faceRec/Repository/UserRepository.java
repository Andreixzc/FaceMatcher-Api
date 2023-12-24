package com.FaceCNN.faceRec.Repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.FaceCNN.faceRec.Model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    List<User> findByEmail(String email);
}
