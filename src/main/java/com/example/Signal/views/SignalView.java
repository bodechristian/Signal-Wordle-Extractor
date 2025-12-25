package com.example.Signal.views;

import com.example.Signal.Components.GroupchatsDialog;
import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.services.CallbackService;
import com.example.Signal.services.SignalDataService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.example.Signal.Utils.inMB;
import static com.example.Signal.Utils.writeToFile;

@Slf4j
@Route("/signal")
public class SignalView extends VerticalLayout {

    TextField decryptionKeyField;
    String filename;
    SignalDataService signalDataService;
    GroupchatsDialog groupchatsDialog;

    public SignalView(SignalDataService signalDataService) {
        this.signalDataService = signalDataService;

        // Self formatting
        setAlignItems(Alignment.CENTER);
        setSizeFull();

        // Components formatting
        H1 h1 = new H1("Extract Wordle scores");

        Button btnGo = createStartButton();

        decryptionKeyField = createEncryptionkeyTextField();

        Upload upload = createDBUploadArea();

        groupchatsDialog = new GroupchatsDialog();

        add(h1, btnGo, decryptionKeyField, upload, groupchatsDialog);
    }

    private Button createStartButton() {
        Button btn = new Button(
                "Start",
                buttonClickEvent -> startBtnClicked()
        );
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return btn;
    }

    private void startBtnClicked() {
        String decryptedFilename = signalDataService.decryptDB(filename, decryptionKeyField.getValue());
        List<GroupchatDataSignal> groupchats = signalDataService.analyseFile(decryptedFilename);

        groupchatsDialog.openWithGroupchats(groupchats, new CallbackService() {
            @Override
            public void callbackWithGroup(GroupchatDataSignal groupdata) {
                groupchatsDialog.close();
                signalDataService.groupSelected(decryptedFilename, groupdata);
                UI.getCurrent().navigate(SignalChatView.class, "", QueryParameters.full(Map.of(
                        "filename", new String[]{decryptedFilename},
                        "groupid", new String[]{groupdata.id()}
                )));
            }
        });
    }

    private TextField createEncryptionkeyTextField() {
        TextField tf = new TextField();

        tf.setLabel("Your decryption key");
        tf.setRequiredIndicatorVisible(true);
        tf.setPattern("^[0-9A-Fa-f]{64}");

        return tf;
    }

    private Upload createDBUploadArea() {
        Upload upload = new Upload(dbUploadedHandler());
        upload.setMaxFileSize(inMB(100));
        upload.setSizeFull();
        upload.setAcceptedFileTypes("application/octet-stream", ".sqlite", ".db");
        upload.setMaxFiles(1);

        Button btnUpload = new Button("Upload sqlite...");
        btnUpload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(btnUpload);

        return upload;
    }

    private InMemoryUploadHandler dbUploadedHandler() {
        return UploadHandler.inMemory(
                (metadata, data) -> {
                    String filepath = "DBs/";
                    filename = metadata.fileName();
                    String mimeType = metadata.contentType();
                    long contentLength = metadata.contentLength();

                    log.info(filename);
                    log.info(mimeType);
                    log.info(Long.toString(contentLength));

                    writeToFile(filepath + filename, data);
                }
        );
    }
}