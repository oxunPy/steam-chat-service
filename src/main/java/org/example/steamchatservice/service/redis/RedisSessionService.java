package org.example.steamchatservice.service.redis;

import org.example.steamchatservice.entity.ChatMessage;
import org.example.steamchatservice.repository.ChatMessageR2Repository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RedisSessionService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final Map<String, Sinks.Many<String>> userMessageSinks = new ConcurrentHashMap<>();
    private final ChatMessageR2Repository chatMessageR2Repository;

    public RedisSessionService(@Qualifier("reactiveRedisTemplateStr") ReactiveRedisTemplate<String, String> redisTemplate, ChatMessageR2Repository chatMessageR2Repository) {
        this.redisTemplate = redisTemplate;
        this.chatMessageR2Repository = chatMessageR2Repository;
    }

    // Save user sessionId in Redis (user -> sessionId)
    public Mono<Void> saveSession(String username, String sessionId) {
        return redisTemplate.opsForValue()
                .set("session:" + username, sessionId)
                .then();
    }

    // Find sessionId by username
    public Mono<String> findSessionId(String username) {
        return redisTemplate.opsForValue().get("session:" + username);
    }

    // Register a sink for the user to send/receive messages
    public void registerUserSink(String username) {
        userMessageSinks.computeIfAbsent(username, key -> Sinks.many().multicast().directBestEffort());
    }

    // Get user's sink for message sending
    public Sinks.Many<String> getUserSink(String username) {
        return userMessageSinks.get(username);
    }

    // Send message to a user via their sink
    public void sendMessageToUser(String username, String message) {
        Sinks.Many<String> sink = userMessageSinks.get(username);
        if (sink != null) {
            sink.tryEmitNext(message);
        }
    }

    // Remove session from Redis when user disconnects
    public Mono<Void> removeSession(String username) {
        return redisTemplate.opsForValue().delete("session:" + username).then();
    }

    // Remove user sink when user disconnects
    public void removeUserSink(String username) {
        userMessageSinks.remove(username);
    }

    public int getActiveConnections() {
        return userMessageSinks.size();
    }
}
