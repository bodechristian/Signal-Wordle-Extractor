package com.example.Signal.repositories;

import lombok.Getter;

@Getter
public enum Querynames {
    GETOWNERID("getOwnerId"),
    GETUSERSNAME("getOwnerName"),
    GETALLCHATROOMS("getAllChatrooms"),
    GETALLCHATROOMMESSAGES("getAllChatroomMessages");

    private final String value;

    Querynames(String name) {
        this.value = name;
    }
}
