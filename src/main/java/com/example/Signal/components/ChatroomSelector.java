package com.example.Signal.components;

import com.example.Signal.models.ChatroomData;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;

import java.util.Collection;
import java.util.function.Consumer;

@Getter
public class ChatroomSelector extends HorizontalLayout {

    private final MultiSelectComboBox<ChatroomData> multiselectChats;

    public ChatroomSelector(Consumer<Collection<ChatroomData>> onSelectionChange) {
        multiselectChats = new MultiSelectComboBox<>();
        multiselectChats.setLabel("Select conversations");
        multiselectChats.setItemLabelGenerator(ChatroomData::name);
        multiselectChats.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                onSelectionChange.accept(event.getValue());
            }
        });

        add(multiselectChats);
    }
}
