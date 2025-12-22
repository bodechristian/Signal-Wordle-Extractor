package com.example.Signal.services;

import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
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
    private final DataRepository dataRepository;

    public List<GroupchatDataSignal> analyseFile(String fileName, String decryptionkey) {
        if (fileName == null) {
            return List.of();
        }
        String decryptedFileName = decryptDB(fileName, decryptionkey);
        return sqLiteRepository.getGroups(decryptedFileName);
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

    public void groupSelected(String filename, GroupchatDataSignal groupdata) {
        List<GroupchatMessage> msgs = sqLiteRepository.getGroupsMessages(filename, groupdata.id());
        dataRepository.addGroupWithMessages(groupdata, msgs);
    }
}
