package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.sec.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnalysisRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String language;

    @Lob
    @Column(nullable = false)
    private String sourceCode;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, FAILED

    @OneToMany(mappedBy = "analysisRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VulnerabilityFinding> findings = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AnalysisRequest(Project project, String language, String sourceCode) {
        this.project = project;
        this.language = language;
        this.sourceCode = sourceCode;
        this.status = "PENDING";
    }

    public void addFinding(VulnerabilityFinding finding) {
        this.findings.add(finding);
        finding.setAnalysisRequest(this);
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }
}