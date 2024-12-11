package com.example.apilogin.Controller;

import com.example.apilogin.Model.userModel;
import com.example.apilogin.Service.LoginService;
import com.example.apilogin.Service.UserService; // Assuming you have a service for handling user operations
import com.example.apilogin.Utility.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @Autowired
    private LoginService loginService;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public LoginController() {
        // Initialize with some values for machine ID and datacenter ID
        this.snowflakeIdGenerator = new SnowflakeIdGenerator(1L, 1L); // Change to your own values
    }

    @Autowired
    private UserService userService; // Assuming you have a service to manage users

    // Existing login endpoint
    @PostMapping
    public ResponseEntity<String> login(@RequestBody userModel requestBod, HttpServletRequest request) {

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

    // New GET endpoint to fetch user info
    @GetMapping("/user")
    public ResponseEntity<userModel> getUserInfo(@RequestParam String username) {
        userModel user = loginService.getUserByUsername(username);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/users")
    public List<userModel> getAllUserInfo() {
        List<userModel> users = userService.getAllUsers();
        return users;
    }

    // New POST endpoint to create a new user
    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody userModel newUser) {
        long generatedId = snowflakeIdGenerator.generateId();
        newUser.setId(generatedId);
        boolean isCreated = userService.createUser(newUser);

        if (isCreated) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create user");
        }
    }
}
