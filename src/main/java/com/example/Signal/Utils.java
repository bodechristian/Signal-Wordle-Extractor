package com.example.Signal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class Utils {

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

    public static void writeToFile(String fileName, byte[] data) {
        try {
            FileUtils.writeByteArrayToFile(new File(fileName), data);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static int inMB(Integer mb) {
        return mb * 1024 * 1024;
    }
}
