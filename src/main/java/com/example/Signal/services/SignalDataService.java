package com.example.Signal.services;

import com.example.Signal.Components.SelectionRow;
import com.example.Signal.models.GroupchatData;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SignalDataService {
    public void analyseFile(String fileName, String decryptionkey) {
        if (fileName != null) {
            String decryptedFileName = decryptDB(fileName, decryptionkey);
            List<GroupchatData> groupchats = readDB(decryptedFileName);

            // todo: send to new view with data
            VerticalLayout popupVL = new VerticalLayout();
            popupVL.getStyle().set("gap", "0px !important");
            for (GroupchatData groupchatData : groupchats) {
                popupVL.add(new SelectionRow(groupchatData.getId())); // todo: besser aufbereiten, mit mehr daten
            }

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Your groups");
            dialog.getHeader().getElement().getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center");
            dialog.add(popupVL);
            dialog.setMaxHeight("45%");
            dialog.open();
        }
    }

    private String decryptDB(String fileName, String decryptionKey) {
        log.info("Decrypting file: " + fileName);
        log.info("Decrypting file: " + decryptionKey);

        // 1st prepare .sql file
        String command = String.format("sed s/INSERTHERE/%s/ unencryptDB-template.sql > unencryptDB.sql", decryptionKey);
        commandRunner(command);

        // 2nd execute sql file to create plaintext.db
        command = String.format("sqlcipher %s < unencryptDB.sql", fileName);
        commandRunner(command);

        return "plaintext.db";
    }

    private void commandRunner(String command) {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            log.info("ExitCode: " + exitCode);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
