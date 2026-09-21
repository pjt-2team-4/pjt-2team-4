/* ---------------------------------------------------------
   학생 등록 · 수정 페이지 — 주소 "/new" 와 "/edit/:id"
   한 컴포넌트가 두 주소를 맡습니다. 등록인지 수정인지는
   주소에 id 가 붙어 있는지로 가릅니다.

     /new       →  id 가 없다   →  등록 모드
     /edit/3    →  id 가 "3"   →  수정 모드

   5부에서 useState 로 들고 있던 editingId 가 사라졌습니다.
   그 값이 주소로 옮겨갔기 때문입니다. 덕분에 수정 화면에서
   새로고침을 해도 수정 모드가 그대로 유지됩니다.
   --------------------------------------------------------- */

import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { createStudent, fetchStudent, updateStudent } from "../api/studentApi.js";
import { validateStudent } from "../lib/validation.js";
import { EMPTY_FORM, toRequest, toFormValues } from "../lib/studentData.js";
import StudentForm from "../components/StudentFormField.jsx";

function StudentFormPage() {
    /* useParams 는 주소의 edit/:id 자리에 있던 값을 돌려준다.
       언제나 문자열이고, /new 처럼 그 자리가 없으면 undefined 다. */
    const { id } = useParams();
    //id 값이 있다면 수정모드
    const isEditing = id !== undefined;

    // useNavigate 는 "다른 주소로 옮겨 가는 함수" 를 돌려준다.
    const navigate = useNavigate();

    const [form, setForm] = useState(EMPTY_FORM);
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    /* 수정 모드면 서버에서 그 학생을 불러와 폼을 채운다.
       의존성 배열에 id 가 있으므로, 주소가 /edit/3 에서 /edit/7 로
       바뀌면 이 효과가 다시 실행된다. */
    useEffect(() => {
        //등록모드이면 처리 안됨
        if (!isEditing) {
            return;
        }

        let cancelled = false;

        async function loadStudent() {
            setLoading(true);
            try {
                const student = await fetchStudent(id);
                // 불러오는 도중에 다른 페이지로 떠났으면 state 를 건드리지 않는다.
                if (!cancelled) {
                    setForm(toFormValues(student));
                }
            } catch (error) {
                console.error("Error:", error);
                if (!cancelled) {
                    setMessage({ text: error.message, type: "error" });
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadStudent();

        // 정리 함수 — 이 페이지를 떠날 때 위 플래그를 올린다.
        return () => {
            cancelled = true;
        };
    }, [id, isEditing]);

    function handleChange(event) {
        // 입력칸 여섯 개가 모두 이 함수 하나를 부른다.
        // 어느 칸인지는 input 에 적어 둔 name 이 알려 준다.
        const name = event.target.name;
        const value = event.target.value;

        const next = { ...form };
        next[name] = value;
        setForm(next);
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setMessage(null);

        const studentData = toRequest(form);

        const errorMessage = validateStudent(studentData);
        if (errorMessage) {
            setMessage({ text: errorMessage, type: "error" });
            return;
        }

        try {
            //수정
            if (isEditing) {
                await updateStudent(id, studentData);
            } else {
                await createStudent(studentData);
            }

            /* 목록으로 돌아가면서 보여 줄 메시지를 함께 실어 보낸다.
               받는 쪽은 StudentListPage 의 useLocation() 이다. */
            const text = isEditing
                ? "학생 정보가 성공적으로 수정되었습니다."
                : "학생이 성공적으로 등록되었습니다.";

            // navigate("/") 목록 페이지로 포워딩 해라    
            navigate("/", { state: { message: text } });
        } catch (error) {
            console.error("Error:", error);
            setMessage({ text: error.message, type: "error" });
        }
    }

    // 취소하면 목록으로 돌아간다.
    function handleCancel() {
        navigate("/");
    }

    return (
        <div className="page">
            {/* 폼만 있는 페이지이므로 목록으로 돌아갈 길을 위쪽에 둔다.
                머리말의 내비게이션과 겹치지만, 보고 있던 자리에서 가까운 편이 낫다. */}
            <Link to="/" className="back-link">&larr; 학생 목록으로</Link>

            {loading && <div className="loading">불러오는 중...</div>}

            <StudentForm
                form={form}
                isEditing={isEditing}
                message={message}
                onChange={handleChange}
                onSubmit={handleSubmit}
                onCancel={handleCancel}
            />
        </div>
    );
}

export default StudentFormPage;