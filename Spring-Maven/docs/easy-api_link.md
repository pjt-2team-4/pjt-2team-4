

# 주요 연결 및 API · Entity

 - 브런치 : Inchan , 커밋 : ee503b9 직후 기준

---

<details>
<summary><strong> 1. users (회원) </strong></summary>

## 1. users (회원)

[User.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/user/entity/User.java)

### 1-1. 데이터베이스 테이블 명세

| 설명       | 컬럼명          | 타입           | 제약 사항              |
| -------- | ------------ | ------------ | ------------------ |
| 회원 고유 ID | `user_id`    | BIGINT       | PK, AUTO_INCREMENT |
| 이메일      | `email`      | VARCHAR(100) | NOT NULL, UNIQUE   |
| 해시된 비밀번호 | `password`   | VARCHAR(255) | NOT NULL           |
| 생성 일시    | `created_at` | DATETIME(6)  | NOT NULL           |
| 수정 일시    | `updated_at` | DATETIME(6)  | NOT NULL           |

### 1-2. API 명세

| 기능         | Method | Endpoint                  | 요청 데이터                                 | 설명                               |
| ---------- | ------ | ------------------------- | -------------------------------------- | -------------------------------- |
| 회원가입       | POST   | `/api/user`               | Body: `email`, `password`              | 새로운 회원을 등록합니다. 이메일 중복 여부를 확인합니다. |
| 전체 회원 조회   | GET    | `/api/user`               | 없음                                     | 등록된 모든 회원 목록을 조회합니다.             |
| ID로 회원 조회  | GET    | `/api/user/{id}`          | Path: `id` (BIGINT)                    | 특정 ID의 회원 정보를 조회합니다.             |
| 이메일로 회원 조회 | GET    | `/api/user/email/{email}` | Path: `email` (VARCHAR)                | 특정 이메일의 회원 정보를 조회합니다.            |
| 회원 정보 수정   | PUT    | `/api/user/{id}`          | Path: `id` + Body: `email`, `password` | 기존 회원의 이메일 및 비밀번호 정보를 수정합니다.     |
| 회원 삭제      | DELETE | `/api/user/{id}`          | Path: `id` (BIGINT)                    | 특정 회원을 삭제합니다.                    |

</details>


<details>
<summary><strong> 2. analysis_requests(분석요청) </strong></summary>

## 2. analysis_requests (분석 요청)

`Project`와 `analysis_request`가 통합된 **최신 분석 파이프라인의 핵심 도메인**입니다.

[AnalysisRequest.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysis/entity/AnalysisRequest.java)

### 2-1. 데이터베이스 테이블 명세

| 설명          | 컬럼명                          | 타입           | 제약 사항                          |
| ----------- | ---------------------------- | ------------ | ------------------------------ |
| 분석 요청 고유 ID | `id`                         | BIGINT       | PK, AUTO_INCREMENT             |
| 유저 ID       | `user_id`                    | BIGINT       | FK → `users.user_id`, NOT NULL |
| 분석 제목       | `title`                      | VARCHAR(200) | NOT NULL                       |
| 메인 코드 언어    | `language`                   | VARCHAR(50)  | NOT NULL                       |
| 분석 상태       | `status`                     | ENUM         | NOT NULL, 기본값 `PENDING`        |
| 예상 소요 시간    | `estimated_duration_seconds` | INT          | NULL                           |
| 에러 메시지      | `error_message`              | TEXT         | NULL                           |
| 생성 일시       | `created_at`                 | DATETIME(6)  | NOT NULL                       |
| 수정 일시       | `updated_at`                 | DATETIME(6)  | NOT NULL                       |

**분석 상태**

`PENDING` → `SCANNING` → `EXPLAINING` → `COMPLETED`

오류 발생 시 → `FAILED`

### 2-2. API 명세

| 기능          | Method | Endpoint                      | 요청 데이터                                                        | 설명                                         |
| ----------- | ------ | ----------------------------- | ------------------------------------------------------------- | ------------------------------------------ |
| 분석 요청 생성    | POST   | `/api/analysis`               | Body: `title`, `language`, `files` (`filePath`, `content` 배열) | 새로운 코드 분석 요청을 등록하고 비동기 스캔 워커를 트리거합니다.      |
| 내 분석 목록 조회  | GET    | `/api/analysis/user/{userId}` | Path: `userId`                                                | 특정 사용자가 요청한 분석 목록을 조회합니다.                  |
| 분석 결과 상세 조회 | GET    | `/api/analysis/{id}`          | Path: `id`                                                    | 분석 완료된 전체 결과를 조회합니다.                       |
| 분석 진행 상태 조회 | GET    | `/api/analysis/{id}/status`   | Path: `id`                                                    | 현재 분석 상태를 polling하기 위한 경량 API입니다.          |
| 분석 기록 삭제    | DELETE | `/api/analysis/{id}`          | Path: `id`                                                    | 분석 요청과 하위 파일 및 취약점 내역을 Cascade 방식으로 삭제합니다. |

</details>

<details>
<summary><strong> 3. analysis_files(분석파일) </strong></summary>

## 3. analysis_files (분석 파일)

