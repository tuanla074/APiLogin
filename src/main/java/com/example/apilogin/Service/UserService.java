package com.example.apilogin.Service;

import com.example.apilogin.Model.userModel;
import com.example.apilogin.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public List<userModel> getAllUsers() {
        return userRepo.findAll();
    }

    public userModel getUserById(Long id) {
        return (userModel) userRepo.findById(id).orElse(null);
    }

    public userModel getUserByUsername(String username) {
        return (userModel) userRepo.findByUsername(username).orElse(null);
    }

    public boolean createUser(userModel user) {
        userRepo.save(user);
        return true;
    }

    public UserService() {}


    public boolean authenticate(String username, String password) {
        if(getUserByUsername(username) != null) {
            String storedPassword = getUserByUsername(username).getPassword();
            return storedPassword != null && storedPassword.equals(password);
        }
        System.out.print("user " + getUserByUsername(username) );
        System.out.print("user " + getUserById(0L) );
        return false;
    }
}
