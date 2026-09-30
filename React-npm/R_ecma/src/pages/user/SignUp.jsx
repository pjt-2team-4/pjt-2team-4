import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios';
import styles from './SignUp.module.css';

const SignUp = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: ''
    });

    const [isLoading, setIsLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (formData.password !== formData.confirmPassword) {
            setErrorMsg('비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            return;
        }

        setIsLoading(true);

        try {
            // POST /api/v1/users/signup (email, password 전송)
            await api.post('/users/signup', {
                email: formData.email,
                password: formData.password
            });

            alert('회원가입이 완료되었습니다! 로그인해 주세요.');
            navigate('/login');
        } catch (error) {
            console.error('회원가입 실패:', error);
            const serverMsg = error.response?.data?.message || '회원가입 처리 중 오류가 발생했습니다.';
            setErrorMsg(serverMsg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>코드 보안 분석기 회원가입</h2>

            {errorMsg && <div className={styles.errorMessage}>{errorMsg}</div>}

            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formGroup}>
                    <label>이메일 주소</label>
                    <input 
                        type="email" 
                        name="email"
                        value={formData.email} 
                        onChange={handleChange} 
                        placeholder="user@codeguard.com"
                        required 
                        className={styles.input}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label>비밀번호</label>
                    <input 
                        type="password" 
                        name="password"
                        value={formData.password} 
                        onChange={handleChange} 
                        placeholder="비밀번호 입력"
                        required 
                        className={styles.input}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label>비밀번호 확인</label>
                    <input 
                        type="password" 
                        name="confirmPassword"
                        value={formData.confirmPassword} 
                        onChange={handleChange} 
                        placeholder="비밀번호 재입력"
                        required 
                        className={styles.input}
                    />
                </div>

                <button 
                    type="submit" 
                    disabled={isLoading}
                    className={styles.submitBtn}
                >
                    {isLoading ? '가입 처리 중...' : '회원가입'}
                </button>
            </form>

            <div className={styles.footer}>
                <p>이미 계정이 있으신가요? <Link to="/login">로그인</Link></p>
            </div>
        </div>
    );
};

export default SignUp;