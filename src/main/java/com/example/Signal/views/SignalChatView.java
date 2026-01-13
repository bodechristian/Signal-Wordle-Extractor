package com.example.Signal.views;

import com.example.Signal.Components.MessagesAccordionRow;
import com.example.Signal.Components.ScoreHistogram;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.models.MessageTuple;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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

    HorizontalLayout contentHeader;
    VerticalLayout contentContainer;

    Accordion accordionSuper;
    Accordion accordionAllMessages;
    MultiSelectComboBox<GroupchatData> multiselectChats;
    HorizontalLayout hlEvaluation;

    private String filename;
    private String groupid;
    private int chunkidx = 0;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Overview"));

        // Header
        contentHeader = new HorizontalLayout();
        multiselectChats = new MultiSelectComboBox<>();
        multiselectChats.setItemLabelGenerator(GroupchatData::name);
        multiselectChats.addValueChangeListener(event -> {
            if (!event.isFromClient()) {
                return; // ignore programmatic changes
            }
            dataRepository.setActiveGroups(event.getValue());
            this.updatePage();
        });
        contentHeader.add(multiselectChats);

        // Content
        contentContainer = new VerticalLayout();
        contentContainer.addClassNames("chat-container");

        accordionSuper = new Accordion();
        accordionSuper.addClassName("chat-accordion-super");
        hlEvaluation = new HorizontalLayout();
        hlEvaluation.addClassName("evaluation-container");
        hlEvaluation.setWidth("100%");
        hlEvaluation.getStyle().set("overflow-x", "auto");
        hlEvaluation.getStyle().set("flex-wrap", "nowrap");
        accordionAllMessages = new Accordion();
        accordionSuper.add("Statistics", hlEvaluation);
        accordionSuper.add("All Messages", accordionAllMessages);

        Button btnLoadMore = new Button("Load More");
        btnLoadMore.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnLoadMore.addClickListener(e -> this.addAccordionMessages());
        HorizontalLayout hlLoadMore = new HorizontalLayout(btnLoadMore);
        hlLoadMore.addClassName("load-more-container");

        contentContainer.add(accordionSuper, hlLoadMore);

        this.add(contentHeader, contentContainer);
    }

    private void updateMultiselectGroups() {
        List<GroupchatData> allGroups = dataRepository.getAllGroups();

        multiselectChats.setItems(allGroups);
        multiselectChats.setValue(dataRepository.getActiveGroups());
    }

    private void addAccordionMessages() {
        Map<String, List<MessageTuple>> dailyGames = this.getNextChunkAccordionMessages();
        dailyGames.forEach((day, msgs) -> this.accordionAllMessages.add(day, new MessagesAccordionRow(msgs)));
    }

    private Map<String, List<MessageTuple>> getNextChunkAccordionMessages() {
        // loads one chunk of messages
        // currently does it for each group, which obviously is wrong and would load a lot when many groups
        // this gets fixed when reworking groups into 1 supergroup in dataRepository
        Map<String, List<MessageTuple>> dailyGames = new HashMap<>();
        for (GroupchatData groupdata : dataRepository.getActiveGroups()) {
            int chunkStart = chunkidx * CHUNKSIZE;
            int chunkEnd = min(groupdata.days_played().size(), (chunkidx + 1) * CHUNKSIZE);
            if (chunkStart > groupdata.days_played().size()) {
                continue;
            }

            for (LocalDate day : groupdata.days_played().subList(chunkStart, chunkEnd)) {
                List<MessageTuple> dailyMsgs = new ArrayList<>();
                for (GroupchatMember member : groupdata.members()) {
                    Map<LocalDate, GroupchatMessage> msgs = member.getMessages();
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

    private void updatePage() {
        this.updateMultiselectGroups();
        this.updateEvaluation();
        this.updateAccordionAllMessages();
    }

    /**
     * Aggregates Wordle scores by person from active groups
     * @return Map of person name to their list of scores
     */
    private Map<String, List<Integer>> aggregateScoresByPerson() {
        Map<String, List<Integer>> personScores = new HashMap<>();
        
        for (GroupchatData groupdata : dataRepository.getActiveGroups()) {
            for (GroupchatMember member : groupdata.members()) {
                String personName = member.getName();
                Map<LocalDate, GroupchatMessage> messages = member.getMessages();
                
                for (GroupchatMessage message : messages.values()) {
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
     * Updates the evaluation section with score histograms for each person
     */
    private void updateEvaluation() {
        hlEvaluation.removeAll();
        
        if (dataRepository.getActiveGroups().isEmpty()) {
            hlEvaluation.add(new H3("Select groups to see statistics"));
            return;
        }
        
        Map<String, List<Integer>> personScores = aggregateScoresByPerson();
        
        if (personScores.isEmpty()) {
            hlEvaluation.add(new H3("No Wordle scores found in selected groups"));
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
        groupid = parametersMap.get("groupid").getFirst();

        log.info("Received {} and {}", filename, groupid);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        log.info("IM IN");

        saveQueryParameters(beforeEvent);

        signalDataService.loadAllGroups(filename);
        dataRepository.setGroupActive(groupid);

        this.updatePage();
    }
}
