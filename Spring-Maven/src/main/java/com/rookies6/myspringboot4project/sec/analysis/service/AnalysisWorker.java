package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
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
    public void runScannerAndLlmProcess(
            Long analysisId
    ) {

        log.info(
                "[AnalysisWorker] 분석 시작 - analysisId={}",
                analysisId
        );

        AnalysisRequest request =
                analysisRequestRepository.findById(analysisId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 분석 요청입니다. id="
                                                + analysisId
                                )
                        );

        try {

            /*
             * 전체 파일 수 설정
             */
            int totalFiles =
                    request.getAnalysisFiles() != null
                            ? request.getAnalysisFiles().size()
                            : 0;

            /*
             * SCANNING 시작
             */
            request.startScanning();

            /*
             * 전체 파일 수 설정
             *
             * addFile()에서도 설정하지만
             * Worker 시작 시 다시 한 번 확정.
             */
            request.updateFileProgress(0);

            analysisRequestRepository.saveAndFlush(request);


            /*
             * Scanner
             */
            runScanner(request);


            /*
             * EXPLAINING 시작
             */
            request.startExplaining();

            analysisRequestRepository.saveAndFlush(request);

            log.info(
                    "[AnalysisWorker] EXPLAINING 시작 - analysisId={}",
                    analysisId
            );


            /*
             * LLM 설명
             */
            runLlmExplanation(request);


            /*
             * 완료
             */
            request.markAsCompleted();

            analysisRequestRepository.saveAndFlush(request);

            log.info(
                    "[AnalysisWorker] 분석 완료 - analysisId={}",
                    analysisId
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.error(
                    "[AnalysisWorker] 분석 작업 중단 - analysisId={}",
                    analysisId,
                    e
            );

            request.markAsFailed(
                    "분석 작업이 중단되었습니다."
            );

            analysisRequestRepository.save(request);

        } catch (Exception e) {

            log.error(
                    "[AnalysisWorker] 분석 실패 - analysisId={}",
                    analysisId,
                    e
            );

            String errorMessage =
                    e.getMessage();

            if (errorMessage == null
                    || errorMessage.isBlank()) {

                errorMessage =
                        "코드를 분석하는 중 오류가 발생했습니다.";
            }

            request.markAsFailed(
                    errorMessage
            );

            analysisRequestRepository.save(request);
        }
    }


    /**
     * 규칙 기반 탐지
     */
    private void runScanner(
            AnalysisRequest request
    ) throws InterruptedException {

        log.info(
                "[AnalysisWorker] 규칙 기반 탐지 실행"
        );

        if (request.getAnalysisFiles() == null
                || request.getAnalysisFiles().isEmpty()) {

            log.warn(
                    "[AnalysisWorker] 분석 대상 파일이 없습니다."
            );

            return;
        }


        int totalFiles =
                request.getAnalysisFiles().size();

        int processedFiles = 0;

        int findings = 0;


        /*
         * 모든 파일을 순회
         */
        for (AnalysisFile targetFile :
                request.getAnalysisFiles()) {

            /*
             * 현재 파일
             */
            request.updateCurrentFile(
                    targetFile.getRelativePath()
            );

            analysisRequestRepository.saveAndFlush(
                    request
            );


            log.info(
                    "[AnalysisWorker] 파일 분석 - {}",
                    targetFile.getRelativePath()
            );


            /*
             * 실제 Scanner 위치
             */
            Thread.sleep(2000);


            /*
             * 테스트용 Finding
             *
             * 실제 Scanner 결과로 교체 예정
             */
            FindingVulnerability finding =
                    FindingVulnerability.builder()
                            .analysisFile(targetFile)
                            .ruleId("SQLI-001")
                            .vulnerabilityType(
                                    VulnerabilityType.SQL_INJECTION
                            )
                            .severity(Severity.HIGH)
                            .status(FindingStatus.OPEN)
                            .startLine(5)
                            .endLine(5)
                            .codeSnippet(
                                    "String query = \"SELECT * FROM users WHERE id = \" + userInput;"
                            )
                            .build();


            targetFile.addFinding(finding);

            findings++;


            /*
             * 파일 처리 완료
             */
            processedFiles++;

            request.updateFindingsCount(
                    findings
            );

            request.updateFileProgress(
                    processedFiles
            );

            analysisRequestRepository.saveAndFlush(
                    request
            );
        }
    }


    /**
     * LLM 설명 생성
     */
    private void runLlmExplanation(
            AnalysisRequest request
    ) throws InterruptedException {

        log.info(
                "[AnalysisWorker] LLM 설명 생성 실행"
        );


        int totalFindings =
                request.getTotalFindings() != null
                        ? request.getTotalFindings()
                        : 0;


        /*
         * 현재는 테스트용
         */
        Thread.sleep(3000);


        /*
         * 현재 구현에서는
         * 모든 Finding 설명이 완료되었다고 처리
         */
        request.updateExplainedFindings(
                totalFindings
        );

        analysisRequestRepository.saveAndFlush(
                request
        );
    }
}
