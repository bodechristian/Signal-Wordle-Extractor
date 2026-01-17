package com.example.Signal.repositories;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Query Manager stores and handles the SQL queries with and without parameters
 * If a SQL query has a parameter it should be added to queriesWithParameters and have a specific function
 * where it inserts the parameter into the SQL query
 */
@Slf4j
public class QueryManager {
    private static final Map<Querynames, String> queries = Map.ofEntries(
            Map.entry(Querynames.GETOWNERID, "SELECT json FROM items WHERE id = 'uuid_id'"),
            Map.entry(Querynames.GETUSERSNAME, "SELECT profileFullName FROM conversations WHERE serviceId = '%s'"),
            Map.entry(Querynames.GETDMS, """
                    SELECT id,
                           COALESCE(NULLIF(name, ''), NULLIF(profileFullName, ''), 'Unknown') as name,
                           serviceId as members
                    FROM conversations
                    WHERE conversations.type = 'private'
                    AND profileName IS NOT NULL
                    AND profileName != ''"""),
            Map.entry(Querynames.GETGROUPS, "SELECT id, name, members FROM conversations WHERE conversations.type = 'group'"),
            Map.entry(Querynames.GETCHATROOMMESSAGES, """
                    SELECT conversations.serviceId, conversations.profileFullName, messages.body, messages.sent_at
                    FROM messages
                    LEFT JOIN conversations
                    ON messages.sourceServiceId = conversations.serviceId
                    WHERE messages.conversationId = '%s'
                    AND messages.body GLOB 'Wordle [0-9.,]* [1-6X]/6*'
                    ORDER BY messages.sent_at DESC""")
    );

    public static String getQuery(Querynames queryname) {
        if (!queries.containsKey(queryname)) {
            log.error("Query {} does not have an SQL query at the moment.", queryname);
            return "";
        }

        if (queries.get(queryname).contains("%s")) {
            log.error("{} requires extra parameters and should be called by its specific function", queryname);
            return "";
        }

        return queries.get(queryname);
    }

    public static String getGroupsMessagesQuery(String groupid) {
        return queries.get(Querynames.GETCHATROOMMESSAGES).formatted(groupid);
    }

    public static String getUsersName(String userId) {
        return queries.get(Querynames.GETUSERSNAME).formatted(userId);
    }

    public static String getChatroomMessagesQuery(String chatroomId) {
        return queries.get(Querynames.GETCHATROOMMESSAGES).formatted(chatroomId);
    }
}
