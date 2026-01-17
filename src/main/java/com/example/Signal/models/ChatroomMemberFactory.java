package com.example.Signal.models;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatroomMemberFactory {
    public ChatroomMember createChatroomMember(String id, Map<LocalDate, ChatroomMessage> messages) {
        return ChatroomMember.builder()
                .member_id(id)
                .name(messages.values().stream().toList().getFirst().author())
                .messages(messages)
                .build();
    }

    public ChatroomMember createChatroomMember(String id, List<ChatroomMessage> messages) {
        return ChatroomMember.builder()
                .member_id(id)
                .name(messages.getFirst().author())
                .messages(convertMessagesToDayMap(messages))
                .build();
    }

    /**
     * Takes a list chatroom messages and converts it into a hashmap with the timestamp as the key
     * Should only be called with a single person's messages
     *
     * @param msgs the list of messages from a person in a chatroom
     * @return the timestamp of each message as the key and the message as the value
     */
    private Map<LocalDate, ChatroomMessage> convertMessagesToDayMap(List<ChatroomMessage> msgs) {
        Map<LocalDate, ChatroomMessage> mymap = new HashMap<>();

        for (ChatroomMessage msg : msgs) {
            mymap.putIfAbsent(msg.timestamp(), msg);
        }

        return mymap;
    }
}
