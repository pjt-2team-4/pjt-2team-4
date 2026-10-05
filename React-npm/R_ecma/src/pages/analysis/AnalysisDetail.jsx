import React, {
    useEffect,
    useState,
} from 'react';

import {
    useNavigate,
    useParams,
} from 'react-router-dom';

import api from '../../api/axios';

import './AnalysisDetail.css';


function AnalysisDetail() {

    const { id } = useParams();

    const navigate = useNavigate();


    const [data, setData] =
        useState(null);

    const [loading, setLoading] =
        useState(true);


    // =========================================================
    // 분석 상세 조회
    // =========================================================

    useEffect(() => {

        if (!id) {
            return;
        }


        const fetchDetail = async () => {

            try {

                setLoading(true);


                const response =
                    await api.get(
                        `/analyses/${id}`
                    );


                console.log(
                    '[AnalysisDetail] 응답:',
                    response.data
                );


                const result =
                    response.data?.data ??
                    response.data;


                setData(result);

            } catch (error) {

                console.error(
                    '[AnalysisDetail] 조회 실패:',
                    error
                );

                console.error(
                    '[AnalysisDetail] 서버 응답:',
                    error.response?.data
                );

            } finally {

                setLoading(false);

            }

        };


        fetchDetail();

    }, [id]);


    // =========================================================
    // Loading
    // =========================================================

    if (loading) {

        return (
            <div className="analysis-detail">
                분석 상세 정보를 불러오는 중입니다...
            </div>
        );

    }


    // =========================================================
    // 데이터 없음
    // =========================================================

    if (!data) {

        return (
            <div className="analysis-detail">
                분석 정보를 찾을 수 없습니다.
            </div>
        );

    }


    return (

        <div className="analysis-detail">

            {/* =================================================
                Header
            ================================================= */}

            <div className="analysis-detail__header">

                <div>

                    <div className="analysis-detail__breadcrumb">
                        분석 / {data.title}
                    </div>


                    <h1>
                        {data.title}
                    </h1>


                    <p>
                        분석 ID #{data.analysisId}
                    </p>

                </div>


                <span className="analysis-detail__completed">
                    {data.status}
                </span>

            </div>


            {/* =================================================
                Summary
            ================================================= */}

            <div className="analysis-detail__summary">

                <div className="analysis-detail__summary-item">

                    <span>
                        전체 파일
                    </span>

                    <strong>
                        {data.totalFiles}
                    </strong>

                </div>


                <div className="analysis-detail__summary-item">

                    <span>
                        전체 취약점
                    </span>

                    <strong>
                        {data.totalFindings}
                    </strong>

                </div>


                <div className="analysis-detail__summary-item">

                    <span>
                        최고 심각도
                    </span>

                    <strong className="critical">
                        {data.overallSeverity}
                    </strong>

                </div>


                <div className="analysis-detail__summary-item">

                    <span>
                        분석 시간
                    </span>

                    <strong>
                        {data.durationSeconds != null
                            ? `${data.durationSeconds}초`
                            : '-'}
                    </strong>

                </div>

            </div>


            {/* =================================================
                Severity
            ================================================= */}

            <section className="analysis-detail__section">

                <div className="analysis-detail__section-header">

                    <h2>
                        심각도별 취약점
                    </h2>


                    <button
                        type="button"
                        onClick={() =>
                            navigate(
                                `/analysis/${id}/findings`
                            )
                        }
                    >
                        전체 취약점 보기 →
                    </button>

                </div>


                <div className="analysis-detail__severity-grid">

                    {Object.entries(
                        data.severityCount || {}
                    ).map(
                        ([severity, count]) => (

                            <div
                                key={severity}
                                className={
                                    `analysis-detail__severity-card ` +
                                    `analysis-detail__severity-card--` +
                                    severity.toLowerCase()
                                }
                            >

                                <span>
                                    {severity}
                                </span>

                                <strong>
                                    {count}
                                </strong>

                            </div>

                        )
                    )}

                </div>

            </section>


            {/* =================================================
                Vulnerability Type
            ================================================= */}

            <section className="analysis-detail__section">

                <h2>
                    취약점 유형
                </h2>


                <div className="analysis-detail__type-list">

                    {(data.typeCount || []).map(
                        (item) => (

                            <div
                                key={item.type}
                                className="analysis-detail__type-item"
                            >

                                <div>

                                    <strong>
                                        {item.displayName}
                                    </strong>

                                    <span>
                                        {item.cweId}
                                    </span>

                                </div>


                                <strong>
                                    {item.count}
                                </strong>

                            </div>

                        )
                    )}

                </div>

            </section>


            {/* =================================================
                Actions
            ================================================= */}

            <div className="analysis-detail__actions">

                <button
                    type="button"
                    onClick={() =>
                        navigate(
                            `/analysis/${id}`
                        )
                    }
                >
                    파일 분석 화면
                </button>


                <button
                    type="button"
                    onClick={() =>
                        navigate(
                            `/analysis/${id}/findings`
                        )
                    }
                >
                    전체 취약점 보기
                </button>

            </div>

        </div>

    );
}


export default AnalysisDetail;
