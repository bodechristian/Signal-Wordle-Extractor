package com.example.Signal.Components;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectionRow extends HorizontalLayout {
    public String id;

    public SelectionRow(String id) {
        // am liebsten links image - aber die sind encrypted
        this.add(id);
        this.setPadding(true);
        this.addClassName("groupdialog__row");
        this.addClickListener(clickEvent -> {
            visualiseGroup(id);
        });
    }

    public void visualiseGroup(String id) {
        //todo: neuer view? wie route ich dahin
        log.info(id);
    }
}
