package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.RecentLogDto;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnalysisProgressStore {

    private static final int MAX_LOG_SIZE = 5;

    private final Map<Long, Deque<RecentLogDto>> logs =
            new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");


    public void addLog(
            Long analysisId,
            String message
    ) {

        Deque<RecentLogDto> queue =
                logs.computeIfAbsent(
                        analysisId,
                        key -> new ArrayDeque<>()
                );

        synchronized (queue) {

            queue.addLast(
                    RecentLogDto.builder()
                            .time(
                                    LocalTime.now()
                                            .format(TIME_FORMAT)
                            )
                            .message(message)
                            .build()
            );

            while (queue.size() > MAX_LOG_SIZE) {
                queue.removeFirst();
            }
        }
    }


    public List<RecentLogDto> getLogs(
            Long analysisId
    ) {

        Deque<RecentLogDto> queue =
                logs.get(analysisId);

        if (queue == null) {
            return List.of();
        }

        synchronized (queue) {
            return new ArrayList<>(queue);
        }
    }


    public void remove(
            Long analysisId
    ) {
        logs.remove(analysisId);
    }
}
