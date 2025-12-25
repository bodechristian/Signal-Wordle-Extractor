package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    SignalDataService signalDataService;
    DataRepository dataRepository;

    VerticalLayout chatMessageContainer;
    HorizontalLayout contentHeader;

    String filename;
    String groupid;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Overview"));

        contentHeader = new HorizontalLayout();

        chatMessageContainer = new VerticalLayout();
        chatMessageContainer.addClassNames("chat-container");

        this.add(contentHeader, chatMessageContainer);
    }

    private void createMultiBox() {
        List<GroupchatData> allGroups = dataRepository.getAllGroups();

        MultiSelectComboBox<GroupchatData> multiselect = new MultiSelectComboBox<>();
        multiselect.setItems(allGroups);
        multiselect.setItemLabelGenerator(GroupchatData::name);
        multiselect.setValue(dataRepository.getActiveGroups());
        multiselect.addValueChangeListener(e -> {
            dataRepository.setActiveGroups(e.getValue());
            this.updatePage();
        });

        contentHeader.add(multiselect);
    }

    private void createAccordionAllMessages() {
        Accordion acc = new Accordion();
        for (GroupchatData groupdata : dataRepository.getActiveGroups()) {
            for (LocalDate day : groupdata.days_played()) {
                HorizontalLayout bubbleDay = new HorizontalLayout();
                bubbleDay.setWrap(true);
                bubbleDay.setWidthFull();
                for (GroupchatMember member : groupdata.members()) {
                    Map<LocalDate, GroupchatMessage> msgs = member.getMessages();
                    if (msgs.containsKey(day)) {
                        bubbleDay.add(new CardChatMessage(msgs.get(day).author(), msgs.get(day).message()));
                    }
                }
                acc.add(String.valueOf(day), bubbleDay);
            }
        }
        acc.close();
        chatMessageContainer.add(acc);
    }

    private void updatePage() {
        createAccordionAllMessages();
        // TODO: create and update should be separated
        // othweise keep creating new accordions down below
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

        this.createMultiBox();
        this.createAccordionAllMessages();
    }
}
