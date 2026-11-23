package com.github.yashwant3.reconengine.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadLetterLogger {

    private static final Path DEAD_LETTER_DIR = Paths.get("data", "dead-letters");
    private static final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    public static void log(String source, String rawLine, String reason) {
        try {
            BufferedWriter writer = writers.computeIfAbsent(source, DeadLetterLogger::openWriter);
            String entry = Instant.now() + " | reason=" + reason + " | line=" + rawLine;
            synchronized (writer) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write dead-letter entry: " + rawLine + " (" + reason + ")");
        }
    }

    private static BufferedWriter openWriter(String source) {
        try {
            Files.createDirectories(DEAD_LETTER_DIR);
            Path file = DEAD_LETTER_DIR.resolve(source + "_dead_letters.log");
            return Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open dead-letter log for " + source, e);
        }
    }

    public static void closeAll() {
        for (BufferedWriter writer : writers.values()) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close dead-letter writer: " + e.getMessage());
            }
        }
        writers.clear();
    }
}