package com.example.Signal.views;

import com.example.Signal.components.AllMessagesSection;
import com.example.Signal.components.ChatroomSelector;
import com.example.Signal.components.MessagesAccordionRow;
import com.example.Signal.components.ScoreHistogram;
import com.example.Signal.components.StatisticsPanel;
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
        int chunkEnd = min(superChatroom.days_played().size(), (chunkidx + 1) * CHUNKSIZE);

        if (chunkStart >= superChatroom.days_played().size()) {
            return dailyGames; // No more messages
        }

        for (LocalDate day : superChatroom.days_played().subList(chunkStart, chunkEnd)) {
            List<MessageTuple> dailyMsgs = new ArrayList<>();
            for (ChatroomMember member : superChatroom.members()) {
                Map<LocalDate, ChatroomMessage> msgs = member.getMessages();
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
        return chunkStart < superChatroom.days_played().size();
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

    private Map<String, List<Integer>> aggregateScoresByPerson(EvaluationTimeframe timeframe) {
        Map<String, List<Integer>> personScores = new HashMap<>();

        ChatroomData superChatroom = dataRepository.getSuperChatroom();
        if (superChatroom == null) {
            return personScores;
        }

        LocalDate cutoffDate;
        LocalDate endDate = LocalDate.now();

        if (timeframe.isCustomRange()) {
            cutoffDate = statisticsPanel.getDatePickerFrom().getValue();
            endDate = statisticsPanel.getDatePickerTo().getValue();
            if (cutoffDate == null || endDate == null) {
                return personScores;
            }
        } else {
            cutoffDate = timeframe.getCutoffDate();
        }

        for (ChatroomMember member : superChatroom.members()) {
            String personName = member.getName();
            Map<LocalDate, ChatroomMessage> messages = member.getMessages();

            for (Map.Entry<LocalDate, ChatroomMessage> entry : messages.entrySet()) {
                LocalDate messageDate = entry.getKey();
                ChatroomMessage message = entry.getValue();

                if (messageDate.isBefore(cutoffDate) || messageDate.isAfter(endDate)) {
                    continue;
                }

                int score = parseWordleScore(message.message());
                if (score != -1) {
                    personScores.computeIfAbsent(personName, k -> new ArrayList<>()).add(score);
                }
            }
        }

        return personScores;
    }

    private void updateEvaluation() {
        HorizontalLayout hlEvaluation = statisticsPanel.getHlEvaluation();
        hlEvaluation.removeAll();

        if (dataRepository.getActiveChatrooms().isEmpty()) {
            hlEvaluation.add(new H3("Select conversations to see statistics"));
            return;
        }

        EvaluationTimeframe selectedTimeframe = statisticsPanel.getSelectTimeframe().getValue();
        statisticsPanel.getHlCustomDateRange()
                .setVisible(selectedTimeframe != null && selectedTimeframe.isCustomRange());
        Map<String, List<Integer>> personScores = aggregateScoresByPerson(selectedTimeframe);

        if (personScores.isEmpty()) {
            hlEvaluation.add(new H3("No Wordle scores found in selected conversations"));
            return;
        }

        for (Map.Entry<String, List<Integer>> entry : personScores.entrySet()) {
            ScoreHistogram histogram = new ScoreHistogram(entry.getKey(), entry.getValue());
            hlEvaluation.add(histogram);
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
}
