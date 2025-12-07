package com.example.Signal.models;

import java.util.List;

public record GroupchatData(String id, List<String> members_id) {

    public GroupchatData(String id, String members_id) {
        this(id, List.of(members_id.split("\\s+")));
    }
}
