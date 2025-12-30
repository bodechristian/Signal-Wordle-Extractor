package com.example.Signal.Components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import lombok.Getter;

@Getter
public class CardChatMessage extends Div {

    private final FlexLayout card;

    public CardChatMessage(String name, String message) {
        this.addClassNames("chat-row");

        card = new FlexLayout();
        card.addClassNames("chat-message");

        H3 h3name = new H3(name);
        // h3name.getStyle().set("text-align", "center");

        Span span = createMessage(message);

        card.add(h3name, span);
        this.add(card);
    }

    public Span createMessage(String wordleText) {
        Span chatmessage = new Span();
        chatmessage.setClassName("chat-message__content");

        String[] lines = wordleText.split("\n");

        for (String line : lines) {
            if (isWordleLine(line)) {
                Span emojiLine = new Span(line);
                emojiLine.setClassName("emoji-line");
                chatmessage.add(emojiLine);
            } else { // Normal text line
                chatmessage.add(line + "\n");
            }
        }

        return chatmessage;
    }

    private boolean isWordleLine(String line) {
        String trimmed = line.trim();
        int emojiCount = 0;
        int i = 0;

        while (i < trimmed.length()) {
            char c = trimmed.charAt(i);

            if (c == '\uD83D' && i + 1 < trimmed.length()) {
                // Surrogate pair emoji (yellow, green)
                char low = trimmed.charAt(i + 1);
                if (low == '\uDFE8' || low == '\uDFE9') {
                    emojiCount++;
                    i += 2;
                } else {
                    return false;
                }
            } else if (c == '⬜' || c == '⬛') {
                // Single char emoji (white, black squares)
                emojiCount++;
                i += 1;
            } else {
                return false;
            }
        }

        return emojiCount == 5;
    }
}
