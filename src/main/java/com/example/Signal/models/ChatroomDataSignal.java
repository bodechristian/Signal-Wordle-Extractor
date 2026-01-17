package com.example.Signal.models;

import java.util.List;

/**
 * Raw chatroom data from Signal database.
 */
public record ChatroomDataSignal(String id, String name, List<String> members_id) {

    public ChatroomDataSignal(String id, String name, String members_id) {
        this(id, name, List.of(members_id.split("\\s+")));
    }
}
