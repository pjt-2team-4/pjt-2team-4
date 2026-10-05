import React, { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AnalysisCreate.css';

function AnalysisCreate() {
    const navigate = useNavigate();

    const fileInputRef = useRef(null);
    const folderInputRef = useRef(null);

    const [title, setTitle] = useState('');
    const [files, setFiles] = useState([]);

    const [scanOptions, setScanOptions] = useState({
        detectSqlInjection: true,
        detectHardcodedSecret: true,
        detectXss: true,
    });

    // =========================================================
    // 파일 / 폴더 선택
    // =========================================================

    const handleFileChange = (e) => {
        const selectedFiles = Array.from(e.target.files);

        addFiles(selectedFiles);

        // 같은 파일을 다시 선택할 수 있도록 초기화
        e.target.value = '';
    };


    // =========================================================
    // 선택된 파일 추가
    // =========================================================

    const addFiles = (selectedFiles) => {
        if (!selectedFiles || selectedFiles.length === 0) {
            return;
        }

        const mappedFiles = selectedFiles.map((file) => ({
            id: `${file.name}-${file.size}-${file.lastModified}-${Math.random()}`,

            // 폴더 선택 시
            // webkitRelativePath:
            // project/src/main/App.java
            //
            // 일반 파일 선택 시:
            // App.java
            relativePath:
                file.webkitRelativePath || file.name,

            name: file.name,

            size: file.size,

            type: file.type,

            lastModified: file.lastModified,

            content: '',

            file,
        }));


        setFiles((prevFiles) => {

            const merged = [
                ...prevFiles,
                ...mappedFiles,
            ];


            // =====================================================
            // 중복 파일 제거
            //
            // 같은 경로의 파일을 두 번 선택했을 경우
            // 하나만 유지
            // =====================================================

            const uniqueFiles = Array.from(
                new Map(
                    merged.map((file) => [
                        file.relativePath,
                        file,
                    ])
                ).values()
            );


            return uniqueFiles;
        });
    };


    // =========================================================
    // 파일 제거
    // =========================================================

    const handleRemoveFile = (relativePath) => {
        setFiles((prevFiles) =>
            prevFiles.filter(
                (file) =>
                    file.relativePath !== relativePath
            )
        );
    };


    // =========================================================
    // 전체 파일 제거
    // =========================================================

    const handleClearFiles = () => {
        setFiles([]);
    };


    // =========================================================
    // 탐지 옵션
    // =========================================================

    const handleOptionChange = (key) => {
        setScanOptions((prev) => ({
            ...prev,
            [key]: !prev[key],
        }));
    };


    // =========================================================
    // 분석 시작
    // =========================================================

    const handleSubmit = (e) => {
        e.preventDefault();


        if (!title.trim()) {
            alert('분석 제목을 입력해주세요.');
            return;
        }


        if (files.length === 0) {
            alert('분석할 파일 또는 폴더를 선택해주세요.');
            return;
        }


        // =====================================================
        // 실제 API 연동 예정
        //
        // POST /api/v1/analyses
        //
        // FormData 예시:
        //
        // const formData = new FormData();
        //
        // formData.append(
        //     'title',
        //     title.trim()
        // );
        //
        // formData.append(
        //     'detectSqlInjection',
        //     scanOptions.detectSqlInjection
        // );
        //
        // formData.append(
        //     'detectHardcodedSecret',
        //     scanOptions.detectHardcodedSecret
        // );
        //
        // formData.append(
        //     'detectXss',
        //     scanOptions.detectXss
        // );
        //
        // files.forEach((item) => {
        //     formData.append(
        //         'files',
        //         item.file,
        //         item.relativePath
        //     );
        // });
        //
        // await api.post(
        //     '/analyses',
        //     formData,
        //     {
        //         headers: {
        //             'Content-Type':
        //                 'multipart/form-data',
        //         },
        //     }
        // );
        // =====================================================


        console.log(
            '[ANALYSIS CREATE] title:',
            title
        );

        console.log(
            '[ANALYSIS CREATE] files:',
            files
        );

        console.log(
            '[ANALYSIS CREATE] options:',
            scanOptions
        );


        // 현재 테스트용
        navigate('/analysis/1/loading');
    };


    return (
        <div className="analysis-create">

            {/* =================================================
                Header
            ================================================= */}

            <div className="analysis-create__header">

                <div>

                    <h1>
                        새 코드 분석
                    </h1>

                    <p>
                        프로젝트 코드를 업로드하여
                        보안 취약점을 분석합니다.
                    </p>

                </div>

            </div>


            <form
                className="analysis-create__form"
                onSubmit={handleSubmit}
            >

                {/* =================================================
                    분석 정보
                ================================================= */}

                <section className="analysis-create__card">

                    <h2>
                        분석 정보
                    </h2>


                    <div className="analysis-create__field">

                        <label htmlFor="title">
                            분석 제목
                        </label>


                        <input
                            id="title"
                            type="text"
                            value={title}
                            maxLength={100}
                            placeholder="예: payments-api"
                            onChange={(e) =>
                                setTitle(
                                    e.target.value
                                )
                            }
                        />


                        <span>
                            {title.length}/100
                        </span>

                    </div>

                </section>


                {/* =================================================
                    분석 대상 코드
                ================================================= */}

                <section className="analysis-create__card">

                    <h2>
                        분석 대상 코드
                    </h2>


                    {/* =================================================
                        업로드 버튼
                    ================================================= */}

                    <div className="analysis-create__upload-area">

                        {/* 파일 선택 */}

                        <label className="analysis-create__upload">

                            <input
                                ref={fileInputRef}
                                type="file"
                                multiple
                                onChange={
                                    handleFileChange
                                }
                            />

                            <div>

                                <strong>
                                    📄 파일 선택
                                </strong>

                                <p>
                                    분석할 소스 파일을
                                    선택해주세요.
                                </p>

                            </div>

                        </label>


                        {/* 폴더 선택 */}

                        <label className="analysis-create__upload analysis-create__upload--folder">

                            <input
                                ref={folderInputRef}
                                type="file"
                                multiple
                                webkitdirectory=""
                                directory=""
                                onChange={
                                    handleFileChange
                                }
                            />

                            <div>

                                <strong>
                                    📁 폴더 선택
                                </strong>

                                <p>
                                    프로젝트 폴더 전체를
                                    선택해주세요.
                                </p>

                            </div>

                        </label>

                    </div>


                    {/* =================================================
                        선택된 파일
                    ================================================= */}

                    {files.length > 0 && (

                        <div className="analysis-create__files">

                            <div className="analysis-create__files-header">

                                <strong>
                                    선택된 파일 {files.length}개
                                </strong>


                                <button
                                    type="button"
                                    onClick={
                                        handleClearFiles
                                    }
                                >
                                    전체 삭제
                                </button>

                            </div>


                            <div className="analysis-create__file-list">

                                {files.map((file) => (

                                    <div
                                        key={
                                            file.relativePath
                                        }
                                        className="analysis-create__file"
                                    >

                                        <span className="analysis-create__file-icon">
                                            📄
                                        </span>


                                        <div className="analysis-create__file-info">

                                            <span className="analysis-create__file-path">
                                                {file.relativePath}
                                            </span>


                                            <span className="analysis-create__file-size">

                                                {formatFileSize(
                                                    file.size
                                                )}

                                            </span>

                                        </div>


                                        <button
                                            type="button"
                                            className="analysis-create__file-remove"
                                            onClick={() =>
                                                handleRemoveFile(
                                                    file.relativePath
                                                )
                                            }
                                        >
                                            ×
                                        </button>

                                    </div>

                                ))}

                            </div>

                        </div>

                    )}


                    <p className="analysis-create__hint">

                        최대 20개 파일, 파일당 100KB,
                        전체 500KB까지 지원합니다.

                    </p>

                </section>


                {/* =================================================
                    탐지 옵션
                ================================================= */}

                <section className="analysis-create__card">

                    <h2>
                        탐지 옵션
                    </h2>


                    <div className="analysis-create__options">

                        <label>

                            <input
                                type="checkbox"
                                checked={
                                    scanOptions.detectSqlInjection
                                }
                                onChange={() =>
                                    handleOptionChange(
                                        'detectSqlInjection'
                                    )
                                }
                            />

                            <span>
                                SQL 삽입 탐지
                            </span>

                        </label>


                        <label>

                            <input
                                type="checkbox"
                                checked={
                                    scanOptions.detectHardcodedSecret
                                }
                                onChange={() =>
                                    handleOptionChange(
                                        'detectHardcodedSecret'
                                    )
                                }
                            />

                            <span>
                                비밀 키 탐지
                            </span>

                        </label>


                        <label>

                            <input
                                type="checkbox"
                                checked={
                                    scanOptions.detectXss
                                }
                                onChange={() =>
                                    handleOptionChange(
                                        'detectXss'
                                    )
                                }
                            />

                            <span>
                                XSS 탐지
                            </span>

                        </label>

                    </div>

                </section>


                {/* =================================================
                    Actions
                ================================================= */}

                <div className="analysis-create__actions">

                    <button
                        type="button"
                        className="analysis-create__cancel"
                        onClick={() =>
                            navigate(-1)
                        }
                    >
                        취소
                    </button>


                    <button
                        type="submit"
                        className="analysis-create__submit"
                    >
                        분석 시작
                    </button>

                </div>

            </form>

        </div>
    );
}


// =============================================================
// 파일 크기 표시
// =============================================================

function formatFileSize(size) {

    if (size < 1024) {
        return `${size} B`;
    }


    if (size < 1024 * 1024) {
        return `${(
            size / 1024
        ).toFixed(1)} KB`;
    }


    return `${(
        size / (1024 * 1024)
    ).toFixed(1)} MB`;
}


export default AnalysisCreate;
