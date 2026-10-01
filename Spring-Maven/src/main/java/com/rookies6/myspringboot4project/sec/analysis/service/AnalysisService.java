package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisRequestDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisStatusResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final UserRepository userRepository;
    private final AnalysisWorker analysisWorker;
    private final AnalysisProgressStore progressStore;


    /**
     * 전체 분석 목록 조회
     */
    public List<AnalysisResponseDto> getAllAnalyses() {

        return analysisRequestRepository.findAll()
                .stream()
                .map(AnalysisResponseDto::fromEntity)
                .toList();
    }


    /**
     * 분석 상세 조회
     */
    public AnalysisResponseDto getAnalysisById(Long id) {

        AnalysisRequest request = findAnalysisRequest(id);

        return AnalysisResponseDto.fromEntity(request);
    }


    /**
     * 분석 진행 상태 조회
     */
    public AnalysisStatusResponseDto getAnalysisStatus(Long id) {

        AnalysisRequest request = findAnalysisRequest(id);

        return AnalysisStatusResponseDto.fromEntity(
                request,
                progressStore.getLogs(id)
        );
    }


    /**
     * 새 분석 생성
     *
     * userId를 프론트에서 받지 않고
     * JWT 로그인 사용자 정보에서 가져온다.
     */
    @Transactional
    public AnalysisResponseDto createAnalysis(
            AnalysisRequestDto requestDto
    ) {

        /*
         * 1. 현재 로그인 사용자 확인
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "로그인한 사용자 정보를 확인할 수 없습니다."
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase();


        /*
         * 2. DB에서 User 조회
         */
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "User",
                                "email",
                                email
                        )
                );


        /*
         * 3. 예상 분석 시간 계산
         */
        int estimatedDurationSeconds =
                calculateEstimatedDuration(
                        requestDto.getFiles()
                );


        /*
         * 4. AnalysisRequest 생성
         */
        AnalysisRequest request =
                AnalysisRequest.create(
                        user,
                        requestDto.getTitle().trim(),
                        requestDto.getLanguage().trim().toUpperCase(),
                        estimatedDurationSeconds
                );


        /*
         * 5. 분석 파일 추가
         */
        for (AnalysisRequestDto.FileRequestDto fileDto
                : requestDto.getFiles()) {

            String relativePath =
                    fileDto.getFilePath();

            String fileName =
                    extractFileName(relativePath);

            int lineCount =
                    calculateLineCount(
                            fileDto.getContent()
                    );

            AnalysisFile analysisFile =
                    AnalysisFile.builder()
                            .relativePath(relativePath)
                            .fileName(fileName)
                            .language(
                                    requestDto.getLanguage()
                                            .trim()
                                            .toUpperCase()
                            )
                            .content(fileDto.getContent())
                            .lineCount(lineCount)
                            .build();

            request.addFile(analysisFile);
        }


        /*
         * 6. DB 저장
         */
        AnalysisRequest savedRequest =
                analysisRequestRepository.save(request);

        Long analysisId =
                savedRequest.getId();


        /*
         * 7. 트랜잭션 COMMIT 이후 분석 Worker 실행
         */
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                analysisWorker
                                        .runScannerAndLlmProcess(
                                                analysisId
                                        );
                            }
                        }
                );


        /*
         * 8. 응답
         */
        return AnalysisResponseDto
                .fromEntity(savedRequest);
    }


    /**
     * 분석 삭제
     */
    @Transactional
    public void deleteAnalysis(Long id) {

        AnalysisRequest request =
                findAnalysisRequest(id);

        if (request.isInProgress()) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "진행 중인 분석은 삭제할 수 없습니다."
            );
        }

        progressStore.remove(id);

        analysisRequestRepository.delete(request);
    }


    /**
     * 분석 조회
     */
    private AnalysisRequest findAnalysisRequest(Long id) {

        return analysisRequestRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "AnalysisRequest",
                                "id",
                                id
                        )
                );
    }


    /**
     * 파일명 추출
     */
    private String extractFileName(String filePath) {

        if (filePath == null
                || filePath.isBlank()) {

            return "unknown";
        }

        int lastSlash =
                filePath.lastIndexOf('/');

        int lastBackslash =
                filePath.lastIndexOf('\\');

        int lastSeparator =
                Math.max(
                        lastSlash,
                        lastBackslash
                );

        return lastSeparator >= 0
                ? filePath.substring(
                        lastSeparator + 1
                )
                : filePath;
    }


    /**
     * 코드 라인 수 계산
     */
    private int calculateLineCount(String content) {

        if (content == null
                || content.isEmpty()) {

            return 0;
        }

        return content.split(
                "\\R",
                -1
        ).length;
    }


    /**
     * 예상 분석 시간 계산
     *
     * 파일 개수 + 전체 코드 라인 수를 기준으로
     * 간단하게 예상 시간을 계산한다.
     */
    private int calculateEstimatedDuration(
            List<AnalysisRequestDto.FileRequestDto> files
    ) {

        int totalLines = files.stream()
                .mapToInt(file ->
                        calculateLineCount(
                                file.getContent()
                        )
                )
                .sum();

        int fileCount = files.size();

        int estimated =
                5
                + (fileCount * 2)
                + (totalLines / 100);

        return Math.max(estimated, 5);
    }
}
