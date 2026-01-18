package com.example.Signal.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Processed chatroom data with members and messages.
 * Daysplayed is in reverse order - so the latest game is the first item
 */
public record ChatroomData(String id,
                           String name,
                           List<ChatroomMember> members,
                           List<LocalDate> daysPlayed,
                           Optional<LocalDate> firstDayPlayed,
                           Optional<LocalDate> lastDayPlayed) {
    public ChatroomData(String id, String name, List<ChatroomMember> members, List<LocalDate> daysPlayed) {
        this(id,
             name,
             members,
             daysPlayed,
             daysPlayed.isEmpty() ? Optional.empty() : Optional.of(daysPlayed.getLast()),
             daysPlayed.isEmpty() ? Optional.empty() : Optional.of(daysPlayed.getFirst()));
    }
}
