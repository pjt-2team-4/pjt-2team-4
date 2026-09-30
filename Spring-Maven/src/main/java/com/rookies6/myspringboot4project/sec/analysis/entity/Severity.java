package com.rookies6.myspringboot4project.sec.analysis.entity;

import lombok.Getter;

import java.util.Collection;
import java.util.Comparator;

@Getter
public enum Severity {

    CRITICAL("치명적", 4),
    HIGH("높음", 3),
    MEDIUM("중간", 2),
    LOW("낮음", 1);

    private final String displayName;
    private final int weight;

    Severity(String displayName, int weight) {
        this.displayName = displayName;
        this.weight = weight;
    }

    /**
     * 취약점 목록 중 가장 심각한(가중치가 높은) Severity를 반환하는 메서드
     * 취약점이 없거나 null인 경우 null을 반환합니다.
     */
    public static Severity highest(Collection<Severity> severities) {
        if (severities == null || severities.isEmpty()) {
            return null; // 또는 필요에 따라 LOW 반환
        }
        return severities.stream()
                .max(Comparator.comparingInt(Severity::getWeight))
                .orElse(null);
    }

    /**
     * 테스트 파일 등에서 탐지된 경우 심각도를 1단계 낮추는 메서드
     */
    public Severity downgrade() {
        return switch (this) {
            case CRITICAL -> HIGH;
            case HIGH -> MEDIUM;
            case MEDIUM, LOW -> LOW;
        };
    }
}