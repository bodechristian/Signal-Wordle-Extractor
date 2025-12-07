package com.example.Signal.Components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class CardProfile extends FlexLayout {

    String text;
    public CardProfile(String text) {
        this();
        this.text = text;
    }

    public CardProfile() {
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setFlexWrap(FlexWrap.WRAP);
        setAlignContent(ContentAlignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        this.text = "SAMPLE TEXT";

        add(new H1(text));
        Button btn = new Button("click me :)");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(btn);
    }
}
