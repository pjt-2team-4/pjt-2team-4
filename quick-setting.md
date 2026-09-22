

# 📐 시스템 아키텍처 및 환경 설정 명세서

## 1. 프로젝트 개요 및 기술 스택 (Tech Stack)

본 프로젝트는 다수의 사용자가 자신의 프로젝트(작업 공간)를 생성하고, 소스 코드를 관리하며, AI 기반의 코드 보안 분석을 수행하는 SaaS형 백엔드 시스템입니다.

* **언어**: Java 17
* **프레임워크**: Spring Boot 4.0.8
* **데이터베이스**: MariaDB
* **ORM**: Spring Data JPA (Hibernate)
* **템플릿 엔진**: Thymeleaf (Spring MVC)

---

## 2. 모듈 및 의존성 구성 (`pom.xml`)

프로젝트는 `maven-compiler-plugin` 3.14.1 버전을 사용하며, 주요 의존성은 다음과 같이 목적별로 분류됩니다.

| 분류 | 의존성 (Artifact) | 설명 |
| --- | --- | --- |
| **Web / MVC** | `spring-boot-starter-webmvc` | REST API 및 Spring MVC 기반의 웹 애플리케이션 구현 |
| **Database** | `spring-boot-starter-data-jpa` | JPA 기반의 객체-관계 매핑 및 DB 조작 |
|  | `mariadb-java-client` | MariaDB 연결을 위한 JDBC 드라이버 |
| **Validation** | `spring-boot-starter-validation` | API 요청 객체(DTO)의 데이터 유효성 검증 (`@Valid`) |
| **Monitoring** | `spring-boot-admin-starter-server` (4.0.0) | Spring Boot Admin을 통한 애플리케이션 상태 모니터링 |
| **Utils** | `lombok` | Getter/Setter, Builder 등 보일러플레이트 코드 제거 |
|  | `spring-boot-devtools` | 코드 수정 시 자동 재시작 및 개발 생산성 향상 |

---

## 3. 핵심 환경 설정 (`application.properties`)

시스템 구동에 필요한 핵심 설정값들입니다. 현재 개발/테스트 환경에 맞추어 구성되어 있습니다.

### 3.1 서버 및 데이터베이스 설정

* **Server Port**: `8080`
* **DB URL**: `jdbc:mariadb://172.17.128.1:3306/lab_db` (MariaDB 연결)
* **JPA DDL 전략**: `update` (엔티티 변경 사항을 DB에 자동 반영)
* **SQL 로깅**: 활성화 (`show-sql=true`, `format_sql=true`)

### 3.2 모니터링 및 Actuator 설정

* **Actuator 노출**: 모든 엔드포인트 개방 (`exposure.include=*`)
* **Health Check**: 상세 정보 항상 표시 (`show-details=always`)
* **Admin Client URL**: `http://localhost:8090` (별도의 어드민 서버로 메트릭 전송)

### 3.3 비즈니스 로직(Validation) 커스텀 속성

DTO 유효성 검사(`@DynamicSize` 등)에 사용되는 도메인별 최대 길이 제한 설정입니다.

* `student.name.max-length=100` 외 다수
* `analysis.language.max-length=50` (코드 분석 언어명 제한)

---

## 4. 패키지 아키텍처 (Domain-Driven Design)

시스템은 각 기능(도메인)별로 패키지를 분리하는 도메인형 구조(Domain-driven Structure)를 채택하여 응집도를 높이고 모듈 간 결합도를 낮췄습니다.

### 4.1 핵심 도메인 구조 (`sec/` 폴더 하위)

```text
com.rookies6.myspringboot4project.sec/
├── project/             # [프로젝트 관리 도메인]
│   ├── controller/      # (ProjectController) 프로젝트 생성/수정/조회 API
│   ├── service/         # (ProjectService) 비즈니스 로직
│   ├── repository/      # (ProjectRepository) DB 접근
│   ├── dto/             # (ProjectDTO) 요청/응답 데이터 규격
│   └── entity/          # (Project) 유저와 1:N 관계의 작업 공간 엔티티
│
├── analysis/            # [보안 분석 도메인]
│   ├── controller/      # (AnalysisController) 분석 요청 및 결과 조회 API
│   ├── service/         # (AnalysisService) LLM 통신 및 분석 로직
│   ├── repository/      # (AnalysisRequestRepository)
│   ├── dto/             # (AnalysisDTO)
│   └── entity/          # (AnalysisRequest, VulnerabilityFinding) 분석 요청 및 취약점 결과
│
└── codefile/            # [소스 코드 파일 도메인]
    ├── controller/      # (CodeFileController) 파일 업로드 및 조회 API
    ├── service/         # (CodeFileService) 파일 처리 메타데이터 로직
    ├── repository/      # (CodeFileRepository)
    ├── dto/             # (CodeFileDTO)
    └── entity/          # (CodeFile) 프로젝트에 종속된 소스 코드 메타데이터

```

### 4.2 도메인 간 관계 (ERD 요약)

1. **User (1) ↔ (N) Project**: 한 명의 회원은 여러 개의 프로젝트를 가질 수 있습니다.
2. **Project (1) ↔ (N) CodeFile**: 하나의 프로젝트 안에 여러 개의 소스 코드 파일(메타데이터)이 존재합니다.
3. **Project (1) ↔ (N) AnalysisRequest**: 특정 프로젝트에 대해 여러 번의 코드 보안 분석을 요청할 수 있습니다.
4. **AnalysisRequest (1) ↔ (N) VulnerabilityFinding**: 한 번의 분석 요청에서 여러 개의 취약점(SQL Injection, XSS 등)이 발견될 수 있습니다. (Cascade 삭제 적용)

---



