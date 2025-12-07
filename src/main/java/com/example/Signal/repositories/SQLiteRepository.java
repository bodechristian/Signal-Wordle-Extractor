package com.example.Signal.repositories;

import com.example.Signal.models.GroupchatData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class SQLiteRepository {

    private final String filename;

    public SQLiteRepository(String filename) {
        this.filename = filename;
    }

    public List<GroupchatData> getGroups() {
        try (
                // create a database connection
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + this.filename);
                Statement statement = connection.createStatement();
        ) {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT id, members FROM conversations WHERE conversations.type = 'group'");
        } catch (SQLException e) {
            log.error(e.getMessage());
            return List.of();
        }
        ResultSet rs =
                List < GroupchatData > groupchatDataList = new ArrayList<>();
        while (rs.next()) {
            groupchatDataList.add(new GroupchatData(rs.getString("id"), rs.getString("members")));
        }
        return groupchatDataList;
    }
}
