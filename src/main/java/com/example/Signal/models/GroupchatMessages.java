package com.example.Signal.models;

import java.util.Date;

public record GroupchatMessages(String author, String message, Date timestamp) {
    GroupchatMessages(String author, String message, String timestampString) {
        this(author, message, new Date(Long.parseLong(timestampString)));
    }
}
