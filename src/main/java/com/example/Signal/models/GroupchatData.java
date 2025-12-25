package com.example.Signal.models;

import java.time.LocalDate;
import java.util.List;

public record GroupchatData(
        String id,
        String name,
        List<GroupchatMember> members,
        List<LocalDate> days_played
) {}
