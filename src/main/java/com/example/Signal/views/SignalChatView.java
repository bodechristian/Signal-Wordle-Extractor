package com.example.Signal.views;

import com.example.Signal.Components.MessagesAccordionRow;
import com.example.Signal.Components.ScoreHistogram;
import com.example.Signal.models.ChatroomData;
import com.example.Signal.models.ChatroomMember;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.models.EvaluationTimeframe;
import com.example.Signal.models.MessageTuple;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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

    HorizontalLayout contentHeader;
    VerticalLayout contentContainer;

    Accordion accordionSuper;
    Accordion accordionAllMessages;
    MultiSelectComboBox<ChatroomData> multiselectChats;
    HorizontalLayout hlEvaluation;
    Select<EvaluationTimeframe> selectTimeframe;
    DatePicker datePickerFrom;
    DatePicker datePickerTo;
    HorizontalLayout hlCustomDateRange;

    private String filename;
    private int chunkidx = 0;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Overview"));

        // Header
        contentHeader = new HorizontalLayout();
        multiselectChats = new MultiSelectComboBox<>();
        multiselectChats.setLabel("Select conversations");
        multiselectChats.setItemLabelGenerator(ChatroomData::name);
        multiselectChats.addValueChangeListener(event -> {
            if (!event.isFromClient()) {
                return; // ignore programmatic changes
            }
            dataRepository.setActiveChatrooms(event.getValue());
            this.updatePage();
        });
        contentHeader.add(multiselectChats);

        // Content
        contentContainer = new VerticalLayout();
        contentContainer.addClassNames("chat-container");

        accordionSuper = new Accordion();
        accordionSuper.addClassName("chat-accordion-super");

        // Create statistics panel with timeframe selector
        VerticalLayout statisticsPanel = new VerticalLayout();
        statisticsPanel.setPadding(false);
        statisticsPanel.setSpacing(true);
        statisticsPanel.addClassName("statistics-panel");

        // Timeframe selector
        selectTimeframe = new Select<>();
        selectTimeframe.setLabel("Timeframe");
        selectTimeframe.setItems(EvaluationTimeframe.values());
        selectTimeframe.setValue(EvaluationTimeframe.ALL_TIME);
        selectTimeframe.addValueChangeListener(event -> {
            if (!event.isFromClient()) {
                return; // ignore programmatic changes
            }
            updateCustomDateRangeVisibility();
            this.updateEvaluation();
        });
        selectTimeframe.setWidth("200px");
        
        // Custom date range pickers
        datePickerFrom = new DatePicker("From");
        datePickerFrom.setValue(LocalDate.now().minusDays(30));
        datePickerFrom.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                this.updateEvaluation();
            }
        });
        datePickerFrom.setWidth("150px");
        
        datePickerTo = new DatePicker("To");
        datePickerTo.setValue(LocalDate.now());
        datePickerTo.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                this.updateEvaluation();
            }
        });
        datePickerTo.setWidth("150px");
        
        hlCustomDateRange = new HorizontalLayout(datePickerFrom, datePickerTo);
        hlCustomDateRange.setVisible(false);
        hlCustomDateRange.setSpacing(true);

        // Histograms container
        hlEvaluation = new HorizontalLayout();
        hlEvaluation.addClassName("evaluation-container");
        hlEvaluation.setWidth("100%");

        statisticsPanel.add(selectTimeframe, hlCustomDateRange, hlEvaluation);

        accordionAllMessages = new Accordion();
        
        // Create Load More button and container
        Button btnLoadMore = new Button("Load More");
        btnLoadMore.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnLoadMore.addClickListener(e -> this.addAccordionMessages());
        HorizontalLayout hlLoadMore = new HorizontalLayout(btnLoadMore);
        hlLoadMore.addClassName("load-more-container");
        
        // Create a container for messages + load more button
        VerticalLayout allMessagesContent = new VerticalLayout();
        allMessagesContent.setPadding(false);
        allMessagesContent.setSpacing(false);
        allMessagesContent.add(accordionAllMessages, hlLoadMore);
        
        accordionSuper.add("Statistics", statisticsPanel);
        accordionSuper.add("All Messages", allMessagesContent);

        contentContainer.add(accordionSuper);

        this.add(contentHeader, contentContainer);
    }

    private void updateMultiselectGroups() {
        List<ChatroomData> allChatrooms = dataRepository.getAllChatrooms();

        multiselectChats.setItems(allChatrooms);
        multiselectChats.setValue(dataRepository.getActiveChatrooms());
    }

    private void addAccordionMessages() {
        Map<String, List<MessageTuple>> dailyGames = this.getNextChunkAccordionMessages();
        
        // Sort by date (most recent first) before adding to accordion
        dailyGames.entrySet().stream()
                .sorted((e1, e2) -> LocalDate.parse(e2.getKey()).compareTo(LocalDate.parse(e1.getKey())))
                .forEach(entry -> this.accordionAllMessages.add(entry.getKey(), new MessagesAccordionRow(entry.getValue())));
    }

    private Map<String, List<MessageTuple>> getNextChunkAccordionMessages() {
        // loads one chunk of messages
        // currently does it for each group, which obviously is wrong and would load a lot when many groups
        // this gets fixed when reworking groups into 1 supergroup in dataRepository
        Map<String, List<MessageTuple>> dailyGames = new HashMap<>();
        for (ChatroomData chatroomData : dataRepository.getActiveChatrooms()) {
            int chunkStart = chunkidx * CHUNKSIZE;
            int chunkEnd = min(chatroomData.days_played().size(), (chunkidx + 1) * CHUNKSIZE);
            if (chunkStart > chatroomData.days_played().size()) {
                continue;
            }

            for (LocalDate day : chatroomData.days_played().subList(chunkStart, chunkEnd)) {
                List<MessageTuple> dailyMsgs = new ArrayList<>();
                for (ChatroomMember member : chatroomData.members()) {
                    Map<LocalDate, ChatroomMessage> msgs = member.getMessages();
                    if (msgs.containsKey(day)) {
                        dailyMsgs.add(new MessageTuple(msgs.get(day).author(), msgs.get(day).message()));
                    }
                }
                dailyGames.put(String.valueOf(day), dailyMsgs);
            }
        }
        this.chunkidx += 1;
        return dailyGames;
    }

    private void updateAccordionAllMessages() {
        accordionAllMessages.getChildren().forEach(accordionAllMessages::remove);
        this.chunkidx = 0;
        this.addAccordionMessages();
    }

    private void setupPage() {
        List<ChatroomData> allChatrooms = dataRepository.getAllChatrooms();

        multiselectChats.setItems(allChatrooms);

        updateEvaluation();
    }

    private void updatePage() {
        this.updateMultiselectGroups();
        this.updateEvaluation();
        this.updateAccordionAllMessages();
    }

    /**
     * Aggregates Wordle scores by person from active chatrooms within the specified timeframe
     * @param timeframe The timeframe enum value to filter messages
     * @return Map of person name to their list of scores
     */
    private Map<String, List<Integer>> aggregateScoresByPerson(EvaluationTimeframe timeframe) {
        Map<String, List<Integer>> personScores = new HashMap<>();
        
        // Determine cutoff date
        LocalDate cutoffDate;
        LocalDate endDate = LocalDate.now();
        
        if (timeframe.isCustomRange()) {
            cutoffDate = datePickerFrom.getValue();
            endDate = datePickerTo.getValue();
            if (cutoffDate == null || endDate == null) {
                return personScores; // Empty map if dates not set
            }
        } else {
            cutoffDate = timeframe.getCutoffDate();
        }

        for (ChatroomData chatroomData : dataRepository.getActiveChatrooms()) {
            for (ChatroomMember member : chatroomData.members()) {
                String personName = member.getName();
                Map<LocalDate, ChatroomMessage> messages = member.getMessages();

                for (Map.Entry<LocalDate, ChatroomMessage> entry : messages.entrySet()) {
                    LocalDate messageDate = entry.getKey();
                    ChatroomMessage message = entry.getValue();

                    // Filter by timeframe
                    if (messageDate.isBefore(cutoffDate) || messageDate.isAfter(endDate)) {
                        continue;
                    }

                    int score = parseWordleScore(message.message());
                    if (score != -1) { // Valid score
                        personScores.computeIfAbsent(personName, k -> new ArrayList<>()).add(score);
                    }
                }
            }
        }

        return personScores;
    }
    
    /**
     * Updates the visibility of the custom date range pickers based on selected timeframe
     */
    private void updateCustomDateRangeVisibility() {
        EvaluationTimeframe selected = selectTimeframe.getValue();
        hlCustomDateRange.setVisible(selected != null && selected.isCustomRange());
    }
    
    /**
     * Updates the evaluation section with score histograms for each person
     */
    private void updateEvaluation() {
        hlEvaluation.removeAll();

        if (dataRepository.getActiveChatrooms().isEmpty()) {
            hlEvaluation.add(new H3("Select conversations to see statistics"));
            return;
        }

        EvaluationTimeframe selectedTimeframe = selectTimeframe.getValue();
        Map<String, List<Integer>> personScores = aggregateScoresByPerson(selectedTimeframe);

        if (personScores.isEmpty()) {
            hlEvaluation.add(new H3("No Wordle scores found in selected conversations"));
            return;
        }

        // Create a histogram for each person
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
