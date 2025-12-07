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
    private static final Map<Querynames, String> queries = Map.of(
            Querynames.GETGROUPS, "SELECT id, members FROM conversations WHERE conversations.type = 'group'",
            Querynames.GETGROUPSMESSAGES, "SELECT conversations.name, messages.body, messages.sent_at\n" +
                    "FROM messages\n" +
                    "LEFT JOIN conversations\n" +
                    "ON messages.sourceServiceId = conversations.serviceId\n" +
                    "WHERE messages.conversationId = '<INSERTGROUPID>'\n" +
                    "ORDER BY messages.sent_at DESC"
    );

    private static final List<Querynames> queriesWithParameters = List.of(Querynames.GETGROUPSMESSAGES);

    public static String getQuery(Querynames queryname) {
        if (!queries.containsKey(queryname)) {
            log.error("Query " + queryname + " does not have an SQL query at the moment.");
            return "";
        }

        if (queriesWithParameters.contains(queryname)) {
            log.error(queryname + " requires extra parameters and should be called by its specific function");
            return "";
        }

        return queries.get(queryname);
    }

    public static String getGroupsMessagesQuery(String groupid) {
        String query = queries.get(Querynames.GETGROUPSMESSAGES);
        return query.replace("<INSERTGROUPID>", groupid);
    }
}
