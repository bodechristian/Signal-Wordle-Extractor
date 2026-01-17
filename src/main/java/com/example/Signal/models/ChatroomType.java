package com.example.Signal.models;

import lombok.Getter;

@Getter
public enum ChatroomType {
    PRIVATE("private"),
    GROUP("group"),
    SUPERGROUP("super-group");

    private final String value;

    ChatroomType(String value) {
        this.value = value;
    }

    public static ChatroomType fromString(String value) {
        for (ChatroomType type : ChatroomType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown chatroom type: " + value);
    }
}
