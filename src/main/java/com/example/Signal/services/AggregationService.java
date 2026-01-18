package com.example.Signal.services;

import com.example.Signal.models.ChatroomMember;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.models.DateTimeframe;
import com.example.Signal.repositories.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.example.Signal.Utils.parseWordleScore;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final DataRepository dataRepository;

    private <T> Map<String, T> aggregateByPerson(DateTimeframe timeframe,
                                                 Supplier<T> containerSupplier,
                                                 BiConsumer<T, ScoreEntry> aggregator) {
        Map<String, T> result = new HashMap<>();

        for (ChatroomMember member : dataRepository.getSuperChatroom().members()) {
            for (Map.Entry<LocalDate, ChatroomMessage> entry : member.messages().entrySet()) {
                LocalDate messageDate = entry.getKey();

                if (messageDate.isBefore(timeframe.start()) || messageDate.isAfter(timeframe.end())) {
                    continue;
                }

                int score = parseWordleScore(entry.getValue().message());
                if (score != -1) {
                    T container = result.computeIfAbsent(member.name(), k -> containerSupplier.get());
                    aggregator.accept(container, new ScoreEntry(messageDate, score));
                }
            }
        }

        return result;
    }

    public Map<String, List<Integer>> aggregateScoresByPerson(DateTimeframe timeframe) {
        return aggregateByPerson(timeframe, ArrayList::new, (list, entry) -> list.add(entry.score()));
    }

    public Map<String, Map<LocalDate, Integer>> aggregateTemporalScoresByPerson(DateTimeframe timeframe) {
        return aggregateByPerson(timeframe, HashMap::new, (map, entry) -> map.put(entry.date(), entry.score()));
    }

    private record ScoreEntry(LocalDate date, int score) {
    }
}
