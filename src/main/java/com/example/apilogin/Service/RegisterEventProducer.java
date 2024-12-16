package com.example.apilogin.Service;

import com.example.apilogin.DTO.RegisterRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void sendRegisterEvent(RegisterRequestDTO registerRequestDTO) {
        try {
            // Serialize the DTO to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = objectMapper.writeValueAsString(registerRequestDTO);

            // Send to Kafka
            kafkaTemplate.send(TOPIC, eventJson);
            System.out.println("Produced RegisterEvent: " + eventJson);

        } catch (Exception e) {
            System.err.println("Error sending RegisterEvent: " + e.getMessage());
        }
    }
}
