import React, { useEffect, useState } from 'react';
import api from '../../api/axios';
import styles from './Dashboard.module.css';

const Dashboard = () => {

    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchDashboard = async () => {

        try {

            setLoading(true);
            setError(null);

            const response = await api.get(
                '/dashboard/summary'
            );

            console.log(
                '[Dashboard]',
                response.data
            );

            /*
             * ApiResponse<T>
             */
            setDashboard(response.data.data);

        } catch (error) {

            console.error(
                '[Dashboard]',
                error
            );

            setError(
                error.response?.data?.message ||
                error.message ||
                'Dashboard 조회에 실패했습니다.'
            );

        } finally {

            setLoading(false);

        }
    };


    useEffect(() => {
        fetchDashboard();
    }, []);


    if (loading) {
        return (
            <div className={styles.center}>
                Dashboard를 불러오는 중...
            </div>
        );
    }


    if (error) {
        return (
            <div className={styles.error}>
                <h2>Dashboard 조회 실패</h2>

                <p>{error}</p>

                <button onClick={fetchDashboard}>
                    다시 조회
                </button>
            </div>
        );
    }


    if (!dashboard) {
        return null;
    }


    return (
        <div className={styles.container}>

            <div className={styles.header}>

                <div>
                    <h1>보안 분석 Dashboard</h1>

                    <p>
                        전체 보안 분석 현황을 확인하세요.
                    </p>
                </div>

                <button onClick={fetchDashboard}>
                    ↻ 새로고침
                </button>

            </div>


            {/* =========================
                Summary
            ========================= */}

            <div className={styles.summaryGrid}>

                <SummaryCard
                    label="이번 주 분석"
                    value={dashboard.weeklyAnalysisCount}
                    description={
                        `${dashboard.weeklyAnalysisDelta >= 0 ? '+' : ''}` +
                        `${dashboard.weeklyAnalysisDelta}%`
                    }
                />

                <SummaryCard
                    label="전체 취약점"
                    value={dashboard.totalFindings}
                    description="누적 발견 취약점"
                />

                <SummaryCard
                    label="Critical"
                    value={dashboard.criticalCount}
                    description="치명적 취약점"
                />

                <SummaryCard
                    label="해결률"
                    value={`${dashboard.resolutionRate}%`}
                    description={
                        `전 기간 대비 ` +
                        `${dashboard.resolutionRateDelta >= 0 ? '+' : ''}` +
                        `${dashboard.resolutionRateDelta}%`
                    }
                />

                <SummaryCard
                    label="평균 분석 시간"
                    value={formatDuration(
                        dashboard.averageDurationSeconds
                    )}
                    description="평균 소요 시간"
                />

            </div>


            {/* =========================
                Severity Trend
            ========================= */}

            <section className={styles.panel}>

                <div className={styles.panelHeader}>

                    <div>
                        <h2>심각도 추이</h2>

                        <p>
                            날짜별 발견된 취약점
                        </p>
                    </div>

                </div>


                <div className={styles.trendList}>

                    {dashboard.severityTrend?.map(
                        item => (

                            <div
                                key={item.date}
                                className={styles.trendRow}
                            >

                                <span>
                                    {item.date}
                                </span>

                                <div className={styles.severityNumbers}>

                                    <span className={styles.critical}>
                                        C {item.critical}
                                    </span>

                                    <span className={styles.high}>
                                        H {item.high}
                                    </span>

                                    <span className={styles.medium}>
                                        M {item.medium}
                                    </span>

                                    <span className={styles.low}>
                                        L {item.low}
                                    </span>

                                </div>

                            </div>

                        )
                    )}

                </div>

            </section>


            {/* =========================
                Bottom
            ========================= */}

            <div className={styles.twoColumn}>

                {/* 취약점 유형 */}

                <section className={styles.panel}>

                    <div className={styles.panelHeader}>

                        <h2>
                            취약점 유형
                        </h2>

                    </div>

                    <div className={styles.typeList}>

                        {dashboard.typeDistribution?.map(
                            item => (

                                <div
                                    key={item.type}
                                    className={styles.typeRow}
                                >

                                    <div>
                                        <strong>
                                            {item.displayName}
                                        </strong>

                                        <small>
                                            {item.type}
                                        </small>
                                    </div>

                                    <div>
                                        <strong>
                                            {item.count}
                                        </strong>

                                        <span>
                                            {item.ratio}%
                                        </span>
                                    </div>

                                </div>

                            )
                        )}

                    </div>

                </section>


                {/* 최근 분석 */}

                <section className={styles.panel}>

                    <div className={styles.panelHeader}>

                        <h2>
                            최근 분석
                        </h2>

                    </div>

                    <div className={styles.recentList}>

                        {dashboard.recentAnalyses?.map(
                            analysis => (

                                <div
                                    key={analysis.analysisId}
                                    className={styles.recentRow}
                                >

                                    <div>

                                        <strong>
                                            {analysis.title}
                                        </strong>

                                        <span>
                                            {analysis.language}
                                        </span>

                                    </div>

                                    <div>

                                        {analysis.riskScore !== null &&
                                            analysis.riskScore !== undefined && (
                                                <span>
                                                    Risk {analysis.riskScore}
                                                </span>
                                            )}

                                        <small>
                                            {formatDate(
                                                analysis.createdAt
                                            )}
                                        </small>

                                    </div>

                                </div>

                            )
                        )}

                    </div>

                </section>

            </div>

        </div>
    );
};


const SummaryCard = ({
    label,
    value,
    description
}) => {

    return (
        <div className={styles.summaryCard}>

            <span>
                {label}
            </span>

            <strong>
                {value}
            </strong>

            <small>
                {description}
            </small>

        </div>
    );
};


const formatDuration = (seconds) => {

    if (!seconds) {
        return '0초';
    }

    if (seconds < 60) {
        return `${seconds}초`;
    }

    const minutes = Math.floor(seconds / 60);
    const remain = seconds % 60;

    return `${minutes}분 ${remain}초`;
};


const formatDate = (date) => {

    if (!date) {
        return '-';
    }

    return new Date(date).toLocaleString('ko-KR');
};


export default Dashboard;
