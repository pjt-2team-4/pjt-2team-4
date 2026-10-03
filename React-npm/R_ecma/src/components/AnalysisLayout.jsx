import React, {
    useEffect,
    useState,
} from 'react';

import {
    useNavigate,
    useParams,
} from 'react-router-dom';

import api from '../api/axios';


const AnalysisLayout = () => {

    const { id } = useParams();

    const navigate = useNavigate();


    // =========================================================
    // 상태
    // =========================================================

    const [report, setReport] = useState(null);

    const [files, setFiles] = useState([]);

    const [selectedFile, setSelectedFile] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [fileLoading, setFileLoading] =
        useState(false);


    // =========================================================
    // 분석 정보 + 파일 목록 조회
    // =========================================================

    useEffect(() => {

        if (!id) {
            return;
        }


        const fetchAnalysis = async () => {

            try {

                setLoading(true);


                const [
                    reportResponse,
                    filesResponse,
                ] = await Promise.all([

                    api.get(
                        `/analyses/${id}`
                    ),

                    api.get(
                        `/analyses/${id}/files`
                    ),

                ]);


                console.log(
                    '[AnalysisLayout] 분석 응답:',
                    reportResponse.data
                );


                console.log(
                    '[AnalysisLayout] 파일 목록:',
                    filesResponse.data
                );


                // -----------------------------------------
                // 분석 결과
                // -----------------------------------------

                const reportData =
                    reportResponse.data?.data ??
                    reportResponse.data;


                // -----------------------------------------
                // 파일 목록
                // -----------------------------------------

                const fileData =
                    Array.isArray(filesResponse.data)
                        ? filesResponse.data
                        : filesResponse.data?.data ?? [];


                setReport(reportData);

                setFiles(fileData);


                // -----------------------------------------
                // 첫 번째 파일 자동 선택
                // -----------------------------------------

                if (fileData.length > 0) {

                    await loadFile(
                        fileData[0],
                        false
                    );

                } else {

                    setSelectedFile(null);

                }

            } catch (error) {

                console.error(
                    '[AnalysisLayout] 분석 조회 실패:',
                    error
                );

                console.error(
                    '[AnalysisLayout] 서버 응답:',
                    error.response?.data
                );

            } finally {

                setLoading(false);

            }

        };


        fetchAnalysis();

    }, [id]);


    // =========================================================
    // 파일 상세 조회
    // =========================================================

    const loadFile = async (
        file,
        showLoading = true
    ) => {

        if (!file?.fileId) {
            return;
        }


        try {

            if (showLoading) {
                setFileLoading(true);
            }


            console.log(
                '[AnalysisLayout] 파일 상세 조회:',
                file.fileId
            );


            const response = await api.get(
                `/analyses/${id}/files/${file.fileId}`
            );


            console.log(
                '[AnalysisLayout] 파일 상세 응답:',
                response.data
            );


            const data =
                response.data?.data ??
                response.data;


            setSelectedFile(data);

        } catch (error) {

            console.error(
                '[AnalysisLayout] 파일 조회 실패:',
                error
            );


            console.error(
                '[AnalysisLayout] 서버 응답:',
                error.response?.data
            );


            alert(
                '파일 내용을 불러오지 못했습니다.'
            );

        } finally {

            if (showLoading) {
                setFileLoading(false);
            }

        }

    };


    // =========================================================
    // 로딩
    // =========================================================

    if (loading) {

        return (
            <div style={styles.message}>
                분석 결과를 불러오는 중입니다...
            </div>
        );

    }


    // =========================================================
    // 분석 없음
    // =========================================================

    if (!report) {

        return (
            <div style={styles.message}>
                분석 결과를 찾을 수 없습니다.
            </div>
        );

    }


    // =========================================================
    // 화면
    // =========================================================

    return (

        <div style={styles.container}>

            {/* =================================================
                LEFT
                파일 목록
            ================================================= */}

            <FileTree
                files={files}
                selectedFile={selectedFile}
                onSelectFile={loadFile}
                onAnalysisDetail={() =>
                    navigate(
                        `/analysis/${id}/detail`
                    )
                }
            />


            {/* =================================================
                CENTER
                실제 파일 코드
            ================================================= */}

            <main style={styles.codePanel}>

                {fileLoading ? (

                    <div style={styles.message}>
                        파일 코드를 불러오는 중...
                    </div>

                ) : selectedFile ? (

                    <CodeViewer
                        file={selectedFile}
                    />

                ) : (

                    <div style={styles.message}>
                        왼쪽에서 파일을 선택해주세요.
                    </div>

                )}

            </main>


            {/* =================================================
                RIGHT
                분석 결과
            ================================================= */}

            <AnalysisResult
                report={report}
                analysisId={id}
            />

        </div>

    );
};


