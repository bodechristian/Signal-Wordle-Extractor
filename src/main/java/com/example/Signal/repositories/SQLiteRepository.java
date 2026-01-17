package com.example.Signal.repositories;

import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.Signal.Utils.PATHTODBS;

@Slf4j
@Getter
@Repository
public class SQLiteRepository {

    public String getOwnerId(String filename) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getQuery(Querynames.GETOWNERID));

            // Parse query result
            String uuidJsonString = rs.getString("json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(uuidJsonString);
            String uuidOwner = root.get("value").asText().split("\\.")[0];

            rs.close();
            return uuidOwner;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return "";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsersName(String filename, String userId) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getUsersName(userId));

            // Parse query result
            String name = rs.getString("profileFullName");
            rs.close();
            return name;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return "";
        }
    }

    /**
     * Retrieves all chatrooms (both groups and DMs) from the database.
     * 
     * @param filename The database filename
     * @return List of all chatrooms
     */
    public List<ChatroomDataSignal> getAllChatrooms(String filename) {
        log.info("Reading all chatrooms from {}", filename);
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getQuery(Querynames.GETALLCHATROOMS));

            // Parse query result
            List<ChatroomDataSignal> chatroomList = new ArrayList<>();
            while (rs.next()) {
                chatroomList.add(new ChatroomDataSignal(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("members")
                ));
            }

            rs.close();
            return chatroomList;
        } catch (SQLException e) {
            log.error("Failed to retrieve chatrooms", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Retrieves all messages for multiple chatrooms in a single query.
     * 
     * @param filename The database filename
     * @param chatroomIds List of chatroom IDs to fetch messages for
     * @return Map of chatroom ID to list of messages
     */
    public Map<String, List<ChatroomMessage>> getAllChatroomMessages(String filename, List<String> chatroomIds) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getAllChatroomMessagesQuery(chatroomIds));

            Map<String, List<ChatroomMessage>> messagesByChatroom = new HashMap<>();
            while (rs.next()) {
                String chatroomId = rs.getString("conversationId");
                ChatroomMessage message = new ChatroomMessage(
                        rs.getString("serviceId"),
                        rs.getString("profileFullName"),
                        rs.getString("body"),
                        rs.getString("sent_at")
                );
                
                messagesByChatroom.computeIfAbsent(chatroomId, k -> new ArrayList<>()).add(message);
            }

            rs.close();
            return messagesByChatroom;
        } catch (SQLException e) {
            log.error("Failed to batch retrieve messages for chatrooms", e);
            return Collections.emptyMap();
        }
    }
}
