package com.example.Signal.repositories;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class QueryManager {
    private static final Map<Querynames, String> queries = Map.of(
            Querynames.A, "b",
            Querynames.GETGROUPS, "SELECT id, members FROM conversations WHERE conversations.type = 'group'",
            Querynames.GETGROUPSMESSAGES, "SELECT conversations.profileFullName, messages.body, messages.sent_at\n" +
                    "FROM messages\n" +
                    "LEFT JOIN conversations\n" +
                    "ON messages.sourceServiceId = conversations.id\n" +
                    "WHERE messages.conversationId = <INSERTGROUPID>\n" + // TODO: How do I handle the inserting of groupid?
                    "ORDER BY messages.sent_at DESC" // TODO: recheck how to get name. Is conversation table even necessary?
    );

    public static Optional<ResultSet> executeQuery(String filename, Querynames queryname) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
                Statement statement = connection.createStatement()
        ) {
            statement.setQueryTimeout(30);
            return Optional.of(statement.executeQuery(queries.get(queryname)));
        } catch (SQLException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    public static String getQuery(Querynames queryname) {
        return queries.get(queryname);
    }
}
