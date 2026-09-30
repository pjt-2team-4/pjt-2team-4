package com.rookies6.myspringboot4project.sec.analysis.entity;

public enum AnalysisStatus { 	// 분석 요청 → 폴링 PENDING → SCANNING → COMPLETED
    PENDING,
    SCANNING,
    EXPLAINING,
    COMPLETED,
    FAILED
}