


# 프로젝트 패키지 Overview (MasterDataInitializer 포함)

 - 브런치 : Inchan , 커밋 : ee503b9 직후 기준

`sec` (보안 스캐너 및 분석 핵심 로직) 영역은 별도로 다루고, 여기서는 애플리케이션의 기반이 되는 **`common`, `config`, `exception`, `pages**` 영역을 최신 아키텍처에 맞추어 정리합니다.

| 패키지 | 주요 역할 | 주요 파일 |
| --- | --- | --- |
| `common` | 공통 기능 및 검증 | `BaseEntity`, `DynamicSize`, `DynamicSizeValidator` |
| `config` | 애플리케이션 환경 설정 | `CorsConfig`, **`MasterDataInitializer`** |
| `exception` | API 예외 및 오류 응답 처리 | `BusinessException`, `ErrorCode`, `ErrorObject`, `DefaultExceptionAdvice` |
| `common.advice` | View 공통 데이터 제공 | `GlobalControllerAdvice` |
| `pages` | 화면 이동 및 View 처리 | `ApiTestViewController`, `AnalysisViewController`, `UserViewController` |

---

## 1. `common` — 공통 기능

`common`에는 여러 도메인에서 공통으로 사용할 수 있는 기능이 모여 있습니다.

```text
common/
├── BaseEntity.java
└── validation/
    ├── DynamicSize.java
    └── DynamicSizeValidator.java

```

* **`BaseEntity`**: `@MappedSuperclass`와 JPA Auditing을 사용하여, 이를 상속받는 Entity에서 생성 시간(`createdAt`)과 수정 시간(`updatedAt`)을 자동으로 관리합니다.
* **`DynamicSize` / `DynamicSizeValidator**`: 설정값(`application.properties`)에 정의된 프로퍼티 키를 읽어와, 런타임에 동적으로 문자열 최대 길이를 검증하는 커스텀 Validation 기능입니다.

---

## 2. `config` — 애플리케이션 설정 (초기 데이터 포함)

`config` 패키지는 Spring의 환경 설정과 서버 기동 시 필요한 초기 설정들을 담당합니다.

```text
config/
├── CorsConfig.java
├── AsyncConfig.java
└── MasterDataInitializer.java

```

### `CorsConfig`

**전역 CORS(교차 출처 리소스 공유) 정책을 설정하는 Spring Configuration**입니다.

* `app.cors.allowed-origins` 설정에서 허용할 Origin을 동적으로 로드합니다.
* 모든 HTTP 메서드를 허용하며, `CorsFilter`를 가장 높은 우선순위(`HIGHEST_PRECEDENCE`)로 적용하여 Security 필터보다 먼저 동작하게 합니다.

### `MasterDataInitializer` (마스터 데이터 초기화)

**애플리케이션(서버) 기동 시 초기 샘플 데이터를 데이터베이스에 자동 적재하는 클래스**입니다.

* **동작 방식:** Spring Boot의 `CommandLineRunner`를 구현하여, Spring Context가 완전히 로드된 직후 자동으로 `run()` 메서드가 1회 실행됩니다.
* **주요 역할 (데이터 세팅):**
1. **유저 세팅:** 관리자, 개발자, 보안담당자 등 3명의 기본 사용자(`User`)를 DB에 생성합니다.
2. **분석 파이프라인 세팅:** 테스트를 위해 `COMPLETED`, `SCANNING`, `PENDING`, `FAILED` 등 다양한 상태를 가진 분석 요청(`AnalysisRequest`) 샘플을 생성합니다.
3. **취약점 매핑:** 생성된 요청 안에 `AnalysisFile`을 만들고, 우리가 설정한 4단계 Severity와 3대 취약점 Enum(`HARDCODED_SECRET`, `XSS` 등)을 사용해 `VulnerabilityFinding` 결과를 매핑하여 저장합니다.


* **의의:** MVP 개발 및 테스트 환경에서 빈 DB 화면 대신 즉각적으로 UI 렌더링 및 API 조회를 테스트할 수 있도록 돕는 필수 구성 요소입니다.

---

## 3. `exception` — 예외 및 API 오류 처리

`exception`은 실제 API 요청에서 발생하는 **비즈니스 예외와 입력 오류 등을 일관된 JSON 형태의 HTTP 응답으로 변환**하는 영역입니다.

```text
exception/
├── BusinessException.java
├── ErrorCode.java
├── ErrorObject.java
└── DefaultExceptionAdvice.java

```

* **`BusinessException` & `ErrorCode**`: 비즈니스 로직에서 의도적으로 발생시키는 커스텀 예외이며, 오류 종류, 메시지, HTTP 상태 코드(400, 404, 409 등)를 `ErrorCode` 상수로 중앙 관리합니다.
* **`DefaultExceptionAdvice`**: `@RestControllerAdvice`로 전역 예외를 낚아채어, 규격화된 `ErrorObject` 폼(status, message, timestamp, 상세 필드 에러)으로 클라이언트에 반환합니다.

---

## 4. `common.advice` — View 공통 데이터

```text
common/advice/
└── GlobalControllerAdvice.java

```

`@ControllerAdvice`와 `@ModelAttribute`를 사용하여 **모든 Thymeleaf View 컨트롤러에서 사용자의 분석 요청(Analysis Request) 목록을 자동으로 Model에 넣어주는 역할**을 합니다. 이를 통해 모든 페이지의 사이드바나 네비게이션에서 분석 목록을 렌더링할 수 있습니다.

---

## 5. `pages` — 화면(View) Controller

`pages`는 REST API 서버 역할과 별개로, **사용자가 브라우저를 통해 접근하는 화면 렌더링과 페이지 이동(Routing)을 담당**하는 영역입니다.

```text
pages/
├── ApiTestViewController.java
├── AnalysisViewController.java
└── UserViewController.java

```

* **`ApiTestViewController`**: 백엔드 API 테스트를 위한 관리용 프래그먼트 뷰를 반환합니다.
* **`AnalysisViewController`**: 새로운 코드 분석 요청 등록 화면, 분석 진행 상태 조회 화면, 분석 결과(취약점 리스트) 리포트 화면을 제공합니다.
* **`UserViewController`**: 메인 홈페이지(`/`), 회원가입(`/signup`), 회원 정보 수정(`/edit-user/{id}`) 등 유저 관련 기본 화면을 제공합니다.


