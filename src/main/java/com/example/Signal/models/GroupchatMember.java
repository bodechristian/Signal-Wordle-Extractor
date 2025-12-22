package com.example.Signal.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@Builder
public class GroupchatMember {
    private final String member_id;
    private String name;
    private Map<LocalDate, GroupchatMessage> messages;
    private LocalDate first_played;
    private LocalDate last_played;
    private double avg_score;
    private double last_ten_scores_avg;
    private double last_seven_days_avg;
    private double last_thirty_days_avg;

    public String toString() {
        return "[GroupchatMember: %s, id: %s]".formatted(this.name, this.member_id);
    }

}
