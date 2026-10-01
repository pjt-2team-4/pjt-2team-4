package com.rookies6.myspringboot4project.sec.common.enums;

/**
 * 취약점 처리 상태. 사용자가 결과 화면에서 변경한다 (14번 API).
 * 백엔드 충돌 방지를 위해 공통 enum 추가 하였음.
 */
public enum FindingStatus {

    OPEN("미해결"),
    RESOLVED("해결"),
    IGNORED("무시");   // 오탐 처리 — 통계 집계에서 제외

    private final String displayName;

    FindingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCountable() {
        return this != IGNORED;
    }
}