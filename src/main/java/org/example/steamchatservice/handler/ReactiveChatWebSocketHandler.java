package org.example.steamchatservice.handler;

import org.example.steamchatservice.service.ChatService;
import org.example.steamchatservice.service.JwtService;
import org.example.steamchatservice.service.redis.ReactiveRedisChatPublisher;
import org.example.steamchatservice.service.redis.RedisSessionService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component("reactive_web_socket_handler")
public class ReactiveChatWebSocketHandler implements WebSocketHandler {

    private final RedisSessionService redisSessionService;
    private final JwtService jwtService;
    private final ReactiveRedisChatPublisher chatPublisher;
    private static final int MAX_CONNECTIONS = 5000;
    private final ChatService chatService;

    public ReactiveChatWebSocketHandler(RedisSessionService redisSessionService, JwtService jwtService, ReactiveRedisChatPublisher chatPublisher, ChatService chatService) {
        this.redisSessionService = redisSessionService;
        this.jwtService = jwtService;
        this.chatPublisher = chatPublisher;
        this.chatService = chatService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        if(redisSessionService.getActiveConnections() >= MAX_CONNECTIONS && session != null) {
            return session.close();
        }

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
                    // send late user's messages
                    // sendLatestMessages(username, chatService.handleUserOnline(username));
                    Flux<WebSocketMessage>pendingMessages = chatService.handleUserOnline(username)
                            .map(msg -> session.textMessage(msg));

                    // Listen for incoming messages
                    Flux<Void> incomingMessages = session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .flatMap(message -> {
                                if(message != null && !message.trim().isEmpty()) {
                                    chatService.saveChatMessage(username, friendUsername, message);
                                }
                                return chatPublisher.sendMessage(username, friendUsername, message);
                            });

                    // Listen for messages sent via the Sink
                    Flux<WebSocketMessage> outgoingMessages = redisSessionService.getUserSink(username)
                            .asFlux()
                            .map(session::textMessage);

                    Flux<WebSocketMessage> allMessages = Flux.concat(pendingMessages, outgoingMessages);

                    return session.send(allMessages)
                            .and(incomingMessages)
                            .doFinally(signalType -> {
                                redisSessionService.removeSession(username).then();
                                redisSessionService.removeUserSink(username);
                            });
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

    private Mono<Void> sendLatestMessages(String username, Flux<String> messages) {
        return Mono.defer(() -> {
            Sinks.Many<String> userSink = redisSessionService.getUserSink(username);
            if (userSink != null) {
                System.out.println("✅ Sending to " + username);
                return messages.doOnNext(msg -> userSink.tryEmitNext(msg)).then();
            } else {
                System.err.println("❌ User session not found: " + username);
                return Mono.empty();
            }
        });
    }

    private Map<String, String> parseQueryParams(URI uri) {
        if(uri.getQuery() == null) return new HashMap<>();
        return Arrays.stream(uri.getQuery().split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }
}
