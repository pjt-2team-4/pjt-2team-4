import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisCreate.module.css';

const AnalysisCreate = () => {
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [language, setLanguage] = useState('JAVA');

    // 폴더 안에서 읽어온 파일들
    const [files, setFiles] = useState([]);

    // 선택한 폴더 이름
    const [folderName, setFolderName] = useState('');

    const [isLoading, setIsLoading] = useState(false);

    /*
     * 분석 대상 코드 파일 확장자
     *
     * 필요하면 여기에 확장자를 추가하면 됩니다.
     */
    const allowedExtensions = [
        '.java',
        '.js',
        '.jsx',
        '.ts',
        '.tsx',
        '.py',
        '.html',
        '.css',
        '.scss',
        '.xml',
        '.json'
    ];

    /*
     * =========================================================
     * 폴더 선택
     * =========================================================
     *
     * 사용자가 폴더 하나를 선택하면
     * 폴더 내부의 파일들을 전부 가져옵니다.
     *
     * 예:
     *
     * my-project/
     * ├── src/
     * │   ├── Main.java
     * │   └── User.java
     * ├── service/
     * │   └── UserService.java
     * └── controller/
     *     └── UserController.java
     *
     * ↓
     *
     * files = [
     *   {
     *      filePath: "my-project/src/Main.java",
     *      content: "..."
     *   },
     *   ...
     * ]
     */
    const handleFolderChange = async (e) => {
        const selectedFiles = Array.from(e.target.files);

        // 선택 취소
        if (selectedFiles.length === 0) {
            setFiles([]);
            setFolderName('');
            return;
        }

        /*
         * 첫 번째 파일의 상대 경로를 이용해서
         * 최상위 폴더 이름을 가져옵니다.
         */
        const firstFilePath =
            selectedFiles[0].webkitRelativePath ||
            selectedFiles[0].name;

        const rootFolderName =
            firstFilePath.split('/')[0];

        setFolderName(rootFolderName);

        /*
         * 코드 파일만 필터링
         */
        const codeFiles = selectedFiles.filter((file) => {
            const fileName =
                file.name.toLowerCase();

            return allowedExtensions.some((extension) =>
                fileName.endsWith(extension)
            );
        });

        if (codeFiles.length === 0) {
            alert(
                '선택한 폴더에 분석할 수 있는 코드 파일이 없습니다.'
            );

            setFiles([]);
            return;
        }

        try {
            /*
             * FileReader를 이용해서
             * 각각의 파일 내용을 읽습니다.
             */
            const filePromises = codeFiles.map((file) => {

                return new Promise((resolve, reject) => {

                    const reader = new FileReader();

                    reader.onload = (event) => {

                        resolve({
                            /*
                             * 폴더 구조를 유지하기 위해
                             * webkitRelativePath를 사용합니다.
                             */
                            filePath:
                                file.webkitRelativePath ||
                                file.name,

                            /*
                             * 실제 코드 내용
                             */
                            content:
                                event.target.result
                        });
                    };

                    reader.onerror = () => {
                        reject(
                            new Error(
                                `파일 읽기 실패: ${file.name}`
                            )
                        );
                    };

                    /*
                     * UTF-8 텍스트로 읽기
                     */
                    reader.readAsText(file, 'UTF-8');
                });
            });

            const parsedFiles =
                await Promise.all(filePromises);

            setFiles(parsedFiles);

            console.log(
                '[AnalysisCreate] 선택 폴더:',
                rootFolderName
            );

            console.log(
                '[AnalysisCreate] 전체 파일:',
                parsedFiles.length
            );

            console.log(
                '[AnalysisCreate] 파일 목록:',
                parsedFiles
            );

        } catch (error) {

            console.error(
                '[AnalysisCreate] 파일 읽기 실패:',
                error
            );

            alert(
                '폴더의 파일을 읽는 도중 오류가 발생했습니다.'
            );

            setFiles([]);
        }
    };


    /*
     * =========================================================
     * 분석 요청
     * =========================================================
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        /*
         * 제목 확인
         */
        if (!title.trim()) {
            alert('분석 제목을 입력해 주세요.');
            return;
        }

        /*
         * 폴더 확인
         */
        if (files.length === 0) {
            alert(
                '분석할 코드 폴더를 하나 선택해 주세요.'
            );
            return;
        }

        /*
         * 로그인 여부 확인
         *
         * userId는 가져오지 않습니다.
         *
         * accessToken만 존재하면 됩니다.
         *
         * Axios interceptor가 자동으로:
         *
         * Authorization: Bearer {token}
         *
         * 을 붙여줍니다.
         */
        const accessToken =
            localStorage.getItem('accessToken');

        if (!accessToken) {
            alert(
                '로그인이 필요합니다. 로그인 페이지로 이동합니다.'
            );

            navigate('/login');
            return;
        }

        setIsLoading(true);

        try {

            /*
             * 중요:
             *
             * userId를 보내지 않습니다.
             *
             * 백엔드에서 JWT를 이용해
             * 현재 로그인한 사용자를 확인해야 합니다.
             */
            const requestData = {
                title: title.trim(),
                language,
                files
            };

            console.log(
                '[AnalysisCreate] 분석 요청:',
                requestData
            );

            /*
             * POST /api/v1/analyses
             *
             * Axios interceptor가 accessToken을
             * Authorization 헤더에 자동으로 추가합니다.
             */
            const response = await api.post(
                '/analyses',
                requestData
            );

            console.log(
                '[AnalysisCreate] 분석 요청 성공:',
                response.data
            );

            /*
             * 백엔드 응답
             *
             * 예:
             *
             * {
             *     "analysisId": 1,
             *     "estimatedDurationSeconds": 30
             * }
             */
            const {
                analysisId,
                estimatedDurationSeconds
            } = response.data;

            /*
             * analysisId가 없는 경우
             */
            if (!analysisId) {

                console.error(
                    '[AnalysisCreate] analysisId가 없습니다:',
                    response.data
                );

                alert(
                    '분석 요청은 처리되었지만 분석 ID를 받지 못했습니다.'
                );

                return;
            }

            alert(
                '분석 요청이 정상적으로 등록되었습니다!'
            );

            /*
             * 분석 대기 화면으로 이동
             */
            navigate(
                `/analysis/${analysisId}/loading`,
                {
                    state: {
                        estimatedDurationSeconds:
                            estimatedDurationSeconds || 0
                    }
                }
            );

        } catch (error) {

            console.error(
                '[AnalysisCreate] 분석 요청 실패:',
                error
            );

            /*
             * =================================================
             * 서버 응답이 있는 경우
             * =================================================
             */
            if (error.response) {

                const status =
                    error.response.status;

                const data =
                    error.response.data;

                console.error(
                    '[AnalysisCreate] 서버 응답:',
                    data
                );

                /*
                 * Spring Boot 에러 메시지
                 */
                let message =
                    data?.message;

                /*
                 * Validation 오류
                 *
                 * 예:
                 *
                 * {
                 *   "message": "입력항목 검증 오류",
                 *   "errors": {
                 *      "title": "제목은 필수입니다."
                 *   }
                 * }
                 */
                if (
                    data?.errors &&
                    typeof data.errors === 'object'
                ) {

                    const validationMessages =
                        Object.values(data.errors);

                    if (
                        validationMessages.length > 0
                    ) {
                        message =
                            validationMessages.join('\n');
                    }
                }

                /*
                 * 400 Bad Request
                 */
                if (status === 400) {

                    alert(
                        message ||
                        '입력한 분석 정보를 확인해 주세요.'
                    );

                    return;
                }

                /*
                 * 401 Unauthorized
                 */
                if (status === 401) {

                    /*
                     * 잘못된 JWT이거나
                     * 로그인 정보가 없는 경우
                     */
                    localStorage.removeItem(
                        'accessToken'
                    );

                    localStorage.removeItem(
                        'tokenType'
                    );

                    localStorage.removeItem(
                        'user'
                    );

                    alert(
                        '로그인이 만료되었습니다. 다시 로그인해 주세요.'
                    );

                    navigate('/login');

                    return;
                }

                /*
                 * 403 Forbidden
                 */
                if (status === 403) {

                    alert(
                        '분석 요청에 대한 접근 권한이 없습니다.'
                    );

                    return;
                }

                /*
                 * 404
                 */
                if (status === 404) {

                    alert(
                        message ||
                        '분석 요청 경로를 찾을 수 없습니다.'
                    );

                    return;
                }

                /*
                 * 500
                 */
                if (status >= 500) {

                    alert(
                        '서버 내부 오류가 발생했습니다.'
                    );

                    return;
                }

                /*
                 * 기타 HTTP 오류
                 */
                alert(
                    message ||
                    `분석 요청에 실패했습니다. (${status})`
                );

            } else if (error.request) {

                /*
                 * 서버 응답 자체가 없는 경우
                 */
                alert(
                    '백엔드 서버와 통신할 수 없습니다. 서버가 실행 중인지 확인해 주세요.'
                );

            } else {

                /*
                 * Axios 요청 생성 오류
                 */
                alert(
                    '분석 요청을 생성하는 중 오류가 발생했습니다.'
                );
            }

        } finally {

            setIsLoading(false);
        }
    };


    return (
        <div className={styles.container}>

            <h2 className={styles.title}>
                새로운 코드 보안 분석 요청
            </h2>


            <form
                onSubmit={handleSubmit}
                className={styles.form}
            >

                {/* =================================================
                    분석 제목
                ================================================= */}
                <div className={styles.field}>

                    <label className={styles.label}>
                        분석 제목
                    </label>

                    <input
                        type="text"
                        value={title}
                        onChange={(e) =>
                            setTitle(e.target.value)
                        }
                        placeholder="예: 뱅킹 시스템 로그인 모듈 검사"
                        required
                        disabled={isLoading}
                        className={styles.input}
                    />

                </div>


                {/* =================================================
                    주요 언어
                ================================================= */}
                <div className={styles.field}>

                    <label className={styles.label}>
                        주요 언어
                    </label>

                    <select
                        value={language}
                        onChange={(e) =>
                            setLanguage(e.target.value)
                        }
                        disabled={isLoading}
                        className={styles.select}
                    >

                        <option value="JAVA">
                            JAVA
                        </option>

                        <option value="JAVASCRIPT">
                            JAVASCRIPT
                        </option>

                        <option value="TYPESCRIPT">
                            TYPESCRIPT
                        </option>

                        <option value="PYTHON">
                            PYTHON
                        </option>

                    </select>

                </div>


                {/* =================================================
                    폴더 선택
                ================================================= */}
                <div className={styles.field}>

                    <label className={styles.label}>
                        분석할 코드 폴더
                    </label>

                    <div className={styles.fileBox}>

                        <input
                            type="file"

                            /*
                             * 폴더 선택
                             *
                             * Chrome / Edge 등 Chromium
                             * 브라우저에서 사용 가능
                             */
                            webkitdirectory=""
                            directory=""
                            multiple

                            onChange={handleFolderChange}

                            disabled={isLoading}
                        />

                    </div>


                    {/* 선택한 폴더 */}
                    {folderName && (

                        <div
                            style={{
                                marginTop: '12px',
                                padding: '10px 12px',
                                backgroundColor: '#f1f5f9',
                                borderRadius: '8px',
                                fontSize: '14px'
                            }}
                        >
                            📁 선택된 폴더:{' '}
                            <strong>
                                {folderName}
                            </strong>
                        </div>

                    )}


                    {/* 파일 목록 */}
                    {files.length > 0 && (

                        <div
                            className={styles.fileList}
                            style={{
                                marginTop: '12px'
                            }}
                        >

                            <small
                                style={{
                                    fontWeight: 'bold'
                                }}
                            >
                                분석 대상 코드 파일 (
                                {files.length}
                                개)
                            </small>

                            <ul>

                                {files.map(
                                    (file, index) => (

                                        <li
                                            key={`${file.filePath}-${index}`}
                                        >
                                            {file.filePath}
                                        </li>

                                    )
                                )}

                            </ul>

                        </div>

                    )}

                </div>


                {/* =================================================
                    분석 시작
                ================================================= */}
                <button
                    type="submit"
                    disabled={
                        isLoading ||
                        files.length === 0
                    }
                    className={styles.submitBtn}
                >

                    {isLoading
                        ? '서버로 제출 중...'
                        : '보안 스캔 시작하기'}

                </button>

            </form>

        </div>
    );
};

export default AnalysisCreate;
