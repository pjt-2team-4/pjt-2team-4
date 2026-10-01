import React, { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import styles from './Sidebar.module.css';

const Sidebar = () => {
    const navigate = useNavigate();

    const [user, setUser] = useState(null);

    // =========================================================
    // 로그인 사용자 정보 확인
    // =========================================================
    useEffect(() => {
        const loadUser = () => {
            const accessToken = localStorage.getItem('accessToken');
            const savedUser = localStorage.getItem('user');

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

        /*
         * 다른 컴포넌트에서 로그인/로그아웃이 발생했을 때
         * 사이드바도 바로 갱신하기 위한 이벤트
         */
        window.addEventListener('authChanged', loadUser);

        return () => {
            window.removeEventListener('authChanged', loadUser);
        };
    }, []);

    // =========================================================
    // 로그아웃
    // =========================================================
    const handleLogout = () => {
        if (!window.confirm('로그아웃 하시겠습니까?')) {
            return;
        }

        // 인증 관련 정보 전부 삭제
        localStorage.removeItem('accessToken');
        localStorage.removeItem('tokenType');
        localStorage.removeItem('user');

        // Sidebar 로그인 상태 즉시 변경
        setUser(null);

        // 다른 컴포넌트에도 로그인 상태 변경 알림
        window.dispatchEvent(new Event('authChanged'));

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

                <div className={styles.brand}>
                    🛡️ 보안 분석 시스템
                </div>

                <ul className={styles.menuList}>

                    {/* 메인 대시보드 */}
                    <li className={styles.menuItem}>
                        <NavLink
                            to="/"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.active
                                    : ''
                            }
                        >
                            📊 메인 대시보드
                        </NavLink>
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

                    {/* 관리자 */}
                    <li
                        className={`${styles.menuItem} ${styles.adminItem}`}
                    >
                        <NavLink
                            to="/admin/users"
                            className={({ isActive }) =>
                                isActive
                                    ? styles.adminActive
                                    : ''
                            }
                        >
                            👥 회원 관리 (Admin)
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
                            {/* =================================
                                현재 로그인 계정
                            ================================= */}
                            <li className={styles.menuItem}>
                                <div
                                    style={{
                                        padding: '10px 14px',
                                        color: '#64748b',
                                        fontSize: '13px',
                                        lineHeight: '1.5',
                                        borderRadius: '8px',
                                        backgroundColor: '#f8fafc',
                                        marginBottom: '6px',
                                    }}
                                >
                                    <div
                                        style={{
                                            fontSize: '12px',
                                            color: '#94a3b8',
                                            marginBottom: '3px',
                                        }}
                                    >
                                        현재 로그인 계정
                                    </div>

                                    <div
                                        style={{
                                            color: '#1e293b',
                                            fontWeight: '600',
                                            wordBreak: 'break-all',
                                        }}
                                    >
                                        👤 {user.email}
                                    </div>
                                </div>
                            </li>

                            {/* =================================
                                로그아웃
                            ================================= */}
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
                            {/* =================================
                                로그인
                            ================================= */}
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

                            {/* =================================
                                회원가입
                            ================================= */}
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
