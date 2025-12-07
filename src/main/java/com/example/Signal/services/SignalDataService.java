package com.example.Signal.services;

import com.example.Signal.Components.GroupchatsDialog;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.SQLiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.Signal.Utils.commandRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDataService {

    private final SQLiteRepository sqLiteRepository;
    private final GroupchatsDialog groupchatsDialog;

    public void analyseFile(String fileName, String decryptionkey) {
        if (fileName == null) {
            return;
        }
        String decryptedFileName = decryptDB(fileName, decryptionkey);
        List<GroupchatData> groupchats = sqLiteRepository.getGroups(decryptedFileName);

        groupchatsDialog.openWithGroupchats(groupchats, new CallbackService() {
            @Override
            public void callbackWithGroupId(String groupId) {
//                invokeRoute("/Signal/Chat", params = {"groupId", groupId}); // localhost:8080/Signal/Chat&groupid={groupid}
                List<GroupchatMessage> groupies = sqLiteRepository.getGroupsMessages(decryptedFileName, groupId);
                extractWordleScores(groupies);
            }
        });
    }

    private String decryptDB(String fileName, String decryptionKey) {
        log.info("Decrypting file: " + fileName);
        log.info("Decrypting file: " + decryptionKey);

        // 1st prepare .sql file
        String command = String.format("sed s/INSERTHERE/%s/ unencryptDB-template.sql > unencryptDB.sql", decryptionKey);
        commandRunner(command);

        // 2nd execute sql file IN SQLCIPHER to create plaintext.db
        command = String.format("sqlcipher %s < unencryptDB.sql", fileName);
        commandRunner(command);

        return "plaintext.db";
    }

    private void extractWordleScores(List<GroupchatMessage> messages) {
        int cnt = 0;
        for (GroupchatMessage group : messages) {
            log.info(group.toString());
            if (++cnt == 10) {
                break;
            }
        }
    }
}