// =============================================================
// 파일 목록
// =============================================================

const FileTree = ({
    files,
    selectedFile,
    onSelectFile,
    onAnalysisDetail,
}) => {

    return (

        <aside style={styles.sidebar}>

            <div style={styles.sidebarHeader}>
                📁 분석 대상 파일
            </div>


            <div style={styles.fileList}>

                {files.length === 0 ? (

                    <div style={styles.empty}>
                        분석 파일이 없습니다.
                    </div>

                ) : (

                    files.map((file) => {

                        const selected =
                            selectedFile?.fileId ===
                            file.fileId;


                        return (

                            <button
                                key={file.fileId}
                                type="button"
                                onClick={() =>
                                    onSelectFile(file)
                                }
                                style={{
                                    ...styles.fileItem,
                                    ...(selected
                                        ? styles.selectedFile
                                        : {}),
                                }}
                                title={
                                    file.relativePath
                                }
                            >

                                <span>
                                    📄
                                </span>

                                <span
                                    style={
                                        styles.fileName
                                    }
                                >
                                    {file.relativePath}
                                </span>

                            </button>

                        );

                    })

                )}

            </div>


            {/* =================================================
                하단 분석 상세 버튼
            ================================================= */}

            <div style={styles.sidebarBottom}>

                <button
                    type="button"
                    onClick={onAnalysisDetail}
                    style={styles.detailButton}
                >
                    📊 분석 상세 보기
                </button>

            </div>

        </aside>

    );
};


// =============================================================
// 중앙 코드
// =============================================================

const CodeViewer = ({ file }) => {

    const lines =
        (file.content || '').split('\n');


    return (

        <section style={styles.codeSection}>

            {/* -----------------------------------------------
                코드 헤더
            ----------------------------------------------- */}

            <div style={styles.codeHeader}>

                <div>

                    <div style={styles.codeFileName}>
                        {file.fileName}
                    </div>

                    <div style={styles.codePath}>
                        {file.relativePath}
                    </div>

                </div>


                <div style={styles.lineCount}>
                    {lines.length} lines
                </div>

            </div>


            {/* -----------------------------------------------
                코드
            ----------------------------------------------- */}

            <div style={styles.codeBody}>

                {lines.map((line, index) => {

                    const lineNumber =
                        index + 1;


                    const vulnerability =
                        file.vulnerabilities?.find(
                            (item) =>
                                lineNumber >=
                                    item.startLine &&
                                lineNumber <=
                                    item.endLine
                        );


                    return (

                        <div
                            key={lineNumber}
                            style={{
                                ...styles.codeLine,

                                ...(vulnerability
                                    ? styles.vulnerableLine
                                    : {}),
                            }}
                        >

                            <span
                                style={
                                    styles.lineNumber
                                }
                            >
                                {lineNumber}
                            </span>


                            <code
                                style={
                                    styles.codeText
                                }
                            >
                                {line || ' '}
                            </code>


                            {vulnerability && (

                                <span
                                    style={
                                        styles.vulnerabilityLabel
                                    }
                                >
                                    {vulnerability.severity}
                                    {' · '}
                                    {
                                        vulnerability.typeDisplayName
                                    }
                                </span>

                            )}

                        </div>

                    );

                })}

            </div>

        </section>

    );
};


// =============================================================
// 오른쪽 분석 결과
// =============================================================

