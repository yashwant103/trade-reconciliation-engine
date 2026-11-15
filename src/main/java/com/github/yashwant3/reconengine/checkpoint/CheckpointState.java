package com.github.yashwant3.reconengine.checkpoint;

public record CheckpointState(String runId, int lastCommittedBatch, int batchSize, int totalBreaks) {}