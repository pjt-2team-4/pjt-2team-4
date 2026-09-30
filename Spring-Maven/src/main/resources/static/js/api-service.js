// js/api-service.js

const ApiService = {

    async request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
            }
        });

        const text = await response.text();
        
        // 응답 텍스트를 JSON으로 안전하게 파싱 시도
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (e) {
            data = { message: text };
        }

        if (!response.ok) {
            // 💡 핵심: 백엔드가 내려준 에러 JSON(code, message 등)을 에러 객체에 실어줍니다.
            const error = new Error(data.message || `HTTP Error: ${response.status}`);
            
            // Axios 스타일의 error.response 구조를 모방하여 주입
            error.response = {
                status: response.status,
                statusText: response.statusText,
                data: data // 백엔드 GlobalExceptionHandler가 보낸 ErrorCode, message 등이 여기에 담김!
            };

            // 커스텀 데이터 속성도 함께 제공
            error.data = data;

            throw error;
        }

        return data;
    },

    async get(url) {
        return this.request(url);
    },

    async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    async delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }
};