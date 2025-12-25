package com.example.Signal.services;

import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.repositories.SQLiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.Signal.Utils.PATHTODBS;
import static com.example.Signal.Utils.commandRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDataService {

    private final SQLiteRepository sqLiteRepository;
    private final DataRepository dataRepository;

    public String decryptDB(String filename, String decryptionKey)  {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String outputFilename = "%s%s.db".formatted(filename, timestamp);

        // 1st prepare .sql file
        try {
            String template = Files.readString(Path.of("unencryptDB-template.sql"));
            template = template.replace("INSERTKEY", decryptionKey)
                    .replace("INSERTFILENAME", PATHTODBS+outputFilename);
            Files.writeString(Path.of("unencryptDB.sql"), template);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // TODO: make these commands atomic somehow.
        // 2nd simultaneous process could change unencryptdb.sql before last command

        // 2nd execute sql file IN SQLCIPHER to create plaintext.db
        String command = String.format("sqlcipher %s < unencryptDB.sql", PATHTODBS+filename);
        commandRunner(command);

        return outputFilename;
    }

    public List<GroupchatDataSignal> analyseFile(String decryptedFilename) {
        return sqLiteRepository.getGroups(decryptedFilename);
    }

    public GroupchatData groupSelected(String filename, String groupId) {
        List<GroupchatMessage> msgs = sqLiteRepository.getGroupsMessages(filename, groupId);
        GroupchatDataSignal groupdata = sqLiteRepository.getGroupById(filename, groupId);
        return dataRepository.addGroupWithMessages(groupdata, msgs);
    }

    public List<GroupchatData> loadAllGroups(String filename) {
        List<GroupchatDataSignal> allGroupsFromFile = sqLiteRepository.getGroups(filename); // TODO: do all chats instead
        List<GroupchatData> loadedGroups = new ArrayList<>();
        for (GroupchatDataSignal group : allGroupsFromFile) {
            loadedGroups.add(this.groupSelected(filename, group.id()));
        }
        return loadedGroups;
    }
}
