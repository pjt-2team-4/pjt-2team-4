# REST API 설계서

## 문서 정보

| 항목 | 내용 |
|------|------|
| **프로젝트명** | 온라인 도서관 시스템 (Library Management System) |
| **작성자** | 홍길동, 김영희, 이철수 |
| **작성일** | 2025-05-26 |
| **버전** | v1.0 |
| **Base URL** | `https://localhost:8080/api` |

---

## 1. API 설계 개요

### 1.1 설계 목적

RESTful 원칙에 따라 클라이언트(React)와 서버(Spring Boot) 간의 통신 규격을 정의하여
일관되고 확장 가능한 API를 제공한다.

### 1.2 설계 원칙

- **RESTful**: HTTP 메서드와 상태 코드의 올바른 사용
- **일관성**: 모든 API에서 동일한 응답 구조 사용
- **보안**: JWT 기반 인증, `/api/auth/**` 제외 보호
- **성능**: 페이지네이션 (기본 size=10)

### 1.3 기술 스택

| 항목 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 3.4.6 |
| 인증 | JWT (액세스 토큰 1시간, 리프레시 토큰 7일) |
| 직렬화 | JSON |
| API 문서 | OpenAPI 3.1.0 (Swagger UI: `/swagger-ui.html`) |

---

## 2. API 공통 규칙

### 2.1 URL 설계 규칙

| 규칙 | 좋은 예 | 나쁜 예 |
|------|---------|---------|
| 명사 사용 | `GET /api/books` | `GET /api/getBooks` |
| 복수형 사용 | `/api/books`, `/api/loans` | `/api/book`, `/api/loan` |
| 계층 구조 | `/api/members/123/loans` | `/api/getMemberLoans` |
| 소문자+하이픈 | `/api/book-categories` | `/api/BookCategories` |
| HTTP 메서드로 동작 표현 | `POST /api/loans` | `/api/createLoan` |

### 2.2 HTTP 메서드 사용 규칙

| 메서드 | 용도 | 멱등성 | 예시 |
|--------|------|--------|------|
| `GET` | 리소스 조회 | ✅ | `GET /api/books` |
| `POST` | 리소스 생성 | ❌ | `POST /api/loans` |
| `PUT` | 리소스 전체 수정 | ✅ | `PUT /api/books/1` |
| `PATCH` | 리소스 부분 수정 | ❌ | `PATCH /api/loans/1/return` |
| `DELETE` | 리소스 삭제 | ✅ | `DELETE /api/books/1` |

### 2.3 공통 응답 구조

#### 성공 응답 (단일 객체)
```json
{
  "success": true,
  "data": { },
  "message": "요청이 성공적으로 처리되었습니다",
  "timestamp": "2025-05-26T10:30:00Z"
}
```

#### 성공 응답 (목록/페이지네이션)
```json
{
  "success": true,
  "data": {
    "content": [ ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 98,
      "totalPages": 10
    }
  }
}
```

#### 에러 응답
```json
{
  "success": false,
  "error": {
    "code": "BOOK_NOT_FOUND",
    "message": "해당 도서를 찾을 수 없습니다",
    "timestamp": "2025-05-26T10:30:00Z"
  }
}
```

### 2.4 HTTP 상태 코드

| 코드 | 상태 | 사용 예시 |
|------|------|-----------|
| `200` | OK | GET/PUT/PATCH 성공 |
| `201` | Created | POST 성공 (도서 등록, 대출 신청) |
| `204` | No Content | DELETE 성공 |
| `400` | Bad Request | 유효성 검사 실패 |
| `401` | Unauthorized | 토큰 없음 또는 만료 |
| `403` | Forbidden | 권한 없음 (ADMIN 전용 API) |
| `404` | Not Found | 존재하지 않는 리소스 |
| `409` | Conflict | 중복 대출 신청 |
| `422` | Unprocessable Entity | 비즈니스 규칙 위반 (재고 없음, 대출 한도 초과) |
| `500` | Internal Server Error | 서버 오류 |

