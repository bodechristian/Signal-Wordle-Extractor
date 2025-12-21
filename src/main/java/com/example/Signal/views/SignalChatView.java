package com.example.Signal.views;

import com.example.Signal.Components.CardChatMessage;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Route("/signal/chat")
public class SignalChatView extends VerticalLayout implements HasUrlParameter<String> {

    SignalDataService signalDataService;

    VerticalLayout chatMessageContainer;

    String groupid;
    String filename;

    public SignalChatView(SignalDataService signalDataService) {
        this.signalDataService = signalDataService;

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
        filename = parametersMap.get("filename").getFirst();
        
        List<GroupchatMessage> msgs = signalDataService.extractWordleMessages(filename, groupid);
        for (GroupchatMessage msg : msgs) {
            chatMessageContainer.add(new CardChatMessage(msg.author(), msg.message()));
        }
    }
}
