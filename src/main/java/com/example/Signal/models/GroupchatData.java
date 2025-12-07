package com.example.Signal.models;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class GroupchatData {
    String id;
    List<String> members_id;

    public GroupchatData(String id, String members_id) {
        this.id = id;
        this.members_id = List.of(members_id.split("\\s+"));
    }

    public GroupchatData(String id, List<String> members_id) {
        this.id = id;
        this.members_id = members_id;
    }
}
