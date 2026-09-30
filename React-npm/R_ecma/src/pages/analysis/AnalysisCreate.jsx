import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisCreate.module.css';

const AnalysisCreate = () => {
    const navigate = useNavigate();
    const [title, setTitle] = useState('');
    const [language, setLanguage] = useState('JAVA');
    const [files, setFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    // 파일 선택 핸들러
    const handleFileChange = async (e) => {
        const selectedFiles = Array.from(e.target.files);
        if (selectedFiles.length === 0) return;

        const filePromises = selectedFiles.map((file) => {
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = (event) => {
                    resolve({
                        filePath: file.webkitRelativePath || file.name,
                        content: event.target.result
                    });
                };
                reader.onerror = (error) => reject(error);
                reader.readAsText(file);
            });
        });

        try {
            const parsedFiles = await Promise.all(filePromises);
            setFiles(parsedFiles);
        } catch (error) {
            console.error('파일 읽기 실패:', error);
            alert('파일을 읽는 도중 오류가 발생했습니다.');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (files.length === 0) {
            alert('분석할 코드 파일을 하나 이상 첨부해 주세요.');
            return;
        }

        setIsLoading(true);

        try {
            // POST /api/v1/analyses
            const response = await api.post('/analyses', {
                title,
                language,
                files
            });

            // 백엔드가 DB에 저장하고 응답으로 내려준 estimatedDurationSeconds 추출
            const { analysisId, estimatedDurationSeconds } = response.data;

            alert('분석 요청이 정상적으로 등록되었습니다!');
            
            // 📌 백엔드에서 반환된 DB 컬럼 값을 state로 전송
            navigate(`/analysis/${analysisId}/loading`, {
                state: { estimatedDurationSeconds }
            });
        } catch (error) {
            console.error('분석 요청 실패:', error);
            alert('분석 요청 제출 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>새로운 코드 보안 분석 요청</h2>
            
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.field}>
                    <label className={styles.label}>분석 제목</label>
                    <input 
                        type="text" 
                        value={title} 
                        onChange={(e) => setTitle(e.target.value)} 
                        placeholder="예: 뱅킹 시스템 로그인 모듈 검사"
                        required 
                        className={styles.input}
                    />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>주요 언어</label>
                    <select 
                        value={language} 
                        onChange={(e) => setLanguage(e.target.value)}
                        className={styles.select}
                    >
                        <option value="JAVA">JAVA</option>
                        <option value="JAVASCRIPT">JAVASCRIPT</option>
                        <option value="TYPESCRIPT">TYPESCRIPT</option>
                        <option value="PYTHON">PYTHON</option>
                    </select>
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>코드 파일 첨부</label>
                    <div className={styles.fileBox}>
                        <input 
                            type="file" 
                            multiple 
                            onChange={handleFileChange}
                            accept=".java,.js,.jsx,.ts,.tsx,.py,.html"
                        />
                    </div>
                    
                    {files.length > 0 && (
                        <div className={styles.fileList}>
                            <small style={{ fontWeight: 'bold' }}>선택된 파일 ({files.length}개):</small>
                            <ul>
                                {files.map((file, idx) => (
                                    <li key={idx}>{file.filePath}</li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                <button 
                    type="submit" 
                    disabled={isLoading}
                    className={styles.submitBtn}
                >
                    {isLoading ? '서버로 제출 중...' : '보안 스캔 시작하기'}
                </button>
            </form>
        </div>
    );
};

export default AnalysisCreate;