package com.example.Signal.Components;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import lombok.Getter;

@Getter
public class CardProfile extends FlexLayout {

    private String name;
    private String message;

    public CardProfile(String name, String message) {
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setFlexWrap(FlexWrap.WRAP);
        setAlignContent(ContentAlignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setClassName("seperatorBorder");

        add(new H3(name));
        Span span = new Span(message);
        span.setWhiteSpace(HasText.WhiteSpace.PRE_LINE);
        span.getStyle().set("text-align", "center");

        add(span);
    }
}
