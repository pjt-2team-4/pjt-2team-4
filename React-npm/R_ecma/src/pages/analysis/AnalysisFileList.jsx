import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import api from '../../api/axios';

import './AnalysisFileList.css';

function AnalysisFileList() {
    const { analysisId } = useParams();
    const navigate = useNavigate();

    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchFiles = async () => {
            try {
                setLoading(true);
                setError('');

                const response = await api.get(
                    `/analyses/${analysisId}/files`
                );

                console.log(
                    '[AnalysisFileList] files:',
                    response.data
                );

                setFiles(
                    Array.isArray(response.data)
                        ? response.data
                        : []
                );

            } catch (err) {
                console.error(
                    '[AnalysisFileList] 파일 목록 조회 실패:',
                    err
                );

                setError(
                    '분석 파일 목록을 불러오지 못했습니다.'
                );
            } finally {
                setLoading(false);
            }
        };

        if (analysisId) {
            fetchFiles();
        }
    }, [analysisId]);

    // =====================================================
    // 경로별 그룹
    // =====================================================

    const groupedFiles = useMemo(() => {
        const groups = {};

        files.forEach((file) => {
            const path = file.relativePath || file.fileName;

            const parts = path.split('/');

            let current = groups;

            parts.forEach((part, index) => {
                const isFile = index === parts.length - 1;

                if (isFile) {
                    if (!current.__files) {
                        current.__files = [];
                    }

                    current.__files.push(file);
                } else {
                    if (!current[part]) {
                        current[part] = {};
                    }

                    current = current[part];
                }
            });
        });

        return groups;
    }, [files]);

    const handleFileClick = (file) => {
        navigate(
            `/analysis/${analysisId}/files/${file.fileId}`
        );
    };

    const renderTree = (node, depth = 0) => {
        const entries = Object.entries(node)
            .filter(([key]) => key !== '__files');

        const currentFiles = node.__files || [];

        return (
            <>
                {entries.map(([folderName, child]) => (
                    <div
                        key={`${folderName}-${depth}`}
                        className="analysis-file-list__folder"
                    >
                        <div
                            className="analysis-file-list__folder-name"
                            style={{
                                paddingLeft:
                                    `${16 + depth * 18}px`,
                            }}
                        >
                            <span className="analysis-file-list__icon">
                                📁
                            </span>

                            <span>
                                {folderName}
                            </span>
                        </div>

                        {renderTree(
                            child,
                            depth + 1
                        )}
                    </div>
                ))}

                {currentFiles.map((file) => (
                    <button
                        key={file.fileId}
                        type="button"
                        className="analysis-file-list__file"
                        style={{
                            paddingLeft:
                                `${16 + depth * 18}px`,
                        }}
                        onClick={() =>
                            handleFileClick(file)
                        }
                    >
                        <span className="analysis-file-list__icon">
                            📄
                        </span>

                        <span className="analysis-file-list__file-info">
                            <span className="analysis-file-list__file-name">
                                {file.fileName}
                            </span>

                            <span className="analysis-file-list__file-meta">
                                {file.language} · {file.lineCount} lines
                            </span>
                        </span>

                        {file.findingCount > 0 && (
                            <span className="analysis-file-list__finding-count">
                                {file.findingCount}
                            </span>
                        )}
                    </button>
                ))}
            </>
        );
    };

    // =====================================================
    // Loading
    // =====================================================

    if (loading) {
        return (
            <div className="analysis-file-list">
                <div className="analysis-file-list__loading">
                    파일 목록을 불러오는 중...
                </div>
            </div>
        );
    }

    // =====================================================
    // Error
    // =====================================================

    if (error) {
        return (
            <div className="analysis-file-list">
                <div className="analysis-file-list__error">
                    {error}
                </div>
            </div>
        );
    }

    // =====================================================
    // UI
    // =====================================================

    return (
        <div className="analysis-file-list">

            <div className="analysis-file-list__header">
                <div>
                    <div className="analysis-file-list__breadcrumb">
                        분석 #{analysisId}
                    </div>

                    <h1>
                        분석 파일
                    </h1>

                    <p>
                        분석에 포함된 소스 코드 파일입니다.
                    </p>
                </div>

                <button
                    type="button"
                    className="analysis-file-list__back"
                    onClick={() =>
                        navigate(
                            `/analysis/${analysisId}`
                        )
                    }
                >
                    분석 상세
                </button>
            </div>

            <div className="analysis-file-list__card">

                <div className="analysis-file-list__card-header">
                    <div>
                        <strong>
                            프로젝트 파일
                        </strong>

                        <span>
                            {files.length}개
                        </span>
                    </div>
                </div>

                {files.length === 0 ? (
                    <div className="analysis-file-list__empty">
                        분석된 파일이 없습니다.
                    </div>
                ) : (
                    <div className="analysis-file-list__tree">
                        {renderTree(groupedFiles)}
                    </div>
                )}

            </div>
        </div>
    );
}

export default AnalysisFileList;
