package org.example.steamchatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageItem {
    private String sender;
    private String receiver;
    private String content;
    private String roomId;
}
