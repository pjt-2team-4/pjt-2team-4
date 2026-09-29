

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
| 프로젝트ID | id | BIGINT | PK |
| 유저ID | user_id | BIGINT | FK → users.id, NOT NULL |
| 프로젝트 이름 | title | VARCHAR(100) | NOT NULL |
| 언어 | language | VARCHAR(20) | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |

#### project_file

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 파일ID | id | BIGINT | PK, NOT NULL |
| 프로젝트ID | project_id | BIGINT | FK, NOT NULL |
| 파일경로 | file_path | VARCHAR(255) | NOT NULL |
| 파일이름 | file_name | VARCHAR(255) | NOT NULL |
| 코드 언어 | language | VARCHAR(20) | NOT NULL |
| 소스 코드 | content | MEDIUMTEXT | NOT NULL |
| 파일사이즈 | file_size_bytes | INT | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |

#### analysis_request (분석 요청)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 분석요청 고유ID | id | BIGINT | PK, AUTO_INCREMENT |
| 유저ID | user_id | BIGINT | FK → users.id, NOT NULL |
| 프로젝트ID | project_id | BIGINT | FK |
| 코드 언어 | language | VARCHAR(20) | NOT NULL |
| 소스 코드 | source_code | MEDIUMTEXT | NOT NULL |
| 예상 소요 시간 | estimated_duration_seconds | INT | NULL |
| 분석요청상태 | status | VARCHAR(20) | NOT NULL, 기본값 `PENDING` |
| 에러메시지 | error_message | VARCHAR(500) | NULL |
| 분석 시작시간 | started_at | DATETIME(6) | NULL |
| 분석 종료시간 | completed_at | DATETIME(6) | NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |

#### analysis_request_file (분석 요청 파일)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 분석요청 고유ID | analysis_request_id | BIGINT | NOT NULL |
| 프로젝트ID | project_file_id | BIGINT | NOT NULL |




<details>
<summary><strong> finding_Vulnerability (탐지된 취약점) 미확정 </strong></summary>


| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 취약점분석결과ID | id | BIGINT | PK, AUTO_INCREMENT |
| 분석요청 고유ID | analysis_request_id | BIGINT | FK → analysis_request.id, NOT NULL |
| 적용된 규칙 | rule_id | BIGINT | FK → security_rule.id, NOT NULL |
| 심각도(높음/중간/낮음) | severity | VARCHAR(10)	 | NOT NULL |
| 시작 라인 (1부터) | start_line | INT | NOT NULL |
| 종료 라인 (start_line 이상) | end_line | INT | NOT NULL |
| 문제 코드 조각 (Before) | code_snippet | TEXT | NOT NULL |
| 생성날짜 | created_at | DATETIME(6) | NOT NULL |

</details>


<details>
<summary><strong> LLM 분석 (추후 얘기해봐야함)  미확정 </strong></summary>

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

</details>


<details>
<summary><strong> security_rule (보안 규칙 목록) 미확정 </strong></summary>

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

</details>


### ✅ Enum 값

| 컬럼 | 허용 값 |
| --- | --- |
| analysis_request.language | JAVA, JAVASCRIPT |
| analysis_request.status | PENDING, SCANNING, LLM_ANALYZING, COMPLETED, FAILED |


<details>
<summary><strong> Enum 값 미확정 </strong></summary>

| 컬럼 | 허용 값 |
| --- | --- |
| security_rule.vulnerability_type | SQL_INJECTION, HARDCODED_SECRET, XSS, DANGEROUS_FUNCTION, CORS_MISCONFIGURATION, SENSITIVE_DATA_LOGGING, BROKEN_AUTHENTICATION, PLAINTEXT_PASSWORD |
| security_rule.default_severity, finding.severity | HIGH, MEDIUM, LOW |
| security_rule.target_language | COMMON, JAVA, JAVASCRIPT |
| llm_analysis.status | COMPLETED, FAILED |

</details>

