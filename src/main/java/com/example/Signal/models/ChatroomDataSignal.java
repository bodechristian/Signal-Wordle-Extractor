package com.example.Signal.models;

import java.util.List;

public record ChatroomDataSignal(String id, String name, ChatroomType type, List<String> members_id) {

    public ChatroomDataSignal(String id, String name, ChatroomType type, String members_id) {
        this(id, name, type, List.of(members_id.split("\\s+")));
    }
}
