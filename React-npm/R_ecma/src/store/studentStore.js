/* ---------------------------------------------------------
   학생 store — 페이지들이 함께 보는 값과 그 값을 바꾸는 함수
   6부까지는 이 값들이 StudentListPage 안의 useState 에 있었습니다.
   그래서 두 가지가 불편했습니다.

     (1) 목록 페이지를 떠났다 돌아오면 값이 사라져 서버를 다시 불렀다
     (2) 등록 성공 문구를 페이지 밖으로 나르려고
         navigate("/", { state: { message } }) 를 써야 했다

   store 는 컴포넌트 트리 밖에 있습니다. 페이지가 바뀌어도 살아 있고,
   어느 컴포넌트든 직접 꺼내 쓸 수 있습니다. 위 두 가지가 사라집니다.
   --------------------------------------------------------- */

import { create } from "zustand";

import {
    fetchStudents,
    createStudent,
    updateStudent,
    deleteStudent,
} from "../api/studentApi.js";

/* create 에 넘긴 함수가 store 의 내용을 돌려줍니다.
     set  값을 바꾼다. useState 의 setter 와 성격이 같다.
     get  지금 값을 읽는다. 함수 안에서 다른 값이 필요할 때 쓴다.

   set 은 넘긴 것을 기존 값과 합쳐 준다. useState 의 setter 가 통째로
   바꾸는 것과 다르므로 ...state 를 펼쳐 넣지 않아도 된다. */
export const useStudentStore = create((set, get) => ({
    // ── 값(상태변수)을 변경하려면 인자로 받은 set() 함수를 호출해야 함 
    students: [],
    loading: false,
    listError: null,
    message: null,          // { text, type } 또는 null
    loaded: false,          // 한 번이라도 목록을 불러왔는가

    // ── 아래에 정의된 변경된 값(상태변수) ──────────────────────────────────────────
    showError: (text) => set({ message: { text: text, type: "error" } }),
    showSuccess: (text) => set({ message: { text: text, type: "success" } }),
    clearMessage: () => set({ message: null }),

    // ── 목록 불러오기 ───────────────────────────────────
    loadStudents: async () => {
        set({ loading: true, listError: null });

        try {
            const data = await fetchStudents();
            set({ students: data, loaded: true });
        } catch (error) {
            console.error("Error:", error);
            set({
                message: { text: error.message, type: "error" },
                listError: "오류: 데이터를 불러올 수 없습니다.",
            });
        } finally {
            set({ loading: false });
        }
    },

    /* 등록과 수정을 한 함수로 받는다. id 가 있으면 수정이다.
       성공하면 true, 실패하면 false 를 돌려준다.
       페이지는 그 값을 보고 목록으로 옮겨 갈지 정한다. */
    saveStudent: async (id, studentData) => {
        try {
            if (id) {
                await updateStudent(id, studentData);
                get().showSuccess("학생 정보가 성공적으로 수정되었습니다.");
            } else {
                await createStudent(studentData);
                get().showSuccess("학생이 성공적으로 등록되었습니다.");
            }

            // 서버 값이 바뀌었으므로 목록을 다시 불러 둔다.
            await get().loadStudents();
            return true;
        } catch (error) {
            console.error("Error:", error);
            get().showError(error.message);
            return false;
        }
    },

    // ── 삭제 ────────────────────────────────────────────
    removeStudent: async (id) => {
        try {
            await deleteStudent(id);
            get().showSuccess("학생이 성공적으로 삭제되었습니다.");
            await get().loadStudents();
        } catch (error) {
            console.error("Error:", error);
            get().showError(error.message);
        }
    },
}));