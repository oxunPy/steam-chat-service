package org.example.steamchatservice.repository;

import org.example.steamchatservice.entity.ChatMessage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ChatMessageR2Repository extends ReactiveCrudRepository<ChatMessage, Long> {

    Flux<ChatMessage> findByChatRoomId(Long chatRoomId);
}
