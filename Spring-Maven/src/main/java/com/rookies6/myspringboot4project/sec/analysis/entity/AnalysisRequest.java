package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.project.entity.Project;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisRequest extends BaseEntity { // BaseEntity 상속을 통한 created_at, updated_at 자동 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 유저ID 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") // 명세에 NOT NULL이 없으므로 제거 (필요시 nullable = false 추가 가능)
    private Project project;

    @Column(nullable = false, length = 20) // 언어 길이 20으로 수정
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT") // MEDIUMTEXT 반영
    private String sourceCode;

    @Column(name = "estimated_duration_seconds")
    private Integer estimatedDurationSeconds; // 예상 소요 시간 추가

    @Column(nullable = false, length = 20)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED 등

    @Column(length = 500)
    private String errorMessage; // 에러 메시지 추가

    @Column(name = "started_at")
    private LocalDateTime startedAt; // 분석 시작시간 추가

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 분석 종료시간 추가

    @OneToMany(mappedBy = "analysisRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VulnerabilityFinding> findings = new ArrayList<>();

    @Builder
    public AnalysisRequest(User user, Project project, String language, String sourceCode, Integer estimatedDurationSeconds) {
        this.user = user;
        this.project = project;
        this.language = language;
        this.sourceCode = sourceCode;
        this.estimatedDurationSeconds = estimatedDurationSeconds;
        this.status = "PENDING";
    }

    public void addFinding(VulnerabilityFinding finding) {
        this.findings.add(finding);
        finding.setAnalysisRequest(this);
    }

    // 분석 시작 시 호출
    public void markAsStarted() {
        this.status = "IN_PROGRESS";
        this.startedAt = LocalDateTime.now();
    }

    // 분석 성공 시 호출
    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    // 분석 실패 시 에러 메시지와 함께 호출
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}