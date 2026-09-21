/* ---------------------------------------------------------
   학생 API — 서버와 대화하는 부분만 모아 둔다
   하는 일은 5부와 같습니다. 달라진 것은 fetch 대신 axios 를
   쓴다는 것뿐이고, 그래서 함수마다 두 줄이면 끝납니다.

   오류 처리는 client.js 의 가로채기가 맡습니다.
   여기에는 try / catch 가 한 줄도 없습니다.
   화면에 무엇을 보여줄지는 페이지 컴포넌트가 정합니다.
   --------------------------------------------------------- */

import { client } from "./client.js";
import { STUDENTS_PATH } from "../config.js";

/* axios 는 서버가 준 본문을 response.data 에 담아 준다.
   5부에서 await response.json() 을 부르던 일이 없어졌다. */
export const fetchStudents = async () => {
    console.log('API fetchStudents');
    const response = await client.get(STUDENTS_PATH);
    return response.data;
};

export const fetchStudent = async (id) => {
    const response = await client.get(`${STUDENTS_PATH}/${id}`);
    return response.data;
};

// 두 번째 인자가 본문이다. JSON.stringify 를 부를 필요가 없다.
export const createStudent = async (student) => {
    const response = await client.post(STUDENTS_PATH, student);
    return response.data;
};

export const updateStudent = async (id, student) => {
    const response = await client.put(`${STUDENTS_PATH}/${id}`, student);
    return response.data;
};

// 삭제는 서버가 204 를 주고 본문이 없다. 돌려줄 것이 없으므로 null 이다.
export const deleteStudent = async (id) => {
    await client.delete(`${STUDENTS_PATH}/${id}`);
    return null;
};