### 2.5 공통 요청 헤더

```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {JWT_ACCESS_TOKEN}   # 인증 필요한 API에만
```

---

## 3. 인증 및 권한 관리

### 3.1 권한 레벨

| 역할 | 접근 가능 API | 설명 |
|------|--------------|------|
| **공개(미인증)** | `GET /api/books`, `GET /api/books/:id`, `/api/auth/**` | 도서 조회만 허용 |
| **MEMBER** | 공개 + 내 대출이력, 대출 신청, 반납 신청, 내 정보 | 로그인 회원 |
| **ADMIN** | 전체 + 관리자 전용 | 도서 등록/수정/삭제, 대출 승인 |

### 3.2 JWT 인증 흐름

```
[클라이언트]  POST /api/auth/login  →  [서버] 검증
                                    ←  accessToken (1시간)
                                    ←  refreshToken (7일)

[클라이언트]  Authorization: Bearer {accessToken}  →  [서버] 인증
             (토큰 만료 시) POST /api/auth/refresh  →  [서버] 새 토큰 발급
```

---

## 4. 상세 API 명세

---

### 4.1 인증 API (Auth)

#### 4.1.1 회원 가입

```
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "password123!",
  "phoneNumber": "010-1234-5678"
}
```

**Response 201 Created:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "memberNumber": "M000000001",
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "message": "회원가입이 완료되었습니다"
}
```

**Response 409 Conflict:**
```json
{
  "success": false,
  "error": {
    "code": "DUPLICATE_EMAIL",
    "message": "이미 사용 중인 이메일입니다"
  }
}
```

---

#### 4.1.2 로그인

```
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "hong@example.com",
  "password": "password123!"
}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "name": "홍길동",
      "email": "hong@example.com",
      "memberNumber": "M000000001",
      "role": "MEMBER"
    }
  }
}
```

**Response 401 Unauthorized:**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다"
  }
}
```

---

#### 4.1.3 토큰 갱신

```
POST /api/auth/refresh
Content-Type: application/json
```

**Request Body:**
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

---

### 4.2 도서 API (Books)

#### 4.2.1 도서 목록 조회 (공개)

```
GET /api/books?keyword=클린코드&category=IT&page=0&size=10
```

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `keyword` | string | 선택 | "" | 도서명 또는 저자명 검색 |
| `category` | string | 선택 | "" | 카테고리 필터 (소설/IT/역사/과학/기타) |
| `page` | number | 선택 | 0 | 페이지 번호 (0-index) |
| `size` | number | 선택 | 10 | 페이지당 항목 수 |

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "isbn": "9788966261031",
        "title": "클린 코드",
        "author": "로버트 C. 마틴",
        "publisher": "인사이트",
        "category": "IT",
        "totalCopies": 3,
        "availableCopies": 2,
        "imageUrl": "https://example.com/images/clean-code.jpg"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 98,
      "totalPages": 10
    }
  }
}
```

---

#### 4.2.2 도서 상세 조회 (공개)

```
GET /api/books/{id}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "isbn": "9788966261031",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "publicationDate": "2013-12-24",
    "category": "IT",
    "totalCopies": 3,
    "availableCopies": 2,
    "description": "좋은 코드를 작성하는 방법을 알려주는 프로그래밍 바이블",
    "imageUrl": "https://example.com/images/clean-code.jpg"
  }
}
```

**Response 404 Not Found:**
```json
{
  "success": false,
  "error": { "code": "BOOK_NOT_FOUND", "message": "해당 도서를 찾을 수 없습니다" }
}
```

---

#### 4.2.3 도서 등록 (ADMIN 전용)

```
POST /api/books
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "isbn": "9788966261031",
  "title": "클린 코드",
  "author": "로버트 C. 마틴",
  "publisher": "인사이트",
  "publicationDate": "2013-12-24",
  "categoryId": 2,
  "totalCopies": 3,
  "description": "좋은 코드를 작성하는 방법을 알려주는 프로그래밍 바이블"
}
```

**Response 201 Created:**
```json
{
  "success": true,
  "data": { "id": 1, "isbn": "9788966261031", "title": "클린 코드" },
  "message": "도서가 등록되었습니다"
}
```

---

#### 4.2.4 도서 수정 (ADMIN 전용)

```
PUT /api/books/{id}
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Request Body:** (등록과 동일, isbn 제외)

