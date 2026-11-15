package com.github.yashwant3.reconengine.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AtomicFileWriter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_DELAY_MS = 50;

    public static void writeAtomically(Path targetPath, String content) throws IOException {
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        Files.writeString(tempPath, content, StandardCharsets.UTF_8);

        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch (IOException e) {
                lastFailure = e;
                try {
                    Thread.sleep(BASE_DELAY_MS * attempt); // 50ms, 100ms, 150ms, 200ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw lastFailure;
    }
}