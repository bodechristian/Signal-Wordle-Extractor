package com.example.Signal.services;

import com.example.Signal.Components.GroupchatsDialog;
import com.example.Signal.models.GroupchatData;
import com.example.Signal.repositories.SQLiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDataService {

    private final SQLiteRepository sqLiteRepository;
    private final GroupchatsDialog groupchatsDialog;

    public void analyseFile(String fileName, String decryptionkey) {
        if (fileName != null) {
            String decryptedFileName = decryptDB(fileName, decryptionkey);
            List<GroupchatData> groupchats = sqLiteRepository.getGroups(decryptedFileName);

            groupchatsDialog.openWithGroupchats(groupchats);
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
