/* ---------------------------------------------------------
   학생 목록 페이지 — 주소 "/"
   6부에서는 이 페이지가 students · loading · listError · message 를
   직접 갖고 있었습니다. 그 넷이 모두 store 로 옮겨갔습니다.

   그래서 이 파일에 useState 가 한 줄도 없습니다. 하는 일은 둘뿐입니다.
     (1) 아직 안 불러왔으면 목록을 불러오라고 시킨다
     (2) store 에서 꺼낸 값을 표에 넘긴다
   --------------------------------------------------------- */

import { useEffect } from "react";

import { useStudentStore } from "../store/studentStore.js";
import StudentTable from "../components/StudentTable.jsx";

function StudentListPage() {
    /* 필요한 것만 하나씩 꺼낸다. store 전체를 꺼내면
       상관없는 값이 바뀔 때도 이 페이지가 다시 그려진다. */
    const students = useStudentStore((s) => s.students);
    const loading = useStudentStore((s) => s.loading);
    const listError = useStudentStore((s) => s.listError);
    const loaded = useStudentStore((s) => s.loaded);

    const loadStudents = useStudentStore((s) => s.loadStudents);
    const removeStudent = useStudentStore((s) => s.removeStudent);

    /* 한 번도 안 불러왔을 때만 부른다.
       6부에서는 이 페이지로 돌아올 때마다 서버를 다시 불렀습니다.
       store 는 페이지가 바뀌어도 살아 있으므로 그럴 필요가 없습니다. */
    useEffect(() => {
        if (!loaded) {
            loadStudents();
        }
    }, [loaded, loadStudents]);

    async function handleDelete(studentId) {
        if (!confirm("정말로 이 학생을 삭제하시겠습니까?")) {
            return;
        }

        // 지우고 목록을 다시 불러오는 일은 store 가 맡는다.
        await removeStudent(studentId);
    }

    return (
        <div className="page">
            <StudentTable
                students={students}
                loading={loading}
                error={listError}
                onDelete={handleDelete}
            />
        </div>
    );
}

export default StudentListPage;