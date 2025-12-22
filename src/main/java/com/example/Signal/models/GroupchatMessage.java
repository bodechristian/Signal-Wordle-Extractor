package com.example.Signal.models;

import java.time.LocalDate;

import static com.example.Signal.Utils.toLocalDate;

public record GroupchatMessage(String authorId, String author, String message, LocalDate timestamp) {
    public GroupchatMessage(String authorId, String author, String message, String timestampString) {
        this(authorId, author != null ? author : "Myself", message, toLocalDate(timestampString));
    }
}
