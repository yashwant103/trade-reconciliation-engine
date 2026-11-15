package com.github.yashwant3.reconengine.checkpoint;

import com.github.yashwant3.reconengine.util.AtomicFileWriter;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CheckpointManager {
    private static final Path CHECKPOINT_DIR = Paths.get("data", "checkpoints");

    private static Path pathFor(String runId){
        return CHECKPOINT_DIR.resolve(runId + ".chk");
    }

    public static void save(CheckpointState state){
        try{
            Files.createDirectories(CHECKPOINT_DIR);
            String content = "runId=" + state.runId() + "\n"
                    + "lastCommittedBatch=" + state.lastCommittedBatch() + "\n"
                    + "batchSize=" + state.batchSize() + "\n"
                    + "totalBreaks=" + state.totalBreaks() + "\n";
            AtomicFileWriter.writeAtomically(pathFor(state.runId()), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save checkpoint for run " + state.runId(), e);
        }
    }
    public static CheckpointState load(String runId){
        Path path = pathFor(runId);
        if(!Files.exists(path)){
            return new CheckpointState(runId, -1, 500, 0); // no checkpoint yet
        }
        try{
            Properties props = new Properties();
            props.load(new StringReader(Files.readString(path)));
            return new CheckpointState(
                    props.getProperty("runId"),
                    Integer.parseInt(props.getProperty("lastCommittedBatch")),
                    Integer.parseInt(props.getProperty("batchSize")),
                    Integer.parseInt(props.getProperty("totalBreaks"))
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load checkpoint for run " + runId, e);
        }
    }
    public static void delete(String runId){
        try{
            Files.deleteIfExists(pathFor(runId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete checkpoint for run " + runId, e);
        }
    }
}