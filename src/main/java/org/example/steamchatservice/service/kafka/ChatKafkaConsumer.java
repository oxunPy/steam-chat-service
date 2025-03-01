package org.example.steamchatservice.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatKafkaConsumer {

    @KafkaListener(topics = {"${kafka.request.chat-messages}"}, groupId = "${kafka.request.group.id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(String message) {
        System.out.println(message);
    }
}
