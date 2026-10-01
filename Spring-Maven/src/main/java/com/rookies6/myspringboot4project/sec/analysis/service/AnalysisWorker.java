package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus; // 📌 추가: 취약점 상태 Enum
import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
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

    // 📌 메서드명을 AnalysisService에서 호출하는 이름으로 일치시킴
    @Async("analysisTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runScannerAndLlmProcess(Long analysisId) {

        log.info("[AnalysisWorker] 비동기 분석 시작 - Analysis ID: {}", analysisId);

        AnalysisRequest request = analysisRequestRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석 요청입니다. ID: " + analysisId));

        try {
            request.updateStatus(AnalysisStatus.SCANNING);
            analysisRequestRepository.saveAndFlush(request);

            log.info("[AnalysisWorker] 코드 탐지 시작 - Analysis ID: {}", analysisId);
            Thread.sleep(2000); // 가상 딜레이

            request.updateStatus(AnalysisStatus.EXPLAINING);
            analysisRequestRepository.saveAndFlush(request);

            log.info("[AnalysisWorker] AI 설명 생성 시작 - Analysis ID: {}", analysisId);
            Thread.sleep(3000); // 가상 딜레이

            if (request.getAnalysisFiles() != null && !request.getAnalysisFiles().isEmpty()) {
                AnalysisFile targetFile = request.getAnalysisFiles().get(0);
                
                // 📌 연관관계(analysisFile) 및 초기 상태(OPEN) 추가 세팅
                FindingVulnerability finding = FindingVulnerability.builder()
                        .analysisFile(targetFile) // 양방향 연관관계 필수 세팅
                        .ruleId("SQLI-001")
                        .vulnerabilityType(VulnerabilityType.SQL_INJECTION)
                        .severity(Severity.HIGH)
                        .status(FindingStatus.OPEN) // 취약점 초기 상태
                        .startLine(5)
                        .endLine(5)
                        .codeSnippet("String query = \"SELECT * FROM users WHERE id = \" + userInput;")
                        .build();

                // 📌 addFinding 대신 컬렉션에 직접 추가 (Cascade.ALL 옵션으로 자동 저장됨)
                targetFile.getFindings().add(finding);
            } else {
                log.warn("[AnalysisWorker] 분석 대상 파일이 존재하지 않아 취약점을 바인딩하지 못했습니다. ID: {}", analysisId);
            }

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