package org.example.steamchatservice.service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatKafkaProducer {

    @Value("${kafka.request.chat-messages}")
    private String chatMessagesTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ChatKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(chatMessagesTopic, message);
        System.out.println("Sent message: " + message);
    }
}

