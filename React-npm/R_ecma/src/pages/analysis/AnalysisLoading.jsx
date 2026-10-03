import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './AnalysisLoading.css';

function AnalysisLoading() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [progress, setProgress] = useState(28);
    const [status, setStatus] = useState('SCANNING');
    const [stage, setStage] = useState('규칙 기반 탐지 중');

    useEffect(() => {
        const timer = setInterval(() => {
            setProgress((prev) => {
                if (prev >= 100) {
                    clearInterval(timer);
                    return 100;
                }

                const next = prev + 8;

                if (next >= 40) {
                    setStatus('EXPLAINING');
                    setStage('AI 설명 생성 중');
                }

                if (next >= 100) {
                    setStatus('COMPLETED');
                    setStage('분석 완료');

                    setTimeout(() => {
                        navigate(`/analysis/${id}`);
                    }, 500);
                }

                return Math.min(next, 100);
            });
        }, 1200);

        return () => clearInterval(timer);
    }, [id, navigate]);

    return (
        <div className="analysis-loading">
            <div className="analysis-loading__card">
                <div className="analysis-loading__icon">
                    {status === 'COMPLETED' ? '✓' : '⌛'}
                </div>

                <h1>
                    {status === 'COMPLETED'
                        ? '분석이 완료되었습니다'
                        : '코드를 분석하고 있습니다'}
                </h1>

                <p>
                    {stage}
                </p>

                <div className="analysis-loading__progress">
                    <div
                        className="analysis-loading__progress-bar"
                        style={{
                            width: `${progress}%`,
                        }}
                    />
                </div>

                <div className="analysis-loading__progress-info">
                    <span>
                        {progress}%
                    </span>

                    <span>
                        {status}
                    </span>
                </div>

                <div className="analysis-loading__logs">
                    <div>
                        <span>✓</span>
                        요청 검증 완료
                    </div>

                    <div>
                        <span>✓</span>
                        분석 파일 저장 완료
                    </div>

                    <div>
                        <span>
                            {progress >= 40 ? '✓' : '○'}
                        </span>
                        규칙 기반 취약점 탐지
                    </div>

                    <div>
                        <span>
                            {progress >= 100 ? '✓' : '○'}
                        </span>
                        AI 설명 생성
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AnalysisLoading;
