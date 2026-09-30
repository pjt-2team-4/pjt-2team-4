import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Sidebar from './components/Sidebar';
import AnalysisLayout from './components/AnalysisLayout';

import MainDashboard from './pages/dashboard/MainDashboard';

import SignUp from './pages/user/SignUp';
import Login from './pages/user/Login';
import UserAdmin from './pages/user/UserAdmin';

import AnalysisCreate from './pages/analysis/AnalysisCreate';
import AnalysisDetail from './pages/analysis/AnalysisDetail';
import AnalysisList from './pages/analysis/AnalysisList';
import AnalysisLoading from './pages/analysis/AnalysisLoading';

function App() {
    return (
        <Router>
            <div style={styles.app}>
                <Sidebar />

                <main style={styles.main}>
                    <Routes>
                        {/* Dashboard */}
                        <Route path="/" element={<MainDashboard />} />

                        {/* User */}
                        <Route path="/signup" element={<SignUp />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/admin/users" element={<UserAdmin />} />

                        {/* Analysis */}
                        <Route path="/analysis/new" element={<AnalysisCreate />} />
                        <Route path="/analysis/list" element={<AnalysisList />} />

                        {/* Analysis Detail */}
                        <Route path="/analysis" element={<AnalysisLayout />}>
                            <Route path=":id" element={<AnalysisDetail />} />
                        </Route>

                        {/* Analysis Loading */}
                        <Route
                            path="/analysis/:id/loading"
                            element={<AnalysisLoading />}
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
    },
};

export default App;
