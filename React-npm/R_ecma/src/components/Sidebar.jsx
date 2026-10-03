import React, { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';

import {
    clearAuth,
    getUser,
    isAuthenticated
} from '../auth/token';

import styles from './Sidebar.module.css';


const Sidebar = () => {

    const navigate = useNavigate();

    const [user, setUser] = useState(null);


    // =========================================================
    // 로그인 사용자 정보 확인
    // =========================================================
    useEffect(() => {

        const loadUser = () => {

            const accessToken =
                localStorage.getItem('accessToken');

            const savedUser =
                localStorage.getItem('user');

            if (accessToken && savedUser) {

                try {

                    setUser(JSON.parse(savedUser));

                } catch (error) {

                    console.error(
                        '[Sidebar] 사용자 정보 파싱 실패:',
                        error
                    );

                    setUser(null);
                }

            } else {

                setUser(null);

            }
        };


        loadUser();

        window.addEventListener(
            'authChanged',
            loadUser
        );


        return () => {

            window.removeEventListener(
                'authChanged',
                loadUser
            );

        };

    }, []);


    // =========================================================
    // 로그아웃
    // =========================================================
    const handleLogout = () => {

        if (!window.confirm('로그아웃 하시겠습니까?')) {
            return;
        }


        clearAuth();

        setUser(null);

        window.dispatchEvent(
            new Event('authChanged')
        );


        alert('로그아웃 되었습니다.');

        navigate('/login');

    };


    // =========================================================
    // 로그인 여부
    // =========================================================
    const isLoggedIn =
        !!localStorage.getItem('accessToken') &&
        !!user;


    return (
        <aside className={styles.sidebar}>

            {/* =================================================
                상단 메뉴
            ================================================= */}
            <div>

                {/* 브랜드 */}
                <div className={styles.brand}>
                    🛡️ 보안 분석 시스템
                </div>


                <ul className={styles.menuList}>

                    {/* =================================================
                        메인
                    ================================================= */}

                    <li className={styles.menuItem}>

                        <NavLink
                            to="/"
                            end
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            📊 메인 대시보드
                        </NavLink>

                    </li>


                    {/* =================================================
                        대시보드
                    ================================================= */}

                    <li className={styles.menuItem}>

                        <NavLink
                            to="/dashboard"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            📈 보안 대시보드
                        </NavLink>

                    </li>


                    <div className={styles.divider} />


                    {/* =================================================
                        분석
                    ================================================= */}

                    <li className={styles.sectionTitle}>
                        분석
                    </li>


                    {/* 분석 히스토리 */}

                    <li className={styles.menuItem}>

                        <NavLink
                            to="/analysis/list"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            📋 분석 히스토리
                        </NavLink>

                    </li>


                    {/* 새 분석 요청 */}

                    <li className={styles.menuItem}>

                        <NavLink
                            to="/analysis/new"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            ➕ 새 분석 요청
                        </NavLink>

                    </li>



                    <div className={styles.divider} />


                    {/* =================================================
                        개발 / 테스트
                    ================================================= */}

                    <li className={styles.sectionTitle}>
                        개발
                    </li>


                    <li className={styles.menuItem}>

                        <NavLink
                            to="/api-test"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            🧪 API 테스트
                        </NavLink>

                    </li>

                </ul>

            </div>


            {/* =================================================
                하단 로그인 영역
            ================================================= */}

            <div>

                <div className={styles.divider} />


                <ul className={styles.menuList}>

                    {isLoggedIn ? (

                        <>

                            {/* 현재 로그인 계정 */}

                            <li className={styles.menuItem}>

                                <div
                                    className={styles.userInfo}
                                >

                                    <div
                                        className={
                                            styles.userInfoLabel
                                        }
                                    >
                                        현재 로그인 계정
                                    </div>


                                    <div
                                        className={
                                            styles.userEmail
                                        }
                                    >
                                        👤 {user.email}
                                    </div>

                                </div>

                            </li>


                            {/* 로그아웃 */}

                            <li className={styles.menuItem}>

                                <button
                                    onClick={handleLogout}
                                    className={styles.logoutBtn}
                                >
                                    🚪 로그아웃
                                </button>

                            </li>

                        </>

                    ) : (

                        <>

                            {/* 로그인 */}

                            <li className={styles.menuItem}>

                                <NavLink
                                    to="/login"
                                    className={({ isActive }) =>
                                        isActive
                                            ? styles.active
                                            : ''
                                    }
                                >
                                    🔑 로그인
                                </NavLink>

                            </li>


                            {/* 회원가입 */}

                            <li className={styles.menuItem}>

                                <NavLink
                                    to="/signup"
                                    className={({ isActive }) =>
                                        isActive
                                            ? styles.active
                                            : ''
                                    }
                                >
                                    📝 회원가입
                                </NavLink>

                            </li>

                        </>

                    )}

                </ul>

            </div>

        </aside>
    );
};


export default Sidebar;
