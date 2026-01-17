package com.example.Signal.repositories;

import com.example.Signal.models.ChatroomData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataRepository {
    private final Map<String, ChatroomData> allChatrooms = new HashMap<>();
    private final Map<String, ChatroomData> activeChatrooms = new HashMap<>();
    @Getter
    @Setter
    private ChatroomData superChatroom;
    @Getter
    @Setter
    private String ownerId = "";
    @Getter
    @Setter
    private String ownerName = "";

    public void addChatroom(ChatroomData chatroomData) {
        allChatrooms.put(chatroomData.id(), chatroomData);
    }

    public List<ChatroomData> getActiveChatrooms() {
        return this.activeChatrooms.values().stream().toList();
    }

    public void setActiveChatrooms(Collection<ChatroomData> chatrooms) {
        activeChatrooms.clear();
        for (ChatroomData chatroomData : chatrooms) {
            this.setChatroomActive(chatroomData.id());
        }
    }

    public void setChatroomActive(String id) {
        if (allChatrooms.containsKey(id)) {
            activeChatrooms.put(id, allChatrooms.get(id));
        }
    }

    public List<ChatroomData> getAllChatrooms() {
        return this.allChatrooms.values().stream().toList();
    }
}
