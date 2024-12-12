package com.example.apilogin.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class UserInfoService {

    private final RestTemplate restTemplate;

    @Autowired
    public UserInfoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object getUserInfoByUserId(Long userId) {
        // External API URL
        String url = "http://localhost:8081/api/user-info/user/" + userId;

        // Make the GET request
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        // Return the response body as JSON
        return response.getBody();
    }
}

