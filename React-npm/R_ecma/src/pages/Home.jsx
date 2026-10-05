import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Home.module.css';

const Home = () => {

    const navigate = useNavigate();

    return (
        <div className={styles.container}>

            <div className={styles.header}>

                <div>
                    <h1>보안 분석 플랫폼</h1>

                    <p>
                        코드 보안 분석과 취약점 관리를 한곳에서 확인하세요.
                    </p>
                </div>

            </div>


            <div className={styles.grid}>

                <MenuCard
                    icon="📊"
                    title="Dashboard"
                    description="전체 보안 분석 현황과 취약점 통계를 확인합니다."
                    onClick={() => navigate('/dashboard')}
                />

                <MenuCard
                    icon="🔍"
                    title="새 분석"
                    description="새로운 코드를 등록하고 보안 분석을 시작합니다."
                    onClick={() => navigate('/analysis/new')}
                />

                <MenuCard
                    icon="📋"
                    title="분석 목록"
                    description="등록된 분석 요청과 분석 결과를 확인합니다."
                    onClick={() => navigate('/analysis/list')}
                />

                <MenuCard
                    icon="⚠️"
                    title="취약점 관리"
                    description="분석에서 발견된 취약점을 조회하고 상태를 관리합니다."
                    onClick={() => navigate('/analysis/list')}
                />

                <MenuCard
                    icon="🗂️"
                    title="분석 파일"
                    description="분석 대상 파일과 소스 코드를 확인합니다."
                    onClick={() => navigate('/analysis/list')}
                />

                <MenuCard
                    icon="🧪"
                    title="API 테스트"
                    description="현재 연결된 Backend API를 테스트합니다."
                    onClick={() => navigate('/api-test')}
                />

            </div>

        </div>
    );
};


const MenuCard = ({
    icon,
    title,
    description,
    onClick
}) => {

    return (
        <button
            className={styles.card}
            onClick={onClick}
        >

            <div className={styles.icon}>
                {icon}
            </div>

            <div className={styles.content}>

                <h2>
                    {title}
                </h2>

                <p>
                    {description}
                </p>

            </div>

            <span className={styles.arrow}>
                →
            </span>

        </button>
    );
};


export default Home;
