package com.example.Signal.services;

import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.repositories.DataRepository;
import com.example.Signal.repositories.SQLiteRepository;
import com.example.Signal.utils.CommandExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.example.Signal.utils.Utils.PATHTODBS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDataService {

    private final SQLiteRepository sqLiteRepository;
    private final DataRepository dataRepository;
    private final DataStructureService dataStructureService;
    private final CommandExecutor commandExecutor;

    public String decryptDB(String filename, String decryptionKey) throws IOException, InterruptedException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String outputFilename = "%s%s.db".formatted(filename, timestamp);

        // 1st prepare .sql file
        try {
            String template = Files.readString(Path.of("unencryptDB-template.sql"));
            template = template.replace("INSERTKEY", decryptionKey)
                    .replace("INSERTFILENAME", PATHTODBS + outputFilename);
            Files.writeString(Path.of("unencryptDB.sql"), template);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        // 2nd execute sql file IN SQLCIPHER to create plaintext.db
        String command = String.format("sqlcipher %s < unencryptDB.sql", PATHTODBS + filename);
        try {
            commandExecutor.executeBash(command);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return outputFilename;
    }

    /**
     * Loads all messages from the given chatrooms from the DB and adds those to the data structure
     *
     * @param chatrooms List of chatrooms to load messages for
     * @param filename  The database filename
     */
    public void loadChatroomsIntoDataRepository(List<ChatroomDataSignal> chatrooms, String filename) {
        if (chatrooms == null || chatrooms.isEmpty()) {
            log.info("No chatrooms to load");
            return;
        }

        List<String> chatroomIds = chatrooms.stream().map(ChatroomDataSignal::id).toList();

        Map<String, List<ChatroomMessage>> messagesByChatroom = sqLiteRepository.getAllChatroomMessages(filename,
                                                                                                        chatroomIds);

        for (ChatroomDataSignal chatroom : chatrooms) {
            List<ChatroomMessage> msgs = messagesByChatroom.getOrDefault(chatroom.id(), List.of());
            dataStructureService.addChatroomWithMessages(chatroom, msgs);
        }
    }

    public void detectAndSetOwner(String filename) {
        // set owner of DB - as his name is just null some messages in DB
        String ownerId = sqLiteRepository.getOwnerId(filename);
        dataRepository.setOwnerId(ownerId);
        String ownerName = sqLiteRepository.getUsersName(filename, ownerId);
        dataRepository.setOwnerName(ownerName);
    }

    /**
     * Loads all chatrooms (both groups and DMs) from the database.
     * Uses a single query to fetch all chatroom types, then batch loads their messages.
     *
     * @param filename The database filename
     */
    public void loadAllChatrooms(String filename) {
        detectAndSetOwner(filename);
        List<ChatroomDataSignal> allChatrooms = sqLiteRepository.getAllChatrooms(filename);
        loadChatroomsIntoDataRepository(allChatrooms, filename);
    }
}
