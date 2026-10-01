package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    /*
     * 분석 진행 상태
     */
    @Column(length = 30)
    private String stage;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "current_file", length = 500)
    private String currentFile;

    @Column(name = "processed_files")
    @Builder.Default
    private Integer processedFiles = 0;

    @Column(name = "total_files")
    @Builder.Default
    private Integer totalFiles = 0;

    @Column(name = "findings_so_far")
    @Builder.Default
    private Integer findingsSoFar = 0;

    @Column(name = "total_findings")
    @Builder.Default
    private Integer totalFindings = 0;

    @Column(name = "explained_findings")
    @Builder.Default
    private Integer explainedFindings = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /*
     * 분석 파일
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "analysisRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<AnalysisFile> analysisFiles = new ArrayList<>();


    /**
     * 분석 요청 생성
     */
    public static AnalysisRequest create(
            User user,
            String title,
            String language,
            Integer estimatedDurationSeconds
    ) {

        return AnalysisRequest.builder()
                .user(user)
                .title(title)
                .language(language)
                .status(AnalysisStatus.PENDING)
                .stage("PENDING")
                .progress(0)
                .processedFiles(0)
                .totalFiles(0)
                .findingsSoFar(0)
                .totalFindings(0)
                .explainedFindings(0)
                .estimatedDurationSeconds(
                        estimatedDurationSeconds
                )
                .build();
    }


    /**
     * 파일 추가
     */
    public void addFile(AnalysisFile analysisFile) {

        this.analysisFiles.add(analysisFile);

        analysisFile.assignTo(this);

        this.totalFiles = this.analysisFiles.size();
    }


    /**
     * 상태 변경
     */
    public void updateStatus(AnalysisStatus status) {

        this.status = status;

        if (status != null) {
            this.stage = status.name();
        }
    }


    /**
     * String 상태 변경
     */
    public void updateStatus(String statusStr) {

        updateStatus(
                AnalysisStatus.valueOf(statusStr)
        );
    }


    /**
     * 분석 시작
     */
    public void startScanning() {

        this.status = AnalysisStatus.SCANNING;
        this.stage = "SCANNING";

        this.progress = 0;

        this.startedAt = LocalDateTime.now();

        this.processedFiles = 0;

        this.findingsSoFar = 0;
    }


    /**
     * 현재 분석 파일 변경
     */
    public void updateCurrentFile(String currentFile) {

        this.currentFile = currentFile;
    }


    /**
     * 파일 처리 진행
     */
    public void updateFileProgress(
            int processedFiles
    ) {

        this.processedFiles = processedFiles;

        if (this.totalFiles != null
                && this.totalFiles > 0) {

            this.progress =
                    (processedFiles * 100)
                            / this.totalFiles;
        }
    }


    /**
     * Finding 개수 업데이트
     */
    public void updateFindingsCount(
            int findings
    ) {

        this.findingsSoFar = findings;
        this.totalFindings = findings;
    }


    /**
     * 설명 생성 시작
     */
    public void startExplaining() {

        this.status = AnalysisStatus.EXPLAINING;
        this.stage = "EXPLAINING";
    }


    /**
     * 설명 완료 개수 업데이트
     */
    public void updateExplainedFindings(
            int count
    ) {

        this.explainedFindings = count;
    }


    /**
     * 분석 완료
     */
    public void markAsCompleted() {

        this.status = AnalysisStatus.COMPLETED;
        this.stage = "COMPLETED";

        this.progress = 100;

        if (this.totalFiles != null) {
            this.processedFiles = this.totalFiles;
        }

        if (this.totalFindings != null) {
            this.findingsSoFar = this.totalFindings;
        }

        this.completedAt = LocalDateTime.now();
    }


    /**
     * 분석 실패
     */
    public void markAsFailed(
            String errorMessage
    ) {

        this.status = AnalysisStatus.FAILED;
        this.stage = "FAILED";

        this.errorMessage = errorMessage;

        this.completedAt = LocalDateTime.now();
    }


    /**
     * 진행 중인지 확인
     */
    public boolean isInProgress() {

        return this.status == AnalysisStatus.SCANNING
                || this.status == AnalysisStatus.EXPLAINING;
    }


    /**
     * 특정 파일 조회
     */
    public AnalysisFile findFileByPath(
            String relativePath
    ) {

        return this.analysisFiles.stream()
                .filter(file ->
                        file.getRelativePath()
                                .equals(relativePath)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 경로의 파일을 찾을 수 없습니다: "
                                        + relativePath
                        )
                );
    }
}
