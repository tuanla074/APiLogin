package com.example.apilogin.Service;

import com.example.apilogin.Model.userModel;
import com.example.apilogin.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.apilogin.Utility.Hash.hashPassword;

@Service
public class LoginService {

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

    public LoginService() {}


    public boolean authenticate(String username, String password) {
        if(getUserByUsername(username) != null) {
            String storedPassword = getUserByUsername(username).getPassword();

            String hashedPassword = hashPassword(password, getUserByUsername(username).getPassword_salt());
            return storedPassword != null && storedPassword.equals(hashedPassword);
        }
        System.out.print("user " + getUserByUsername(username) );
        System.out.print("user " + getUserById(0L) );
        return false;
    }
}
