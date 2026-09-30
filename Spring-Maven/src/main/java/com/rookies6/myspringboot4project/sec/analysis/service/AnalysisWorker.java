package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisWorker {

    private final AnalysisRequestRepository analysisRequestRepository;

    @Async("analysisTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runAnalysisPipeline(Long analysisId) {

        log.info("[AnalysisWorker] 비동기 분석 시작 - Analysis ID: {}", analysisId);

        AnalysisRequest request = analysisRequestRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석 요청입니다. ID: " + analysisId));

        try {
            // 1단계. 코드 탐지
            request.updateStatus(AnalysisStatus.SCANNING);
            analysisRequestRepository.saveAndFlush(request);

            log.info("[AnalysisWorker] 코드 탐지 시작 - Analysis ID: {}", analysisId);
            Thread.sleep(2000);

            // 2단계. AI 설명
            request.updateStatus(AnalysisStatus.EXPLAINING);
            analysisRequestRepository.saveAndFlush(request);

            log.info("[AnalysisWorker] AI 설명 생성 시작 - Analysis ID: {}", analysisId);
            Thread.sleep(3000);

            // 3단계. 분석 결과 생성 및 파일에 매핑 (Enum 사용)
            FindingVulnerability finding = FindingVulnerability.builder()
                    .ruleId("SQLI-001")
                    .vulnerabilityType(VulnerabilityType.SQL_INJECTION) // Enum 직접 사용
                    .severity(Severity.HIGH)                            // Enum 직접 사용
                    .cweId("CWE-89")
                    .startLine(5)
                    .endLine(5)
                    .codeSnippet("String query = \"SELECT * FROM users WHERE id = \" + userInput;")
                    .build();

            if (request.getAnalysisFiles() != null && !request.getAnalysisFiles().isEmpty()) {
                AnalysisFile targetFile = request.getAnalysisFiles().get(0);
                targetFile.addFinding(finding);
            } else {
                log.warn("[AnalysisWorker] 분석 대상 파일이 존재하지 않아 취약점을 바인딩하지 못했습니다. ID: {}", analysisId);
            }

            // 4단계. 분석 완료
            request.markAsCompleted();
            analysisRequestRepository.save(request);

            log.info("[AnalysisWorker] 비동기 분석 완료 - Analysis ID: {}", analysisId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[AnalysisWorker] 분석 스레드가 중단되었습니다. ID: {}", analysisId, e);
            request.markAsFailed("분석 작업이 중단되었습니다.");
            analysisRequestRepository.save(request);
        } catch (Exception e) {
            log.error("[AnalysisWorker] 분석 중 에러 발생 - ID: {}", analysisId, e);
            request.markAsFailed(e.getMessage());
            analysisRequestRepository.save(request);
        }
    }
}