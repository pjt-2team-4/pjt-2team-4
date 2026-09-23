

# ERD 의 도메인 내용 이나 실제로는 가장 최근의 확정 사안에 의해 변동될 수 있음

## 2026-09-23 _ 깃허브 커밋 sha:

### users (회원)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 회원고유ID | id | BIGINT | PK, AUTO_INCREMENT |
| 이메일 | email | VARCHAR(100) | NOT NULL, UNIQUE |
| 해시된 비밀번호 | password | VARCHAR(255) | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |

### project (프로젝트)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
|  | id | BIGINT | PK |
| 소유 회원 | user_id | BIGINT | FK → users.id, NOT NULL |
| 프로젝트 제목 | title | VARCHAR(100) | NOT NULL |
| 언어 | language | VARCHAR(20) | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |

#### project_file

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
|  | id | BIGINT | PK, NOT NULL |
| 소속 프로젝트 | project_id | BIGINT | NOT NULL |
| 전체 경로 (예: src/main/java/UserService.java) | file_path | VARCHAR(500) | NOT NULL |
| 파일명, 트리 표시용 | file_name | VARCHAR(255) | NOT NULL |
| 파일 언어 | language | VARCHAR(20) | NOT NULL |
| 파일 내용 (코드 뷰어에 표시) | content | MEDIUMTEXT | NOT NULL |
| 업로드 시점 파일 크기 | file_size_bytes | INT | NOT NULL |
|  | created_at | DATETIME(6) | NOT NULL |
|  | updated_at | DATETIME(6) | NOT NULL |

#### security_rule (보안 규칙 목록)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 보안 규칙 고유ID | id | BIGINT | PK, AUTO_INCREMENT |
| 규칙 식별 코드 (예: SQL_INJECTION_CONCAT) | rule_code | VARCHAR(50) | NOT NULL, UNIQUE |
| 취약점 유형(예: SQL_INJECTION) | vulnerability_type | VARCHAR(30) | NOT NULL |
| 적용 언어 | target_language | VARCHAR(20) | NOT NULL, 기본값 `COMMON` |
| 규칙 이름 | name | VARCHAR(100) | NOT NULL |
| 규칙 설명 | description | TEXT | NULL |
|  | created_at | DATETIME(6) | NOT NULL |
|  | updated_at | DATETIME(6) | NOT NULL |

#### analysis_request (분석 요청)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 분석요청 고유ID | id | BIGINT | PK, AUTO_INCREMENT |
| 요청한 회원 | user_id | BIGINT | FK → users.id, NOT NULL |
| 소속 프로젝트 | project_id | BIGINT | FK |
| 입력 코드 언어 | language | VARCHAR(20) | NOT NULL |
| 사용자 입력 코드 
(민감정보 마스킹 권장) | source_code | MEDIUMTEXT | NOT NULL |
| 분석 진행 상태 | status | VARCHAR(20) | NOT NULL, 기본값 `PENDING` |
| 예상 소요 시간 | estimated_duration_seconds | INT | NULL |
| 실패 사유 | error_message | VARCHAR(500) | NULL |
|  | created_at | DATETIME(6) | NOT NULL |
|  | updated_at | DATETIME(6) | NOT NULL |

#### finding_Vulnerability (탐지된 취약점)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
|  | id | BIGINT | PK, AUTO_INCREMENT |
| 소속 분석 요청 | analysis_request_id | BIGINT | FK → analysis_request.id, NOT NULL |
| 적용된 규칙 | rule_id | BIGINT | FK → security_rule.id, NOT NULL |
| 심각도(높음/중간/낮음) | severity | VARCHAR(10)	 | NOT NULL |
| 시작 라인 (1부터) | start_line | INT | NOT NULL |
| 종료 라인 (start_line 이상) | end_line | INT | NOT NULL |
| 문제 코드 조각 (Before) | code_snippet | TEXT | NOT NULL |
| 생성날짜 | created_at | DATETIME(6) | NOT NULL |

#### LLM 분석 (추후 얘기해봐야함)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
|  | id | BIGINT | PK, NOT NULL |
| 1:1 대상 취약점 | finding_id | BIGINT | NOT NULL |
| LLM 호출 결과 | status | VARCHAR(20) | NOT NULL, 기본값 `COMPLETED` |
| 취약점 원인 | cause | TEXT | NULL |
| 위험성 설명 | risk_description | TEXT | NULL |
| 공격 시나리오 | attack_scenario | TEXT | NULL |
| 개선 방법 설명 | remediation | TEXT | NULL |
| 개선 코드 (After) | fixed_code | TEXT | NULL |
| 사용한 LLM 모델 | model_name | VARCHAR(100) | NULL |
| 프롬프트 버전 | prompt_version | VARCHAR(30) | NULL |
| LLM 원본 응답 | raw_response | MEDIUMTEXT | NULL |
|  | prompt_tokens | INT | NULL |
|  | completion_tokens | INT | NULL |
|  | created_at | DATETIME(6) | NOT NULL |

### ✅ Enum 값

| 컬럼 | 허용 값 |
| --- | --- |
| security_rule.vulnerability_type | SQL_INJECTION, HARDCODED_SECRET, XSS, DANGEROUS_FUNCTION, CORS_MISCONFIGURATION, SENSITIVE_DATA_LOGGING, BROKEN_AUTHENTICATION, PLAINTEXT_PASSWORD |
| security_rule.default_severity, finding.severity | HIGH, MEDIUM, LOW |
| security_rule.target_language | COMMON, JAVA, JAVASCRIPT |
| analysis_request.language | JAVA, JAVASCRIPT |
| analysis_request.status | PENDING, SCANNING, LLM_ANALYZING, COMPLETED, FAILED |
| llm_analysis.status | COMPLETED, FAILED |


# ERD

> 📌 **2026-09-23 기준**
>
> 도메인 내용이나 실제 스키마는 가장 최근의 확정 사안에 따라 변경될 수 있음.

## 🗂️ Tables

<details open>
<summary><strong>👤 users — 회원</strong></summary>

| 설명 | 컬럼 | 타입 | 제약 |
|---|---|---|---|
| 회원고유ID | `id` | BIGINT | PK, AUTO_INCREMENT |
| 이메일 | `email` | VARCHAR(100) | NOT NULL, UNIQUE |
| 해시된 비밀번호 | `password` | VARCHAR(255) | NOT NULL |
| 생성일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>

<details>
<summary><strong>📁 project — 프로젝트</strong></summary>

| 설명 | 컬럼 | 타입 | 제약 |
|---|---|---|---|
| 프로젝트ID | `id` | BIGINT | PK |
| 소유 회원 | `user_id` | BIGINT | FK → users.id, NOT NULL |
| 프로젝트 제목 | `title` | VARCHAR(100) | NOT NULL |
| 언어 | `language` | VARCHAR(20) | NOT NULL |
| 생성일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>

<details>
<summary><strong>📄 project_file — 프로젝트 파일</strong></summary>

| 설명 | 컬럼 | 타입 | 제약 |
|---|---|---|---|
| 파일ID | `id` | BIGINT | PK, NOT NULL |
| 소속 프로젝트 | `project_id` | BIGINT | FK → project.id |
| 전체 경로 | `file_path` | VARCHAR(500) | NOT NULL |
| 파일명 | `file_name` | VARCHAR(255) | NOT NULL |
| 파일 언어 | `language` | VARCHAR(20) | NOT NULL |
| 파일 내용 | `content` | MEDIUMTEXT | NOT NULL |
| 파일 크기 | `file_size_bytes` | INT | NOT NULL |
| 생성일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>
