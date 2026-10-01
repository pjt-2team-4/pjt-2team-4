package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisRequestDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisStatusResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
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

        AnalysisRequest request =
                findAnalysisRequest(id);

        return AnalysisResponseDto.fromEntity(request);
    }


    /**
     * 분석 진행 상태 조회
     */
    public AnalysisStatusResponseDto getAnalysisStatus(
            Long id
    ) {

        AnalysisRequest request =
                findAnalysisRequest(id);

        return AnalysisStatusResponseDto.fromEntity(
                request,
                progressStore.getLogs(id)
        );
    }


    /**
     * 분석 생성
     */
    @Transactional
    public AnalysisResponseDto createAnalysis(
            AnalysisRequestDto requestDto
    ) {

        // 1. 사용자 확인
        User user =
                userRepository.findById(
                                requestDto.getUserId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "User",
                                        "id",
                                        requestDto.getUserId()
                                )
                        );


        // 2. 분석 요청 생성
        AnalysisRequest request =
                AnalysisRequest.create(
                        user,
                        requestDto.getTitle(),
                        requestDto.getLanguage(),
                        null
                );


        // 3. 파일 생성
        if (requestDto.getFiles() != null) {

            requestDto.getFiles()
                    .forEach(fileDto -> {

                        String relativePath =
                                fileDto.getFilePath();

                        String fileName =
                                extractFileName(
                                        relativePath
                                );

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
                                        )
                                        .content(fileDto.getContent())
                                        .lineCount(lineCount)
                                        .build();

                        request.addFile(
                                analysisFile
                        );
                    });
        }


        // 4. 저장
        AnalysisRequest savedRequest =
                analysisRequestRepository.save(request);

        Long analysisId =
                savedRequest.getId();


        /*
         * 5. COMMIT 이후 Worker 실행
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


    private AnalysisRequest findAnalysisRequest(
            Long id
    ) {

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


    private String extractFileName(
            String filePath
    ) {

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


    private int calculateLineCount(
            String content
    ) {

        if (content == null
                || content.isEmpty()) {

            return 0;
        }

        return content.split(
                "\\R",
                -1
        ).length;
    }
}
