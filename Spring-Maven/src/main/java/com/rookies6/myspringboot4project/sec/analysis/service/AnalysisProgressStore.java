package com.rookies6.myspringboot4project.sec.analysis.service;

import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnalysisProgressStore {

    private static final int MAX_LOGS = 5;
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Map<Long, Progress> store = new ConcurrentHashMap<>();

    public record LogLine(String time, String message) {
    }

    public record Snapshot(
            String currentFile,
            int processedFiles,
            int totalFiles,
            int findingsSoFar,
            List<LogLine> recentLogs
    ) {
    }

    private static final class Progress {
        private final int totalFiles;
        private final Deque<LogLine> logs = new ArrayDeque<>();
        private String currentFile;
        private int processedFiles;
        private int findingsSoFar;

        private Progress(int totalFiles) {
            this.totalFiles = totalFiles;
        }

        private synchronized void updateFile(String path, int processed) {
            currentFile = path;
            processedFiles = processed;
        }

        private synchronized void addFindings(int count) {
            findingsSoFar += count;
        }

        private synchronized void log(String message) {
            if (logs.size() == MAX_LOGS) {
                logs.removeFirst();
            }
            logs.addLast(new LogLine(LocalTime.now().format(TIME), message));
        }

        private synchronized Snapshot snapshot() {
            return new Snapshot(
                    currentFile, processedFiles, totalFiles,
                    findingsSoFar, List.copyOf(logs));
        }
    }

    public void start(Long analysisId, int totalFiles) {
        store.put(analysisId, new Progress(totalFiles));
    }

    public void updateFile(Long analysisId, String path, int processed) {
        Progress progress = store.get(analysisId);
        if (progress != null) {
            progress.updateFile(path, processed);
        }
    }

    public void addFindings(Long analysisId, int count) {
        Progress progress = store.get(analysisId);
        if (progress != null) {
            progress.addFindings(count);
        }
    }

    public void log(Long analysisId, String message) {
        Progress progress = store.get(analysisId);
        if (progress != null) {
            progress.log(message);
        }
    }

    public Optional<Snapshot> get(Long analysisId) {
        return Optional.ofNullable(store.get(analysisId))
                .map(Progress::snapshot);
    }

    public void clear(Long analysisId) {
        store.remove(analysisId);
    }
}