`AnalysisRequest` 생성 시 함께 저장되며, `VulnerabilityFinding`이 종속되는 **소스 코드 파일 엔티티**입니다.

[AnalysisFile.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysisfile/entity/AnalysisFile.java)

### 3-1. 데이터베이스 테이블 명세

| 설명       | 컬럼명                   | 타입           | 제약 사항                                 |
| -------- | --------------------- | ------------ | ------------------------------------- |
| 파일 고유 ID | `id`                  | BIGINT       | PK, AUTO_INCREMENT                    |
| 분석 요청 ID | `analysis_request_id` | BIGINT       | FK → `analysis_requests.id`, NOT NULL |
| 파일 상대 경로 | `relative_path`       | VARCHAR(500) | NOT NULL                              |
| 파일명      | `file_name`           | VARCHAR(255) | NOT NULL                              |
| 코드 언어    | `language`            | VARCHAR(20)  | NOT NULL                              |
| 소스 코드    | `content`             | TEXT         | NULL                                  |
| 전체 라인 수  | `line_count`          | INT          | NOT NULL                              |
| 생성 일시    | `created_at`          | DATETIME(6)  | NOT NULL                              |
| 수정 일시    | `updated_at`          | DATETIME(6)  | NOT NULL                              |

### 3-2. API 명세

| 기능           | Method | Endpoint                                 | 요청 데이터            | 설명                                           |
| ------------ | ------ | ---------------------------------------- | ----------------- | -------------------------------------------- |
| 요청별 파일 목록 조회 | GET    | `/api/analysis-file/request/{requestId}` | Path: `requestId` | 특정 분석 요청에 포함된 파일 목록을 조회합니다. 코드 내용은 제외합니다.    |
| 파일 상세 조회     | GET    | `/api/analysis-file/{id}`                | Path: `id`        | 특정 파일의 소스 코드 원문 및 해당 파일에서 발견된 취약점 목록을 조회합니다. |

</details>

<details>
<summary><strong> 4. vulnerability_finding(취약점탐지) </strong></summary>

## 4. vulnerability_finding (취약점 탐지 결과)

스캐너(`AnalysisWorker`)가 생성하며, 분석 파일 단위로 매핑되는 **최종 취약점 분석 결과 엔티티**입니다.

[VulnerabilityFinding.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysis/entity/VulnerabilityFinding.java)

### 4-1. 데이터베이스 테이블 명세

| 설명        | 컬럼명                  | 타입           | 제약 사항                          |
| --------- | -------------------- | ------------ | ------------------------------ |
| 취약점 결과 ID | `id`                 | BIGINT       | PK, AUTO_INCREMENT             |
| 분석 파일 ID  | `analysis_file_id`   | BIGINT       | FK → `analysis_files.id`, NULL |
| 룰 식별자     | `rule_id`            | VARCHAR(255) | NULL, 코드 레벨 RuleCatalog 매핑     |
| 취약점 유형    | `vulnerability_type` | ENUM         | NOT NULL                       |
| 심각도       | `severity`           | ENUM         | NOT NULL                       |
| 취약점 제목    | `title`              | VARCHAR(255) | NULL                           |
| 상세 설명     | `description`        | TEXT         | NULL                           |
| 시작 라인     | `start_line`         | INT          | NULL                           |
| 종료 라인     | `end_line`           | INT          | NULL                           |
| 문제 코드 조각  | `code_snippet`       | TEXT         | NULL                           |

**취약점 유형**

`SQL_INJECTION`, `HARDCODED_SECRET`, `XSS`

**심각도**

`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`

### 4-2. API 명세

| 기능         | Method | Endpoint                     | 요청 데이터         | 설명                             |
| ---------- | ------ | ---------------------------- | -------------- | ------------------------------ |
| 파일별 취약점 조회 | GET    | `/api/finding/file/{fileId}` | Path: `fileId` | 특정 파일에 매핑된 모든 취약점 목록을 조회합니다.   |
| 취약점 상세 조회  | GET    | `/api/finding/{id}`          | Path: `id`     | 특정 취약점의 코드 스니펫 및 세부 정보를 조회합니다. |

</details>

<details>
<summary><strong> 5. 공통 Enum 및 Entity </strong></summary>

## 5. 공통 Enum 및 Entity

### 5-1. Enum

| 구분     | 클래스                 | 역할                      |
| ------ | ------------------- | ----------------------- |
| 코드 언어  | `Language`          | 분석 대상 코드의 언어를 정의합니다.    |
| 취약점 유형 | `VulnerabilityType` | 탐지 가능한 취약점 유형을 정의합니다.   |
| 심각도    | `Severity`          | 탐지된 취약점의 심각도 수준을 정의합니다. |

* [Language.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/common/enums/Language.java)
* [VulnerabilityType.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/common/enums/VulnerabilityType.java)
* [Severity.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysis/entity/Severity.java)

### 5-2. 공통 생성·수정 일시

각 엔티티의 공통 생성 일시 및 수정 일시는 `BaseEntity`를 통해 관리합니다.

[BaseEntity.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/common/BaseEntity.java)

| 공통 필드        | 역할        |
| ------------ | --------- |
| `created_at` | 데이터 생성 일시 |
| `updated_at` | 데이터 수정 일시 |

</details>

