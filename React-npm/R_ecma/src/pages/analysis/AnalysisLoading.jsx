import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisLoading.module.css';

const AnalysisLoading = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [estimatedSeconds, setEstimatedSeconds] = useState(location.state?.estimatedDurationSeconds || 10);
    const [remainingSeconds, setRemainingSeconds] = useState(estimatedSeconds);

    const [statusInfo, setStatusInfo] = useState({
        status: 'PENDING',
        stageLabel: '스캔 대기 중...',
        progress: 15,
        currentFile: null,
        processedFiles: 0,
        totalFiles: 0,
        findingsSoFar: 0,
        errorMessage: null,
        recentLogs: []
    });

    // 1초 단위 카운트다운 타이머 (예상 시간용)
    useEffect(() => {
        if (remainingSeconds <= 0 || statusInfo.status === 'COMPLETED') return;

        const timer = setInterval(() => {
            setRemainingSeconds((prev) => (prev > 1 ? prev - 1 : 1));
        }, 1000);

        return () => clearInterval(timer);
    }, [remainingSeconds, statusInfo.status]);

    // Fallback UI 데이터 (백엔드에서 stageLabel 등이 없을 경우 대비)
    const getFallbackDetails = (status) => {
        switch (status) {
            case 'PENDING': return { title: '스캔 대기 중...', percent: 25 };
            case 'SCANNING': return { title: '취약점 정적 스캔 진행 중...', percent: 60 };
            case 'EXPLAINING': return { title: 'AI 보안 가이드 생성 중...', percent: 85 };
            case 'COMPLETED': return { title: '분석 완료!', percent: 100 };
            case 'FAILED': return { title: '분석 중 오류 발생', percent: 100 };
            default: return { title: '처리 중...', percent: 50 };
        }
    };

    useEffect(() => {
        let isMounted = true;

        const checkStatus = async () => {
            try {
                // GET /api/v1/analyses/{id}/status (AnalysisStatusResponseDto 구조 반환)
                const response = await api.get(`/analyses/${id}/status`);
                const data = response.data;

                if (!isMounted) return;

                setStatusInfo(prev => ({
                    ...prev,
                    ...data
                }));
                
                // 백엔드 예상 시간 동기화
                if (data.estimatedDurationSeconds) {
                    setEstimatedSeconds(data.estimatedDurationSeconds);
                }

                if (data.status === 'COMPLETED') {
                    setTimeout(() => {
                        navigate(`/analysis/${id}`);
                    }, 800);
                }
            } catch (error) {
                console.error('상태 조회 실패:', error);
            }
        };

        checkStatus();
        const intervalId = setInterval(checkStatus, 1500);

        return () => {
            isMounted = false;
            clearInterval(intervalId);
        };
    }, [id, navigate]);

    // 화면에 보여줄 값 (백엔드 응답 최우선, 없으면 Fallback 적용)
    const displayTitle = statusInfo.stageLabel || getFallbackDetails(statusInfo.status).title;
    const displayProgress = statusInfo.progress > 0 ? statusInfo.progress : getFallbackDetails(statusInfo.status).percent;

    return (
        <div className={styles.card}>
            {statusInfo.status !== 'FAILED' && statusInfo.status !== 'COMPLETED' ? (
                <div className={styles.spinner} />
            ) : statusInfo.status === 'COMPLETED' ? (
                <div style={{ fontSize: '48px', marginBottom: '20px' }}>✅</div>
            ) : (
                <div style={{ fontSize: '48px', marginBottom: '20px' }}>⚠️</div>
            )}

            <h2 className={styles.statusTitle}>{displayTitle}</h2>

            {/* 백엔드 진행 상태 상세 정보 렌더링 */}
            {statusInfo.status !== 'COMPLETED' && statusInfo.status !== 'FAILED' && (
                <div style={{ margin: '16px 0', backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', textAlign: 'left' }}>
                    
                    {statusInfo.currentFile && (
                        <p style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#475569' }}>
                            <strong>현재 분석 중:</strong> {statusInfo.currentFile}
                        </p>
                    )}
                    
                    {statusInfo.totalFiles > 0 && (
                        <p style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#475569' }}>
                            <strong>처리 파일:</strong> {statusInfo.processedFiles} / {statusInfo.totalFiles} 개
                        </p>
                    )}
                    
                    {statusInfo.findingsSoFar > 0 && (
                        <p style={{ margin: '0', fontSize: '14px', color: '#b91c1c', fontWeight: '600' }}>
                            🚨 탐지된 취약점: {statusInfo.findingsSoFar} 건
                        </p>
                    )}
                </div>
            )}

            {/* 예상 시간 카운트다운 */}
            {statusInfo.status !== 'COMPLETED' && statusInfo.status !== 'FAILED' && (
                <div style={{ fontSize: '15px', fontWeight: 'bold', color: '#2563eb', marginBottom: '16px' }}>
                    ⏱️️ 예상 완료 시간: 약 {remainingSeconds}초 (전체 예상 {estimatedSeconds}초)
                </div>
            )}

            <div className={styles.progressTrack}>
                <div 
                    className={styles.progressBar} 
                    style={{ width: `${displayProgress}%`, transition: 'width 0.5s ease-in-out' }} 
                />
            </div>

            <div className={styles.badgeInfo}>
                요청 ID: #{id} | 상태: {statusInfo.status}
            </div>

            {statusInfo.status === 'FAILED' && (
                <div className={styles.errorBox}>
                    {statusInfo.errorMessage && <p>사유: {statusInfo.errorMessage}</p>}
                    <button onClick={() => navigate('/analysis/list')} style={{ marginTop: '10px', padding: '6px 12px', cursor: 'pointer' }}>
                        분석 목록으로 돌아가기
                    </button>
                </div>
            )}
        </div>
    );
};

export default AnalysisLoading;