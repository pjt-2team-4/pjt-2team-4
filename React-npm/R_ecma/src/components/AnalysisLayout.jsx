import React, { useEffect, useState } from 'react';
import { Outlet, useParams } from 'react-router-dom';
import api from '../api/axios';

const AnalysisLayout = () => {
    const { id } = useParams();

    const [report, setReport] = useState(null);
    const [selectedFile, setSelectedFile] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;

        const fetchReport = async () => {
            try {
                setLoading(true);

                const res = await api.get(`/analyses/${id}`);
                const data = res.data;

                setReport(data);

                // 처음 들어왔을 때 첫 번째 파일 선택
                if (data.files?.length > 0) {
                    setSelectedFile(data.files[0]);
                }
            } catch (error) {
                console.error('분석 결과 조회 실패:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchReport();
    }, [id]);

    if (loading) {
        return (
            <div style={{ padding: '30px' }}>
                분석 결과를 불러오는 중입니다...
            </div>
        );
    }

    if (!report) {
        return (
            <div style={{ padding: '30px' }}>
                분석 결과를 찾을 수 없습니다.
            </div>
        );
    }

    return (
        <div style={styles.container}>

            {/* =========================
                왼쪽 파일 트리
            ========================= */}
            <FileTree
                files={report.files || []}
                selectedFile={selectedFile}
                onSelectFile={setSelectedFile}
            />

            {/* =========================
                오른쪽 상세 영역
            ========================= */}
            <div style={styles.content}>
                <Outlet
                    context={{
                        report,
                        selectedFile,
                    }}
                />
            </div>

        </div>
    );
};


const FileTree = ({
    files,
    selectedFile,
    onSelectFile,
}) => {
    return (
        <aside style={styles.sidebar}>

            <div style={styles.header}>
                📁 분석 대상 파일
            </div>

            <div style={styles.tree}>

                {files.length === 0 ? (
                    <div style={styles.empty}>
                        분석 파일이 없습니다.
                    </div>
                ) : (
                    files.map((file, index) => {

                        const fileId = file.id || index;

                        const selected =
                            selectedFile &&
                            (
                                selectedFile.id
                                    ? selectedFile.id === file.id
                                    : selectedFile.filePath === file.filePath
                            );

                        return (
                            <div
                                key={fileId}
                                onClick={() => onSelectFile(file)}
                                style={{
                                    ...styles.file,
                                    ...(selected
                                        ? styles.selectedFile
                                        : {}),
                                }}
                            >
                                📄{' '}
                                {file.filePath ||
                                    file.relativePath ||
                                    file.fileName}
                            </div>
                        );
                    })
                )}

            </div>
        </aside>
    );
};


const styles = {
    container: {
        display: 'flex',
        width: '100%',
        height: 'calc(100vh - 48px)',
        gap: '8px',
        boxSizing: 'border-box',
    },

    sidebar: {
        width: '220px',
        minWidth: '220px',
        background: '#fff',
        border: '1px solid #e2e8f0',
        borderRadius: '8px',
        overflow: 'hidden',
    },

    header: {
        padding: '14px 16px',
        borderBottom: '1px solid #e2e8f0',
        fontSize: '13px',
        fontWeight: 'bold',
        color: '#1e293b',
    },

    tree: {
        padding: '8px',
        overflowY: 'auto',
        height: 'calc(100% - 48px)',
    },

    file: {
        padding: '7px 8px',
        marginBottom: '2px',
        borderRadius: '5px',
        fontSize: '12px',
        color: '#334155',
        cursor: 'pointer',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        transition: 'background-color 0.15s',
    },

    selectedFile: {
        backgroundColor: '#eff6ff',
        color: '#1d4ed8',
        fontWeight: '600',
    },

    empty: {
        padding: '15px 8px',
        color: '#94a3b8',
        fontSize: '12px',
        textAlign: 'center',
    },

    content: {
        flex: 1,
        minWidth: 0,
        background: '#fff',
        border: '1px solid #e2e8f0',
        borderRadius: '8px',
        overflow: 'hidden',
    },
};

export default AnalysisLayout;
