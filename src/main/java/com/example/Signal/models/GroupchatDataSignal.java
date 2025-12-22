package com.example.Signal.models;

import java.util.List;

public record GroupchatDataSignal(String id, String name, List<String> members_id) {

    public GroupchatDataSignal(String id, String name, String members_id) {
        this(id, name, List.of(members_id.split("\\s+")));
    }
}
