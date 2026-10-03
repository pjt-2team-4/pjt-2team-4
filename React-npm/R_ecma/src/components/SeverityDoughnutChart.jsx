import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

// Chart.js 모듈 등록
ChartJS.register(ArcElement, Tooltip, Legend);

const SeverityDoughnutChart = ({ report }) => {
    // 심각도별 데이터 설정
    const data = {
        labels: ['Critical', 'High', 'Medium', 'Low'],
        datasets: [
            {
                label: '취약점 수',
                data: [
                    report?.criticalCount || 0,
                    report?.highCount || 0,
                    report?.mediumCount || 0,
                    report?.lowCount || 0,
                ],
                backgroundColor: [
                    '#dc3545', // Critical (Red)
                    '#fd7e14', // High (Orange)
                    '#ffc107', // Medium (Yellow)
                    '#17a2b8', // Low (Teal)
                ],
                borderColor: [
                    '#ffffff',
                    '#ffffff',
                    '#ffffff',
                    '#ffffff',
                ],
                borderWidth: 2,
            },
        ],
    };

    // 차트 옵션 설정
    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom', // 범례 위치
                labels: {
                    font: {
                        size: 13,
                    },
                    padding: 15,
                },
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.raw || 0;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = total > 0 ? Math.round((value / total) * 100) : 0;
                        return ` ${label}: ${value}건 (${percentage}%)`;
                    },
                },
            },
        },
        cutout: '65%', // 도넛 중앙의 빈 공간 비율
    };

    return (
        <div style={{ width: '100%', height: '300px', position: 'relative' }}>
            <Doughnut data={data} options={options} />
        </div>
    );
};

export default SeverityDoughnutChart;