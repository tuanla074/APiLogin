package com.example.apilogin.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaConsumerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @KafkaListener(topics = "user-requests", groupId = "user-group")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);

        try {
            Object userInfo;
            ObjectMapper objectMapper = new ObjectMapper();

            Long userId = Long.parseLong(message);
            userInfo = userInfoService.getUserInfoByUserId(userId);

            // Serialize the response to a JSON string
            String jsonResponse = objectMapper.writeValueAsString(userInfo);

            // Send the JSON response to Kafka
            kafkaTemplate.send("user-responses", jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "{\"error\": \"Error processing message: " + message + "\"}";
            kafkaTemplate.send("user-responses", errorMessage);
        }
    }

    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
