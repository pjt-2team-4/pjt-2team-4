import React, { useState } from 'react';

const SeverityBadge = ({ severity }) => {
    const styles = {
        CRITICAL: { bg: '#dc3545', color: '#fff', label: '치명적 (Critical)' },
        HIGH: { bg: '#fd7e14', color: '#fff', label: '높음 (High)' },
        MEDIUM: { bg: '#ffc107', color: '#212529', label: '중간 (Medium)' },
        LOW: { bg: '#17a2b8', color: '#fff', label: '낮음 (Low)' },
    };
    const current = styles[severity] || { bg: '#6c757d', color: '#fff', label: severity };

    return (
        <span style={{
            backgroundColor: current.bg,
            color: current.color,
            padding: '4px 10px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: 'bold',
            display: 'inline-block'
        }}>
            {current.label}
        </span>
    );
};

const AnalysisDashboard = ({ report }) => {
    const [selectedFilter, setSelectedFilter] = useState('ALL');

    // report 데이터가 없는 경우의 기본값 설정
    const counts = {
        CRITICAL: report?.criticalCount || 0,
        HIGH: report?.highCount || 0,
        MEDIUM: report?.mediumCount || 0,
        LOW: report?.lowCount || 0,
    };

    const total = report?.totalCount || (counts.CRITICAL + counts.HIGH + counts.MEDIUM + counts.LOW);

    // 심각도별 비율 계산 (%)
    const getPercentage = (count) => total === 0 ? 0 : Math.round((count / total) * 100);

    // 필터링된 취약점 리스트
    const filteredVulnerabilities = (report?.vulnerabilities || []).filter(v => {
        if (selectedFilter === 'ALL') return true;
        return v.severity === selectedFilter;
    });

    return (
        <div style={{ maxWidth: '1000px', margin: '0 auto', fontFamily: 'sans-serif' }}>
            {/* 1. 요약 통계 카드 영역 */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '15px', marginBottom: '25px' }}>
                <div 
                    onClick={() => setSelectedFilter('ALL')}
                    style={{
                        padding: '15px',
                        backgroundColor: '#f8f9fa',
                        borderRadius: '8px',
                        border: selectedFilter === 'ALL' ? '2px solid #333' : '1px solid #e0e0e0',
                        cursor: 'pointer',
                        textAlign: 'center'
                    }}
                >
                    <div style={{ fontSize: '13px', color: '#666', marginBottom: '5px' }}>전체 취약점</div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{total}건</div>
                </div>

                <div 
                    onClick={() => setSelectedFilter('CRITICAL')}
                    style={{
                        padding: '15px',
                        backgroundColor: '#fff5f5',
                        borderRadius: '8px',
                        border: selectedFilter === 'CRITICAL' ? '2px solid #dc3545' : '1px solid #fecaca',
                        cursor: 'pointer',
                        textAlign: 'center'
                    }}
                >
                    <div style={{ fontSize: '13px', color: '#dc3545', fontWeight: 'bold', marginBottom: '5px' }}>CRITICAL</div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#dc3545' }}>{counts.CRITICAL}건</div>
                    <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>{getPercentage(counts.CRITICAL)}%</div>
                </div>

                <div 
                    onClick={() => setSelectedFilter('HIGH')}
                    style={{
                        padding: '15px',
                        backgroundColor: '#fff7ed',
                        borderRadius: '8px',
                        border: selectedFilter === 'HIGH' ? '2px solid #fd7e14' : '1px solid #ffedd5',
                        cursor: 'pointer',
                        textAlign: 'center'
                    }}
                >
                    <div style={{ fontSize: '13px', color: '#c2410c', fontWeight: 'bold', marginBottom: '5px' }}>HIGH</div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#c2410c' }}>{counts.HIGH}건</div>
                    <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>{getPercentage(counts.HIGH)}%</div>
                </div>

                <div 
                    onClick={() => setSelectedFilter('MEDIUM')}
                    style={{
                        padding: '15px',
                        backgroundColor: '#fefce8',
                        borderRadius: '8px',
                        border: selectedFilter === 'MEDIUM' ? '2px solid #eab308' : '1px solid #fef08a',
                        cursor: 'pointer',
                        textAlign: 'center'
                    }}
                >
                    <div style={{ fontSize: '13px', color: '#854d0e', fontWeight: 'bold', marginBottom: '5px' }}>MEDIUM</div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#854d0e' }}>{counts.MEDIUM}건</div>
                    <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>{getPercentage(counts.MEDIUM)}%</div>
                </div>

                <div 
                    onClick={() => setSelectedFilter('LOW')}
                    style={{
                        padding: '15px',
                        backgroundColor: '#f0fdf4',
                        borderRadius: '8px',
                        border: selectedFilter === 'LOW' ? '2px solid #17a2b8' : '1px solid #bbf7d0',
                        cursor: 'pointer',
                        textAlign: 'center'
                    }}
                >
                    <div style={{ fontSize: '13px', color: '#15803d', fontWeight: 'bold', marginBottom: '5px' }}>LOW</div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#15803d' }}>{counts.LOW}건</div>
                    <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>{getPercentage(counts.LOW)}%</div>
                </div>
            </div>

            {/* 2. 비율 프로그레스 바 (막대 차트) */}
            <div style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '14px', fontWeight: 'bold' }}>
                    <span>심각도 분포 비율</span>
                    <span>총 {total}개 탐지됨</span>
                </div>
                
                {/* 누적 바 차트 */}
                <div style={{ display: 'flex', height: '20px', borderRadius: '10px', overflow: 'hidden', backgroundColor: '#e9ecef' }}>
                    {counts.CRITICAL > 0 && (
                        <div style={{ width: `${getPercentage(counts.CRITICAL)}%`, backgroundColor: '#dc3545' }} title={`Critical: ${counts.CRITICAL}건 (${getPercentage(counts.CRITICAL)}%)`} />
                    )}
                    {counts.HIGH > 0 && (
                        <div style={{ width: `${getPercentage(counts.HIGH)}%`, backgroundColor: '#fd7e14' }} title={`High: ${counts.HIGH}건 (${getPercentage(counts.HIGH)}%)`} />
                    )}
                    {counts.MEDIUM > 0 && (
                        <div style={{ width: `${getPercentage(counts.MEDIUM)}%`, backgroundColor: '#ffc107' }} title={`Medium: ${counts.MEDIUM}건 (${getPercentage(counts.MEDIUM)}%)`} />
                    )}
                    {counts.LOW > 0 && (
                        <div style={{ width: `${getPercentage(counts.LOW)}%`, backgroundColor: '#17a2b8' }} title={`Low: ${counts.LOW}건 (${getPercentage(counts.LOW)}%)`} />
                    )}
                </div>
            </div>

            {/* 3. 필터링된 탐지 내역 리스트 */}
            <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                    <h3>탐지 항목 목록 ({filteredVulnerabilities.length}건)</h3>
                    {selectedFilter !== 'ALL' && (
                        <button 
                            onClick={() => setSelectedFilter('ALL')}
                            style={{ padding: '4px 10px', fontSize: '12px', border: '1px solid #ccc', borderRadius: '4px', cursor: 'pointer' }}
                        >
                            필터 해제
                        </button>
                    )}
                </div>

                {filteredVulnerabilities.length === 0 ? (
                    <div style={{ padding: '30px', textAlign: 'center', backgroundColor: '#f9f9f9', borderRadius: '8px', color: '#666' }}>
                        해당 심각도의 취약점이 없습니다.
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {filteredVulnerabilities.map((vuln) => (
                            <div key={vuln.id} style={{ padding: '15px', border: '1px solid #e0e0e0', borderRadius: '8px', backgroundColor: '#fff' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                    <strong style={{ fontSize: '16px' }}>[{vuln.ruleId}] {vuln.typeDisplayName || vuln.type}</strong>
                                    <SeverityBadge severity={vuln.severity} />
                                </div>
                                <p style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#555' }}>{vuln.description}</p>
                                {vuln.fileName && (
                                    <span style={{ fontSize: '12px', color: '#888', backgroundColor: '#f1f1f1', padding: '2px 6px', borderRadius: '4px' }}>
                                        {vuln.fileName} (Line: {vuln.startLine})
                                    </span>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalysisDashboard;