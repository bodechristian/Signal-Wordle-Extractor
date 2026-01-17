package com.example.Signal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
public class Utils {
    public final static String PATHTODBS = "DBs/";

    public static void writeToFile(String filepath, byte[] data) {
        try {
            FileUtils.writeByteArrayToFile(new File(filepath), data);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static LocalDate toLocalDate(String timestampString) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(timestampString)), ZoneId.systemDefault());
    }

    public static int inMB(Integer mb) {
        return mb * 1024 * 1024;
    }

    /**
     * Parses a Wordle score from a message.
     * Expected format: "Wordle XXX X/6" where X is the score (1-6, or X for failed)
     *
     * @param message The message to parse
     * @return The score (1-6), or 7 for failed attempts (X/6), or -1 if not a valid Wordle message
     */
    public static int parseWordleScore(String message) {
        if (message == null || message.isEmpty()) {
            return -1;
        }

        // Match pattern like "Wordle 1234 3/6" or "Wordle 1,234 X/6"
        String pattern = "Wordle\\s[0-9.,]+\\s+([1-6X])/6";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(message);

        if (m.find()) {
            String scoreStr = m.group(1);
            if (scoreStr.equals("X")) {
                return 7; // Failed attempt
            }
            try {
                return Integer.parseInt(scoreStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
