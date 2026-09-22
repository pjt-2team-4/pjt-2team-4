

 # 프로젝트 패키지 Overview

 `sec`는 별도로 다루고, 여기서는 실제 확인한 **`common`, `config`, `exception`, `pages`** 영역을 중심으로 정리합니다.

 | 패키지 | 주요 역할 | 주요 파일 |
| --- | --- | --- |
| `common` | 공통 기능 및 검증 | `BaseEntity`, `DynamicSize`, `DynamicSizeValidator` |
| `config` | 애플리케이션 환경 설정 | `CorsConfig` |
| `exception` | API 예외 및 오류 응답 처리 | `BusinessException`, `ErrorCode`, `ErrorObject`, `DefaultExceptionAdvice` |
| `common.advice` | View 공통 데이터 제공 | `GlobalControllerAdvice` |
| `pages` | 화면 이동 및 View 처리 | `ApiTestViewController`, `ProjectViewController`, `UserViewController` |

---

 ## 1\. `common` — 공통 기능

 `common`에는 여러 도메인에서 공통으로 사용할 수 있는 기능이 모여 있습니다.

```
common/
├── BaseEntity.java
└── validation/
    ├── DynamicSize.java
    └── DynamicSizeValidator.java
```

 ### `BaseEntity`

 `BaseEntity`는 여러 Entity가 공통으로 사용하는 **생성일/수정일 관리 기능**을 제공합니다.

```
BaseEntity
├── createdAt
└── updatedAt
```

 `@MappedSuperclass`와 JPA Auditing을 사용하여, 이를 상속받는 Entity에서 생성·수정 시간을 자동으로 관리할 수 있도록 구성되어 있습니다. `createdAt`은 생성 시 기록되고 수정되지 않도록 설정되어 있으며, `updatedAt`은 수정 시 갱신됩니다.  GitHub

 즉,

 > **Entity마다 `createdAt`, `updatedAt`을 반복해서 구현하지 않도록 공통화한 클래스**

 라고 볼 수 있습니다.

 ### `DynamicSize` / `DynamicSizeValidator`

 두 클래스는 **설정값을 이용한 동적 문자열 길이 검증**을 담당합니다.

 `DynamicSize`는 직접 만든 Bean Validation 어노테이션이고, `maxProperty`에 최대 길이가 저장된 설정 프로퍼티의 이름을 지정합니다.  GitHub

 `DynamicSizeValidator`는 해당 설정값을 Spring의 `Environment`에서 가져와 실제 문자열 길이를 검사합니다. 값이 `null`이면 다른 검증(`@NotBlank` 등)이 처리하도록 통과시키고, 설정값이 없을 경우에도 통과시키며, 설정된 최대 길이를 넘으면 검증 실패가 됩니다.  GitHub

```
DTO 필드
   ↓
@DynamicSize(maxProperty = "...")
   ↓
DynamicSizeValidator
   ↓
application 설정에서 최대 길이 조회
   ↓
문자열 길이 검증
```

 따라서 `common`은 단순한 유틸리티 모음이라기보다 **Entity 공통 기능과 재사용 가능한 입력 검증 기능을 제공하는 영역**입니다.

---

 # 2\. `config` — 애플리케이션 설정

 현재 확인되는 `config`의 핵심 파일은 `CorsConfig`입니다.

```
config/
└── CorsConfig.java
```

 `CorsConfig`는 **CORS 정책을 설정하는 Spring Configuration**입니다. `app.cors.allowed-origins` 설정에서 허용할 Origin 목록을 가져오고, GET/POST/PUT/PATCH/DELETE/OPTIONS 요청을 허용합니다. 또한 모든 Header를 허용하고 Credential 사용을 활성화하며, Preflight 요청 결과를 3600초 동안 캐시하도록 설정합니다.  GitHub

```
Frontend
   │
   │ 다른 Origin에서 API 요청
   ▼
CorsFilter
   │
   ├── 허용된 Origin인가?
   ├── 허용된 Method인가?
   └── 허용된 요청인가?
   │
   ▼
Backend API
```

 특히 `CorsFilter`를 `HIGHEST_PRECEDENCE`로 등록하여 CORS 처리가 높은 우선순위로 적용되도록 구성되어 있습니다.  GitHub

 즉,

 > **`config` = 애플리케이션 전체에 적용되는 Spring 환경 및 동작 방식을 설정하는 영역**

 으로 정리할 수 있습니다.

