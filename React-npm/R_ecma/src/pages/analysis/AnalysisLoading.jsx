import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisLoading.module.css';

const AnalysisLoading = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    // 초기값: 전달받은 state 값 또는 기본 10초
    const [estimatedSeconds, setEstimatedSeconds] = useState(location.state?.estimatedDurationSeconds || 10);
    const [remainingSeconds, setRemainingSeconds] = useState(estimatedSeconds);

    const [statusInfo, setStatusInfo] = useState({
        status: 'PENDING',
        errorMessage: null
    });
    const [progressPercent, setProgressPercent] = useState(15);

    // 1초 단위 카운트다운 타이머
    useEffect(() => {
        if (remainingSeconds <= 0 || statusInfo.status === 'COMPLETED') return;

        const timer = setInterval(() => {
            setRemainingSeconds((prev) => (prev > 1 ? prev - 1 : 1));
        }, 1000);

        return () => clearInterval(timer);
    }, [remainingSeconds, statusInfo.status]);

    const getStatusDetails = (status) => {
        switch (status) {
            case 'PENDING':
                return { title: '스캔 대기 중...', desc: '분석 요청을 작업 큐에 대기 중입니다.', percent: 25 };
            case 'SCANNING':
                return { title: '취약점 정적 스캔 진행 중...', desc: '룰 기반 패턴 매칭 분석을 진행하고 있습니다.', percent: 60 };
            case 'EXPLAINING':
                return { title: 'AI 보안 가이드 생성 중...', desc: '탐지 항목에 대한 해결 가이드 리포트를 작성 중입니다.', percent: 85 };
            case 'COMPLETED':
                return { title: '분석 완료!', desc: '결과 리포트로 이동합니다...', percent: 100 };
            case 'FAILED':
                return { title: '분석 중 오류 발생', desc: '스캔 처리 실패', percent: 100 };
            default:
                return { title: '처리 중...', desc: '잠시만 기다려 주세요.', percent: 50 };
        }
    };

    useEffect(() => {
        let isMounted = true;

        const checkStatus = async () => {
            try {
                // GET /api/v1/analyses/{id}/status
                const response = await api.get(`/analyses/${id}/status`);
                const data = response.data;

                if (!isMounted) return;

                setStatusInfo(data);
                
                // 백엔드 DB에서 읽어온 estimatedDurationSeconds가 응답에 포함되어 있다면 동기화
                if (data.estimatedDurationSeconds) {
                    setEstimatedSeconds(data.estimatedDurationSeconds);
                }

                const details = getStatusDetails(data.status);
                setProgressPercent(details.percent);

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

    const currentDetails = getStatusDetails(statusInfo.status);

    return (
        <div className={styles.card}>
            {statusInfo.status !== 'FAILED' && statusInfo.status !== 'COMPLETED' ? (
                <div className={styles.spinner} />
            ) : statusInfo.status === 'COMPLETED' ? (
                <div style={{ fontSize: '48px', marginBottom: '20px' }}>✅</div>
            ) : (
                <div style={{ fontSize: '48px', marginBottom: '20px' }}>⚠️</div>
            )}

            <h2 className={styles.statusTitle}>{currentDetails.title}</h2>
            <p className={styles.statusDesc}>{currentDetails.desc}</p>

            {/* ⏱️ DB 상의 estimated_duration_seconds 기반 남은 시간 표시 */}
            {statusInfo.status !== 'COMPLETED' && statusInfo.status !== 'FAILED' && (
                <div style={{ fontSize: '15px', fontWeight: 'bold', color: '#2563eb', marginBottom: '16px' }}>
                    ⏱️ 예상 완료 시간: 약 {remainingSeconds}초 (전체 예상 {estimatedSeconds}초)
                </div>
            )}

            <div className={styles.progressTrack}>
                <div 
                    className={styles.progressBar} 
                    style={{ width: `${progressPercent}%` }} 
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