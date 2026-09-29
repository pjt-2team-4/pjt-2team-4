package com.rookies6.myspringboot4project.sec.common.enums;

import java.util.Collection;
import java.util.Comparator;

/**
 * 취약점 심각도. 값은 LLM이 아니라 룰(SecurityRule)이 부여한다.
 */
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

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }

    /** 테스트 파일에서 탐지된 경우 1단계 하향 */
    public Severity downgrade() {
        return switch (this) {
            case CRITICAL -> HIGH;
            case HIGH -> MEDIUM;
            default -> LOW;
        };
    }

    /** analysis_request.overall_severity 산출용 */
    public static Severity highest(Collection<Severity> severities) {
        return severities.stream()
                .max(Comparator.comparingInt(Severity::getWeight))
                .orElse(null);
    }
}