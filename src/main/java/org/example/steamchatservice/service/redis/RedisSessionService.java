package org.example.steamchatservice.service.redis;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisSessionService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, WebSocketSession> localSessions = new ConcurrentHashMap<>();
    public static final String SESSION_KEY_PREFIX = "chat:session:";

    public RedisSessionService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
         subscribeToExpirationEvents();
    }

    private void subscribeToExpirationEvents() {
        redisTemplate.listenToChannel("__keyevent@0__:expired")
                .doOnNext(message -> {
                    String expiredKey = message.getMessage();
                    if (expiredKey.startsWith(SESSION_KEY_PREFIX)) {
                        String username = expiredKey.substring(SESSION_KEY_PREFIX.length());
                        System.out.println("Session expired for username: " + username);
                        localSessions.remove(username); // Remove from local cache
                    }
                })
                .subscribe();
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupLocalSessions() {
        System.out.println("Cleaning up local sessions...");
        localSessions.entrySet().removeIf(entry -> {
            WebSocketSession session = entry.getValue();
            if (session == null || !session.isOpen()) {
                System.out.println("Removing stale session for username: " + entry.getKey());
                return true;
            }
            return false;
        });
    }

    public Mono<Void> saveSession(String username, WebSocketSession session) {
        System.out.println("Saving session for username: " + username);
        localSessions.put(username, session);
        return redisTemplate.opsForValue()
                .set(SESSION_KEY_PREFIX + username, session.getId(), Duration.ofHours(1))
                .doOnSuccess(unused -> System.out.println("Session saved to Redis: " + username))
                .doOnError(err -> System.err.println("Error saving session to Redis: " + err.getMessage()))
                .then();
    }

    public Mono<String> findSessionId(String username) {
        System.out.println("Fetching session ID for username: " + username);
        return redisTemplate.opsForValue()
                .get(SESSION_KEY_PREFIX + username)
                .doOnNext(sessionId -> System.out.println("Fetched session ID: " + sessionId))
                .doOnError(err -> System.err.println("Error fetching session ID: " + err.getMessage()));
    }

    public WebSocketSession getLocalSession(String username) {
        return localSessions.get(username);
    }

    public Mono<Void> removeSession(String username) {
        System.out.println("Removing session for username: " + username);
        localSessions.remove(username);
        return redisTemplate.delete(SESSION_KEY_PREFIX + username)
                .doOnSuccess(unused -> System.out.println("Session removed from Redis: " + username))
                .doOnError(err -> System.err.println("Error removing session from Redis: " + err.getMessage()))
                .then();
    }
}
