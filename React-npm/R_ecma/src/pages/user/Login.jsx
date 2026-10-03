import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import api from '../../api/axios';

import {
    saveAuth
} from '../../auth/token';

import styles from './Login.module.css';


const Login = () => {

    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');


    const handleSubmit = async (e) => {

        e.preventDefault();

        if (loading) {
            return;
        }

        setErrorMsg('');

        try {

            setLoading(true);

            const response = await api.post(
                '/auth/login',
                {
                    email: email.trim(),
                    password,
                }
            );

            console.log(
                '[LOGIN] response:',
                response.data
            );

            const data = response.data;


            // ==========================================
            // LoginDTO.Response
            //
            // {
            //   id,
            //   email,
            //   accessToken,
            //   tokenType,
            //   expiresIn
            // }
            // ==========================================

            if (!data?.accessToken) {

                throw new Error(
                    '로그인 응답에 accessToken이 없습니다.'
                );

            }


            // ==========================================
            // JWT 저장
            // ==========================================

            saveAuth({
                accessToken: data.accessToken,
                tokenType: data.tokenType || 'Bearer',
                email: data.email,
            });


            console.log(
                '[LOGIN] accessToken 저장 완료'
            );

            console.log(
                '[LOGIN] token:',
                localStorage.getItem('accessToken')
            );


            // ==========================================
            // Sidebar 갱신
            // ==========================================

            window.dispatchEvent(
                new Event('authChanged')
            );


            alert('로그인되었습니다.');

            navigate('/');

        } catch (error) {

            console.error(
                '[LOGIN] 로그인 실패:',
                error
            );


            if (error.response) {

                console.error(
                    '[LOGIN] 서버 응답:',
                    error.response.data
                );


                const message =
                    error.response.data?.message ||
                    error.response.data?.error ||
                    '이메일 또는 비밀번호를 확인해주세요.';

                setErrorMsg(message);

            } else {

                setErrorMsg(
                    error.message ||
                    '로그인 처리 중 오류가 발생했습니다.'
                );

            }

        } finally {

            setLoading(false);

        }
    };


    return (

        <div className={styles.container}>

            <h2 className={styles.title}>
                코드 보안 분석기 로그인
            </h2>


            {/* 에러 메시지 */}

            {errorMsg && (

                <div className={styles.errorMessage}>
                    {errorMsg}
                </div>

            )}


            {/* 로그인 Form */}

            <form
                onSubmit={handleSubmit}
                className={styles.form}
            >

                {/* 이메일 */}

                <div className={styles.formGroup}>

                    <label>
                        이메일
                    </label>

                    <input
                        type="email"
                        value={email}
                        onChange={(e) =>
                            setEmail(e.target.value)
                        }
                        placeholder="admin@codeguard.com"
                        autoComplete="email"
                        required
                        disabled={loading}
                        className={styles.input}
                    />

                </div>


                {/* 비밀번호 */}

                <div className={styles.formGroup}>

                    <label>
                        비밀번호
                    </label>

                    <input
                        type="password"
                        value={password}
                        onChange={(e) =>
                            setPassword(e.target.value)
                        }
                        placeholder="비밀번호 입력"
                        autoComplete="current-password"
                        required
                        disabled={loading}
                        className={styles.input}
                    />

                </div>


                {/* 로그인 */}

                <button
                    type="submit"
                    disabled={loading}
                    className={styles.submitBtn}
                >

                    {loading
                        ? '인증 처리 중...'
                        : '로그인'}

                </button>

            </form>


            {/* 회원가입 */}

            <div className={styles.footer}>

                <p>

                    계정이 없으신가요?{' '}

                    <Link to="/signup">
                        회원가입
                    </Link>

                </p>

            </div>

        </div>

    );
};


export default Login;
