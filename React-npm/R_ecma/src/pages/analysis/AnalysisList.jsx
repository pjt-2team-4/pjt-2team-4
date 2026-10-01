import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisList.module.css';

const AnalysisList = () => {
    const navigate = useNavigate();
    const [analyses, setAnalyses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchAnalyses = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await api.get('/analyses');
            setAnalyses(response.data);
        } catch (err) {
            console.error('분석 목록 조회 실패:', err);
            setError('분석 목록을 불러오는 도중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchAnalyses();
    }, []);

    const handleDelete = async (id, title, e) => {
        e.stopPropagation(); 
        if (!window.confirm(`'${title}' 분석 기록을 정말 삭제하시겠습니까?`)) {
            return;
        }

        try {
            await api.delete(`/analyses/${id}`);
            alert('성공적으로 삭제되었습니다.');
            setAnalyses((prev) => prev.filter((item) => item.analysisId !== id));
        } catch (err) {
            console.error('삭제 실패:', err);
            alert('삭제 처리 중 오류가 발생했습니다.');
        }
    };

    const renderStatusBadge = (status) => {
        const statusMap = {
            COMPLETED: { label: '완료', color: '#15803d', bgColor: '#f0fdf4', borderColor: '#bbf7d0' },
            SCANNING: { label: '스캔 중', color: '#0369a1', bgColor: '#f0f9ff', borderColor: '#bae6fd' },
            EXPLAINING: { label: 'AI 분석 중', color: '#6b21a8', bgColor: '#faf5ff', borderColor: '#e9d5ff' },
            PENDING: { label: '대기 중', color: '#b45309', bgColor: '#fffbeb', borderColor: '#fde68a' },
            FAILED: { label: '실패', color: '#b91c1c', bgColor: '#fef2f2', borderColor: '#fecaca' },
        };
        const current = statusMap[status] || { label: status, color: '#475569', bgColor: '#f8fafc', borderColor: '#cbd5e1' };

        return (
            <span style={{
                padding: '4px 10px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: '700',
                color: current.color,
                backgroundColor: current.bgColor,
                border: `1px solid ${current.borderColor}`,
                display: 'inline-block'
            }}>
                {current.label}
            </span>
        );
    };

    const getItemTargetUrl = (item) => {
        if (item.status === 'COMPLETED' || item.status === 'FAILED') {
            return `/analysis/${item.analysisId}`;
        }
        return `/analysis/${item.analysisId}/loading`;
    };

    if (isLoading) return <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>분석 히스토리를 불러오는 중...</div>;
    if (error) return <div style={{ padding: '40px', textAlign: 'center', color: '#ef4444' }}>{error}</div>;

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2 className={styles.title}>📋 내 분석 요청 히스토리</h2>
                <Link to="/analysis/new" className={styles.createBtn}>
                    + 새 분석 요청
                </Link>
            </div>

            {analyses.length === 0 ? (
                <div className={styles.emptyState}>
                    <p style={{ fontSize: '16px', marginBottom: '12px' }}>아직 진행한 보안 분석 요청이 없습니다.</p>
                    <Link to="/analysis/new" style={{ color: '#2563eb', fontWeight: '600', textDecoration: 'none' }}>
                        첫 번째 분석을 시작해 보세요!
                    </Link>
                </div>
            ) : (
                <div className={styles.tableWrapper}>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>제목</th>
                                <th>언어</th>
                                <th>상태</th>
                                <th>취약점 요약</th>
                                <th>생성일</th>
                                <th style={{ textAlign: 'center' }}>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            {analyses.map((item) => {
                                const targetUrl = getItemTargetUrl(item);
                                return (
                                    <tr 
                                        key={item.analysisId}
                                        onClick={() => navigate(targetUrl)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <td style={{ color: '#94a3b8', fontWeight: '600' }}>
                                            #{item.analysisId}
                                        </td>
                                        <td style={{ fontWeight: '600' }}>
                                            <Link to={targetUrl} style={{ color: '#0f172a', textDecoration: 'none' }}>
                                                {item.title}
                                            </Link>
                                        </td>
                                        <td>
                                            <span className={styles.langBadge}>{item.language}</span>
                                        </td>
                                        <td>
                                            {renderStatusBadge(item.status)}
                                        </td>
                                        <td>
                                            {item.status === 'COMPLETED' ? (
                                                <div style={{ display: 'flex', gap: '8px', fontSize: '12px' }}>
                                                    {(item.criticalCount > 0) && (
                                                        <span style={{ color: '#dc2626', fontWeight: '700' }}>
                                                            Critical: {item.criticalCount}
                                                        </span>
                                                    )}
                                                    {(item.highCount > 0) && (
                                                        <span style={{ color: '#ea580c', fontWeight: '600' }}>
                                                            High: {item.highCount}
                                                        </span>
                                                    )}
                                                    {(item.totalCount === 0 || (!item.criticalCount && !item.highCount && !item.mediumCount && !item.lowCount)) && (
                                                        <span style={{ color: '#16a34a', fontWeight: '600' }}>
                                                            취약점 없음 🎉
                                                        </span>
                                                    )}
                                                </div>
                                            ) : (
                                                <span style={{ color: '#94a3b8', fontSize: '13px' }}>-</span>
                                            )}
                                        </td>
                                        <td style={{ fontSize: '13px', color: '#64748b' }}>
                                            {new Date(item.createdAt).toLocaleString('ko-KR', {
                                                year: 'numeric', month: '2-digit', day: '2-digit',
                                                hour: '2-digit', minute: '2-digit'
                                            })}
                                        </td>
                                        <td style={{ textAlign: 'center' }} onClick={(e) => e.stopPropagation()}>
                                            <Link 
                                                to={targetUrl} 
                                                style={{ marginRight: '12px', color: '#2563eb', fontSize: '13px', textDecoration: 'none', fontWeight: '600' }}
                                            >
                                                보기
                                            </Link>
                                            <button 
                                                onClick={(e) => handleDelete(item.analysisId, item.title, e)}
                                                className={styles.deleteBtn}
                                            >
                                                삭제
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default AnalysisList;