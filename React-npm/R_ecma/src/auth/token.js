// src/auth/token.js

const ACCESS_TOKEN_KEY = 'accessToken';
const TOKEN_TYPE_KEY = 'tokenType';
const USER_KEY = 'user';

/**
 * Access Token 저장
 */
export const setAccessToken = (accessToken) => {
    if (!accessToken) {
        throw new Error('Access Token이 없습니다.');
    }

    localStorage.setItem(
        ACCESS_TOKEN_KEY,
        accessToken
    );
};


/**
 * Access Token 조회
 */
export const getAccessToken = () => {
    return localStorage.getItem(
        ACCESS_TOKEN_KEY
    );
};


/**
 * Token Type 저장
 */
export const setTokenType = (tokenType) => {
    localStorage.setItem(
        TOKEN_TYPE_KEY,
        tokenType || 'Bearer'
    );
};


/**
 * Token Type 조회
 */
export const getTokenType = () => {
    return localStorage.getItem(
        TOKEN_TYPE_KEY
    ) || 'Bearer';
};


/**
 * 로그인 사용자 정보 저장
 */
export const setUser = (user) => {
    localStorage.setItem(
        USER_KEY,
        JSON.stringify(user)
    );
};


/**
 * 로그인 사용자 정보 조회
 */
export const getUser = () => {

    const user = localStorage.getItem(
        USER_KEY
    );

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


/**
 * 로그인 정보 전체 저장
 */
export const saveAuth = ({
    accessToken,
    tokenType = 'Bearer',
    email
}) => {

    setAccessToken(accessToken);

    setTokenType(tokenType);

    setUser({
        email
    });
};


/**
 * Authorization Header 생성
 */
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


/**
 * 로그아웃
 */
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


/**
 * 로그인 여부
 */
export const isAuthenticated = () => {
    return !!getAccessToken();
};
