package org.example.steamchatservice.service.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ReactiveRedisChatPublisher {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private static final String CHAT_STREAM = "chat-messages";

    public ReactiveRedisChatPublisher(@Qualifier("reactiveRedisTemplateObj") ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> sendMessage(String sender, String receiver, String message) {
        return redisTemplate.opsForStream()
                .add(ObjectRecord.create(CHAT_STREAM, Map.of(
                        "sender", sender,
                        "receiver", receiver,
                        "message", message
                )))
                .doOnSuccess(recordId -> System.out.println("✅ Message published to Redis Stream: " + recordId))
                .doOnError(error -> System.err.println("❌ Redis Stream publish error: " + error.getMessage()))
                .then();
    }
}
