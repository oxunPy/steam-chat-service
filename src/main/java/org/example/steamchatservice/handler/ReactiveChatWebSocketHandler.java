package org.example.steamchatservice.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component("reactive_web_socket_handler")
public class ReactiveChatWebSocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Handle incoming messages
        return session.receive() // Receive messages from the client
                .map(webSocketMessage -> {
                    String message = webSocketMessage.getPayloadAsText();
                    System.out.println("Received: " + message);
                    return "Echo: " + message; // Echo the message back
                })
                .doOnNext(response -> System.out.println("Sending: " + response))
                .map(session::textMessage) // Convert the response to a WebSocket message
                .as(session::send); // Send the response back to the client
    }
}