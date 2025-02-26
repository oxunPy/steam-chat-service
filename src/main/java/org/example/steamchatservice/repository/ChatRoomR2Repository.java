package org.example.steamchatservice.repository;

import org.example.steamchatservice.entity.ChatRoom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ChatRoomR2Repository extends ReactiveCrudRepository<ChatRoom, Long> {
   Mono<ChatRoom> findByRoomId(String roomId);
   Mono<ChatRoom> findByUser1IdAndUser2Id(String user1Id, String user2Id);
}
