package com.example.Signal.models;

import java.time.LocalDate;
import java.util.List;

public record ChatroomData(
        String id,
        String name,
        ChatroomType type,
        List<ChatroomMember> members,
        List<LocalDate> days_played
) {}