**Response 200 OK:**
```json
{
  "success": true,
  "data": { "id": 1, "title": "클린 코드", "totalCopies": 5 }
}
```

---

#### 4.2.5 도서 삭제 (ADMIN 전용)

```
DELETE /api/books/{id}
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Response 204 No Content**

**Response 409 Conflict:**
```json
{
  "success": false,
  "error": {
    "code": "BOOK_IN_USE",
    "message": "현재 대출 중인 도서는 삭제할 수 없습니다"
  }
}
```

---

### 4.3 대출 API (Loans)

#### 4.3.1 대출 신청 (로그인 필요)

```
POST /api/loans
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{ "bookId": 1 }
```

**Response 201 Created:**
```json
{
  "success": true,
  "data": {
    "loanId": 42,
    "bookTitle": "클린 코드",
    "loanDate": "2025-05-26",
    "dueDate": "2025-06-09",
    "status": "REQUESTED"
  },
  "message": "대출 신청이 완료되었습니다. 관리자 승인 후 대출이 시작됩니다."
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "success": false,
  "error": {
    "code": "BOOK_UNAVAILABLE",
    "message": "현재 대출 가능한 도서가 없습니다"
  }
}
```

**Response 422 — 대출 한도 초과:**
```json
{
  "success": false,
  "error": {
    "code": "LOAN_LIMIT_EXCEEDED",
    "message": "최대 대출 가능 권수(5권)를 초과했습니다"
  }
}
```

---

#### 4.3.2 내 대출이력 조회 (로그인 필요)

```
GET /api/loans/my?status=BORROWED&page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `status` | string | 선택 | 상태 필터 (BORROWED/RETURNED/OVERDUE/REQUESTED) |
| `page` | number | 선택 | 페이지 번호 |
| `size` | number | 선택 | 페이지당 항목 수 |

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "loanId": 42,
        "bookId": 1,
        "bookTitle": "클린 코드",
        "author": "로버트 C. 마틴",
        "loanDate": "2025-05-26",
        "dueDate": "2025-06-09",
        "returnDate": null,
        "status": "BORROWED",
        "overdueDays": 0,
        "overdueFee": 0
      }
    ],
    "page": { "number": 0, "size": 10, "totalElements": 3, "totalPages": 1 }
  }
}
```

---

#### 4.3.3 반납 신청 (로그인 필요)

```
PATCH /api/loans/{loanId}/return
Authorization: Bearer {JWT_TOKEN}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "loanId": 42,
    "bookTitle": "클린 코드",
    "returnDate": "2025-06-05",
    "status": "RETURNED"
  },
  "message": "반납 신청이 완료되었습니다"
}
```

---

#### 4.3.4 전체 대출 목록 조회 (ADMIN 전용)

```
GET /api/admin/loans?status=BORROWED&memberId=&page=0&size=20
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "loanId": 42,
        "memberNumber": "M000000001",
        "memberName": "홍길동",
        "bookTitle": "클린 코드",
        "loanDate": "2025-05-26",
        "dueDate": "2025-06-09",
        "status": "BORROWED",
        "overdueDays": 0
      }
    ],
    "page": { "number": 0, "size": 20, "totalElements": 150, "totalPages": 8 }
  }
}
```

---

#### 4.3.5 대출 승인 (ADMIN 전용)

```
PATCH /api/admin/loans/{loanId}/approve
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "loanId": 42,
    "status": "BORROWED",
    "dueDate": "2025-06-09"
  },
  "message": "대출이 승인되었습니다"
}
```

---

### 4.4 회원 API (Members)

#### 4.4.1 내 정보 조회 (로그인 필요)

```
GET /api/members/me
Authorization: Bearer {JWT_TOKEN}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "memberNumber": "M000000001",
    "name": "홍길동",
    "email": "hong@example.com",
    "phoneNumber": "010-1234-5678",
    "status": "ACTIVE",
    "maxLoanCount": 5,
    "currentLoanCount": 2
  }
}
```

---

#### 4.4.2 내 정보 수정 (로그인 필요)

```
PUT /api/members/me
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "홍길동",
  "phoneNumber": "010-9876-5432"
}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "phoneNumber": "010-9876-5432"
  },
  "message": "정보가 수정되었습니다"
}
```

---

#### 4.4.3 회원 목록 조회 (ADMIN 전용)

```
GET /api/admin/members?keyword=홍길동&status=ACTIVE&page=0&size=20
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "memberNumber": "M000000001",
        "name": "홍길동",
        "email": "hong@example.com",
        "status": "ACTIVE",
        "currentLoanCount": 2,
        "maxLoanCount": 5
      }
    ],
    "page": { "number": 0, "size": 20, "totalElements": 350, "totalPages": 18 }
  }
}
```

---

### 4.5 카테고리 API (Categories)

#### 4.5.1 카테고리 목록 조회 (공개)

```
GET /api/categories
```

**Response 200 OK:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "소설" },
    { "id": 2, "name": "IT" },
    { "id": 3, "name": "역사" },
    { "id": 4, "name": "과학" },
    { "id": 5, "name": "기타" }
  ]
}
```

