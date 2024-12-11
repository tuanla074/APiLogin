package com.example.apilogin.Controller;

import com.example.apilogin.Model.LginRequest;
import com.example.apilogin.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LginRequest request) {
        boolean isAuthenticated = loginService.authenticate(request.getUsername(), request.getPassword());

        if (isAuthenticated) {
            return ResponseEntity.ok("Success");
        } else {
            return ResponseEntity.status(401).body("Failure");
        }
    }
}

