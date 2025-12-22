package com.example.Signal.Components;

import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.services.CallbackService;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectionRow extends HorizontalLayout {
    public String id;
    public CallbackService callbackService;

    public SelectionRow(GroupchatDataSignal groupdata, CallbackService callbackService) {
        this.callbackService = callbackService;
        // am liebsten links image - aber die sind encrypted
        this.add(groupdata.name());
        this.setPadding(true);
        this.addClassName("groupdialog__row");
        this.addClickListener(clickEvent ->
                this.callbackService.callbackWithGroup(groupdata)
        );
    }
}
