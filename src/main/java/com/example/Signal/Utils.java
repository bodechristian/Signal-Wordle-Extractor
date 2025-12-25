package com.example.Signal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
public class Utils {
    public final static String PATHTODBS = "DBs/";

    public static void commandRunner(String command) {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            log.info("ExitCode: " + exitCode);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

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
}
