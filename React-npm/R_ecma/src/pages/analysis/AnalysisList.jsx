import React, {
    useEffect,
    useState,
} from 'react';

import { useNavigate } from 'react-router-dom';

import api from '../../api/axios';

import './AnalysisList.css';


function AnalysisList() {

    const navigate = useNavigate();

    const [analyses, setAnalyses] =
        useState([]);

    const [loading, setLoading] =
        useState(true);


    useEffect(() => {

        const fetchAnalyses = async () => {

            try {

                const response =
                    await api.get('/analyses');

                console.log(
                    '[AnalysisList] 분석 목록:',
                    response.data
                );

                const data =
                    Array.isArray(response.data)
                        ? response.data
                        : response.data?.data || [];

                setAnalyses(data);

            } catch (error) {

                console.error(
                    '분석 목록 조회 실패:',
                    error
                );

            } finally {

                setLoading(false);

            }

        };

        fetchAnalyses();

    }, []);


    if (loading) {

        return (
            <div className="analysis-list">
                분석 목록을 불러오는 중입니다...
            </div>
        );

    }


    return (

        <div className="analysis-list">

            <div className="analysis-list__header">

                <div>

                    <h1>
                        분석 목록
                    </h1>

                    <p>
                        실행한 코드 분석 내역을 확인합니다.
                    </p>

                </div>


                <button
                    onClick={() =>
                        navigate('/analysis/new')
                    }
                >
                    + 새 분석
                </button>

            </div>


            <div className="analysis-list__card">

                <div className="analysis-list__table-wrapper">

                    <table className="analysis-list__table">

                        <thead>

                            <tr>
                                <th>분석명</th>
                                <th>상태</th>
                                <th>심각도</th>
                                <th>파일</th>
                                <th>취약점</th>
                            </tr>

                        </thead>


                        <tbody>

                            {analyses.map((analysis) => (

                                <tr
                                    key={analysis.analysisId}
                                    onClick={() =>
                                        navigate(
                                            `/analysis/${analysis.analysisId}`
                                        )
                                    }
                                >

                                    <td>
                                        <strong>
                                            {analysis.title}
                                        </strong>
                                    </td>

                                    <td>
                                        {analysis.status}
                                    </td>

                                    <td>
                                        {analysis.overallSeverity}
                                    </td>

                                    <td>
                                        {analysis.totalFiles}
                                    </td>

                                    <td>
                                        {analysis.totalFindings}
                                    </td>

                                </tr>

                            ))}

                        </tbody>

                    </table>

                </div>

            </div>

        </div>

    );

}


export default AnalysisList;
