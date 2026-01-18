package com.example.Signal.views;

import com.example.Signal.components.AllMessagesSection;
import com.example.Signal.components.ChatroomSelector;
import com.example.Signal.components.MessagesAccordionRow;
import com.example.Signal.components.ScoreHistogram;
import com.example.Signal.components.StatisticsPanel;
import com.example.Signal.components.TemporalScoreGraph;
import com.example.Signal.models.ChatroomData;
import com.example.Signal.models.ChatroomMember;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.models.EvaluationTimeframe;
import com.example.Signal.models.MessageTuple;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.DataStructureService;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.example.Signal.Utils.parseWordleScore;
import static java.lang.Integer.min;

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    private final static int CHUNKSIZE = 10;

    SignalDataService signalDataService;
    DataRepository dataRepository;
    DataStructureService dataStructureService;

    ChatroomSelector chatroomSelector;
    VerticalLayout contentContainer;
    StatisticsPanel statisticsPanel;
    AllMessagesSection allMessagesSection;

    private String filename;
    private int chunkidx = 0;

    public SignalChatView(SignalDataService signalDataService,
                          DataRepository dataRepository,
                          DataStructureService dataStructureService) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;
        this.dataStructureService = dataStructureService;

        chatroomSelector = new ChatroomSelector(chatrooms -> {
            dataStructureService.changeActiveChatrooms(chatrooms);
            updatePage();
        });

        contentContainer = new VerticalLayout();
        contentContainer.addClassNames("chat-container");

        statisticsPanel = new StatisticsPanel(this::updateEvaluation);
        allMessagesSection = new AllMessagesSection(() -> {
            addAccordionMessages();
            updateLoadMoreButton();
        });

        contentContainer.add(statisticsPanel, allMessagesSection);

        add(new H1("Overview"), chatroomSelector, contentContainer);
    }

    private void updateMultiselectGroups() {
        chatroomSelector.getMultiselectChats().setValue(dataRepository.getActiveChatrooms());
    }

    private void addAccordionMessages() {
        Map<String, List<MessageTuple>> dailyGames = getNextChunkAccordionMessages();

        dailyGames.entrySet()
                .stream()
                .sorted((e1, e2) -> LocalDate.parse(e2.getKey()).compareTo(LocalDate.parse(e1.getKey())))
                .forEach(entry -> allMessagesSection.getDailyMessagesAccordion()
                        .add(entry.getKey(), new MessagesAccordionRow(entry.getValue())));
    }

    private Map<String, List<MessageTuple>> getNextChunkAccordionMessages() {
        Map<String, List<MessageTuple>> dailyGames = new HashMap<>();

        ChatroomData superChatroom = dataRepository.getSuperChatroom();
        if (superChatroom == null) {
            return dailyGames; // No active chatrooms
        }

        int chunkStart = chunkidx * CHUNKSIZE;
        int chunkEnd = min(superChatroom.daysPlayed().size(), (chunkidx + 1) * CHUNKSIZE);

        if (chunkStart >= superChatroom.daysPlayed().size()) {
            return dailyGames; // No more messages
        }

        for (LocalDate day : superChatroom.daysPlayed().subList(chunkStart, chunkEnd)) {
            List<MessageTuple> dailyMsgs = new ArrayList<>();
            for (ChatroomMember member : superChatroom.members()) {
                Map<LocalDate, ChatroomMessage> msgs = member.messages();
                if (msgs.containsKey(day)) {
                    dailyMsgs.add(new MessageTuple(msgs.get(day).author(), msgs.get(day).message()));
                }
            }
            dailyGames.put(String.valueOf(day), dailyMsgs);
        }

        this.chunkidx += 1;
        return dailyGames;
    }

    private void updateAccordionAllMessages() {
        Accordion dailyAccordion = allMessagesSection.getDailyMessagesAccordion();
        dailyAccordion.getChildren().forEach(dailyAccordion::remove);
        chunkidx = 0;
        addAccordionMessages();
        updateLoadMoreButton();
    }

    private boolean hasMoreMessages() {
        ChatroomData superChatroom = dataRepository.getSuperChatroom();
        if (superChatroom == null) {
            return false;
        }

        int chunkStart = chunkidx * CHUNKSIZE;
        return chunkStart < superChatroom.daysPlayed().size();
    }

    private void updateLoadMoreButton() {
        Button loadMoreBtn = allMessagesSection.getBtnLoadMore();
        if (hasMoreMessages()) {
            loadMoreBtn.setEnabled(true);
            loadMoreBtn.setText("Load More");
        } else {
            loadMoreBtn.setEnabled(false);
            loadMoreBtn.setText("No More Messages");
        }
    }

    private void setupPage() {
        List<ChatroomData> allChatrooms = dataRepository.getAllChatrooms();

        chatroomSelector.getMultiselectChats().setItems(allChatrooms);

        List<ChatroomData> activeChatrooms = dataRepository.getActiveChatrooms();
        if (!activeChatrooms.isEmpty()) {
            chatroomSelector.getMultiselectChats().setValue(activeChatrooms);
        }

        updateEvaluation();
        updateAccordionAllMessages();
        updateLoadMoreButton();
    }

    private void updatePage() {
        updateMultiselectGroups();
        updateEvaluation();
        updateAccordionAllMessages();
    }

    private <T> Map<String, T> aggregateByPerson(LocalDate start,
                                                 LocalDate end,
                                                 Supplier<T> containerSupplier,
                                                 BiConsumer<T, ScoreEntry> aggregator) {
        Map<String, T> result = new HashMap<>();

        for (ChatroomMember member : dataRepository.getSuperChatroom().members()) {
            for (Map.Entry<LocalDate, ChatroomMessage> entry : member.messages().entrySet()) {
                LocalDate messageDate = entry.getKey();

                if (messageDate.isBefore(start) || messageDate.isAfter(end)) {
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

    private Map<String, List<Integer>> aggregateScoresByPerson(LocalDate start, LocalDate end) {
        return aggregateByPerson(
                start,
                end,
                ArrayList::new,
                (list, entry) -> list.add(entry.score())
        );
    }

    private Map<String, Map<LocalDate, Integer>> aggregateTemporalScoresByPerson(LocalDate start, LocalDate end) {
        return aggregateByPerson(
                start,
                end,
                HashMap::new,
                (map, entry) -> map.put(entry.date(), entry.score())
        );
    }

    private void updateEvaluation() {
        HorizontalLayout hlEvaluation = statisticsPanel.getHlEvaluation();
        hlEvaluation.removeAll();

        if (dataRepository.getActiveChatrooms().isEmpty()) {
            hlEvaluation.add(new H3("Select conversations to see statistics"));
            return;
        }

        EvaluationTimeframe selectedTimeframe = statisticsPanel.getSelectTimeframe().getValue();
        final LocalDate cutoffDate;
        final LocalDate endDate;

        if (selectedTimeframe != null && selectedTimeframe.isCustomRange()) {
            statisticsPanel.getHlCustomDateRange().setVisible(true);
            cutoffDate = statisticsPanel.getDatePickerFrom().getValue();
            endDate = statisticsPanel.getDatePickerTo().getValue();
            if (cutoffDate == null || endDate == null) {
                hlEvaluation.add(new H3("Please select both start and end dates"));
                return;
            }
        } else if (selectedTimeframe != null) {
            cutoffDate = selectedTimeframe.getCutoffDate();
            endDate = LocalDate.now();
        } else {
            cutoffDate = LocalDate.MIN;
            endDate = LocalDate.now();
        }

        Map<String, List<Integer>> personScores = aggregateScoresByPerson(cutoffDate, endDate);
        Map<String, Map<LocalDate, Integer>> personTemporalScores = aggregateTemporalScoresByPerson(cutoffDate, endDate);

        if (personScores.isEmpty()) {
            hlEvaluation.add(new H3("No Wordle scores found in selected conversations"));
            return;
        }

        ChatroomData superChatroom = dataRepository.getSuperChatroom();

        LocalDate graphStartDate = superChatroom.firstDayPlayed()
                .filter(date -> !date.isBefore(cutoffDate))
                .orElse(cutoffDate);

        LocalDate graphEndDate = superChatroom.lastDayPlayed().filter(date -> !date.isAfter(endDate)).orElse(endDate);

        for (String personName : personScores.keySet()) {
            ScoreHistogram histogram = new ScoreHistogram(personName, personScores.get(personName));
            hlEvaluation.add(histogram);

            if (personTemporalScores.containsKey(personName)) {
                TemporalScoreGraph temporalGraph = new TemporalScoreGraph(personName,
                                                                          personTemporalScores.get(personName),
                                                                          graphStartDate,
                                                                          graphEndDate);
                hlEvaluation.add(temporalGraph);
            }
        }
    }

    private void saveQueryParameters(BeforeEvent beforeEvent) {
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        filename = parametersMap.get("filename").getFirst();

        log.info("SignalChatView received {}", filename);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        log.info("IM IN");

        saveQueryParameters(beforeEvent);

        signalDataService.loadAllChatrooms(filename);

        this.setupPage();
    }

    private record ScoreEntry(LocalDate date, int score) {}

}
