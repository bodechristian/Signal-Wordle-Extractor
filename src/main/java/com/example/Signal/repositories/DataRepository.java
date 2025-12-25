package com.example.Signal.repositories;


import com.example.Signal.models.GroupchatData;
import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMember;
import com.example.Signal.models.GroupchatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class DataRepository {
    private Map<String, GroupchatData> allGroups = new HashMap<>();
    private Map<String, GroupchatData> activeGroups = new HashMap<>();

    private final SQLiteRepository sqLiteRepository;

    public DataRepository(SQLiteRepository sqLiteRepository) {
        this.sqLiteRepository = sqLiteRepository;
    }

    public GroupchatData groupSelected(String filename, String groupId) {
        List<GroupchatMessage> msgs = sqLiteRepository.getGroupsMessages(filename, groupId);
        GroupchatDataSignal groupdata = sqLiteRepository.getGroupById(filename, groupId);
        return this.addGroupWithMessages(groupdata, msgs);
    }

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

        List<GroupchatMember> mylist = new ArrayList<>(List.of());
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
        } else {

        }
    }

    public void setGroupInactive(String id) {
        activeGroups.remove(id);
    }

    public List<GroupchatData> getActiveGroups() {
        return this.activeGroups.values().stream().toList();
    }

    public void addActiveGroups(Collection<GroupchatData> groups) {
        for (GroupchatData groupdata : groups) {
            this.setGroupActive(groupdata.id());
        }
    }

    public void setActiveGroups(Collection<GroupchatData> groups) {
        activeGroups.clear();
        for (GroupchatData groupdata : groups) {
            this.setGroupActive(groupdata.id());
        }
    }

    public List<GroupchatData> getAllGroups() {
        return this.allGroups.values().stream().toList();
    }
}
