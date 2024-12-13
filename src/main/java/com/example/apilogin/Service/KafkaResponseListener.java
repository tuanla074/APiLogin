package com.example.apilogin.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaResponseListener {

    private final ConcurrentHashMap<String, CompletableFuture<Object>> responseFutures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompletableFuture<Object> getResponseFuture(String requestKey) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        responseFutures.put(requestKey, future);
        return future;
    }

    @KafkaListener(topics = "user-responses", groupId = "user-group")
    public void consumeResponse(String message) {
        System.out.println("Received response from Kafka: " + message);

        try {
            JsonNode jsonNode = objectMapper.readTree(message); // Parse the JSON response
            String requestKey = jsonNode.get("userId").asText(); // Modify as per your response format

            CompletableFuture<Object> future = responseFutures.remove(requestKey);
            if (future != null) {
                future.complete(jsonNode); // Pass parsed JSON to waiting controller
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
