package org.example.steamchatservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.request.chat-messages}")
    private String chatMessagesTopic;

    @Bean
    public NewTopic chatMessagesTopic() {
        return new NewTopic(chatMessagesTopic, 3, (short) 2); // 3 partitions, replication factory 2
    }
}
