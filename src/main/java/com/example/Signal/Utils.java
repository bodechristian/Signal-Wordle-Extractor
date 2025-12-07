package com.example.Signal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Slf4j
public class Utils {

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
