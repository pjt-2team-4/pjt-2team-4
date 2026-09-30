import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios';
import styles from './Login.module.css';

const Login = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrorMsg('');

        try {
            const response = await api.post('/users/login', { 
                email: email, 
                password: password 
            });

            if (response.data) {
                localStorage.setItem('user', JSON.stringify(response.data));
            }
            
            alert(`${response.data.email}님, 환영합니다!`);
            navigate('/');
        } catch (error) {
            console.error('로그인 실패:', error);
            const serverMsg = error.response?.data?.message || '이메일 또는 비밀번호가 일치하지 않습니다.';
            setErrorMsg(serverMsg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>코드 보안 분석기 로그인</h2>
            
            {errorMsg && <div className={styles.errorMessage}>{errorMsg}</div>}

            <form onSubmit={handleLogin} className={styles.form}>
                <div className={styles.formGroup}>
                    <label>이메일</label>
                    <input 
                        type="email" 
                        value={email} 
                        onChange={(e) => setEmail(e.target.value)} 
                        placeholder="admin@codeguard.com"
                        required 
                        className={styles.input}
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>비밀번호</label>
                    <input 
                        type="password" 
                        value={password} 
                        onChange={(e) => setPassword(e.target.value)} 
                        placeholder="비밀번호 입력"
                        required 
                        className={styles.input}
                    />
                </div>
                <button 
                    type="submit" 
                    disabled={isLoading}
                    className={styles.submitBtn}
                >
                    {isLoading ? '인증 처리 중...' : '로그인'}
                </button>
            </form>
            
            <div className={styles.footer}>
                <p>계정이 없으신가요? <Link to="/signup">회원가입</Link></p>
            </div>
        </div>
    );
};

export default Login;