---

## 5. API 전체 목록 요약

| 메서드 | 엔드포인트 | 설명 | 인증 |
|--------|-----------|------|------|
| `POST` | `/api/auth/register` | 회원가입 | 불필요 |
| `POST` | `/api/auth/login` | 로그인 | 불필요 |
| `POST` | `/api/auth/refresh` | 토큰 갱신 | 불필요 |
| `GET` | `/api/books` | 도서 목록 조회 | 불필요 |
| `GET` | `/api/books/:id` | 도서 상세 조회 | 불필요 |
| `POST` | `/api/books` | 도서 등록 | ADMIN |
| `PUT` | `/api/books/:id` | 도서 수정 | ADMIN |
| `DELETE` | `/api/books/:id` | 도서 삭제 | ADMIN |
| `GET` | `/api/categories` | 카테고리 목록 | 불필요 |
| `POST` | `/api/loans` | 대출 신청 | MEMBER |
| `GET` | `/api/loans/my` | 내 대출이력 | MEMBER |
| `PATCH` | `/api/loans/:id/return` | 반납 신청 | MEMBER |
| `GET` | `/api/admin/loans` | 전체 대출 목록 | ADMIN |
| `PATCH` | `/api/admin/loans/:id/approve` | 대출 승인 | ADMIN |
| `GET` | `/api/members/me` | 내 정보 조회 | MEMBER |
| `PUT` | `/api/members/me` | 내 정보 수정 | MEMBER |
| `GET` | `/api/admin/members` | 회원 목록 조회 | ADMIN |
| `PATCH` | `/api/admin/members/:id/status` | 회원 상태 변경 | ADMIN |

---

## 6. 작성 체크리스트

- [x] 인증 API (회원가입, 로그인, 토큰 갱신) 정의
- [x] 도서 API (CRUD + 공개 조회) 정의
- [x] 대출 API (신청, 조회, 반납, 관리자 승인) 정의
- [x] 회원 API (내 정보, 관리자 회원 관리) 정의
- [x] 카테고리 API 정의
- [x] Request/Response 예시 포함
- [x] 에러 응답 코드 및 메시지 정의
- [x] 권한 레벨 (공개/MEMBER/ADMIN) 명확히 표시