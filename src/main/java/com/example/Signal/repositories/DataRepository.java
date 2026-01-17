package com.example.Signal.repositories;

import com.example.Signal.models.ChatroomData;
import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMember;
import com.example.Signal.models.ChatroomMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class DataRepository {
    private final Map<String, ChatroomData> allChatrooms = new HashMap<>();
    private final Map<String, ChatroomData> activeChatrooms = new HashMap<>();
    @Getter
    @Setter
    private String ownerId = "";
    @Getter
    @Setter
    private String ownerName = "";

    private void addChatroom(ChatroomData chatroomData) {
        allChatrooms.put(chatroomData.id(), chatroomData);
    }

    public ChatroomData addChatroomWithMessages(ChatroomDataSignal chatroomData, List<ChatroomMessage> messages) {
        List<ChatroomMember> members = this.loadMembers(messages);
        List<LocalDate> days_played = this.getDaysPlayed(members);
        ChatroomData newChatroomData = new ChatroomData(chatroomData.id(), chatroomData.name(), chatroomData.type(), members, days_played);
        log.info("added %s".formatted(String.valueOf(newChatroomData)));
        this.addChatroom(newChatroomData);
        return newChatroomData;
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

    private List<LocalDate> getDaysPlayed(List<ChatroomMember> members) {
        Set<LocalDate> days_played = new java.util.HashSet<>(Set.of());
        for (ChatroomMember member : members) {
            days_played.addAll(member.getMessages().keySet());
        }
        List<LocalDate> d = new ArrayList<>(days_played.stream().toList());
        d.sort(Collections.reverseOrder());
        return d;
    }

    private Map<String, List<ChatroomMessage>> selectEachMembersMessages(List<ChatroomMessage> messages) {
        Map<String, List<ChatroomMessage>> mymap = new HashMap<>();

        for (ChatroomMessage msg : messages) {
            if (msg.authorId() == null) { // fixes discrepency between phone and desktop of owner
                msg = new ChatroomMessage(this.ownerId, this.ownerName, msg.message(), msg.timestamp());
            }
            if (!mymap.containsKey(msg.authorId())) {
                mymap.put(msg.authorId(), new ArrayList<>(List.of(msg)));
            } else {
                mymap.get(msg.authorId()).add(msg);
            }
        }

        return mymap;
    }

    private Map<LocalDate, ChatroomMessage> convertMessagesToDayMap(List<ChatroomMessage> msgs) {
        Map<LocalDate, ChatroomMessage> mymap = new HashMap<>();

        for (ChatroomMessage msg : msgs) {
            if (!mymap.containsKey(msg.timestamp())) {
                mymap.put(msg.timestamp(), msg);
            }
        }

        return mymap;
    }

    public ChatroomData getChatroom(String id) {
        return allChatrooms.get(id);
    }

    public void setChatroomActive(String id) {
        if (allChatrooms.containsKey(id)) {
            activeChatrooms.put(id, allChatrooms.get(id));
        }
    }

    public void setChatroomInactive(String id) {
        activeChatrooms.remove(id);
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

    public void addActiveChatrooms(Collection<ChatroomData> chatrooms) {
        for (ChatroomData chatroomData : chatrooms) {
            this.setChatroomActive(chatroomData.id());
        }
    }

    public List<ChatroomData> getAllChatrooms() {
        return this.allChatrooms.values().stream().toList();
    }

    /**
     * Creates a consolidated super-chatroom from all active chatrooms.
     * Deduplicates messages by member ID and date - if a person sent the same
     * Wordle score on the same day to multiple chats, it's only counted once.
     *
     * @return A single ChatroomData containing all unique members and deduplicated messages
     */
    public ChatroomData getActiveSuperChatroom() {
        if (activeChatrooms.isEmpty()) {
            return null;
        }

        // Collect all unique members across all active chatrooms
        // Key: member_id, Value: ChatroomMember with deduplicated messages
        Map<String, Map<LocalDate, ChatroomMessage>> consolidatedMembersMessages = new HashMap<>();
        Map<String, String> memberNames = new HashMap<>();

        for (ChatroomData chatroom : activeChatrooms.values()) {
            for (ChatroomMember member : chatroom.members()) {
                String memberId = member.getMember_id();

                // Store member name (will be same across all chatrooms)
                memberNames.putIfAbsent(memberId, member.getName());

                // Get or create the message map for this member
                Map<LocalDate, ChatroomMessage> memberMessages =
                    consolidatedMembersMessages.computeIfAbsent(memberId, k -> new HashMap<>());

                // Add messages from this chatroom, deduplicating by date
                // If a message for this date already exists, keep the existing one
                for (Map.Entry<LocalDate, ChatroomMessage> entry : member.getMessages().entrySet()) {
                    memberMessages.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }

        // Build the consolidated member list
        List<ChatroomMember> consolidatedMembers = new ArrayList<>();
        for (Map.Entry<String, Map<LocalDate, ChatroomMessage>> entry : consolidatedMembersMessages.entrySet()) {
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
        String consolidatedName = activeChatrooms.size() == 1
            ? activeChatrooms.values().iterator().next().name()
            : "Combined (" + activeChatrooms.size() + " chats)";

        return new ChatroomData(
            "super-chatroom",
            consolidatedName,
            null, // No specific type for super-chatroom
            consolidatedMembers,
            daysPlayed
        );
    }
}
