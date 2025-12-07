package com.example.Signal.models;

import java.util.Date;

public record GroupchatMessage(String author, String message, Date timestamp) {
    public GroupchatMessage(String author, String message, String timestampString) {
        this(author != null ? author : "Myself", message, new Date(Long.parseLong(timestampString)));
    }
}
