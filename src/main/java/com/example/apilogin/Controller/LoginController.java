package com.example.apilogin.Controller;

import com.example.apilogin.Model.userModel;
import com.example.apilogin.Service.*;
import com.example.apilogin.Utility.Hash;
import com.example.apilogin.Utility.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.example.apilogin.Utility.Hash.hashPassword;

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

    @Autowired
    private UserInfoService userInfoService;


    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaResponseListener kafkaResponseListener;

    @PostMapping
    public ResponseEntity<Object> login(@RequestBody userModel requestBody) {
        // Authenticate the user locally (restore this functionality)
        boolean isAuthenticated = loginService.authenticate(requestBody.getUsername(), requestBody.getPassword());
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        // Send request to Kafka
        kafkaProducerService.sendMessage("user-requests", Long.toString(loginService.getUserByUsername(requestBody.getUsername()).getId()));

        try {
            // Wait for response from Kafka
            CompletableFuture<Object> responseFuture = kafkaResponseListener.getResponseFuture(Long.toString(loginService.getUserByUsername(requestBody.getUsername()).getId()));
            Object response = responseFuture.get(50, TimeUnit.SECONDS); // Timeout after 50 seconds

            // Return the Kafka response wrapped in ResponseEntity
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle timeout or other exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Error: Kafka processing timed out or failed.");
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
        long salt = snowflakeIdGenerator.generateId();
        String pass_salt = Long.toString(salt);
        newUser.setPassword_salt(pass_salt);

        String hashedPassword = hashPassword(newUser.getPassword(), newUser.getPassword_salt());
        newUser.setPassword(hashedPassword);

        newUser.setId(generatedId);
        boolean isCreated = userService.createUser(newUser);
        System.out.println("ID: " + generatedId);
        if (isCreated) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create user");
        }
    }
}
