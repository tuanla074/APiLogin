package com.example.apilogin.Controller;

import com.example.apilogin.Model.LginRequest;
import com.example.apilogin.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LginRequest requestBod, HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();  // Get the client IP address

        // Logging input details
        logger.info("Login attempt: username={}, IP={}", requestBod.getUsername(), clientIp);

        // Example logic for validating login (replace with your real authentication logic)
        boolean isAuthenticated = loginService.authenticate(requestBod.getUsername(), requestBod.getPassword());

        if (isAuthenticated) {
            logger.info("Login successful for username={} from IP={}", requestBod.getUsername(), clientIp);
            return ResponseEntity.ok("Login successful");
        } else {
            logger.warn("Login failed for username={} from IP={}", requestBod.getUsername(), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}
