package com.example.Signal.repositories;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class QueryManager {
    private static final Map<Querynames, String> queries = Map.of(
            Querynames.A, "b",
            Querynames.GETGROUPS, "SELECT id, members FROM conversations WHERE conversations.type = 'group'",
            Querynames.GETGROUPSMESSAGES, "SELECT conversations.name, messages.body, messages.sent_at\n" +
                    "FROM messages\n" +
                    "LEFT JOIN conversations\n" +
                    "ON messages.sourceServiceId = conversations.serviceId\n" +
                    "WHERE messages.conversationId = <INSERTGROUPID>\n" + // TODO: How do I handle the inserting of groupid?
                    "ORDER BY messages.sent_at DESC" // TODO: recheck how to get name. Is conversation table even necessary?
    );

    public static String getQuery(Querynames queryname) {
        return queries.get(queryname);
    }
}
