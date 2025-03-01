package org.example.steamchatservice.handler;

import org.example.steamchatservice.service.JwtService;
import org.example.steamchatservice.service.redis.RedisSessionService;
import org.example.steamchatservice.service.redis.RedisSessionService2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component("reactive_web_socket_handler2")
public class ReactiveChatWebSocketHandler2 implements WebSocketHandler {

    private final RedisSessionService2 redisSessionService;
    private final JwtService jwtService;

    public ReactiveChatWebSocketHandler2(RedisSessionService2 redisSessionService, JwtService jwtService) {
        this.redisSessionService = redisSessionService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        Map<String, String> queryParams = parseQueryParams(uri);

        String token = queryParams.get("token");
        String friendUsername = queryParams.get("friendUsername");

        return jwtService.validateToken(token)
                .flatMap(username -> {
                    if (username == null || username.isEmpty()) {
                        return session.close(CloseStatus.NOT_ACCEPTABLE);
                    }

                    // Register sink for the user
                    redisSessionService.registerUserSink(username);

                    // Listen for incoming messages
                    Flux<String> incomingMessages = session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .flatMap(message -> sendMessageToFriend(friendUsername, message));

                    // Listen for messages sent via the Sink
                    Flux<WebSocketMessage> outgoingMessages = redisSessionService.getUserSink(username)
                            .asFlux()
                            .map(session::textMessage);

                    return session.send(outgoingMessages)
                            .and(incomingMessages);
                });
    }
    private Mono<String> sendMessageToFriend(String friendUsername, String message) {
        return Mono.fromRunnable(() -> {
            Sinks.Many<String> friendSink = redisSessionService.getUserSink(friendUsername);
            if (friendSink != null) {
                System.out.println("✅ Sending to " + friendUsername + ": " + message);
                friendSink.tryEmitNext(message);
            } else {
                System.err.println("❌ Friend session not found: " + friendUsername);
            }
        });
    }

    private Map<String, String> parseQueryParams(URI uri) {
        return Arrays.stream(uri.getQuery().split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }
}
