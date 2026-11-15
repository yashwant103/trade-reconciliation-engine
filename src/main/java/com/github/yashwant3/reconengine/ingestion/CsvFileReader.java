package com.github.yashwant3.reconengine.ingestion;

import com.github.yashwant3.reconengine.metrics.MetricsRegistry;
import com.github.yashwant3.reconengine.model.Trade;
import com.github.yashwant3.reconengine.util.DeadLetterLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CsvFileReader {
    private static final int CHUNK_SIZE = 2000; // lines per virtual-thread task

    public static Map<String,Trade> readTradesConcurrently(Path filePath, Trade.Source source){

        long readStart = System.nanoTime();
        List<String> lines;
        try(BufferedReader reader = Files.newBufferedReader(filePath)){
            lines = reader.lines().skip(1).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + filePath, e);
        }
        long readEnd = System.nanoTime();

        Map<String,Trade> tradeMap = new ConcurrentHashMap<>();
        try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
            List<Future<?>> futures = new ArrayList<>();
            for(int start = 0; start < lines.size(); start += CHUNK_SIZE){
                int end = Math.min(start + CHUNK_SIZE, lines.size());
                List<String> chunk = lines.subList(start, end);
                futures.add(executor.submit(() -> {
                    for(String line : chunk){
                        try {
                            Trade t = TradeParser.parse(line, source);
                            tradeMap.put(t.tradeId(), t);
                            MetricsRegistry.incrementRowsIngested(source.name());
                        } catch (TradeParseException e) {
                            DeadLetterLogger.log(source.name(), e.getRawLine(), e.getMessage());
                            MetricsRegistry.incrementDeadLetters(source.name());
                        }
                    }
                }));
            }
            for(Future<?> f : futures){
                f.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ingestion interrupted for " + filePath, e);
        } catch (Exception e) {
            throw new RuntimeException("A chunk failed to parse in " + filePath, e.getCause());
        }
        long parseEnd = System.nanoTime();

        System.out.printf("  [%s] file read: %.2f ms, parallel parse: %.2f ms%n",
                source, (readEnd - readStart) / 1_000_000.0, (parseEnd - readEnd) / 1_000_000.0);

        return tradeMap;
    }

    // Baseline for benchmarking only — single-threaded, no chunking, plain HashMap.
    public static Map<String,Trade> readTradesSequential(Path filePath, Trade.Source source){
        Map<String,Trade> tradeMap = new HashMap<>();
        try(BufferedReader reader = Files.newBufferedReader(filePath)){
            String line = reader.readLine(); // skip header
            while((line = reader.readLine()) != null){
                Trade t = TradeParser.parse(line, source);
                tradeMap.put(t.tradeId(), t);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + filePath, e);
        }
        return tradeMap;
    }

    // Experiment: each chunk builds its own private HashMap (no synchronization),
    // then all chunk maps are merged into one at the end.
    public static Map<String,Trade> readTradesConcurrentLocalMerge(Path filePath, Trade.Source source){

        long readStart = System.nanoTime();
        List<String> lines;
        try(BufferedReader reader = Files.newBufferedReader(filePath)){
            lines = reader.lines().skip(1).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + filePath, e);
        }
        long readEnd = System.nanoTime();

        List<Future<Map<String,Trade>>> futures = new ArrayList<>();
        try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
            for(int start = 0; start < lines.size(); start += CHUNK_SIZE){
                int end = Math.min(start + CHUNK_SIZE, lines.size());
                List<String> chunk = lines.subList(start, end);
                futures.add(executor.submit(() -> {
                    Map<String,Trade> localMap = new HashMap<>(); // no synchronization needed
                    for(String line : chunk){
                        Trade t = TradeParser.parse(line, source);
                        localMap.put(t.tradeId(), t);
                    }
                    return localMap;
                }));
            }

            long parseStart = System.nanoTime();
            Map<String,Trade> merged = new HashMap<>();
            for(Future<Map<String,Trade>> f : futures){
                merged.putAll(f.get()); // sequential merge — the one serialized step
            }
            long parseEnd = System.nanoTime();

            System.out.printf("  [%s] file read: %.2f ms, parse: %.2f ms, merge: %.2f ms%n",
                    source,
                    (readEnd - readStart) / 1_000_000.0,
                    (parseStart - readEnd) / 1_000_000.0,
                    (parseEnd - parseStart) / 1_000_000.0);

            return merged;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ingestion interrupted for " + filePath, e);
        } catch (Exception e) {
            throw new RuntimeException("A chunk failed to parse in " + filePath, e.getCause());
        }
    }
}