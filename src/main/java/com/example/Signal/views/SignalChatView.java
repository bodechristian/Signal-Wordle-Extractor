package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.repositories.SQLiteRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.H1;
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

    String filename;
    String groupid;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Chat View")); // TODO: get group name
        chatMessageContainer = new VerticalLayout();
        chatMessageContainer.addClassNames("chat-container");

        add(chatMessageContainer);
    }

    private void saveQueryParameters(BeforeEvent beforeEvent) {

        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        filename = parametersMap.get("filename").getFirst();
        groupid = parametersMap.get("groupid").getFirst();

        log.info("Received %s and %s".formatted(filename, groupid));
    }

    private GroupchatData getCurrentGroupdata() {
        GroupchatData groupdata = dataRepository.getGroup(groupid);
        if(groupdata==null) {
            groupdata = signalDataService.groupSelected(filename, groupid);
        }
        return groupdata;
    }

    private void createAccordionAllMessages(GroupchatData groupdata) {
        Accordion acc = new Accordion();
        for (LocalDate day : groupdata.days_played()) {
            VerticalLayout vlDay = new VerticalLayout();
            for (GroupchatMember member : groupdata.members()) {
                Map<LocalDate, GroupchatMessage> msgs = member.getMessages();
                if(msgs.containsKey(day)) {
                    vlDay.add(new CardChatMessage(msgs.get(day).author(), msgs.get(day).message()));
                }
            }
            acc.add(String.valueOf(day), vlDay);
        }
        acc.close();
        chatMessageContainer.add(acc);

    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        log.info("IM IN");

        saveQueryParameters(beforeEvent);

        GroupchatData groupData = this.getCurrentGroupdata();

        this.createAccordionAllMessages(groupData);
    }
}
