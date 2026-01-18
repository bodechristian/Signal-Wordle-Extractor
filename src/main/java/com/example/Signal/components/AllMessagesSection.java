package com.example.Signal.components;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

@Getter
public class AllMessagesSection extends Accordion {

    private final Accordion dailyMessagesAccordion;
    private final Button btnLoadMore;

    public AllMessagesSection(Runnable onLoadMore) {
        addClassName("all-messages-wrapper");

        dailyMessagesAccordion = new Accordion();
        dailyMessagesAccordion.addClassName("daily-messages-accordion");

        btnLoadMore = new Button("Load More");
        btnLoadMore.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnLoadMore.addClickListener(e -> onLoadMore.run());

        HorizontalLayout hlLoadMore = new HorizontalLayout(btnLoadMore);
        hlLoadMore.addClassName("load-more-container");

        VerticalLayout messagesWithButton = new VerticalLayout();
        messagesWithButton.setPadding(false);
        messagesWithButton.setSpacing(false);
        messagesWithButton.add(dailyMessagesAccordion, hlLoadMore);

        add("All Messages", messagesWithButton);
    }
}
