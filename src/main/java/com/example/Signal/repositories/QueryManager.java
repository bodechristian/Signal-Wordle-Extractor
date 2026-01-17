package com.example.Signal.repositories;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
            Map.entry(Querynames.GETALLCHATROOMS, """
                    SELECT id,
                           COALESCE(NULLIF(name, ''), NULLIF(profileFullName, ''), 'Unknown') as name,
                           type,
                           CASE
                               WHEN type = 'private' THEN serviceId
                               WHEN type = 'group' THEN members
                               ELSE ''
                           END as members
                    FROM conversations
                    WHERE (type = 'group' OR (type = 'private' AND profileName IS NOT NULL AND profileName != ''))"""),
            Map.entry(Querynames.GETALLCHATROOMMESSAGES, """
                    SELECT messages.conversationId, conversations.serviceId, conversations.profileFullName, messages.body, messages.sent_at
                    FROM messages
                    LEFT JOIN conversations
                    ON messages.sourceServiceId = conversations.serviceId
                    WHERE messages.conversationId IN (%s)
                    AND messages.body GLOB 'Wordle [0-9.,]* [1-6X]/6*'
                    ORDER BY messages.conversationId, messages.sent_at DESC""")
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

    public static String getUsersName(String userId) {
        return queries.get(Querynames.GETUSERSNAME).formatted(userId);
    }

    /**
     * Generates a query to fetch all messages from multiple chatrooms at once.
     * 
     * @param chatroomIds List of chatroom IDs to fetch messages for
     * @return SQL query for all messages for the given chatroom IDs
     */
    public static String getAllChatroomMessagesQuery(List<String> chatroomIds) {
        String idsString = chatroomIds.stream()
                .map(id -> "'" + id.replace("'", "''") + "'")  // Escape single quotes
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        
        return queries.get(Querynames.GETALLCHATROOMMESSAGES).formatted(idsString);
    }
}
