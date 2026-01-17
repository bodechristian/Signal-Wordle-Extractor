package com.example.Signal.models;

import java.time.LocalDate;
import java.util.List;

/**
 * Processed chatroom data with members and messages.
 * The type field is stored as a string from the database ("group", "private", or "supergroup").
 */
public record ChatroomData(
        String id,
        String name,
        List<ChatroomMember> members,
        List<LocalDate> days_played
) {}
