package com.example.apilogin.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class UserInfoService {

    private final RestTemplate restTemplate;

    @Autowired
    public UserInfoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object getUserInfoByUserId(Long userId) {
        // Step 1: Get the JWT token from the auth API
        String jwtToken = getAuthToken();

        // Step 2: Call the user info API using the token
        String url = "http://localhost:8081/api/user-info/user/" + userId;

        // Create headers and add the JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        // Create an HTTP entity with the headers
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        // Make the GET request with headers
        ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class
        );

        // Return the response body as JSON
        return response.getBody();
    }

    private String getAuthToken() {
        // Auth API URL
        String authUrl = "http://localhost:8081/api/auth/login";

        // Request parameters
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "admin"); // Replace with valid username
        params.add("password", "password"); // Replace with valid password

        // Create an HTTP entity with the request parameters
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        // Call the auth API to get the token
        ResponseEntity<String> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        // Extract the token from the response body
        return response.getBody();
    }
}
