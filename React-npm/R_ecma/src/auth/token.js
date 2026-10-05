// src/auth/token.js

const ACCESS_TOKEN_KEY = 'accessToken';
const TOKEN_TYPE_KEY = 'tokenType';
const USER_KEY = 'user';


export const setAccessToken = (accessToken) => {

    if (!accessToken) {
        throw new Error(
            'Access Token이 없습니다.'
        );
    }

    localStorage.setItem(
        ACCESS_TOKEN_KEY,
        accessToken
    );
};


export const getAccessToken = () => {

    return localStorage.getItem(
        ACCESS_TOKEN_KEY
    );
};


export const setTokenType = (tokenType) => {

    localStorage.setItem(
        TOKEN_TYPE_KEY,
        tokenType || 'Bearer'
    );
};


export const getTokenType = () => {

    return localStorage.getItem(
        TOKEN_TYPE_KEY
    ) || 'Bearer';
};


export const setUser = (user) => {

    localStorage.setItem(
        USER_KEY,
        JSON.stringify(user)
    );
};


export const getUser = () => {

    const user =
        localStorage.getItem(USER_KEY);

    if (!user) {
        return null;
    }

    try {

        return JSON.parse(user);

    } catch (error) {

        console.error(
            '[TOKEN] 사용자 정보 파싱 실패:',
            error
        );

        return null;
    }
};


export const saveAuth = ({
    accessToken,
    tokenType = 'Bearer',
    email,
    role,
    roles
}) => {

    if (!accessToken) {

        console.error(
            '[TOKEN] accessToken 없음'
        );

        throw new Error(
            '로그인 응답에 accessToken이 없습니다.'
        );
    }


    setAccessToken(
        accessToken
    );


    setTokenType(
        tokenType
    );


    setUser({
        email,
        role,
        roles,
    });
};


export const getAuthorizationHeader = () => {

    const accessToken =
        getAccessToken();

    if (!accessToken) {
        return null;
    }


    const tokenType =
        getTokenType();


    return `${tokenType} ${accessToken}`;
};


export const clearAuth = () => {

    localStorage.removeItem(
        ACCESS_TOKEN_KEY
    );

    localStorage.removeItem(
        TOKEN_TYPE_KEY
    );

    localStorage.removeItem(
        USER_KEY
    );
};


export const isAuthenticated = () => {

    return !!getAccessToken();
};
