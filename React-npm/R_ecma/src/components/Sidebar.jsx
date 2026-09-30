import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import styles from './Sidebar.module.css';

const Sidebar = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        if (window.confirm('로그아웃 하시겠습니까?')) {
            localStorage.removeItem('accessToken');
            alert('로그아웃 되었습니다.');
            navigate('/login'); // 📌 로그아웃 시 로그인 페이지로 이동
        }
    };

    return (
        <aside className={styles.sidebar}>
            <div>
                <div className={styles.brand}>
                    🛡️ 보안 분석 시스템
                </div>

                <ul className={styles.menuList}>
                    <li className={styles.menuItem}>
                        {/* 📌 / 경로가 메인 대시보드 */}
                        <NavLink 
                            to="/" 
                            className={({ isActive }) => isActive ? styles.active : ''}
                        >
                            📊 메인 대시보드
                        </NavLink>
                    </li>
                    <li className={styles.menuItem}>
                        <NavLink 
                            to="/analysis/list" 
                            className={({ isActive }) => isActive ? styles.active : ''}
                        >
                            📋 분석 히스토리
                        </NavLink>
                    </li>
                    <li className={styles.menuItem}>
                        <NavLink 
                            to="/analysis/new" 
                            className={({ isActive }) => isActive ? styles.active : ''}
                        >
                            ➕ 새 분석 요청
                        </NavLink>
                    </li>

                    <div className={styles.divider} />

                    <li className={`${styles.menuItem} ${styles.adminItem}`}>
                        <NavLink 
                            to="/admin/users" 
                            className={({ isActive }) => isActive ? styles.adminActive : ''}
                        >
                            👥 회원 관리 (Admin)
                        </NavLink>
                    </li>
                </ul>
            </div>

            <div>
                <div className={styles.divider} />
                <ul className={styles.menuList}>
                    <li className={styles.menuItem}>
                        {/* 📌 /login 경로 연결 */}
                        <NavLink 
                            to="/login" 
                            className={({ isActive }) => isActive ? styles.active : ''}
                        >
                            🔑 로그인
                        </NavLink>
                    </li>
                    <li className={styles.menuItem}>
                        <button onClick={handleLogout} className={styles.logoutBtn}>
                            🚪 로그아웃
                        </button>
                    </li>
                </ul>
            </div>
        </aside>
    );
};

export default Sidebar;