const AnalysisResult = ({
    report,
    analysisId,
}) => {

    const navigate = useNavigate();


    return (

        <aside style={styles.resultPanel}>

            <div style={styles.resultHeader}>
                분석 결과
            </div>


            <div style={styles.resultBody}>

                {/* -------------------------------------------
                    제목
                ------------------------------------------- */}

                <div style={styles.resultTitle}>
                    {report.title}
                </div>


                {/* -------------------------------------------
                    상태
                ------------------------------------------- */}

                <ResultRow
                    label="상태"
                    value={report.status}
                />


                {/* -------------------------------------------
                    파일
                ------------------------------------------- */}

                <ResultRow
                    label="전체 파일"
                    value={report.totalFiles}
                />


                {/* -------------------------------------------
                    취약점
                ------------------------------------------- */}

                <ResultRow
                    label="전체 취약점"
                    value={report.totalFindings}
                />


                {/* -------------------------------------------
                    최고 심각도
                ------------------------------------------- */}

                <ResultRow
                    label="최고 심각도"
                    value={report.overallSeverity}
                    valueStyle={{
                        color: '#dc2626',
                    }}
                />


                {/* -------------------------------------------
                    심각도별
                ------------------------------------------- */}

                <div style={styles.sectionTitle}>
                    심각도별 취약점
                </div>


                {Object.entries(
                    report.severityCount || {}
                ).map(([severity, count]) => (

                    <div
                        key={severity}
                        style={styles.severityRow}
                    >

                        <span>
                            {severity}
                        </span>

                        <strong>
                            {count}
                        </strong>

                    </div>

                ))}


                {/* -------------------------------------------
                    하단 버튼
                ------------------------------------------- */}

                <div style={styles.resultActions}>

                    <button
                        type="button"
                        onClick={() =>
                            navigate(
                                `/analysis/${analysisId}/detail`
                            )
                        }
                        style={styles.primaryButton}
                    >
                        분석 상세 보기
                    </button>


                    <button
                        type="button"
                        onClick={() =>
                            navigate(
                                `/analysis/${analysisId}/findings`
                            )
                        }
                        style={styles.secondaryButton}
                    >
                        전체 상세 보기
                    </button>

                </div>

            </div>

        </aside>

    );
};


// =============================================================
// 결과 Row
// =============================================================

const ResultRow = ({
    label,
    value,
    valueStyle,
}) => {

    return (

        <div style={styles.resultRow}>

            <span>
                {label}
            </span>

            <strong
                style={valueStyle}
            >
                {value}
            </strong>

        </div>

    );
};


// =============================================================
// Styles
// =============================================================

