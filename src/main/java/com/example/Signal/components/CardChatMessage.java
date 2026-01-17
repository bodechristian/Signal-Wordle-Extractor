package com.example.Signal.components;

import com.example.Signal.Utils;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class CardChatMessage extends FlexLayout {

    public CardChatMessage(String name, String message) {
        this.addClassNames("chat-message");

        H3 header = new H3(name);
        header.addClassName("chat-message__author");

        Span content = createMessage(message);

        this.add(header, content);
    }

    /**
     * parses the messages and creates spans around wordle emoji boxes for better visualization (removing gaps in between)
     *
     * @param message the whole message from the chat
     * @return Span of the message with spans around lines with only wordle-boxes
     */
    public Span createMessage(String message) {
        Span chatmessage = new Span();
        chatmessage.setClassName("chat-message__content");

        String[] lines = message.split("\n");

        for (String line : lines) {
            if (Utils.isWordleLine(line)) {
                Span emojiLine = new Span(line);
                emojiLine.setClassName("emoji-line");
                chatmessage.add(emojiLine);
            } else { // Normal text line
                chatmessage.add(line + "\n");
            }
        }

        return chatmessage;
    }
}
