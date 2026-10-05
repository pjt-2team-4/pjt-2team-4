package com.rookies6.myspringboot4project.sec.common.enums;

/**
 * LLM 이 판단한 "룰 탐지가 실제 취약점인지" 여부.
 * 탐지 결과를 취소하는 값이 아니라 화면 배지용 참고값이다 (BR-L008).
 */
public enum LlmVerdict {

    TRUE_POSITIVE("실제 취약점"),
    FALSE_POSITIVE("오탐 가능성"),
    UNCERTAIN("판단 보류");

    private final String displayName;

    LlmVerdict(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LlmVerdict from(String raw) {
        if (raw == null) {
            return UNCERTAIN;
        }
        try {
            return LlmVerdict.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNCERTAIN;
        }
    }
}