const styles = {

    container: {
        display: 'grid',

        gridTemplateColumns:
            '280px minmax(500px, 1fr) 300px',

        width: '100%',

        height: 'calc(100vh - 68px)',

        gap: '8px',

        boxSizing: 'border-box',
    },


    // =========================================================
    // 왼쪽
    // =========================================================

    sidebar: {
        display: 'flex',

        flexDirection: 'column',

        background: '#ffffff',

        border: '1px solid #e2e8f0',

        borderRadius: '8px',

        overflow: 'hidden',

        minWidth: 0,
    },


    sidebarHeader: {
        padding: '15px',

        borderBottom:
            '1px solid #e2e8f0',

        fontSize: '14px',

        fontWeight: '700',

        color: '#1e293b',
    },


    fileList: {
        flex: 1,

        overflowY: 'auto',

        padding: '8px',
    },


    fileItem: {
        display: 'flex',

        alignItems: 'center',

        gap: '8px',

        width: '100%',

        padding: '10px 8px',

        marginBottom: '3px',

        border: 'none',

        borderRadius: '6px',

        background: 'transparent',

        color: '#334155',

        cursor: 'pointer',

        textAlign: 'left',

        fontSize: '12px',

    },


    selectedFile: {
        background: '#eff6ff',

        color: '#1d4ed8',

        fontWeight: '600',
    },


    fileName: {
        minWidth: 0,

        overflow: 'hidden',

        textOverflow: 'ellipsis',

        whiteSpace: 'nowrap',
    },


    empty: {
        padding: '20px 10px',

        color: '#94a3b8',

        fontSize: '12px',

        textAlign: 'center',
    },


    sidebarBottom: {
        padding: '10px',

        borderTop:
            '1px solid #e2e8f0',

        background: '#f8fafc',
    },


    detailButton: {
        width: '100%',

        padding: '10px',

        border: 'none',

        borderRadius: '6px',

        background: '#2563eb',

        color: '#ffffff',

        fontSize: '12px',

        fontWeight: '600',

        cursor: 'pointer',
    },


    // =========================================================
    // 중앙
    // =========================================================

    codePanel: {
        minWidth: 0,

        background: '#ffffff',

        border: '1px solid #e2e8f0',

        borderRadius: '8px',

        overflow: 'hidden',
    },


    codeSection: {
        display: 'flex',

        flexDirection: 'column',

        width: '100%',

        height: '100%',

        minWidth: 0,
    },


    codeHeader: {
        display: 'flex',

        justifyContent: 'space-between',

        alignItems: 'center',

        padding: '14px 16px',

        borderBottom:
            '1px solid #e2e8f0',

        background: '#f8fafc',

        flexShrink: 0,
    },


    codeFileName: {
        fontSize: '14px',

        fontWeight: '700',

        color: '#0f172a',
    },


    codePath: {
        marginTop: '4px',

        fontSize: '11px',

        color: '#64748b',
    },


    lineCount: {
        fontSize: '11px',

        color: '#64748b',
    },


    codeBody: {
        flex: 1,

        overflow: 'auto',

        padding: '10px 0',

        background: '#0f172a',

        color: '#e2e8f0',

        fontFamily:
            "'Consolas', 'Monaco', monospace",

        fontSize: '13px',

        lineHeight: '1.7',
    },


    codeLine: {
        display: 'flex',

        alignItems: 'flex-start',

        minHeight: '22px',

        paddingRight: '16px',
    },


    vulnerableLine: {
        background:
            'rgba(239, 68, 68, 0.18)',

        borderLeft:
            '3px solid #ef4444',
    },


    lineNumber: {
        width: '50px',

        minWidth: '50px',

        paddingRight: '12px',

        textAlign: 'right',

        color: '#64748b',

        userSelect: 'none',
    },


    codeText: {
        flex: 1,

        whiteSpace: 'pre',

        color: '#e2e8f0',
    },


    vulnerabilityLabel: {
        marginLeft: '15px',

        padding: '2px 7px',

        borderRadius: '4px',

        background: '#dc2626',

        color: '#ffffff',

        fontSize: '10px',

        fontFamily: 'sans-serif',

        whiteSpace: 'nowrap',
    },


    // =========================================================
    // 오른쪽
    // =========================================================

    resultPanel: {
        display: 'flex',

        flexDirection: 'column',

        background: '#ffffff',

        border: '1px solid #e2e8f0',

        borderRadius: '8px',

        overflow: 'hidden',

        minWidth: 0,
    },


    resultHeader: {
        padding: '15px',

        borderBottom:
            '1px solid #e2e8f0',

        fontSize: '14px',

        fontWeight: '700',

        color: '#1e293b',
    },


    resultBody: {
        flex: 1,

        overflowY: 'auto',

        padding: '16px',
    },


    resultTitle: {
        marginBottom: '20px',

        fontSize: '15px',

        fontWeight: '700',

        color: '#0f172a',

        lineHeight: '1.5',
    },


    resultRow: {
        display: 'flex',

        justifyContent: 'space-between',

        alignItems: 'center',

        padding: '10px 0',

        borderBottom:
            '1px solid #f1f5f9',

        fontSize: '12px',

        color: '#64748b',
    },


    sectionTitle: {
        marginTop: '22px',

        marginBottom: '10px',

        fontSize: '12px',

        fontWeight: '700',

        color: '#334155',
    },


    severityRow: {
        display: 'flex',

        justifyContent: 'space-between',

        padding: '8px 0',

        fontSize: '12px',

        color: '#475569',
    },


    resultActions: {
        display: 'flex',

        flexDirection: 'column',

        gap: '8px',

        marginTop: '25px',

        paddingTop: '15px',

        borderTop:
            '1px solid #e2e8f0',
    },


    primaryButton: {
        width: '100%',

        padding: '10px',

        border: 'none',

        borderRadius: '6px',

        background: '#2563eb',

        color: '#ffffff',

        fontSize: '12px',

        fontWeight: '600',

        cursor: 'pointer',
    },


    secondaryButton: {
        width: '100%',

        padding: '10px',

        border:
            '1px solid #cbd5e1',

        borderRadius: '6px',

        background: '#ffffff',

        color: '#334155',

        fontSize: '12px',

        fontWeight: '600',

        cursor: 'pointer',
    },


    // =========================================================
    // 공통
    // =========================================================

    message: {
        display: 'flex',

        alignItems: 'center',

        justifyContent: 'center',

        width: '100%',

        height: '100%',

        color: '#64748b',

        fontSize: '13px',
    },

};


export default AnalysisLayout;
