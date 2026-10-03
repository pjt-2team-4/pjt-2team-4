import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './FindingDetail.css';

const FindingDetail = () => {
    const { findingId } = useParams();
    const navigate = useNavigate();

    const [finding, setFinding] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadFinding();
    }, [findingId]);

    const loadFinding = async () => {
        try {
            setLoading(true);
            setError('');

            const token = localStorage.getItem('accessToken');

            const response = await fetch(
                `/api/v1/findings/${findingId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error('취약점 정보를 불러오지 못했습니다.');
            }

            const result = await response.json();
            setFinding(result.data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const changeStatus = async (status) => {
        try {
            const token = localStorage.getItem('accessToken');

            const response = await fetch(
                `/api/v1/findings/${findingId}/status`,
                {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ status }),
                }
            );

            const result = await response.json();

            if (!response.ok) {
                throw new Error(
                    result.message || '상태 변경에 실패했습니다.'
                );
            }

            setFinding((prev) => ({
                ...prev,
                status: result.data.status,
            }));
        } catch (err) {
            alert(err.message);
        }
    };

    if (loading) {
        return (
            <div className="finding-detail-page">
                <div className="finding-detail-loading">
                    취약점 정보를 불러오는 중입니다...
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="finding-detail-page">
                <div className="finding-detail-error">
                    <h2>취약점 정보를 불러올 수 없습니다.</h2>
                    <p>{error}</p>
                    <button
                        className="fd-btn fd-btn-secondary"
                        onClick={() => navigate(-1)}
                    >
                        뒤로가기
                    </button>
                </div>
            </div>
        );
    }

    if (!finding) {
        return null;
    }

    const llm = finding.llmAnalysis;
    const detection = finding.detection;

    return (
        <div className="finding-detail-page">

            {/* Header */}
            <header className="fd-header">

                <div className="fd-header-left">
                    <button
                        className="fd-back-button"
                        onClick={() => navigate(-1)}
                    >
                        ←
                    </button>

                    <div>
                        <div className="fd-breadcrumb">
                            분석 #{finding.analysisId}
                            <span>/</span>
                            {finding.file?.relativePath}
                        </div>

                        <h1>{finding.displayName}</h1>
                    </div>
                </div>

                <div className="fd-header-actions">
                    {finding.status === 'OPEN' && (
                        <>
                            <button
                                className="fd-btn fd-btn-success"
                                onClick={() => changeStatus('RESOLVED')}
                            >
                                해결됨
                            </button>

                            <button
                                className="fd-btn fd-btn-muted"
                                onClick={() => changeStatus('IGNORED')}
                            >
                                무시
                            </button>
                        </>
                    )}

                    {finding.status !== 'OPEN' && (
                        <button
                            className="fd-btn fd-btn-outline"
                            onClick={() => changeStatus('OPEN')}
                        >
                            다시 열기
                        </button>
                    )}
                </div>
            </header>

            {/* Summary */}
            <section className="fd-summary-card">

                <div className="fd-summary-main">

                    <div className={`fd-severity fd-severity-${finding.severity?.toLowerCase()}`}>
                        {finding.severity}
                    </div>

                    <div className="fd-summary-content">
                        <h2>{finding.displayName}</h2>

                        <p>
                            {finding.summary ||
                                llm?.explanation ||
                                detection?.ruleDescription}
                        </p>

                        <div className="fd-meta-list">
                            <span>
                                <strong>Rule</strong>
                                {detection?.ruleId}
                            </span>

                            <span>
                                <strong>CWE</strong>
                                {finding.cweId}
                            </span>

                            <span>
                                <strong>File</strong>
                                {finding.file?.relativePath}
                            </span>

                            <span>
                                <strong>Line</strong>
                                {finding.startLine}
                                {finding.endLine !== finding.startLine &&
                                    ` - ${finding.endLine}`}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="fd-confidence">
                    <span>AI Confidence</span>
                    <strong>
                        {llm?.confidence ?? finding.confidence ?? '-'}%
                    </strong>

                    {llm?.verdict && (
                        <small>{llm.verdict}</small>
                    )}
                </div>

            </section>

            {/* Detection */}
            <section className="fd-section">

                <div className="fd-section-header">
                    <div>
                        <span className="fd-source-badge fd-source-scanner">
                            SCANNER
                        </span>
                        <h2>탐지 결과</h2>
                    </div>
                </div>

                <div className="fd-detection-card">

                    <div className="fd-info-row">
                        <span>Rule ID</span>
                        <strong>{detection?.ruleId}</strong>
                    </div>

                    <div className="fd-info-row">
                        <span>Rule Title</span>
                        <strong>{detection?.ruleTitle}</strong>
                    </div>

                    <div className="fd-info-row">
                        <span>설명</span>
                        <p>{detection?.ruleDescription}</p>
                    </div>

                    <div className="fd-matched-code">
                        <div className="fd-code-title">
                            Matched Text
                        </div>

                        <pre>
                            <code>
                                {detection?.matchedText}
                            </code>
                        </pre>
                    </div>

                </div>
            </section>

            {/* LLM */}
            <section className="fd-section">

                <div className="fd-section-header">
                    <div>
                        <span className="fd-source-badge fd-source-ai">
                            AI
                        </span>
                        <h2>AI 분석</h2>
                    </div>

                    {llm?.modelName && (
                        <span className="fd-model">
                            {llm.modelName}
                        </span>
                    )}
                </div>

                {llm?.status === 'FAILED' ? (
                    <div className="fd-ai-failed">
                        <h3>AI 설명 생성 실패</h3>
                        <p>
                            탐지 결과는 정상적으로 확인할 수 있습니다.
                        </p>
                    </div>
                ) : (
                    <div className="fd-ai-grid">

                        <div className="fd-ai-card fd-ai-card-wide">
                            <h3>설명</h3>
                            <p>{llm?.explanation || '-'}</p>
                        </div>

                        <div className="fd-ai-card">
                            <h3>위험 설명</h3>
                            <p>{llm?.riskDescription || '-'}</p>
                        </div>

                        <div className="fd-ai-card">
                            <h3>공격 시나리오</h3>
                            <p>{llm?.attackScenario || '-'}</p>
                        </div>

                        <div className="fd-ai-card fd-ai-card-wide">
                            <h3>개선 방법</h3>
                            <p>{llm?.remediation || '-'}</p>
                        </div>

                    </div>
                )}

            </section>

            {/* Code */}
            <section className="fd-section">

                <div className="fd-section-header">
                    <div>
                        <h2>코드 비교</h2>
                        <p>
                            탐지된 코드와 AI가 제안한 수정 코드를 비교합니다.
                        </p>
                    </div>
                </div>

                <div className="fd-code-grid">

                    <div className="fd-code-panel">

                        <div className="fd-code-panel-header fd-before-header">
                            <span>BEFORE</span>
                            <small>
                                {finding.startLine} line
                            </small>
                        </div>

                        <pre className="fd-code">
                            {(finding.beforeCode?.lines || []).map(
                                (line, index) => (
                                    <code key={index}>
                                        <span className="fd-line-number">
                                            {(finding.beforeCode.startLine || 1) + index}
                                        </span>
                                        {line}
                                        {'\n'}
                                    </code>
                                )
                            )}
                        </pre>

                    </div>

                    <div className="fd-code-panel">

                        <div className="fd-code-panel-header fd-after-header">
                            <span>AFTER</span>
                            <small>Suggested Fix</small>
                        </div>

                        <pre className="fd-code">
                            {(finding.afterCode?.lines || []).map(
                                (line, index) => (
                                    <code key={index}>
                                        <span className="fd-line-number">
                                            {(finding.afterCode.startLine || 1) + index}
                                        </span>
                                        {line}
                                        {'\n'}
                                    </code>
                                )
                            )}
                        </pre>

                    </div>

                </div>
            </section>

            {/* Footer */}
            <section className="fd-footer-info">

                <span>
                    LLM Status:
                    <strong>{llm?.status || 'N/A'}</strong>
                </span>

                <span>
                    Prompt:
                    <strong>{llm?.promptVersion || 'N/A'}</strong>
                </span>

            </section>

        </div>
    );
};

export default FindingDetail;
