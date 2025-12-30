package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
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
import com.vaadin.flow.router.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.min;

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    private final int chunksize = 10;

    SignalDataService signalDataService;
    DataRepository dataRepository;

    HorizontalLayout contentHeader;
    VerticalLayout contentContainer;

    Accordion accordionSuper;
    Accordion accordionAllMessages;
    MultiSelectComboBox<GroupchatData> multiselectChats;

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
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(new H3("tadaa"));
        accordionAllMessages = new Accordion();
        accordionSuper.add("Statistics", hl);
        accordionSuper.add("All Messages", accordionAllMessages);

        Button btnLoadMore = new Button("Load More");
        btnLoadMore.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnLoadMore.addClickListener(e -> this.addAccordionMessages());
        HorizontalLayout hl2 = new HorizontalLayout(btnLoadMore);
        hl2.setWidthFull();
        hl2.getStyle().set("justify-content", "center");

        contentContainer.add(accordionSuper, hl2);

        this.add(contentHeader, contentContainer);
    }

    private void updateMultiselectGroups() {
        List<GroupchatData> allGroups = dataRepository.getAllGroups();

        multiselectChats.setItems(allGroups);
        multiselectChats.setValue(dataRepository.getActiveGroups());
    }

    private void addAccordionMessages() {
        // loads one chunk of messages
        // currently does it for each group, which obviously is wrong and would load a lot when many groups
        // this gets fixed when reworking groups into 1 supergroup in dataRepository
        for (GroupchatData groupdata : dataRepository.getActiveGroups()) {
            int chunkStart = chunkidx * chunksize;
            int chunkEnd = min(groupdata.days_played().size(),(chunkidx + 1) * chunksize);
            if (chunkStart > groupdata.days_played().size()) { continue; }

            for (LocalDate day : groupdata.days_played().subList(chunkStart, chunkEnd)) {
                HorizontalLayout rowDay = new HorizontalLayout();
                rowDay.addClassName("chat-messages-row");
                for (GroupchatMember member : groupdata.members()) {
                    Map<LocalDate, GroupchatMessage> msgs = member.getMessages();
                    if (msgs.containsKey(day)) {
                        rowDay.add(new CardChatMessage(msgs.get(day).author(), msgs.get(day).message()));
                    }
                }
                accordionAllMessages.add(String.valueOf(day), rowDay);
            }
        }
        this.chunkidx += 1;
    }

    private void updateAccordionAllMessages() {
        accordionAllMessages.getChildren().forEach(accordionAllMessages::remove);
        this.chunkidx = 0;
        this.addAccordionMessages();
    }

    private void updatePage() {
        this.updateMultiselectGroups();
        this.updateAccordionAllMessages();
    }

    private void saveQueryParameters(BeforeEvent beforeEvent) {

        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        filename = parametersMap.get("filename").getFirst();
        groupid = parametersMap.get("groupid").getFirst();

        log.info("Received %s and %s".formatted(filename, groupid));
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
