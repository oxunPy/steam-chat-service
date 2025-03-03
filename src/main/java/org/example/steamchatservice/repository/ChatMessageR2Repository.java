package org.example.steamchatservice.repository;

import org.example.steamchatservice.entity.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface ChatMessageR2Repository extends ReactiveCrudRepository<ChatMessage, Long> {
    Flux<ChatMessage> findByChatRoomId(Long chatRoomId);

    Flux<ChatMessage> findByReceiverIdAndTimestampAfter(String receiverId, LocalDateTime timestamp);
}
