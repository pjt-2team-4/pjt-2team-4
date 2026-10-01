import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import api from '../../api/axios';

import {
    saveAuth
} from '../../auth/token';

import styles from './Login.module.css';


const Login = () => {

    const navigate = useNavigate();


    // =====================================================
    // State
    // =====================================================

    const [email, setEmail] =
        useState('');

    const [password, setPassword] =
        useState('');

    const [isLoading, setIsLoading] =
        useState(false);

    const [errorMsg, setErrorMsg] =
        useState('');


    // =====================================================
    // Login
    // =====================================================

    const handleLogin = async (e) => {

        e.preventDefault();

        setErrorMsg('');


        // -------------------------------------------------
        // 입력값 정리
        // -------------------------------------------------

        const trimmedEmail =
            email.trim();


        if (!trimmedEmail) {

            setErrorMsg(
                '이메일을 입력해 주세요.'
            );

            return;
        }


        if (!password) {

            setErrorMsg(
                '비밀번호를 입력해 주세요.'
            );

            return;
        }


        setIsLoading(true);


        try {

            // =================================================
            // 로그인 요청
            // =================================================

            const response =
                await api.post(
                    '/users/login',
                    {
                        email: trimmedEmail,
                        password: password
                    }
                );


            const data =
                response.data;


            console.log(
                '[LOGIN] 로그인 응답:',
                data
            );


            // =================================================
            // Access Token 검사
            // =================================================

            if (
                !data ||
                !data.accessToken
            ) {

                console.error(
                    '[LOGIN] Access Token 없음:',
                    data
                );

                setErrorMsg(
                    '로그인에 성공했지만 인증 토큰을 받지 못했습니다.'
                );

                return;
            }


            // =================================================
            // 인증 정보 저장
            //
            // 실제 localStorage 처리는
            // auth/token.js에서 담당
            // =================================================

            saveAuth({

                accessToken:
                    data.accessToken,

                tokenType:
                    data.tokenType || 'Bearer',

                email:
                    data.email || trimmedEmail

            });


            console.log(
                '[LOGIN] 인증 토큰 저장 완료'
            );


            // =================================================
            // 로그인 사용자
            // =================================================

            const loginEmail =
                data.email ||
                trimmedEmail;


            // =================================================
            // 로그인 성공
            // =================================================

            alert(
                `${loginEmail}님, 환영합니다!`
            );


            navigate('/');


        } catch (error) {

            console.error(
                '[LOGIN] 로그인 실패:',
                error
            );


            // =================================================
            // 서버 응답 있음
            // =================================================

            if (error.response) {

                const status =
                    error.response.status;

                const data =
                    error.response.data;


                console.error(
                    '[LOGIN] 서버 응답:',
                    data
                );


                // ------------------------------------------------
                // 기본 메시지
                // ------------------------------------------------

                let message =
                    data?.message;


                // ------------------------------------------------
                // Validation 오류
                //
                // {
                //   status: 400,
                //   message: "...",
                //   errors: {
                //      email: "...",
                //      password: "..."
                //   }
                // }
                // ------------------------------------------------

                if (
                    !message &&
                    data?.errors
                ) {

                    const errors =
                        Object.values(
                            data.errors
                        );


                    if (
                        errors.length > 0
                    ) {

                        message =
                            errors.join('\n');
                    }
                }


                // ------------------------------------------------
                // 401
                // ------------------------------------------------

                if (status === 401) {

                    setErrorMsg(
                        message ||
                        '이메일 또는 비밀번호가 올바르지 않습니다.'
                    );

                    return;
                }


                // ------------------------------------------------
                // 400
                // ------------------------------------------------

                if (status === 400) {

                    setErrorMsg(
                        message ||
                        '입력한 정보를 확인해 주세요.'
                    );

                    return;
                }


                // ------------------------------------------------
                // 404
                // ------------------------------------------------

                if (status === 404) {

                    setErrorMsg(
                        message ||
                        '사용자를 찾을 수 없습니다.'
                    );

                    return;
                }


                // ------------------------------------------------
                // 409
                // ------------------------------------------------

                if (status === 409) {

                    setErrorMsg(
                        message ||
                        '요청을 처리할 수 없습니다.'
                    );

                    return;
                }


                // ------------------------------------------------
                // 기타
                // ------------------------------------------------

                setErrorMsg(
                    message ||
                    '로그인 처리 중 서버 오류가 발생했습니다.'
                );


            } else if (error.request) {

                // =================================================
                // 서버 응답 없음
                // =================================================

                setErrorMsg(
                    '백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해 주세요.'
                );


            } else {

                // =================================================
                // 요청 생성 오류
                // =================================================

                setErrorMsg(
                    '로그인 요청 중 오류가 발생했습니다.'
                );
            }


        } finally {

            setIsLoading(false);
        }
    };


    // =====================================================
    // JSX
    // =====================================================

    return (

        <div className={styles.container}>

            <h2 className={styles.title}>
                코드 보안 분석기 로그인
            </h2>


            {/* ============================================
                에러 메시지
            ============================================ */}

            {errorMsg && (

                <div
                    className={
                        styles.errorMessage
                    }
                >
                    {errorMsg}
                </div>

            )}


            {/* ============================================
                로그인 Form
            ============================================ */}

            <form
                onSubmit={handleLogin}
                className={styles.form}
            >


                {/* ========================================
                    이메일
                ======================================== */}

                <div
                    className={
                        styles.formGroup
                    }
                >

                    <label>
                        이메일
                    </label>


                    <input
                        type="email"
                        value={email}
                        onChange={(e) =>
                            setEmail(
                                e.target.value
                            )
                        }
                        placeholder="admin@codeguard.com"
                        autoComplete="email"
                        required
                        disabled={isLoading}
                        className={styles.input}
                    />

                </div>


                {/* ========================================
                    비밀번호
                ======================================== */}

                <div
                    className={
                        styles.formGroup
                    }
                >

                    <label>
                        비밀번호
                    </label>


                    <input
                        type="password"
                        value={password}
                        onChange={(e) =>
                            setPassword(
                                e.target.value
                            )
                        }
                        placeholder="비밀번호 입력"
                        autoComplete="current-password"
                        required
                        disabled={isLoading}
                        className={styles.input}
                    />

                </div>


                {/* ========================================
                    로그인 버튼
                ======================================== */}

                <button
                    type="submit"
                    disabled={isLoading}
                    className={
                        styles.submitBtn
                    }
                >

                    {isLoading
                        ? '인증 처리 중...'
                        : '로그인'}

                </button>

            </form>


            {/* ============================================
                회원가입
            ============================================ */}

            <div
                className={
                    styles.footer
                }
            >

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
