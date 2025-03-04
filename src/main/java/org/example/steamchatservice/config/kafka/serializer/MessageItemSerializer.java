package org.example.steamchatservice.config.kafka.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.example.steamchatservice.dto.MessageItem;

public class MessageItemSerializer implements Serializer<MessageItem> {

    private final ObjectMapper objMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, MessageItem data) {
        try {
            return objMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing MessageItem");
        }
    }
}
