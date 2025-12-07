package com.example.Signal.Components;

import com.example.Signal.models.GroupchatData;
import com.example.Signal.services.CallbackService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupchatsDialog extends Dialog {

    private VerticalLayout popupVL;

    public GroupchatsDialog() {
        this.setHeaderTitle("Your groups");
        this.getHeader().getElement().getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");
        this.setMaxHeight("45%");

        popupVL = new VerticalLayout();
        popupVL.getStyle().set("gap", "0px !important");
        this.add(popupVL);
    }

    public void openWithGroupchats(List<GroupchatData> groupchats, CallbackService callbackService) {
        popupVL.removeAll();
        for (GroupchatData groupchatData : groupchats) {
            popupVL.add(new SelectionRow(groupchatData.id(), callbackService)); // todo: besser aufbereiten, mit mehr daten
        }
        this.open();
    }
}