---

 # 3\. `exception` — 예외 및 API 오류 처리

 `exception`은 실제 API 요청에서 발생하는 **비즈니스 예외와 입력 오류 등을 일관된 HTTP 응답으로 변환**하는 영역입니다.

```
exception/
├── BusinessException.java
├── ErrorCode.java
├── ErrorObject.java
└── DefaultExceptionAdvice.java
```

 ### 주요 구성

 | 클래스 | 역할 |
| --- | --- |
| `BusinessException` | 비즈니스 로직에서 발생시키는 사용자 정의 예외 |
| `ErrorCode` | 오류 종류, 메시지, HTTP 상태 코드 정의 |
| `ErrorObject` | 오류 응답의 데이터 형식 |
| `DefaultExceptionAdvice` | 발생한 예외를 HTTP 응답으로 변환 |

### `BusinessException`

 `RuntimeException`을 상속하며 `ErrorCode`를 함께 가지고 있습니다. 따라서 Service 등에서 특정 오류가 발생했을 때 **어떤 종류의 오류인지 `ErrorCode`를 통해 전달**할 수 있습니다. 메시지를 기본 ErrorCode에서 가져오거나 별도의 메시지/인자를 전달할 수도 있습니다.  GitHub

```
Service
  ↓
BusinessException(ErrorCode)
  ↓
DefaultExceptionAdvice
```

 ### `ErrorCode`

 오류 종류와 HTTP 상태 코드를 한 곳에서 관리합니다.

 현재 코드에는 예를 들어 다음과 같은 오류가 정의되어 있습니다.  GitHub

```
RESOURCE_NOT_FOUND       → 404
STUDENT_NUMBER_DUPLICATE → 409
EMAIL_DUPLICATE          → 409
PHONE_NUMBER_DUPLICATE   → 409
INVALID_INPUT            → 400
INTERNAL_SERVER_ERROR    → 500
```

 따라서 비즈니스 로직에서 오류가 발생했을 때 HTTP 상태 코드와 메시지를 각각 작성하는 대신 **정해진 ErrorCode를 사용할 수 있습니다.**

 ### `ErrorObject`

 API 오류 응답의 기본 형태를 담당합니다.

```
ErrorObject
├── statusCode
├── message
└── timestamp
```

 `timestamp`는 `yyyy-MM-dd HH:mm:ss` 형식으로 현재 시간을 반환합니다.  GitHub

 ### `DefaultExceptionAdvice`

 실제 예외를 잡아서 `ErrorObject` 형태의 응답으로 만들어주는 핵심 클래스입니다.

 `@RestControllerAdvice`를 사용하여 Controller 전반의 예외를 처리하며, 현재 코드에서는 `BusinessException`, `HttpMessageNotReadableException`, `RuntimeException`, `MethodArgumentNotValidException` 등을 각각 처리합니다.  GitHub

 특히 입력값 검증 오류가 발생하면 필드별 오류를 `Map<String, String>`으로 모아 다음과 같은 형태로 반환합니다.

```
{
    status,
    message,
    timestamp,
    errors
}
```

 즉 전체 흐름은:

```
요청
 ↓
Controller
 ↓
Service
 ↓
예외 발생
 ↓
DefaultExceptionAdvice
 ↓
ErrorCode / ErrorObject
 ↓
HTTP 오류 응답
```

 으로 볼 수 있습니다.

---

 # 4\. `common.advice` — View 공통 데이터

 여기서 하나 주의할 점이 있습니다.

 사용자가 주신 경로는:

```
exception/GlobalControllerAdvice.java
```

 이지만 실제 소스의 package 선언은:

```
package com.rookies6.myspringboot4project.common.advice;
```

 입니다. 따라서 **실제 Java 패키지 기준으로는 `exception`이 아니라 `common.advice`에 해당**합니다.  GitHub

 이 클래스의 역할도 예외 처리가 아닙니다.

 `@ControllerAdvice`와 `@ModelAttribute("projects")`를 사용하여 **모든 View Controller에서 사용할 수 있도록 프로젝트 목록을 자동으로 Model에 넣어주는 역할**을 합니다. 내부적으로 `ProjectRepository.findAll()`을 호출합니다.  GitHub

```
View Controller
       ↑
       │ projects 자동 주입
       │
GlobalControllerAdvice
       │
       ▼
ProjectRepository
       │
       ▼
Project 목록
```

 그래서 `ProjectViewController`에서는 별도로 프로젝트 목록을 조회하지 않고 바로 `project/list` 화면을 반환할 수 있습니다. 실제 코드에서도 이 부분을 명시하고 있습니다.  GitHub

