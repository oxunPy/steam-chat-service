package org.example.steamchatservice.service;

import org.example.steamchatservice.entity.ChatMessage;
import org.example.steamchatservice.entity.ChatRoom;
import org.example.steamchatservice.repository.ChatMessageR2Repository;
import org.example.steamchatservice.repository.ChatRoomR2Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ChatService {
    private final ChatMessageR2Repository chatMessageR2Repository;
    private final ChatRoomR2Repository chatRoomR2Repository;

    public ChatService(ChatMessageR2Repository chatMessageR2Repository, ChatRoomR2Repository chatRoomR2Repository) {
        this.chatMessageR2Repository = chatMessageR2Repository;
        this.chatRoomR2Repository = chatRoomR2Repository;
    }

    public void saveChatMessage(String senderId, String receiverId, String message) {
        saveChatMsgMono(senderId, receiverId, message)
                .doOnError(error -> System.err.println("Error occured: " + error.getMessage()))
                .subscribe();
    }

    private Mono<ChatMessage> saveChatMsgMono(String senderId, String receiverId, String message) {
        return chatRoomR2Repository.findByUser1IdAndUser2Id(senderId, receiverId)
                .switchIfEmpty(createNewChatRoom(senderId, receiverId))
                        .flatMap(chatRoom -> {
                            var entity = new ChatMessage();
                            entity.setSenderId(senderId);
                            entity.setReceiverId(receiverId);
                            entity.setMessage(message);
                            entity.setChatRoomId(chatRoom.getId());
                            entity.setTimestamp(LocalDateTime.now());
                            return chatMessageR2Repository.save(entity);
                        });
    }

    private Mono<ChatRoom> createNewChatRoom(String user1Id, String user2Id) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1Id(user1Id);
        chatRoom.setUser2Id(user2Id);
        chatRoom.setRoomId(generateRoomId(user1Id, user2Id));
        chatRoom.setCreatedAt(LocalDateTime.now());

        return chatRoomR2Repository.save(chatRoom);
    }

    // Ensure user order is consistent for the same pair
    private String generateRoomId(String user1Id, String user2Id) {
        return user1Id.compareTo(user2Id) < 0
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;
    }

    public Flux<String> handleUserOnline(String userId) {
        return chatMessageR2Repository.findByReceiverIdAndTimestampAfter(userId, LocalDateTime.now().minusDays(7))
                .map(ChatMessage::getMessage);
    }
}
