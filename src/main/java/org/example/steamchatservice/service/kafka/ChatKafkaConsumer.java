package org.example.steamchatservice.service.kafka;

import org.example.steamchatservice.dto.MessageItem;
import org.example.steamchatservice.service.redis.ReactiveRedisChatPublisher;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatKafkaConsumer {
    private final ReactiveRedisChatPublisher redisChatPublisher;

    public ChatKafkaConsumer(ReactiveRedisChatPublisher redisChatPublisher) {
        this.redisChatPublisher = redisChatPublisher;
    }

    @KafkaListener(topics = {"${kafka.request.chat-messages}"}, groupId = "${kafka.request.group.id}", containerFactory = "kafkaListenerContainerFactoryMsg")
    public void consumeMessage(MessageItem message) {
        System.out.println(message);
        redisChatPublisher.sendMessage(message.getSender(), message.getReceiver(), message.getContent())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> System.out.println("✅ Sent to Redis: " + message))
                .doOnError(error ->  System.err.println("❌ Redis send failed: " + error.getMessage()))
                .subscribe();
    }
}
