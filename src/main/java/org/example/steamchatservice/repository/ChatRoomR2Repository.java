package org.example.steamchatservice.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.steamchatservice.entity.ChatRoom;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ChatRoomR2Repository extends ReactiveCrudRepository<ChatRoom, Long> {
   Mono<ChatRoom> findByRoomId(String roomId);

   @Query(value = """
        SELECT * 
        FROM chat_rooms 
        WHERE (user1_id = :user1Id AND user2_id = :user2Id) 
           OR (user1_id = :user2Id AND user2_id = :user1Id)
        LIMIT 1
        """)
   Mono<ChatRoom> findByUser1IdAndUser2Id(@Param("user1Id") String user1Id,
                                          @Param("user2Id") String user2Id);
}
