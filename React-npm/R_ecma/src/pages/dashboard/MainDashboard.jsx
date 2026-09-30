import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import api from '../../api/axios';
import styles from './MainDashboard.module.css';

// Chart.js 필수 모듈 등록
ChartJS.register(ArcElement, Tooltip, Legend);

const MainDashboard = () => {
    const navigate = useNavigate();
    const [analyses, setAnalyses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    // 1. 전체 분석 목록 불러오기
    useEffect(() => {
        const fetchDashboardData = async () => {
            setIsLoading(true);
            try {
                const response = await api.get('/analyses');
                setAnalyses(response.data);
            } catch (error) {
                console.error('대시보드 데이터 로드 실패:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    // 2. 통계 지표 계산
    const totalAnalyses = analyses.length;
    const completedAnalyses = analyses.filter(a => a.status === 'COMPLETED').length;
    
    // 심각도별 총 탐지 건수 합산
    const totalCritical = analyses.reduce((sum, item) => sum + (item.criticalCount || 0), 0);
    const totalHigh = analyses.reduce((sum, item) => sum + (item.highCount || 0), 0);
    const totalMedium = analyses.reduce((sum, item) => sum + (item.mediumCount || 0), 0);
    const totalLow = analyses.reduce((sum, item) => sum + (item.lowCount || 0), 0);
    const totalVulnerabilities = totalCritical + totalHigh + totalMedium + totalLow;

    // 3. Chart.js 도넛 차트 데이터 구성
    const chartData = {
        labels: ['Critical', 'High', 'Medium', 'Low'],
        datasets: [
            {
                data: [totalCritical, totalHigh, totalMedium, totalLow],
                backgroundColor: ['#dc2626', '#ea580c', '#eab308', '#06b6d4'],
                borderColor: ['#ffffff', '#ffffff', '#ffffff', '#ffffff'],
                borderWidth: 2,
            },
        ],
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    boxWidth: 12,
                    padding: 15,
                    font: { size: 12 }
                }
            }
        },
        cutout: '70%',
    };

    // 상태 배지 렌더링
    const renderStatusBadge = (status) => {
        const isCompleted = status === 'COMPLETED';
        const isFailed = status === 'FAILED';
        return (
            <span style={{
                padding: '2px 8px',
                borderRadius: '4px',
                fontSize: '11px',
                fontWeight: 'bold',
                backgroundColor: isCompleted ? '#f0fdf4' : isFailed ? '#fef2f2' : '#fffbeb',
                color: isCompleted ? '#16a34a' : isFailed ? '#dc2626' : '#d97706',
                border: `1px solid ${isCompleted ? '#bbf7d0' : isFailed ? '#fecaca' : '#fde68a'}`
            }}>
                {isCompleted ? '완료' : isFailed ? '실패' : '진행 중'}
            </span>
        );
    };

    if (isLoading) return <div style={{ padding: '40px', textAlign: 'center' }}>대시보드를 로딩하는 중...</div>;

    return (
        <div className={styles.container}>
            {/* 상단 헤더 */}
            <div className={styles.header}>
                <div>
                    <h1 className={styles.title}>🛡️ 보안 분석 종합 대시보드</h1>
                    <p className={styles.subtitle}>프로젝트 정적 코드 보안 스캔 및 취약점 통합 통계입니다.</p>
                </div>
                <Link to="/analysis/new" className={styles.createBtn}>
                    + 새 분석 요청
                </Link>
            </div>

            {/* 1. 요약 KPI 카드 영역 */}
            <div className={styles.kpiGrid}>
                <div className={styles.kpiCard}>
                    <div className={styles.kpiLabel}>총 분석 요청</div>
                    <div className={styles.kpiValue}>{totalAnalyses} <span style={{ fontSize: '14px', color: '#64748b' }}>건</span></div>
                </div>
                <div className={styles.kpiCard}>
                    <div className={styles.kpiLabel}>분석 완료 건수</div>
                    <div className={styles.kpiValue} style={{ color: '#16a34a' }}>{completedAnalyses} <span style={{ fontSize: '14px', color: '#64748b' }}>건</span></div>
                </div>
                <div className={styles.kpiCard}>
                    <div className={styles.kpiLabel}>총 발견 취약점</div>
                    <div className={styles.kpiValue}>{totalVulnerabilities} <span style={{ fontSize: '14px', color: '#64748b' }}>개</span></div>
                </div>
                <div className={styles.kpiCard} style={{ backgroundColor: '#fff5f5', borderColor: '#fecaca' }}>
                    <div className={styles.kpiLabel} style={{ color: '#dc2626' }}>Critical 취약점</div>
                    <div className={styles.kpiValue} style={{ color: '#dc2626' }}>{totalCritical} <span style={{ fontSize: '14px', color: '#dc2626' }}>개</span></div>
                </div>
            </div>

            {/* 2. 대시보드 메인 그리드 (차트 + 최근 분석 히스토리) */}
            <div className={styles.mainGrid}>
                {/* 왼쪽: 심각도 분포 차트 */}
                <div className={styles.card}>
                    <h3 className={styles.cardTitle}>취약점 심각도 분포</h3>
                    <div className={styles.chartContainer}>
                        {totalVulnerabilities > 0 ? (
                            <Doughnut data={chartData} options={chartOptions} />
                        ) : (
                            <div style={{ color: '#94a3b8', fontSize: '14px' }}>발견된 취약점이 없습니다 🎉</div>
                        )}
                    </div>
                </div>

                {/* 오른쪽: 최근 분석 요청 목록 (최대 5건) */}
                <div className={styles.card}>
                    <div className={styles.cardTitle}>
                        <span>최근 분석 요청</span>
                        <Link to="/analysis/list" className={styles.viewMore}>전체보기 →</Link>
                    </div>

                    {analyses.length === 0 ? (
                        <p style={{ color: '#94a3b8', textAlign: 'center', padding: '30px 0' }}>진행된 분석 기록이 없습니다.</p>
                    ) : (
                        <table className={styles.table}>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>제목</th>
                                    <th>언어</th>
                                    <th>상태</th>
                                    <th>생성일</th>
                                </tr>
                            </thead>
                            <tbody>
                                {analyses.slice(0, 5).map((item) => {
                                    const targetUrl = item.status === 'COMPLETED' ? `/analysis/${item.analysisId}` : `/analysis/${item.analysisId}/loading`;
                                    return (
                                        <tr 
                                            key={item.analysisId}
                                            onClick={() => navigate(targetUrl)}
                                            style={{ cursor: 'pointer' }}
                                        >
                                            <td style={{ color: '#94a3b8', fontWeight: 'bold' }}>#{item.analysisId}</td>
                                            <td style={{ fontWeight: '600' }}>{item.title}</td>
                                            <td><span style={{ fontSize: '12px', padding: '2px 6px', backgroundColor: '#e2e8f0', borderRadius: '4px' }}>{item.language}</span></td>
                                            <td>{renderStatusBadge(item.status)}</td>
                                            <td style={{ fontSize: '12px', color: '#64748b' }}>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MainDashboard;