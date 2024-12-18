package com.example.apilogin.Controller;

import com.example.apilogin.DTO.RegisterRequestDTO;
import com.example.apilogin.Model.userModel;
import com.example.apilogin.Service.*;
import com.example.apilogin.Utility.Hash;
import com.example.apilogin.Utility.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    private KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    private LoginKafkaService loginKafkaService;

    @Autowired
    private RegisterEventProducer registerEventProducer;

    @PostMapping
    public ResponseEntity<Object> login(@RequestBody userModel requestBod, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        logger.info("Login attempt: username={}, IP={}", requestBod.getUsername(), clientIp);

        // Authenticate the user
        boolean isAuthenticated = loginService.authenticate(requestBod.getUsername(), requestBod.getPassword());

        if (!isAuthenticated) {
            logger.warn("Login failed for username={} from IP={}", requestBod.getUsername(), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        // Get userId for the authenticated user
        Long userId = loginService.getUserByUsername(requestBod.getUsername()).getId();

        // Send userId to Kafka
        kafkaTemplate.send("user-id-topic", userId, "Requesting user info for ID: " + userId);
        logger.info("UserID sent to Kafka: {}", userId);

        // Wait for UserInfo response asynchronously
        try {
            CompletableFuture<Map<String, Object>> userInfoFuture = loginKafkaService.waitForUserInfo(userId);
            Map<String, Object> userInfo = userInfoFuture.get(); // Block until the response arrives
            logger.info("UserInfo received for userId={} from Kafka: {}", userId, userInfo);

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            logger.error("Failed to retrieve UserInfo for userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch user info");
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

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequestDTO registerRequest) {
        // Check if username already exists
        if (userService.getUserByUsername(registerRequest.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }

        // Create a userModel for users table
        userModel newUser = new userModel();
        long generatedId = snowflakeIdGenerator.generateId();
        long salt = snowflakeIdGenerator.generateId();
        newUser.setId(generatedId);
        newUser.setFullname(registerRequest.getFullname());
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword_salt(Long.toString(salt));
        newUser.setPassword(hashPassword(registerRequest.getPassword(), newUser.getPassword_salt()));

        // Save user to users table
        userService.createUser(newUser);

        // Set userId in RegisterRequestDTO and send to Kafka
        registerRequest.setId(generatedId);
        registerEventProducer.sendRegisterEvent(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }


}
