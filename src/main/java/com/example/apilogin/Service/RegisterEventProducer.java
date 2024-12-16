package com.example.apilogin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterEventProducer {

    private static final String TOPIC = "register-event-topic";

    @Autowired
    @Qualifier("stringKeyKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendRegisterEvent(Long userId, int age, String addr) {
        // Create event data as a JSON string manually
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", userId);
        eventData.put("age", age);
        eventData.put("addr", addr);

        try {
            String eventJson = String.format(
                    "{\"userId\":%d,\"age\":%d,\"addr\":\"%s\"}", userId, age, addr
            );

            kafkaTemplate.send(TOPIC, eventJson);
            System.out.println("Produced RegisterEvent: " + eventJson);

        } catch (Exception e) {
            System.err.println("Error sending RegisterEvent: " + e.getMessage());
        }
    }
}
