import axios from 'axios';

// Axios 인스턴스 생성
const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 5000, // 5초 초과 시 타임아웃
    headers: {
        'Content-Type': 'application/json',
    },
});

/* ========================================================
   1. Request Interceptor (요청 전 처리)
   ======================================================== */
api.interceptors.request.use(
    (config) => {
        // 로컬 스토리지에 토큰이 있다면 요청 헤더에 자동으로 Bearer 토큰 추가
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/* ========================================================
   2. Response Interceptor (응답 후 에러 공통 감지)
   ======================================================== */
api.interceptors.response.use(
    // [성공 2xx]: 정상 응답은 컴포넌트로 그대로 전달
    (response) => {
        return response;
    },
    // [에러 처리]: 백엔드 에러 및 네트워크 예외 감지
    (error) => {
        if (error.response) {
            // 백엔드가 응답을 반환했으나 2xx 범위가 아닌 에러 코드인 경우
            const { status, data } = error.response;
            
            // Spring Boot ErrorResponse / ExceptionHandler 형태에 맞게 메시지 추출
            const serverMessage = data?.message || data?.error || '처리 중 오류가 발생했습니다.';

            switch (status) {
                case 400:
                    alert(`[잘못된 요청] ${serverMessage}`);
                    break;

                case 401:
                    alert('로그인 세션이 만료되었거나 인증되지 않았습니다. 다시 로그인해 주세요.');
                    localStorage.removeItem('accessToken');
                    // 페이지 강제 이동 (필요 시 주석 해제)
                    // window.location.href = '/'; 
                    break;

                case 403:
                    alert('해당 기능에 대한 접근 권한이 없습니다.');
                    break;

                case 404:
                    alert(`[요청 경로 없음] ${serverMessage}`);
                    break;

                case 500:
                    alert('서버 내부 장애가 발생했습니다. 관리자에게 문의해 주세요.');
                    break;

                default:
                    alert(`[오류 ${status}] ${serverMessage}`);
                    break;
            }
        } else if (error.request) {
            // 요청을 보냈으나 서버로부터 아무런 응답을 받지 못한 경우 (서버 꺼짐, CORS, 네트워크 끊김)
            alert('백엔드 서버(8080 포트)와 통신할 수 없습니다. 서버 구동 상태나 네트워크 연결을 확인하세요.');
        } else {
            // 요청 설정 도중 에러가 발생한 경우
            alert(`클라이언트 요청 생성 오류: ${error.message}`);
        }

        // 개별 컴포넌트의 try-catch 문에서도 개별 에러 처리를 할 수 있도록 에러 전파
        return Promise.reject(error);
    }
);

export default api;