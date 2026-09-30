package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnalysisStatus status;

    @Column(name = "estimated_duration_seconds")
    private Integer estimatedDurationSeconds;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // issue6 탐지 진행 컬럼 추가 (startedAt, completedAt, totalFindings)
    @Column(name = "total_findings", nullable = false)
    private Integer totalFindings = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // AnalysisFile과의 연관관계 (CascadeType.ALL로 파일도 함께 관리)
    @OneToMany(
            mappedBy = "analysisRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<AnalysisFile> analysisFiles = new ArrayList<>();

    @Builder
    public AnalysisRequest(User user, String title, String language, AnalysisStatus status, Integer estimatedDurationSeconds) {
        this.user = user;
        this.title = title;
        this.language = language;
        this.status = status;
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }

    /**
     * 분석 요청 생성 팩토리 메서드
     */
    public static AnalysisRequest create(User user, String title, String language, Integer estimatedDurationSeconds) {
        return AnalysisRequest.builder()
                .user(user)
                .title(title)
                .language(language)
                .status(AnalysisStatus.PENDING) // 초기 상태는 PENDING
                .estimatedDurationSeconds(estimatedDurationSeconds)
                .build();
    }

    /**
     * 연관관계 편의 메서드: 파일 추가
     */
    public void addFile(AnalysisFile analysisFile) {
        this.analysisFiles.add(analysisFile);
        analysisFile.assignTo(this);
    }

    // =========================================================================
    // [핵심] AnalysisWorker 및 서비스에서 호출하는 상태 변경 / 비즈니스 메서드들
    // =========================================================================

    public void updateStatus(AnalysisStatus status) {
        this.status = status;
    }

    public void updateStatus(String statusStr) {
        this.status = AnalysisStatus.valueOf(statusStr);
    }

    public boolean isInProgress() {
        return this.status == AnalysisStatus.PENDING
                || this.status == AnalysisStatus.SCANNING
                || this.status == AnalysisStatus.EXPLAINING;
    }

    public void markAsCompleted() {
        this.status = AnalysisStatus.COMPLETED;
    }

    public void markAsFailed(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 특정 상대 경로(relativePath)를 가진 분석 파일을 찾아 반환합니다.
     * 분석 엔진(Worker)에서 취약점을 올바른 파일에 매핑할 때 사용됩니다.
     */
    public AnalysisFile findFileByPath(String relativePath) {
        return this.analysisFiles.stream()
                .filter(file -> file.getRelativePath().equals(relativePath))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 경로의 파일을 찾을 수 없습니다: " + relativePath));
    }

    // Issue6 상태전이 메서드들 추가
    public void startScanning() {
        if (this.status != AnalysisStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 분석을 시작할 수 있습니다. 현재: " + this.status);
        }
        this.status = AnalysisStatus.SCANNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(int totalFindings) {
        if (this.status != AnalysisStatus.SCANNING && this.status != AnalysisStatus.EXPLAINING) {
            throw new IllegalStateException("진행 중인 분석만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.totalFindings = totalFindings;
        this.status = AnalysisStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String message) {
        if (!isInProgress()) {
            throw new IllegalStateException("진행 중인 분석만 실패 처리할 수 있습니다. 현재: " + this.status);
        }
        this.status = AnalysisStatus.FAILED;
        this.errorMessage = message;
        this.completedAt = LocalDateTime.now();
    }
}