package com.example.apilogin.Repo;
import com.example.apilogin.Model.userModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<userModel, Long> {
    Optional<userModel> findByUsername(String username);
}
