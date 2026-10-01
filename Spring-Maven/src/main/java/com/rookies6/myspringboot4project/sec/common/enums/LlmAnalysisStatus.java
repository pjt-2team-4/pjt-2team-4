package com.rookies6.myspringboot4project.sec.common.enums;

public enum LlmAnalysisStatus {

    PENDING("생성중"),
    SUCCESS("완료"),
    FAILED("실패");

    private final String displayName;

    LlmAnalysisStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}