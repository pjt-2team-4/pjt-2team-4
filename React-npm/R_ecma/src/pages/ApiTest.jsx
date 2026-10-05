import React, { useState } from 'react';
import api from '../api/axios';
import { isAuthenticated } from '../auth/token';

const ApiTest = () => {
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const getDashboard = async () => {
        setLoading(true);
        setResult(null);

        try {
            const response = await api.get(
                '/dashboard/summary'
            );

            console.log(
                '[DASHBOARD] 응답:',
                response.data
            );

            setResult({
                api: 'GET /api/v1/dashboard/summary',
                status: response.status,
                success: true,
                data: response.data
            });

        } catch (error) {
            console.error(
                '[DASHBOARD] 실패:',
                error
            );

            setResult({
                api: 'GET /api/v1/dashboard/summary',
                status:
                    error.response?.status || 'ERROR',
                success: false,
                data:
                    error.response?.data ||
                    error.message
            });

        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>

            <h1>신규 API 테스트</h1>

            <p style={styles.description}>
                Dashboard API 테스트
            </p>

            <div style={styles.card}>
                <h2>인증 상태</h2>

                <p>
                    {isAuthenticated()
                        ? '🟢 로그인 상태'
                        : '🔴 로그인하지 않음'}
                </p>
            </div>

            <div style={styles.card}>
                <h2>Dashboard 조회 API</h2>

                <div style={styles.apiBox}>

                    <div>
                        <strong>Method</strong>

                        <span style={styles.method}>
                            GET
                        </span>
                    </div>

                    <div>
                        <strong>URL</strong>

                        <code>
                            /api/v1/dashboard/summary
                        </code>
                    </div>

                </div>

                <p style={styles.info}>
                    로그인되어 있으면 Axios interceptor가
                    Authorization Bearer Token을 자동으로 추가합니다.
                </p>

                <button
                    onClick={getDashboard}
                    disabled={loading}
                    style={
                        loading
                            ? styles.buttonDisabled
                            : styles.button
                    }
                >
                    {loading
                        ? '조회 중...'
                        : 'Dashboard 조회'}
                </button>
            </div>

            <div style={styles.card}>
                <h2>Response</h2>

                {!result && (
                    <p style={styles.empty}>
                        버튼을 눌러 API를 호출하세요.
                    </p>
                )}

                {result && (
                    <>
                        <p>
                            <strong>API:</strong>{' '}
                            {result.api}
                        </p>

                        <p>
                            <strong>Status:</strong>{' '}

                            <span
                                style={
                                    result.success
                                        ? styles.success
                                        : styles.error
                                }
                            >
                                {result.status}
                            </span>
                        </p>

                        <pre style={styles.json}>
                            {JSON.stringify(
                                result.data,
                                null,
                                2
                            )}
                        </pre>
                    </>
                )}
            </div>

        </div>
    );
};

const styles = {
    container: {
        padding: '30px',
        maxWidth: '900px',
        margin: '0 auto'
    },

    description: {
        color: '#64748b',
        marginBottom: '25px'
    },

    card: {
        background: '#fff',
        padding: '25px',
        borderRadius: '10px',
        marginBottom: '20px'
    },

    apiBox: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
        padding: '15px',
        background: '#f8fafc',
        borderRadius: '8px',
        marginBottom: '15px'
    },

    method: {
        marginLeft: '15px',
        background: '#16a34a',
        color: '#fff',
        padding: '5px 10px',
        borderRadius: '5px',
        fontWeight: 'bold'
    },

    info: {
        color: '#475569',
        marginBottom: '20px'
    },

    button: {
        padding: '12px 24px',
        border: 'none',
        borderRadius: '7px',
        background: '#2563eb',
        color: '#fff',
        fontSize: '15px',
        fontWeight: 'bold',
        cursor: 'pointer'
    },

    buttonDisabled: {
        padding: '12px 24px',
        border: 'none',
        borderRadius: '7px',
        background: '#94a3b8',
        color: '#fff',
        fontSize: '15px',
        fontWeight: 'bold',
        cursor: 'not-allowed'
    },

    empty: {
        color: '#94a3b8'
    },

    success: {
        color: '#16a34a',
        fontWeight: 'bold'
    },

    error: {
        color: '#dc2626',
        fontWeight: 'bold'
    },

    json: {
        background: '#111827',
        color: '#22c55e',
        padding: '20px',
        borderRadius: '8px',
        overflowX: 'auto',
        lineHeight: '1.6'
    }
};

export default ApiTest;
