package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_requests")
@Builder
@Getter
@AllArgsConstructor
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

    // 📌 1. @Builder.Default 추가: Builder를 통해 객체 생성 시 new ArrayList<>() 기본값을 유지하게 함
    @Builder.Default
    @OneToMany(
            mappedBy = "analysisRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<AnalysisFile> analysisFiles = new ArrayList<>();

    // 📌 2. 기존에 있던 @Builder가 붙은 생성자(public AnalysisRequest(...))는 삭제했습니다.
    // 클래스 상단의 @Builder와 충돌하여 analysisFiles를 null로 만듭니다.

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
        return this.status == AnalysisStatus.SCANNING || this.status == AnalysisStatus.EXPLAINING;
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
}