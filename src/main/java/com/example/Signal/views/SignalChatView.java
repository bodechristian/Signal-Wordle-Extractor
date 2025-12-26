package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.accordion.Accordion;
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

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    SignalDataService signalDataService;
    DataRepository dataRepository;

    HorizontalLayout contentHeader;
    VerticalLayout contentContainer;
    Accordion accordionSuper;
    Accordion accordionAllMessages;
    MultiSelectComboBox<GroupchatData> multiselectChats;

    String filename;
    String groupid;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Overview"));

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

        contentContainer = new VerticalLayout();
        contentContainer.addClassNames("chat-container");
        accordionSuper = new Accordion();
        accordionSuper.setWidthFull();
        contentContainer.add(accordionSuper);
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(new H3("tadaa"));
        accordionSuper.add("Statistics", hl);
        accordionAllMessages = new Accordion();
        accordionSuper.add("All Messages", accordionAllMessages);

        this.add(contentHeader, contentContainer);
    }

    private void updateMultiselectGroups() {
        List<GroupchatData> allGroups = dataRepository.getAllGroups();

        multiselectChats.setItems(allGroups);
        multiselectChats.setValue(dataRepository.getActiveGroups());
    }

    private void updateAccordionAllMessages() {
        accordionAllMessages.getChildren().forEach(accordionAllMessages::remove);
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
                accordionAllMessages.add(String.valueOf(day), bubbleDay);
            }
        }
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
