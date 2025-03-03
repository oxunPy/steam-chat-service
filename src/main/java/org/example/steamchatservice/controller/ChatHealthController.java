package org.example.steamchatservice.controller;

import org.example.steamchatservice.service.redis.RedisSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/health")
public class ChatHealthController {
    private final RedisSessionService redisSessionService;


    public ChatHealthController(RedisSessionService redisSessionService) {
        this.redisSessionService = redisSessionService;
    }

    @GetMapping("/connections")
    public ResponseEntity<Integer> getActiveConnections() {
        return ResponseEntity.ok(redisSessionService.getActiveConnections());
    }
}
