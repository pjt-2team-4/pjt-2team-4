import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './FindingList.css';

const FindingList = () => {
    const { analysisId } = useParams();
    const navigate = useNavigate();

    const [findings, setFindings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        severity: '',
        type: '',
        status: '',
    });

    useEffect(() => {
        loadFindings();
    }, [analysisId, filters]);

    const loadFindings = async () => {
        try {
            setLoading(true);

            const token = localStorage.getItem('accessToken');

            const params = new URLSearchParams({
                page: '0',
                size: '50',
            });

            if (filters.severity) {
                params.append('severity', filters.severity);
            }

            if (filters.type) {
                params.append('type', filters.type);
            }

            if (filters.status) {
                params.append('status', filters.status);
            }

            const response = await fetch(
                `/api/v1/analyses/${analysisId}/findings?${params}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error('취약점 목록을 불러오지 못했습니다.');
            }

            const result = await response.json();

            setFindings(result.data?.content || []);
        } catch (error) {
            console.error(error);
            setFindings([]);
        } finally {
            setLoading(false);
        }
    };

    const severityClass = (severity) => {
        return `finding-severity finding-severity-${severity?.toLowerCase()}`;
    };

    return (
        <div className="finding-list-page">

            <header className="fl-header">
                <div>
                    <p className="fl-eyebrow">
                        ANALYSIS #{analysisId}
                    </p>

                    <h1>취약점 목록</h1>

                    <p>
                        탐지된 보안 취약점을 확인하고 처리 상태를 관리합니다.
                    </p>
                </div>

                <button
                    className="fl-back-button"
                    onClick={() => navigate(`/analysis/${analysisId}`)}
                >
                    분석 요약
                </button>
            </header>

            <section className="fl-filter-card">

                <select
                    value={filters.severity}
                    onChange={(e) =>
                        setFilters({
                            ...filters,
                            severity: e.target.value,
                        })
                    }
                >
                    <option value="">전체 심각도</option>
                    <option value="CRITICAL">CRITICAL</option>
                    <option value="HIGH">HIGH</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="LOW">LOW</option>
                </select>

                <select
                    value={filters.type}
                    onChange={(e) =>
                        setFilters({
                            ...filters,
                            type: e.target.value,
                        })
                    }
                >
                    <option value="">전체 유형</option>
                    <option value="SQL_INJECTION">SQL 삽입</option>
                    <option value="HARDCODED_SECRET">
                        하드코딩된 비밀정보
                    </option>
                    <option value="XSS">XSS</option>
                </select>

                <select
                    value={filters.status}
                    onChange={(e) =>
                        setFilters({
                            ...filters,
                            status: e.target.value,
                        })
                    }
                >
                    <option value="">전체 상태</option>
                    <option value="OPEN">OPEN</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="IGNORED">IGNORED</option>
                </select>

                <button
                    className="fl-reset-button"
                    onClick={() =>
                        setFilters({
                            severity: '',
                            type: '',
                            status: '',
                        })
                    }
                >
                    초기화
                </button>
            </section>

            <section className="fl-list-card">

                <div className="fl-list-header">
                    <h2>탐지 결과</h2>
                    <span>{findings.length}건</span>
                </div>

                {loading ? (
                    <div className="fl-empty">
                        취약점 목록을 불러오는 중입니다...
                    </div>
                ) : findings.length === 0 ? (
                    <div className="fl-empty">
                        조건에 맞는 취약점이 없습니다.
                    </div>
                ) : (
                    <div className="fl-table-wrapper">

                        <table className="fl-table">

                            <thead>
                                <tr>
                                    <th>심각도</th>
                                    <th>취약점</th>
                                    <th>파일</th>
                                    <th>Rule</th>
                                    <th>Line</th>
                                    <th>AI</th>
                                    <th>상태</th>
                                </tr>
                            </thead>

                            <tbody>
                                {findings.map((finding) => (
                                    <tr
                                        key={finding.findingId}
                                        onClick={() =>
                                            navigate(
                                                `/findings/${finding.findingId}`
                                            )
                                        }
                                    >
                                        <td>
                                            <span
                                                className={severityClass(
                                                    finding.severity
                                                )}
                                            >
                                                {finding.severity}
                                            </span>
                                        </td>

                                        <td>
                                            <div className="fl-finding-name">
                                                {finding.displayName}
                                            </div>

                                            <div className="fl-summary">
                                                {finding.summary}
                                            </div>
                                        </td>

                                        <td>
                                            <span className="fl-file">
                                                {finding.relativePath}
                                            </span>
                                        </td>

                                        <td>
                                            <code>{finding.ruleId}</code>
                                        </td>

                                        <td>
                                            {finding.startLine}
                                        </td>

                                        <td>
                                            <span
                                                className={
                                                    finding.llmStatus ===
                                                    'SUCCESS'
                                                        ? 'fl-ai-success'
                                                        : 'fl-ai-failed'
                                                }
                                            >
                                                {finding.llmStatus}
                                            </span>
                                        </td>

                                        <td>
                                            <span
                                                className={`fl-status fl-status-${finding.status?.toLowerCase()}`}
                                            >
                                                {finding.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>

                        </table>

                    </div>
                )}

            </section>

        </div>
    );
};

export default FindingList;
