// src/utils/analysisStatusUtils.js

/**
 * 분석 상태(Status)에 따른 한글 라벨 및 스타일 정의
 */
export const getAnalysisStatusBadge = (status) => {
    switch (status) {
        case 'COMPLETED':
            return { label: '분석 완료', backgroundColor: '#d4edda', color: '#155724' };
        case 'EXPLAINING':
            return { label: '결과 설명 생성 중', backgroundColor: '#cce5ff', color: '#004085' };
        case 'SCANNING':
            return { label: '코드 스캔 중', backgroundColor: '#fff3cd', color: '#856404' };
        case 'PENDING':
            return { label: '대기 중', backgroundColor: '#e2e3e5', color: '#383d41' };
        case 'FAILED':
            return { label: '분석 실패', backgroundColor: '#f8d7da', color: '#721c24' };
        default:
            return { label: '알 수 없음', backgroundColor: '#f8f9fa', color: '#6c757d' };
    }
};

/**
 * 분석이 아직 완료되지 않았는지 확인 (PENDING, SCANNING, EXPLAINING)
 */
export const isAnalysisInProgress = (status) => {
    return ['PENDING', 'SCANNING', 'EXPLAINING'].includes(status);
};

/**
 * 예상 소요 시간 포맷팅 (초 단위 -> MM:SS 또는 초)
 */
export const formatEstimatedDuration = (seconds) => {
    if (!seconds || seconds <= 0) return '0초';
    if (seconds < 60) return `${seconds}초`;
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}분 ${secs > 0 ? `${secs}초` : ''}`;
};