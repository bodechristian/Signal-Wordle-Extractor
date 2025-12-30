package com.example.Signal.repositories;


import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class DataRepository {
    private final Map<String, GroupchatData> allGroups = new HashMap<>();
    private final Map<String, GroupchatData> activeGroups = new HashMap<>();
    @Getter
    @Setter
    private String ownerId = "";
    @Getter
    @Setter
    private String ownerName = "";

    private void addGroup(GroupchatData groupdata) {
        allGroups.put(groupdata.id(), groupdata);
    }

    public GroupchatData addGroupWithMessages(GroupchatDataSignal groupdata, List<GroupchatMessage> messages) {
        List<GroupchatMember> members = this.loadMembers(messages);
        List<LocalDate> days_played = this.getDaysPlayed(members);
        GroupchatData newGroupData = new GroupchatData(groupdata.id(), groupdata.name(), members, days_played);
        log.info("added %s".formatted(String.valueOf(newGroupData)));
        this.addGroup(newGroupData);
        return newGroupData;
    }

    private List<GroupchatMember> loadMembers(List<GroupchatMessage> messages) {
        Map<String, List<GroupchatMessage>> membersMessages = this.selectEachMembersMessages(messages);

        List<GroupchatMember> mylist = new ArrayList<>();
        for (String member_id : membersMessages.keySet()) {
            List<GroupchatMessage> msgs = membersMessages.get(member_id);
            mylist.add(GroupchatMember.builder()
                    .member_id(member_id)
                    .name(msgs.getFirst().author())
                    .messages(convertMessagesToDayMap(msgs))
                    .build());
        }

        return mylist;
    }

    private List<LocalDate> getDaysPlayed(List<GroupchatMember> members) {
        Set<LocalDate> days_played = new java.util.HashSet<>(Set.of());
        for(GroupchatMember member : members) {
            days_played.addAll(member.getMessages().keySet());
        }
        List<LocalDate> d = new ArrayList<>(days_played.stream().toList());
        d.sort(Collections.reverseOrder());
        return d;
    }

    private Map<String, List<GroupchatMessage>> selectEachMembersMessages(List<GroupchatMessage> messages) {
        Map<String, List<GroupchatMessage>> mymap = new HashMap<>();

        for(GroupchatMessage msg : messages) {
            if (msg.authorId()==null) { // fix discrepency between phone and desktop of owner
                msg = new GroupchatMessage(this.ownerId, this.ownerName, msg.message(), msg.timestamp());
            }
            if(!mymap.containsKey(msg.authorId())) {
                mymap.put(msg.authorId(), new ArrayList<>(List.of(msg)));
            } else {
                mymap.get(msg.authorId()).add(msg);
            }
        }

        return mymap;
    }

    private Map<LocalDate, GroupchatMessage> convertMessagesToDayMap(List<GroupchatMessage> msgs) {
        Map<LocalDate, GroupchatMessage> mymap = new HashMap<>();

        for (GroupchatMessage msg : msgs) {
            if(!mymap.containsKey(msg.timestamp())) {
                mymap.put(msg.timestamp(), msg);
            }
        }

        return mymap;
    }

    public GroupchatData getGroup(String id) {
        return allGroups.get(id);
    }

    public void setGroupActive(String id) {
        if (allGroups.containsKey(id)) {
            activeGroups.put(id, allGroups.get(id));
        }
    }

    public void setGroupInactive(String id) {
        activeGroups.remove(id);
    }

    public List<GroupchatData> getActiveGroups() {
        return this.activeGroups.values().stream().toList();
    }

    public void setActiveGroups(Collection<GroupchatData> groups) {
        activeGroups.clear();
        for (GroupchatData groupdata : groups) {
            this.setGroupActive(groupdata.id());
        }
    }

    public void addActiveGroups(Collection<GroupchatData> groups) {
        for (GroupchatData groupdata : groups) {
            this.setGroupActive(groupdata.id());
        }
    }

    public List<GroupchatData> getAllGroups() {
        return this.allGroups.values().stream().toList();
    }
}
