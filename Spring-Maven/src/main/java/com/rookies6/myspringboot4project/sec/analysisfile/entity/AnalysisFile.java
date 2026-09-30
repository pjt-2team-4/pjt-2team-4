package com.rookies6.myspringboot4project.sec.analysisfile.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false)
    private AnalysisRequest analysisRequest;

    @Column(name = "relative_path", nullable = false, length = 500)
    private String relativePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 20)
    private String language;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "line_count", nullable = false)
    private Integer lineCount;

    @OneToMany(
            mappedBy = "analysisFile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<FindingVulnerability> findings = new ArrayList<>();

    @Builder
    public AnalysisFile(String relativePath, String fileName, String language, String content, Integer lineCount) {
        this.relativePath = relativePath;
        this.fileName = fileName;
        this.language = language;
        this.content = content;
        this.lineCount = lineCount != null ? lineCount : 0;
    }

    public void assignTo(AnalysisRequest analysisRequest) {
        this.analysisRequest = analysisRequest;
    }

    public void addFinding(FindingVulnerability finding) {
        this.findings.add(finding);
        finding.assignTo(this);
    }
}