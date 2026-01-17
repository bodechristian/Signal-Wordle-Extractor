package com.example.Signal.services;

import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.models.ChatroomType;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.repositories.SQLiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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
        // because 2nd simultaneous process could change unencryptdb.sql before last command

        // 2nd execute sql file IN SQLCIPHER to create plaintext.db
        String command = String.format("sqlcipher %s < unencryptDB.sql", PATHTODBS+filename);
        commandRunner(command);

        return outputFilename;
    }

    public void loadChatrooms(List<ChatroomDataSignal> chatrooms, String filename) {
        for (ChatroomDataSignal chatroom : chatrooms) {
            List<ChatroomMessage> msgs = sqLiteRepository.getChatroomMessages(filename, chatroom.id());
            dataRepository.addChatroomWithMessages(chatroom, msgs);
        }
    }

    public void detectAndSetOwner(String filename) {
        // set owner of DB - as his name is just null elsewhere in DB
        String ownerId = sqLiteRepository.getOwnerId(filename);
        dataRepository.setOwnerId(ownerId);
        String ownerName = sqLiteRepository.getUsersName(filename, ownerId);
        dataRepository.setOwnerName(ownerName);
    }

    public void loadAllChatrooms(String filename) {
        detectAndSetOwner(filename);

        // Load both group chats and DMs
        List<ChatroomDataSignal> allGroupsFromFile = sqLiteRepository.getChatrooms(filename, ChatroomType.GROUP);
        List<ChatroomDataSignal> allDMsFromFile = sqLiteRepository.getChatrooms(filename, ChatroomType.PRIVATE);

        this.loadChatrooms(allGroupsFromFile, filename);
        this.loadChatrooms(allDMsFromFile, filename);
    }
}
