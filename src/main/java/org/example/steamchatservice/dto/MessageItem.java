package org.example.steamchatservice.dto;


public class MessageItem {
    private String sender;
    private String receiver;
    private String content;
    private String roomId;

    public MessageItem(String s, String r, String c) {
        sender = s;
        receiver = r;
        content = c;
    }

    public MessageItem() {}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
