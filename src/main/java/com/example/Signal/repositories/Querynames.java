package com.example.Signal.repositories;

import lombok.Getter;

@Getter
public enum Querynames {
    GETOWNER("getOwner"),
    GETDMS("getDms"),
    GETGROUPS("getGroups"),
    GETGROUPSMESSAGES("getGroupsMessages");

    private final String value;

    Querynames(String name) {
        this.value = name;
    }
}