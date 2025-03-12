package org.example.steamchatservice.service.kafka;

import org.example.steamchatservice.dto.MessageItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ChatKafkaProducer {

    @Value("${kafka.request.chat-messages}")
    private String chatMessagesTopic;
    private final KafkaTemplate<String, MessageItem> kafkaTemplate;

    public ChatKafkaProducer(KafkaTemplate<String, MessageItem> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendMessage(MessageItem message) {
        return Mono.fromFuture(() -> kafkaTemplate.send("chat-messages", message.getSender(), message))
                .doOnSuccess(result -> System.out.println("✅ Sent to Kafka: " + message))
                .doOnError(error -> System.err.println("❌ Kafka send failed: " + error.getMessage()))
                .then();  // Ensures Mono<Void>
    }
}

