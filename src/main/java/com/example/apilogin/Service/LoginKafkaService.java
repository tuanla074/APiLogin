package com.example.apilogin.Service;

import com.example.apilogin.Model.userModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginKafkaService {

    private final ConcurrentHashMap<Long, CompletableFuture<Map<String, Object>>> responseMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Wait for UserInfo response corresponding to the given userId.
     *
     * @param userId The userId for which we are waiting for the response.
     * @return A CompletableFuture that will complete when the UserInfo is received.
     */
    public CompletableFuture<Map<String, Object>> waitForUserInfo(Long userId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        responseMap.put(userId, future);
        return future;
    }

    /**
     * Kafka listener to consume user information responses.
     *
     * @param record The record received from the Kafka topic.
     */
    @KafkaListener(topics = "user-info-topic", groupId = "api-login-group")
    public void consumeUserInfo(ConsumerRecord<Long, String> record) {
        Long userId = record.key();
        String userInfoJson = record.value();

        System.out.println("Received UserInfo for userId=" + userId + ": " + userInfoJson);

        try {
            // Parse JSON dynamically as a Map
            Map<String, Object> userInfo = objectMapper.readValue(userInfoJson, new TypeReference<Map<String, Object>>() {});

            System.out.println("Parsed UserInfo: " + userInfo);

            // Resolve the CompletableFuture for this userId
            CompletableFuture<Map<String, Object>> future = (CompletableFuture<Map<String, Object>>) responseMap.remove(userId);
            if (future != null) {
                future.complete(userInfo);
            }
        } catch (Exception e) {
            System.err.println("Error deserializing UserInfo: " + e.getMessage());
        }
    }


}
