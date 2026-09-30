

# 📐 시스템 아키텍처 및 환경 설정 명세서 (최신 확정본)

 - 브런치 : Inchan , 커밋 : ee503b9 직후 기준

## 1. 프로젝트 개요 및 기술 스택 (Tech Stack)

본 프로젝트는 다수의 사용자가 소스 코드 파일을 업로드하고, 내부 규칙 기반 스캐너와 AI를 활용해 코드 보안 분석(취약점 탐지)을 수행하는 SaaS형 백엔드 시스템입니다. (기존 `Project` 개념을 `Analysis Request` 단위로 통합하여 분석 파이프라인을 경량화했습니다.)

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

* `analysis.language.max-length=50` (코드 분석 언어명 제한 등)

---

## 4. 패키지 아키텍처 (Domain-Driven Design)

시스템은 불필요한 단계를 줄이고 분석 파이프라인의 응집도를 높이기 위해 도메인을 재편했습니다. 보안 분석(`analysis`)과 대상 파일(`analysisfile`), 그리고 순수 자바 기반의 스캐너(`scanner`)로 역할이 명확히 분리되어 있습니다.

### 4.1 핵심 도메인 구조 (`sec/` 폴더 하위)

```text
com.rookies6.myspringboot4project.sec/
├── analysis/            # [보안 분석 메인 도메인] (기존 Project 역할 통합)
│   ├── controller/      # (AnalysisController) 분석 요청 및 결과 조회 API
│   ├── service/         # (AnalysisWorker) 비동기 스캐너 파이프라인 및 AI 설명 로직
│   ├── repository/      # (AnalysisRequestRepository)
│   ├── dto/             # (AnalysisDTO) 요청 및 결과 응답 규격
│   └── entity/          # (AnalysisRequest, VulnerabilityFinding)
│                        # Enum: AnalysisStatus, Severity(4단계), (MVP 2종)
│
├── analysisfile/        # [분석 대상 파일 도메인]
│   ├── controller/      # (AnalysisFileController) 파일 단위 상세 조회 API
│   ├── dto/             # (AnalysisFileDTO) 용량 최적화 목록 및 코드 원문 포함 상세 DTO
│   └── entity/          # (AnalysisFile) AnalysisRequest에 종속된 소스 코드 파일 데이터
│
├── scanner/             # [규칙 기반 취약점 스캐너 모듈] (Spring/DB 의존성 없는 순수 로직)
│   ├── dto/             # (RawFinding) 스캐너의 순수 탐지 결과 레코드
│   ├── rule/            # (SecurityRule, RuleCatalog) 정규식 기반 탐지 룰 메타데이터
│   └── SecurityScanner  # 코드 파싱 및 취약점 패턴 매칭 엔진
│
└── common/enums/        # [공통 상수]
    ├── Language         # 지원 언어 (JAVA, JS, TS, PYTHON, HTML) 및 확장자 추출 로직
    └── VulnerabilityType
```

### 4.2 도메인 간 관계 (ERD 요약)

도메인이 최적화되면서 객체 간의 연관 관계가 다음과 같이 직관적인 1:N 폭포수 구조(Cascade)로 확정되었습니다.

1. **User (1) ↔ (N) AnalysisRequest**:
한 명의 회원이 여러 번의 분석 요청을 생성할 수 있습니다. (기존 `Project` 작업 공간 개념을 분석 요청 자체로 통합)
2. **AnalysisRequest (1) ↔ (N) AnalysisFile**:
한 번의 분석 요청 안에 검사해야 할 여러 개의 소스 코드 파일이 포함됩니다. (요청 삭제 시 파일도 Cascade 삭제)
3. **AnalysisFile (1) ↔ (N) VulnerabilityFinding**:
스캐너가 특정 파일의 코드를 분석하여 찾아낸 여러 개의 취약점(SQL Injection, Secret, XSS)이 해당 **파일 단위**에 매핑되어 저장됩니다.

