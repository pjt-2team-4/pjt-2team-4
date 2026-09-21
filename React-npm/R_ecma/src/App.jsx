/* ---------------------------------------------------------
   App.jsx — 주소에 따라 어느 페이지를 보여 줄지 정하는 곳
   5부에서는 App 이 상태를 모두 갖고 화면도 직접 그렸습니다.
   6부에서는 그 일이 페이지 컴포넌트로 내려가고, App 은
   "주소 → 페이지" 를 이어 주는 일만 합니다.

     주소            보여 줄 페이지
     ------------    ----------------------------
     /               학생 목록
     /new            학생 등록 폼
     /edit/3         3번 학생 수정 폼
     그 밖의 주소     없는 주소 안내

   5부의 editingId state 가 사라진 것에 주목하세요.
   "지금 몇 번 학생을 수정 중인가" 를 주소가 알고 있기 때문입니다.
   --------------------------------------------------------- */

import { Link, NavLink, Route, Routes } from "react-router-dom";

import StudentListPage from "./pages/StudentListPage.jsx";
import StudentFormPage from "./pages/StudentFormPage.jsx";
import NotFoundPage from "./pages/NotFoundPage.jsx";

import AppMessage from "./components/AppMessage.jsx";

// 지금 어느 모드로 도는지 (TEST / PROD)
import { APP_MODE } from "./config.js";

import "./style.css";

function App() {

    // 제목 옆에 붙일 배지의 class. 운영이면 빨강, 아니면 회색.
    let modeClass = "app-mode test";
    if (APP_MODE === "PROD") {
        modeClass = "app-mode prod";
    }

    return (
        <>
            {/* 어느 페이지에서나 보이는 머리말. Routes 바깥에 있어서 바뀌지 않는다. */}
            <header className="app-header">
                {/* Link 는 <a> 처럼 보이지만 페이지를 새로 내려받지 않는다.
                    주소만 바꾸고 React 가 화면을 갈아 끼운다. */}
                <div className="app-brand">
                    <Link to="/" className="app-title">학생 관리 시스템</Link>
                    <span className={modeClass}>{APP_MODE}</span>
                </div>

                <nav className="app-nav">
                    {/* NavLink 는 Link 와 같지만, 지금 보고 있는 주소와 맞으면
                        className 에 isActive 가 true 로 들어온다.
                        그래서 "지금 여기 있다" 를 표시할 수 있다. */}
                    <NavLink
                        to="/"
                        end
                        className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
                    >
                        학생 목록
                    </NavLink>

                    <NavLink
                        to="/new"
                        className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
                    >
                        학생 등록
                    </NavLink>
                </nav>
            </header>

            {/* store 의 메시지를 그리는 자리. 어느 페이지에서든 여기 뜬다. */}            
            <AppMessage />

            {/* Routes 안에서 주소와 맞는 Route 하나만 그려진다. */}
            <Routes>
                <Route path="/" element={<StudentListPage />} />
                <Route path="/new" element={<StudentFormPage />} />

                {/* :id 는 자리를 비워 둔다는 뜻이다. /edit/3 이면 id 가 "3" 이 된다. */}
                <Route path="/edit/:id" element={<StudentFormPage />} />

                {/* * 는 위 어느 것과도 맞지 않는 주소다. */}
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </>
    );
}

export default App;