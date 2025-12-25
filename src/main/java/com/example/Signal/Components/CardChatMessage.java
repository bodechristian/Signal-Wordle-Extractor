package com.example.Signal.Components;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import lombok.Getter;

@Getter
public class CardChatMessage extends Div {

    private FlexLayout card;

    public CardChatMessage(String name, String message) {
        this.addClassNames("chat-row");

        card = new FlexLayout();
        card.addClassNames("chat-message");

        H3 h3name = new H3(name);
        // h3name.getStyle().set("text-align", "center");

        Span span = new Span(message);
        span.setWhiteSpace(HasText.WhiteSpace.PRE_LINE);
        // span.getStyle().set("text-align", "center");
        span.getStyle().set("line-height", "1");

        card.add(h3name, span);
        this.add(card);
    }
}
