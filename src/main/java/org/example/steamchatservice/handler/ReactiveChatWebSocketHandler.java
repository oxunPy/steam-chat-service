package org.example.steamchatservice.handler;

import org.apache.commons.lang.StringUtils;
import org.example.steamchatservice.service.JwtService;
import org.example.steamchatservice.service.redis.RedisSessionService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component("reactive_web_socket_handler")
public class ReactiveChatWebSocketHandler implements WebSocketHandler {
    private final RedisSessionService redisSessionService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final JwtService jwtService;
    private final ConcurrentHashMap<String, Mono<Void>> sendOperations = new ConcurrentHashMap<>();

    public ReactiveChatWebSocketHandler(RedisSessionService redisSessionService, RedisTemplate<Object, Object> redisTemplate, JwtService jwtService) {
        this.redisSessionService = redisSessionService;
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        if(uri != null) {
            Map<String, String> queryParams = parseQueryParams(uri);
            String token = queryParams.get("token");
            String friendUsername = queryParams.get("friendUsername");

            System.out.println("Extracted token: " + token);
            System.out.println("Extracted friendUsername: " + friendUsername);

            return jwtService.validateToken(token)
                    .flatMap(username -> {
                        if(username == null || username.isEmpty()) {
                            return session.close(CloseStatus.NOT_ACCEPTABLE);
                        }

                        return redisSessionService
                                .saveSession(username, session)
                                .thenMany(session.receive()
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .flatMap(message -> {
                                            if(message == null || message.isEmpty()) {
                                                return Mono.empty();
                                            }
                                            System.out.println("Received message: " + message);
                                            return sendMessageToFriend(friendUsername, message);
                                        }))
                                .doFinally(signalType -> {
                                    // Remove session when Websocket is closed
                                    System.out.println("WebSocket closed: " + session.getId() + ", Reason: " + signalType.name());
                                    redisSessionService.removeSession(username);
                                })
                                .doOnError(error -> {
                                    System.err.println("❌ WebSocket error for user: " + username + ", reason: " + error.getMessage());
                                    error.printStackTrace();
                                })
                                .then();
                    });

        }

        return Mono.empty();
    }

    private Mono<Void> sendMessageToFriend(String fUsername, String message) {
        return redisSessionService.findSessionId(fUsername)
                .flatMap(fSessionId -> {
                    if (fSessionId != null) {
                        WebSocketSession fSession = redisSessionService.getLocalSession(fUsername);
                        if (fSession != null) {
                            System.out.println("🔹 Sending message to friend " + fUsername + ": " + message);
                            return sendToWebSocket(fSession, message);
                        } else {
                            System.err.println("❌ Friend session not found in local cache: " + fUsername);
                        }
                    }
                    else {
                        System.err.println("❌ No session found for friend: " + fUsername);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> sendToWebSocket(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            System.err.println("❌ Session is closed or invalid: " + (session != null ? session.getId() : "null"));
            return Mono.empty();
        }

        // Serialize send operations for this session
        return sendOperations.compute(session.getId(), (key, existingMono) -> {
           Mono<Void> newMono = session.send(Mono.just(session.textMessage(message)))
                   .doOnSuccess(unused -> System.out.println("✅ Message sent successfully to " + session.getId()))
                   .doOnError(err -> {
                       System.err.println("❌ Error sending message: " + err.getMessage());
                       // Remove the session if an error occurs
                       redisSessionService.removeSession(session.getId()).subscribe();
                   })
                   .then();
           if(existingMono != null) {
               return existingMono.then(newMono);
           }

           return newMono;
        });
    }


    private Map<String, String> parseQueryParams(URI uri) {
        return Arrays.stream(uri.getQuery().split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }
}
