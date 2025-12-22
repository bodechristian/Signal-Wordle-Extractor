package com.example.Signal.repositories;

import com.example.Signal.models.GroupchatDataSignal;
import com.example.Signal.models.GroupchatMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Getter
@Repository
public class SQLiteRepository {

    public List<GroupchatDataSignal> getGroups(String filename) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getQuery(Querynames.GETGROUPS));

            // Parse query result
            List<GroupchatDataSignal> groupchatDataSignalList = new ArrayList<>();
            while (rs.next()) {
                groupchatDataSignalList.add(new GroupchatDataSignal(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("members")
                ));
            }

            rs.close();
            return groupchatDataSignalList;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<GroupchatMessage> getGroupsMessages(String filename, String groupId) {
        log.info("Filename: {}", filename);
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getGroupsMessagesQuery(groupId));

            // Parse query result
            List<GroupchatMessage> groupchatMessages = new ArrayList<>();
            while (rs.next()) {
                groupchatMessages.add(new GroupchatMessage(
                        rs.getString("serviceId"),
                        rs.getString("profileFullName"),
                        rs.getString("body"),
                        rs.getString("sent_at")
                ));
            }

            rs.close();
            return groupchatMessages;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
}
