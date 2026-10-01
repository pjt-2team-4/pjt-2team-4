package com.rookies6.myspringboot4project.sec.analysisfile.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_files")
@Getter
@Builder
@AllArgsConstructor
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

    @Builder.Default
    @OneToMany(
            mappedBy = "analysisFile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<FindingVulnerability> findings = new ArrayList<>();

    public void assignTo(AnalysisRequest analysisRequest) {
        this.analysisRequest = analysisRequest;
    }

    public void addFinding(FindingVulnerability finding) {
        if (this.findings == null) {
            this.findings = new ArrayList<>();
        }

        this.findings.add(finding);
        finding.assignTo(this);
    }
}
