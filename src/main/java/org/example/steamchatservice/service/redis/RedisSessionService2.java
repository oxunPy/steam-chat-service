package org.example.steamchatservice.service.redis;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RedisSessionService2 {
    private final Map<String, Sinks.Many<String>> userMessageSinks = new ConcurrentHashMap<>();

    // Store a sink for each user
    public void registerUserSink(String username) {
        userMessageSinks.put(username, Sinks.many().multicast().directBestEffort());
    }

    // Get user's sink
    public Sinks.Many<String> getUserSink(String username) {
        return userMessageSinks.get(username);
    }
}
