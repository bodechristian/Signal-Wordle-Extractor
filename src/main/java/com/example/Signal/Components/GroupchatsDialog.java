package com.example.Signal.Components;

import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.services.CallbackService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GroupchatsDialog extends Dialog {

    private VerticalLayout popupVL;

    public GroupchatsDialog() {
        this.setHeaderTitle("Your groups");
        this.getHeader().getElement().getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");
        this.setMaxHeight("45%");

        createComponents();
    }

    private void createVL() {
        popupVL = new VerticalLayout();
        popupVL.getStyle().set("gap", "0px !important");
        this.add(popupVL);
    }

    private void createComponents() {
        createVL();
    }

    private void resetComponents() {
        this.removeAll();
        this.createComponents();
    }

    public void openWithGroupchats(List<GroupchatDataSignal> groupchats, CallbackService callbackService) {
        this.resetComponents();
        for (GroupchatDataSignal groupchatDataSignal : groupchats) {
            popupVL.add(new SelectionRow(groupchatDataSignal, callbackService)); // todo: besser aufbereiten, mit mehr daten
        }
        this.open();
    }
}
