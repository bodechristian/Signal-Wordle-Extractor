package com.example.Signal.repositories;

import com.example.Signal.models.ChatroomDataSignal;
import com.example.Signal.models.ChatroomMessage;
import com.example.Signal.models.ChatroomType;
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
import java.util.List;

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

    public List<ChatroomDataSignal> getChatrooms(String filename, ChatroomType type) {
        log.info("Trying to read {} chatrooms from {}", type, filename);
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            Querynames queryName = type == ChatroomType.GROUP ? Querynames.GETGROUPS : Querynames.GETDMS;
            ResultSet rs = statement.executeQuery(QueryManager.getQuery(queryName));

            // Parse query result
            List<ChatroomDataSignal> chatroomList = new ArrayList<>();
            while (rs.next()) {
                chatroomList.add(new ChatroomDataSignal(
                        rs.getString("id"),
                        rs.getString("name"),
                        type,
                        rs.getString("members")
                ));
            }

            rs.close();
            return chatroomList;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ChatroomMessage> getChatroomMessages(String filename, String chatroomId) {
        log.info("Filename: {}", filename);
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + PATHTODBS + filename);
                Statement statement = connection.createStatement()
        ) {
            // Execute Query
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(QueryManager.getChatroomMessagesQuery(chatroomId));

            // Parse query result
            List<ChatroomMessage> messages = new ArrayList<>();
            while (rs.next()) {
                messages.add(new ChatroomMessage(
                        rs.getString("serviceId"),
                        rs.getString("profileFullName"),
                        rs.getString("body"),
                        rs.getString("sent_at")
                ));
            }

            rs.close();
            return messages;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }
}
