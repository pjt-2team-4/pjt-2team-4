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
                        {selectedFile.filePath ||
                            selectedFile.relativePath ||
                            selectedFile.fileName}
                    </div>
                </div>

                <button
                    onClick={handleDownloadPdf}
                    disabled={isExporting}
                    className={styles.pdfBtn}
                >
                    {isExporting
                        ? 'PDF 생성 중...'
                        : '📄 PDF 리포트 다운로드'}
                </button>
            </div>


            {/* =========================
                선택한 파일 코드
            ========================= */}
            <div
                id="report-content"
                className={styles.codeViewer}
            >
                <div className={styles.codeHeader}>
                    <span>
                        {selectedFile.filePath ||
                            selectedFile.relativePath ||
                            selectedFile.fileName}
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
    const path =
        file.filePath ||
        file.relativePath ||
        file.fileName ||
        '';

    return path.split('/').pop();
};

export default AnalysisDetail;
