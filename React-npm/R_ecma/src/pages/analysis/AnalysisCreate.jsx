import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import styles from './AnalysisCreate.module.css';

const AnalysisCreate = () => {
    const navigate = useNavigate();
    const [title, setTitle] = useState('');
    const [language, setLanguage] = useState('JAVA');
    const [files, setFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    // 컴포넌트 마운트 시 로그인 정보 확인
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        } else {
            alert('로그인이 필요한 서비스입니다.');
            navigate('/login');
        }
    }, [navigate]);

    // 📁 폴더 선택 핸들러 (확장자 필터링 포함)
    const handleFileChange = async (e) => {
        const selectedFiles = Array.from(e.target.files);
        if (selectedFiles.length === 0) return;

        // 허용할 소스 코드 확장자 목록 (필요에 따라 추가)
        const allowedExtensions = ['.java', '.js', '.jsx', '.ts', '.tsx', '.py', '.html', '.css', '.xml', '.json'];

        // 폴더 내에서 허용된 확장자만 필터링
        const validFiles = selectedFiles.filter(file => {
            const fileName = file.name.toLowerCase();
            return allowedExtensions.some(ext => fileName.endsWith(ext));
        });

        if (validFiles.length === 0) {
            alert('선택한 폴더에 분석 가능한 코드 파일이 없습니다.');
            return;
        }

        const filePromises = validFiles.map((file) => {
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = (event) => {
                    resolve({
                        // 폴더 업로드 시 webkitRelativePath에 전체 경로가 담깁니다 (예: src/main/java/.../App.java)
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
            alert('분석할 코드 파일이 없습니다. 올바른 폴더를 선택해 주세요.');
            return;
        }

        setIsLoading(true);

        try {
            // 📌 POST /api/v1/analyses (userId 추가 전송)
            const response = await api.post('/analyses', {
                userId: currentUser.userId || currentUser.id, // 로그인된 사용자 ID
                title,
                language,
                files
            });

            const { analysisId, estimatedDurationSeconds } = response.data;
            alert('분석 요청이 정상적으로 등록되었습니다!');
            
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
                        placeholder="예: 뱅킹 시스템 백엔드 소스코드 스캔"
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
                    <label className={styles.label}>소스 코드 폴더 첨부</label>
                    <div className={styles.fileBox}>
                        {/* 📌 폴더 업로드 핵심 속성: webkitdirectory */}
                        <input 
                            type="file" 
                            webkitdirectory="true" 
                            directory="true"
                            multiple 
                            onChange={handleFileChange}
                        />
                    </div>
                    
                    {files.length > 0 && (
                        <div className={styles.fileList} style={{ maxHeight: '200px', overflowY: 'auto' }}>
                            <small style={{ fontWeight: 'bold' }}>탐색된 파일 ({files.length}개):</small>
                            <ul style={{ fontSize: '13px', color: '#555' }}>
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
                    {isLoading ? '서버로 소스코드 전송 중...' : '보안 스캔 시작하기'}
                </button>
            </form>
        </div>
    );
};

export default AnalysisCreate;