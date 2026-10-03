import React from 'react';
import {
    BrowserRouter as Router,
    Routes,
    Route
} from 'react-router-dom';

import Sidebar from './components/Sidebar';
import AnalysisLayout from './components/AnalysisLayout';

// Home
import Home from './pages/Home';

// Dashboard
import Dashboard from './pages/dashboard/Dashboard';

// API Test
import ApiTest from './pages/ApiTest';

// User
import SignUp from './pages/user/SignUp';
import Login from './pages/user/Login';

// Analysis
import AnalysisCreate from './pages/analysis/AnalysisCreate';
import AnalysisDetail from './pages/analysis/AnalysisDetail';
import AnalysisList from './pages/analysis/AnalysisList';
import AnalysisLoading from './pages/analysis/AnalysisLoading';
import AnalysisFileList from './pages/analysis/AnalysisFileList';
import AnalysisFileDetail from './pages/analysis/AnalysisFileDetail';

// Findings
import FindingList from './pages/finding/FindingList';
import FindingDetail from './pages/finding/FindingDetail';


function App() {
    return (
        <Router>

            <div style={styles.app}>

                <Sidebar />

                <main style={styles.main}>

                    <Routes>

                        {/* ================================
                            Home
                        ================================= */}
                        <Route
                            path="/"
                            element={<Home />}
                        />


                        {/* ================================
                            Dashboard
                        ================================= */}
                        <Route
                            path="/dashboard"
                            element={<Dashboard />}
                        />


                        {/* ================================
                            API Test
                        ================================= */}
                        <Route
                            path="/api-test"
                            element={<ApiTest />}
                        />


                        {/* ================================
                            User
                        ================================= */}
                        <Route
                            path="/signup"
                            element={<SignUp />}
                        />

                        <Route
                            path="/login"
                            element={<Login />}
                        />


                        {/* ================================
                            Analysis Create
                        ================================= */}
                        <Route
                            path="/analysis/new"
                            element={<AnalysisCreate />}
                        />


                        {/* ================================
                            Analysis List
                        ================================= */}
                        <Route
                            path="/analysis/list"
                            element={<AnalysisList />}
                        />


                        {/* ================================
                            Analysis Loading
                        ================================= */}
                        <Route
                            path="/analysis/:id/loading"
                            element={<AnalysisLoading />}
                        />


                        {/* ================================
                            Analysis Workspace
                            
                            왼쪽 : 파일 목록
                            중앙 : 선택한 파일 코드
                            오른쪽 : 분석 결과
                        ================================= */}
                        <Route
                            path="/analysis/:id"
                            element={<AnalysisLayout />}
                        />


                        {/* ================================
                            Analysis Detail
                            
                            기존 전체 분석 상세 페이지
                        ================================= */}
                        <Route
                            path="/analysis/:id/detail"
                            element={<AnalysisDetail />}
                        />


                        {/* ================================
                            Finding List
                        ================================= */}
                        <Route
                            path="/analysis/:analysisId/findings"
                            element={<FindingList />}
                        />


                        {/* ================================
                            Finding Detail
                        ================================= */}
                        <Route
                            path="/findings/:findingId"
                            element={<FindingDetail />}
                        />


                        {/* ================================
                            Analysis File List
                        ================================= */}
                        <Route
                            path="/analysis/:analysisId/files"
                            element={<AnalysisFileList />}
                        />


                        {/* ================================
                            Analysis File Detail
                        ================================= */}
                        <Route
                            path="/analysis/:analysisId/files/:fileId"
                            element={<AnalysisFileDetail />}
                        />

                    </Routes>

                </main>

            </div>

        </Router>
    );
}


const styles = {
    app: {
        display: 'flex',
        minHeight: '100vh',
    },

    main: {
        flex: 1,
        padding: '10px',
        backgroundColor: '#f1f5f9',
        overflowY: 'auto',
        boxSizing: 'border-box',
    },
};


export default App;
