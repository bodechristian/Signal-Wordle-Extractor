package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    SignalDataService signalDataService;
    DataRepository dataRepository;

    VerticalLayout chatMessageContainer;

    String groupid;

    public SignalChatView(SignalDataService signalDataService, DataRepository dataRepository) {
        this.signalDataService = signalDataService;
        this.dataRepository = dataRepository;

        this.add(new H1("Chat View")); // TODO: get group name
        chatMessageContainer = new VerticalLayout();
        chatMessageContainer.addClassNames("chat-container");

        add(chatMessageContainer);

    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        log.info("IM IN");
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        groupid = parametersMap.get("groupid").getFirst();

        GroupchatData groupData = dataRepository.getGroup(groupid);
        if(groupData==null) {
            chatMessageContainer.add(new H3("Invalid Group ID"));
            return;
        }

        for (LocalDate day : groupData.days_played()) {
            Accordion acc = new Accordion();
            VerticalLayout vlDay = new VerticalLayout();
            for (GroupchatMember member : groupData.members()) {
                Map<LocalDate, GroupchatMessage> msgs = member.getMessages();
                if(msgs.containsKey(day)) {
                    vlDay.add(new CardChatMessage(msgs.get(day).author(), msgs.get(day).message()));
                }
            }
            acc.add(String.valueOf(day), vlDay);
            chatMessageContainer.add(acc);
        }
    }
}
