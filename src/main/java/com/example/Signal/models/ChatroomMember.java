package com.example.Signal.models;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public record ChatroomMember(String member_id,
                             String name,
                             Map<LocalDate, ChatroomMessage> messages,
                             int nbGames,
                             Optional<LocalDate> firstPlayed,
                             Optional<LocalDate> lastPlayed) {

    private static String extractName(Map<LocalDate, ChatroomMessage> messages) {
        return messages.values().stream().toList().getFirst().author();
    }

    private static Optional<LocalDate> extractFirstDate(Map<LocalDate, ChatroomMessage> messages) {
        return messages.keySet().stream().min(LocalDate::compareTo);
    }

    private static Optional<LocalDate> extractLastDate(Map<LocalDate, ChatroomMessage> messages) {
        return messages.keySet().stream().max(LocalDate::compareTo);
    }

    public static ChatroomMember fromMessages(String member_id, Map<LocalDate, ChatroomMessage> messages) {
        return new ChatroomMember(member_id,
                                  extractName(messages),
                                  messages,
                                  messages.size(),
                                  extractFirstDate(messages),
                                  extractLastDate(messages));
    }

    @NotNull
    @Override
    public String toString() {
        return "[ChatroomMember: %s, id: %s]".formatted(this.name, this.member_id);
    }
}
