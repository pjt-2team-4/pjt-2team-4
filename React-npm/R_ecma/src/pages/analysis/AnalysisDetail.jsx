import React, { useState } from 'react';
import { useOutletContext, useParams } from 'react-router-dom';
import { downloadPdfReport } from '../../utils/exportToPdf';
import styles from './AnalysisDetail.module.css';

const AnalysisDetail = () => {
    const { id } = useParams();

    const {
        report,
        selectedFile,
    } = useOutletContext();

    const [isExporting, setIsExporting] = useState(false);

    const handleDownloadPdf = async () => {
        try {
            setIsExporting(true);
            await downloadPdfReport(
                'report-content',
                `보안분석리포트_#${id}.pdf`
            );
        } finally {
            setIsExporting(false);
        }
    };

    if (!selectedFile) {
        return (
            <div className={styles.empty}>
                <div>📁</div>
                <p>왼쪽에서 파일을 선택해주세요.</p>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            {/* 상단 */}
            <div className={styles.header}>
                <div>
                    <h2>📄 {getFileName(selectedFile)}</h2>
                    <div className={styles.filePath}>
                        {selectedFile.relativePath || selectedFile.filePath || selectedFile.fileName}
                    </div>
                </div>

                <button
                    onClick={handleDownloadPdf}
                    disabled={isExporting}
                    className={styles.pdfBtn}
                >
                    {isExporting ? 'PDF 생성 중...' : '📄 PDF 리포트 다운로드'}
                </button>
            </div>

            {/* =========================
                발견된 취약점 (Finding Markers)
            ========================= */}
            {selectedFile.findingMarkers && selectedFile.findingMarkers.length > 0 && (
                <div style={{ marginBottom: '20px', padding: '16px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '8px' }}>
                    <h3 style={{ fontSize: '15px', color: '#b91c1c', marginTop: 0, marginBottom: '12px' }}>
                        🚨 발견된 취약점 ({selectedFile.findingMarkers.length}건)
                    </h3>
                    <ul style={{ margin: 0, paddingLeft: '20px', color: '#7f1d1d', fontSize: '14px' }}>
                        {selectedFile.findingMarkers.map((marker, idx) => (
                            <li key={marker.findingId || idx} style={{ marginBottom: '6px' }}>
                                <strong>Line {marker.startLine} ~ {marker.endLine}:</strong> [{marker.severity}] {marker.label}
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {/* =========================
                선택한 파일 코드
            ========================= */}
            <div id="report-content" className={styles.codeViewer}>
                <div className={styles.codeHeader}>
                    <span>
                        {selectedFile.relativePath || selectedFile.filePath || selectedFile.fileName}
                    </span>
                </div>
                <pre className={styles.codeBlock}>
                    {selectedFile.content || '// 코드 내용이 없습니다.'}
                </pre>
            </div>
        </div>
    );
};

const getFileName = (file) => {
    // API 명세의 relativePath 우선 참조
    const path = file.relativePath || file.filePath || file.fileName || '';
    return path.split('/').pop();
};

export default AnalysisDetail;