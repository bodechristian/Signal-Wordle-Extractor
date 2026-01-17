package com.example.Signal.services;

import com.example.Signal.models.ChatroomData;
import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMember;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.repositories.DataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles mappings and data transformations for the DataRepository
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataStructureService {

    private final DataRepository dataRepository;

    public void changeActiveChatrooms(Collection<ChatroomData> chatrooms) {
        log.info("Changing active chatrooms");
        dataRepository.setActiveChatrooms(chatrooms);
        ChatroomData superchatroom = this.createActiveSuperChatroom();
        dataRepository.setSuperChatroom(superchatroom);
    }

    public void addChatroomWithMessages(ChatroomDataSignal chatroomData, List<ChatroomMessage> messages) {
        List<ChatroomMember> members = this.loadMembers(messages);
        List<LocalDate> days_played = this.getDaysPlayed(members);
        ChatroomData newChatroomData = new ChatroomData(chatroomData.id(),
                                                        chatroomData.name(),
                                                        members,
                                                        days_played);
        log.info("added %s".formatted(String.valueOf(newChatroomData)));
        dataRepository.addChatroom(newChatroomData);
    }

    private List<LocalDate> getDaysPlayed(List<ChatroomMember> members) {
        Set<LocalDate> days_played = new HashSet<>();
        for (ChatroomMember member : members) {
            days_played.addAll(member.getMessages().keySet());
        }
        List<LocalDate> d = new ArrayList<>(days_played);
        d.sort(Collections.reverseOrder());
        return d;
    }

    private List<ChatroomMember> loadMembers(List<ChatroomMessage> messages) {
        Map<String, List<ChatroomMessage>> membersMessages = this.selectEachMembersMessages(messages);

        List<ChatroomMember> mylist = new ArrayList<>();
        for (String member_id : membersMessages.keySet()) {
            List<ChatroomMessage> msgs = membersMessages.get(member_id);
            mylist.add(ChatroomMember.builder()
                               .member_id(member_id)
                               .name(msgs.getFirst().author())
                               .messages(convertMessagesToDayMap(msgs))
                               .build());
        }

        return mylist;
    }

    private Map<String, List<ChatroomMessage>> selectEachMembersMessages(List<ChatroomMessage> messages) {
        Map<String, List<ChatroomMessage>> mymap = new HashMap<>();

        for (ChatroomMessage msg : messages) {
            if (msg.authorId() == null) { // fixes discrepency between phone and desktop of owner
                msg = new ChatroomMessage(dataRepository.getOwnerId(),
                                          dataRepository.getOwnerName(),
                                          msg.message(),
                                          msg.timestamp());
            }
            if (!mymap.containsKey(msg.authorId())) {
                mymap.put(msg.authorId(), new ArrayList<>(List.of(msg)));
            } else {
                mymap.get(msg.authorId()).add(msg);
            }
        }

        return mymap;
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

    /**
     * Creates a consolidated super-chatroom from all active chatrooms and sets it in the datarepository.
     * Deduplicates messages by member ID and date - if a person sent the same
     * Wordle score on the same day to multiple chats, it's only counted once.
     *
     * @return A single ChatroomData containing all unique members and deduplicated messages
     */
    public ChatroomData createActiveSuperChatroom() {
        if (dataRepository.getActiveChatrooms().isEmpty()) {
            return null;
        }

        Map<String, String> memberNames = new HashMap<>();
        Map<String, Map<LocalDate, ChatroomMessage>> membersMessages = new HashMap<>();

        for (ChatroomData chatroom : dataRepository.getActiveChatrooms()) {
            for (ChatroomMember member : chatroom.members()) {
                memberNames.putIfAbsent(member.getMember_id(), member.getName());

                Map<LocalDate, ChatroomMessage> memberMessages = membersMessages.computeIfAbsent(member.getMember_id(),
                                                                                                 k -> new HashMap<>());

                for (Map.Entry<LocalDate, ChatroomMessage> entry : member.getMessages().entrySet()) {
                    memberMessages.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }

        // Build the consolidated member list
        List<ChatroomMember> consolidatedMembers = new ArrayList<>();
        for (Map.Entry<String, Map<LocalDate, ChatroomMessage>> entry : membersMessages.entrySet()) {
            String memberId = entry.getKey();
            consolidatedMembers.add(ChatroomMember.builder()
                                            .member_id(memberId)
                                            .name(memberNames.get(memberId))
                                            .messages(entry.getValue())
                                            .build());
        }

        // Get all unique days played across all members
        List<LocalDate> daysPlayed = getDaysPlayed(consolidatedMembers);

        // Create chatroom names from active chatrooms
        String consolidatedName = dataRepository.getActiveChatrooms().size() == 1 ? dataRepository.getActiveChatrooms()
                .getFirst()
                .name() : "Combined (" + dataRepository.getActiveChatrooms().size() + " chats)";

        return new ChatroomData("super-chatroom",
                                consolidatedName,
                                consolidatedMembers,
                                daysPlayed);
    }
}
