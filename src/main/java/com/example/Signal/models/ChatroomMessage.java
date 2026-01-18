package com.example.Signal.models;

import java.time.LocalDate;

import static com.example.Signal.utils.Utils.toLocalDate;

public record ChatroomMessage(String authorId, String author, String message, LocalDate timestamp) {
    public ChatroomMessage(String authorId, String author, String message, String timestampString) {
        this(authorId, author != null ? author : "Myself", message, toLocalDate(timestampString));
    }
}
