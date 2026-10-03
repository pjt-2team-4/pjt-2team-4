import axios from 'axios';

import {
    getAuthorizationHeader,
    clearAuth
} from '../auth/token';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 10000,

    headers: {
        'Content-Type': 'application/json',
    },
});


/*
|--------------------------------------------------------------------------
| Request Interceptor
|--------------------------------------------------------------------------
*/

api.interceptors.request.use(
    (config) => {

        const authorization =
            getAuthorizationHeader();

        if (authorization) {

            config.headers =
                config.headers || {};

            config.headers.Authorization =
                authorization;
        }

        /*
         * 개발 중 Authorization 확인용
         */
        console.log(
            '[API REQUEST]',
            config.method?.toUpperCase(),
            config.url,
            authorization
                ? 'Authorization 있음'
                : 'Authorization 없음'
        );

        return config;
    },

    (error) => {
        return Promise.reject(error);
    }
);


/*
|--------------------------------------------------------------------------
| Response Interceptor
|--------------------------------------------------------------------------
*/

api.interceptors.response.use(

    (response) => {

        console.log(
            '[API RESPONSE]',
            response.status,
            response.config.url
        );

        return response;
    },

    (error) => {

        if (!error.response) {

            if (error.request) {

                alert(
                    '백엔드 서버(8080 포트)와 통신할 수 없습니다.'
                );

            } else {

                alert(
                    `요청 생성 오류: ${error.message}`
                );
            }

            return Promise.reject(error);
        }


        const {
            status,
            data
        } = error.response;


        console.error(
            '[API ERROR]',
            {
                status,
                url: error.config?.url,
                method: error.config?.method,
                response: data,
            }
        );


        const serverMessage =
            data?.message ||
            data?.error ||
            '처리 중 오류가 발생했습니다.';


        /*
        |--------------------------------------------------------------------------
        | 400
        |--------------------------------------------------------------------------
        */

        if (status === 400) {

            alert(
                `[잘못된 요청]\n${serverMessage}`
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 401
        |--------------------------------------------------------------------------
        */

        else if (status === 401) {

            clearAuth();

            window.dispatchEvent(
                new Event('authChanged')
            );

            alert(
                '로그인이 필요하거나 로그인 세션이 만료되었습니다.'
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 403
        |--------------------------------------------------------------------------
        */

        else if (status === 403) {

            console.error(
                '[403 FORBIDDEN]',
                {
                    url: error.config?.url,
                    method: error.config?.method,
                    response: data,
                }
            );

            alert(
                `접근 권한이 없습니다.\n\n${serverMessage}`
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 404
        |--------------------------------------------------------------------------
        */

        else if (status === 404) {

            alert(
                `[요청 경로 없음]\n${serverMessage}`
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 409
        |--------------------------------------------------------------------------
        */

        else if (status === 409) {

            alert(
                `[충돌]\n${serverMessage}`
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 422
        |--------------------------------------------------------------------------
        */

        else if (status === 422) {

            alert(
                `[처리할 수 없는 요청]\n${serverMessage}`
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 500
        |--------------------------------------------------------------------------
        */

        else if (status >= 500) {

            alert(
                '서버 내부 장애가 발생했습니다.'
            );
        }


        /*
        |--------------------------------------------------------------------------
        | 기타
        |--------------------------------------------------------------------------
        */

        else {

            alert(
                `[오류 ${status}]\n${serverMessage}`
            );
        }


        return Promise.reject(error);
    }
);


export default api;
