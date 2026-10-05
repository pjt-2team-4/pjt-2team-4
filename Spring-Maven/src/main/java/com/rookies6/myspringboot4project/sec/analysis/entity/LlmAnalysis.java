package com.rookies6.myspringboot4project.sec.analysis.entity;

import com.rookies6.myspringboot4project.common.entity.BaseEntity;
import com.rookies6.myspringboot4project.sec.common.enums.LlmAnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.LlmVerdict;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "llm_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finding_id", nullable = false, unique = true)
    private FindingVulnerability finding;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LlmAnalysisStatus status;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "risk_description", columnDefinition = "TEXT")
    private String riskDescription;

    @Column(name = "attack_scenario", columnDefinition = "TEXT")
    private String attackScenario;

    @Column(columnDefinition = "TEXT")
    private String remediation;

    @Column(name = "fixed_code", columnDefinition = "TEXT")
    private String fixedCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LlmVerdict verdict;

    @Min(0)
    @Max(100)
    @Column
    private Integer confidence;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_version", length = 30)
    private String promptVersion;

    @Column(name = "raw_response", columnDefinition = "MEDIUMTEXT")
    private String rawResponse;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** LLM 호출 전 자리만 만들어 둘 때 사용 (후속 LLM 연동 이슈) */
    public static LlmAnalysis pending(FindingVulnerability finding) {
        LlmAnalysis analysis = new LlmAnalysis();
        analysis.status = LlmAnalysisStatus.PENDING;
        finding.attachLlmAnalysis(analysis);
        return analysis;
    }

    void assignTo(FindingVulnerability finding) {
        this.finding = finding;
    }

    public boolean isSuccess() {
        return status == LlmAnalysisStatus.SUCCESS;
    }
}
