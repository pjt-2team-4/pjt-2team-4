package com.rookies6.myspringboot4project.sec.analysis.entity;

import lombok.Getter;
import java.util.List;

@Getter
public enum Severity {

    CRITICAL("치명적", 4),
    HIGH("높음", 3),
    MEDIUM("중간", 2),
    LOW("낮음", 1),
    INFO("정보", 0);

    private final String displayName;
    private final int weight;

    Severity(String displayName, int weight) {
        this.displayName = displayName;
        this.weight = weight;
    }

    /**
     * 취약점 목록 중 가장 심각한(가중치가 높은) Severity를 반환하는 메서드
     */
    public static Severity highest(List<Severity> severities) {
        if (severities == null || severities.isEmpty()) {
            return INFO; // 기본값 또는 LOW
        }
        return severities.stream()
                .max(java.util.Comparator.comparingInt(Severity::getWeight))
                .orElse(INFO);
    }

    /**
     * 심각도를 한 단계 낮추는 메서드
     */
    public Severity downgrade() {
        return switch (this) {
            case CRITICAL -> HIGH;
            case HIGH -> MEDIUM;
            case MEDIUM -> LOW;
            case LOW, INFO -> INFO;
        };
    }
}