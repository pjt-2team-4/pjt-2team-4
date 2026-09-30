import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import styles from './UserAdmin.module.css';

const UserAdmin = () => {
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState({
        id: '',
        email: '',
        password: ''
    });

    // 1. 전체 회원 목록 조회 (GET /api/v1/users)
    const fetchUsers = async () => {
        setIsLoading(true);
        try {
            const response = await api.get('/users');
            setUsers(response.data);
        } catch (error) {
            console.error('회원 목록 조회 실패:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // 2. 회원 삭제 처리 (DELETE /api/v1/users/{id})
    const handleDelete = async (userId, userEmail) => {
        if (!window.confirm(`'${userEmail}' 회원을 정말 삭제하시겠습니까?`)) return;

        try {
            await api.delete(`/users/${userId}`);
            alert('회원이 삭제되었습니다.');
            setUsers(prev => prev.filter(u => (u.id || u.userId) !== userId));
        } catch (error) {
            console.error('회원 삭제 실패:', error);
            alert('회원 삭제에 실패했습니다.');
        }
    };

    // 3. 회원 수정 모달 열기
    const openEditModal = (user) => {
        setEditingUser({
            id: user.id || user.userId,
            email: user.email || '',
            password: ''
        });
        setIsModalOpen(true);
    };

    // 4. 회원 정보 수정 제출 (PUT /api/v1/users/{id})
    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            await api.put(`/users/${editingUser.id}`, {
                email: editingUser.email,
                password: editingUser.password
            });

            alert('회원 정보가 수정되었습니다.');
            setIsModalOpen(false);
            fetchUsers();
        } catch (error) {
            console.error('회원 정보 수정 실패:', error);
            const serverMsg = error.response?.data?.message || '수정 처리에 실패했습니다.';
            alert(`오류: ${serverMsg}`);
        }
    };

    // 이메일 검색 필터링
    const filteredUsers = users.filter(user => 
        user.email?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (isLoading) return <div style={{ padding: '40px', textAlign: 'center' }}>회원 목록 불러오는 중...</div>;

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2 className={styles.title}>👥 회원 관리 (Admin)</h2>
            </div>

            {/* 검색 바 */}
            <div className={styles.searchBar}>
                <input 
                    type="text" 
                    placeholder="이메일 검색..." 
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className={styles.searchInput}
                />
            </div>

            {/* 회원 목록 테이블 */}
            <div className={styles.tableWrapper}>
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>이메일</th>
                            <th>생성일시</th>
                            <th>수정일시</th>
                            <th style={{ textAlign: 'center' }}>관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredUsers.length === 0 ? (
                            <tr>
                                <td colSpan="5" style={{ textAlign: 'center', padding: '30px', color: '#94a3b8' }}>
                                    조건에 일치하는 회원이 없습니다.
                                </td>
                            </tr>
                        ) : (
                            filteredUsers.map((user) => {
                                const userId = user.id || user.userId;
                                return (
                                    <tr key={userId}>
                                        <td style={{ color: '#94a3b8', fontWeight: 'bold' }}>#{userId}</td>
                                        <td style={{ fontWeight: '600' }}>{user.email}</td>
                                        <td style={{ fontSize: '13px', color: '#64748b' }}>
                                            {user.createdAt ? new Date(user.createdAt).toLocaleString('ko-KR') : '-'}
                                        </td>
                                        <td style={{ fontSize: '13px', color: '#64748b' }}>
                                            {user.updatedAt ? new Date(user.updatedAt).toLocaleString('ko-KR') : '-'}
                                        </td>
                                        <td style={{ textAlign: 'center' }}>
                                            <button 
                                                onClick={() => openEditModal(user)}
                                                className={styles.editBtn}
                                            >
                                                수정
                                            </button>
                                            <button 
                                                onClick={() => handleDelete(userId, user.email)}
                                                className={styles.deleteBtn}
                                            >
                                                삭제
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })
                        )}
                    </tbody>
                </table>
            </div>

            {/* 회원 정보 수정 모달 */}
            {isModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent}>
                        <h3 className={styles.modalTitle}>회원 정보 수정 (#{editingUser.id})</h3>
                        <form onSubmit={handleUpdate}>
                            <div className={styles.modalFormGroup}>
                                <label>이메일</label>
                                <input 
                                    type="email" 
                                    value={editingUser.email} 
                                    onChange={(e) => setEditingUser({ ...editingUser, email: e.target.value })}
                                    required 
                                    className={styles.modalInput}
                                />
                            </div>

                            <div className={styles.modalFormGroup}>
                                <label>비밀번호 변경 (필수)</label>
                                <input 
                                    type="password" 
                                    value={editingUser.password} 
                                    onChange={(e) => setEditingUser({ ...editingUser, password: e.target.value })}
                                    placeholder="변경할 비밀번호 입력"
                                    required
                                    className={styles.modalInput}
                                />
                            </div>

                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsModalOpen(false)} className={styles.cancelBtn}>
                                    취소
                                </button>
                                <button type="submit" className={styles.saveBtn}>
                                    저장
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserAdmin;