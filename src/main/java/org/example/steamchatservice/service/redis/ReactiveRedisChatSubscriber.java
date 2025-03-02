package org.example.steamchatservice.service.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class ReactiveRedisChatSubscriber {

    private final StreamReceiver<String, MapRecord<String, String, String>> streamReceiver;
    private final RedisSessionService2 redisSessionService;

    public ReactiveRedisChatSubscriber(@Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory factory, RedisSessionService2 redisSessionService) {
        this.streamReceiver = StreamReceiver.create(factory);
        this.redisSessionService = redisSessionService;
    }

    public void startListening() {
        streamReceiver.receive(StreamOffset.fromStart("chat-messages"))
                .flatMap(record -> {
                    String sender = record.getValue().get("sender");
                    String receiver = record.getValue().get("receiver");
                    String message = record.getValue().get("message");

                    System.out.println("📩 Received chat message: " + message);
                    // Process and deliver message to WebSockets
                    return Mono.defer(() -> {
                        Sinks.Many<String> friendSink = redisSessionService.getUserSink(receiver);
                        if (friendSink != null) {
                            System.out.println("✅ Sending to " + receiver + ": " + message);
                            friendSink.tryEmitNext(message);
                        } else {
                            System.err.println("❌ Friend session not found: " + receiver);
                        }
                        return Mono.empty();
                    });
                })
                .onErrorContinue((ex, obj) -> System.out.println("Error processing message: " + ex.getMessage()))
                .subscribe();
    }

}
