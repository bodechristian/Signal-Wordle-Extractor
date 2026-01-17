package com.example.Signal.components;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import lombok.Getter;

@Getter
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

    /**
     * checks if a given text-line is a typical wordle line with those boxes
     *
     * @param line the string to parse
     * @return true if the line consists of 5 of those emoji boxes (black/white-yellow-green)
     */
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
