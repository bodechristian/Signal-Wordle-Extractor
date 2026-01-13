package com.example.Signal.Components;

import com.example.Signal.models.MessageTuple;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

public class MessagesAccordionRow extends HorizontalLayout {
    public MessagesAccordionRow() {
        super();
        this.addClassName("chat-messages-row");
    }

    public MessagesAccordionRow(List<MessageTuple> msgs) {
        this();
        addMessages(msgs);
    }

    public void addMessages(List<MessageTuple> msgs) {
        msgs.forEach(msg -> this.add(new CardChatMessage(msg.author(), msg.message())));
    }
}