---

 # 5\. `pages` — 화면(View) Controller

 `pages`는 REST API 자체보다는 **사용자가 접근하는 화면과 View 이동을 담당하는 Controller**를 모아놓은 영역입니다.

```
pages/
├── ApiTestViewController.java
├── ProjectViewController.java
└── UserViewController.java
```

 | Controller | 주요 역할 |
| --- | --- |
| `ApiTestViewController` | 사용자/프로젝트/코드파일/분석 관리 화면 제공 |
| `ProjectViewController` | 프로젝트 목록·생성·수정 등의 화면 처리 |
| `UserViewController` | 메인·회원 목록·회원가입·수정·분석 등의 화면 처리 |

### `ApiTestViewController`

 API 테스트를 위한 관리 화면들을 제공합니다.

```
/user-management
/project-management
/code-file-management
/analysis-management
```

 각 요청에 대해 `fragments/api/...` View를 반환합니다. 즉 **API 기능을 테스트하거나 확인할 수 있는 화면을 제공하는 Controller**입니다.  GitHub

 ### `ProjectViewController`

 프로젝트 화면을 담당합니다.

```
GET  /projects
GET  /projects/new
POST /projects/new
GET  /projects/{id}/edit
...
```

 프로젝트 목록, 생성 화면, 생성 처리, 수정 화면 등의 흐름을 처리하며, 프로젝트 생성 시 `UserRepository`에서 사용자를 가져와 `Project`를 생성하고 저장하는 코드도 포함되어 있습니다.  GitHub

 즉 `pages`의 Controller이지만 단순히 View만 반환하는 것이 아니라 **화면에서 발생하는 간단한 CRUD 요청까지 직접 처리하는 형태**입니다.

 ### `UserViewController`

 사용자 화면의 전체적인 흐름을 담당합니다.

```
/
 └── 메인 화면

/list-users
 └── 회원 목록

/signup
 └── 회원가입

/edit-user/{id}
 └── 회원 수정

/api-guide
 └── API 가이드

/analysis
 └── 코드 보안 분석 화면
```

 회원가입에서는 `UserDTO.Request`를 사용하고 `@Valid`와 `BindingResult`를 통해 입력값 검증 결과를 확인한 뒤 `UserService`에 사용자 생성을 요청합니다.  GitHub

---

 # 전체 구조 요약

 현재 확인한 실제 코드를 기준으로 하면 다음과 같이 정리하는 것이 가장 정확합니다.

```
myspringboot4project/
│
├── common/
│   ├── validation/
│   │   ├── DynamicSize.java
│   │   └── DynamicSizeValidator.java
│   └── BaseEntity.java
│
├── common/advice/
│   └── GlobalControllerAdvice.java
│
├── config/
│   └── CorsConfig.java
│
├── exception/
│   ├── BusinessException.java
│   ├── ErrorCode.java
│   ├── ErrorObject.java
│   └── DefaultExceptionAdvice.java
│
├── pages/
│   ├── ApiTestViewController.java
│   ├── ProjectViewController.java
│   └── UserViewController.java
│
├── user/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
│
└── sec/                  # 별도 문서에서 설명
```

 ### 핵심 역할만 정리하면

 | 영역 | 핵심 역할 |
| --- | --- |
| `common` | **공통 기능** — Entity 공통 필드와 동적 입력 검증 |
| `common.advice` | **View 공통 처리** — 모든 View에 프로젝트 목록 제공 |
| `config` | **환경 설정** — CORS 및 HTTP 요청 정책 설정 |
| `exception` | **API 예외 처리** — 비즈니스/입력/서버 오류를 일관된 응답으로 변환 |
| `pages` | **화면 처리** — 사용자·프로젝트·API 테스트 화면의 이동 및 처리 |
| `user` | **사용자 도메인** — 사용자 데이터를 Controller/DTO/Entity/Repository/Service 계층으로 관리 |
| `sec` | **핵심 보안 서비스** — 별도 정리 |

**한 문장으로 요약하면:**

 > `common`은 애플리케이션 전반에서 재사용되는 기능을 제공하고, `config`는 실행 환경을 설정하며, `exception`은 API 오류를 표준화하고, `common.advice`와 `pages`는 View에 필요한 데이터를 제공하고 화면 흐름을 처리합니다. `user`는 사용자 도메인을 담당하고, 이 기반 위에서 `sec`의 핵심 보안 분석 기능이 동작하는 구조입니다.