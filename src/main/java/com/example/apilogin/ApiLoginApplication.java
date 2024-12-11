package com.example.apilogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class ApiLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiLoginApplication.class, args);
    }
    @Bean
    CommandLineRunner init() {
        return args -> {
            System.out.println("Login API is running...");
            System.out.println("Visit: http://localhost:8080/api/auth/login");
            System.out.println("Send a POST request with JSON payload {\"username\": \"user1\", \"password\": \"password1\"}");
        };
    }
}
