package com.example.Signal.services;

import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.repositories.SQLiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.example.Signal.Utils.commandRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDataService {

    private final SQLiteRepository sqLiteRepository;
    private final DataRepository dataRepository;

    public List<GroupchatDataSignal> analyseFile(String filename, String decryptionkey) {
        if (filename == null) {
            return List.of();
        }
        String decryptedFilename = decryptDB(filename, decryptionkey);
        return sqLiteRepository.getGroups(decryptedFilename);
    }

    private String decryptDB(String filename, String decryptionKey) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String outputFilename = "%s/%s%s.db".formatted("DBs", filename, timestamp);

        // 1st prepare .sql file
        String command = String.format(
                "sed s=INSERTKEY=%s= unencryptDB-template.sql " +
                "| sed s=INSERTFILENAME=%s= " +
                        "> unencryptDB.sql",
                decryptionKey,
                outputFilename
        );
        commandRunner(command);

        // TODO: make these commands atomic somehow.
        // 2nd simultaneous process could change unencryptdb.sql before last command

        // 2nd execute sql file IN SQLCIPHER to create plaintext.db
        command = String.format("sqlcipher %s < unencryptDB.sql", filename);
        commandRunner(command);

        return outputFilename;
    }

    public void groupSelected(String filename, GroupchatDataSignal groupdata) {
        List<GroupchatMessage> msgs = sqLiteRepository.getGroupsMessages(filename, groupdata.id());
        dataRepository.addGroupWithMessages(groupdata, msgs);
    }
}
