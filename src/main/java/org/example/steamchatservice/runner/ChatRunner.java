package org.example.steamchatservice.runner;

import org.example.steamchatservice.service.redis.ReactiveRedisChatSubscriber;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatRunner implements CommandLineRunner {

    private final ReactiveRedisChatSubscriber chatSubscriber;

    public ChatRunner(ReactiveRedisChatSubscriber chatSubscriber) {
        this.chatSubscriber = chatSubscriber;
    }

    @Override
    public void run(String... args) throws Exception {
        chatSubscriber.startListening();
    }
}
