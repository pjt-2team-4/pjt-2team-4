
# ERD (Entity Relationship Diagram) - 브런치 : Inchan , 커밋 : ee503b9 직후 기준

## 📌 확정된 도메인 스키마 (DB 반영 완료)


<details>
<summary><strong> 1. users (회원) </strong></summary>

### 1. users (회원)

| 설명 | 컬럼명 | 타입 | 제약 사항 |
| --- | --- | --- | --- |
| 회원 고유 ID | `user_id` | BIGINT | PK, AUTO_INCREMENT |
| 이메일 | `email` | VARCHAR(100) | NOT NULL, UNIQUE |
| 해시된 비밀번호 | `password` | VARCHAR(255) | NOT NULL |
| 생성 일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정 일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>

<details>
<summary><strong> 2. analysis_requests (분석 요청) </strong></summary>

### 2. analysis_requests (분석 요청)

*기존 `project` 테이블의 개념이 통합되어, 사용자가 직접 분석 단위를 생성하고 관리합니다.*

| 설명 | 컬럼명 | 타입 | 제약 사항 |
| --- | --- | --- | --- |
| 분석 요청 고유 ID | `id` | BIGINT | PK, AUTO_INCREMENT |
| 유저 ID | `user_id` | BIGINT | FK → `users.user_id`, NOT NULL |
| 분석 제목 | `title` | VARCHAR(200) | NOT NULL |
| 메인 코드 언어 | `language` | VARCHAR(50) | NOT NULL |
| 분석 상태 | `status` | ENUM | NOT NULL, 기본값 `PENDING` |
| 예상 소요 시간 | `estimated_duration_seconds` | INT | NULL |
| 에러 메시지 | `error_message` | TEXT | NULL |
| 생성 일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정 일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>

<details>
<summary><strong> 3. analysis_files (분석 대상 파일) </strong></summary>

### 3. analysis_files (분석 대상 파일)

*분석 요청에 종속되는 1:N 구조의 소스 코드 파일 리스트입니다.*

| 설명 | 컬럼명 | 타입 | 제약 사항 |
| --- | --- | --- | --- |
| 파일 고유 ID | `id` | BIGINT | PK, AUTO_INCREMENT |
| 분석 요청 ID | `analysis_request_id` | BIGINT | FK → `analysis_requests.id`, NOT NULL |
| 파일명 | `file_name` | VARCHAR(255) | NOT NULL |
| 파일 상대 경로 | `relative_path` | VARCHAR(500) | NOT NULL |
| 코드 언어 | `language` | VARCHAR(20) | NOT NULL |
| 소스 코드 내용 | `content` | TEXT | NULL |
| 전체 라인 수 | `line_count` | INT | NOT NULL |
| 생성 일시 | `created_at` | DATETIME(6) | NOT NULL |
| 수정 일시 | `updated_at` | DATETIME(6) | NOT NULL |

</details>

<details>
<summary><strong> 4. vulnerability_finding (탐지된 취약점) </strong></summary>

### 4. vulnerability_finding (탐지된 취약점)

*스캐너가 탐지한 취약점 결과로, 특정 파일(`analysis_files`)에 1:N으로 종속됩니다.*

| 설명 | 컬럼명 | 타입 | 제약 사항 |
| --- | --- | --- | --- |
| 취약점 결과 ID | `id` | BIGINT | PK, AUTO_INCREMENT |
| 분석 파일 ID | `analysis_file_id` | BIGINT | FK → `analysis_files.id`, NULL |
| 취약점 식별 룰 ID | `rule_id` | VARCHAR(255) | 코드 레벨 `RuleCatalog` 맵핑용 (FK 아님) |
| 취약점 유형 | `vulnerability_type` | ENUM | NOT NULL |
| 심각도 | `severity` | ENUM | NOT NULL |
| 취약점 제목 | `title` | VARCHAR(255) | NULL |
| 상세 설명 | `description` | TEXT | NULL |
| 시작 라인 | `start_line` | INT | NULL |
| 종료 라인 | `end_line` | INT | NULL |
| 탐지 코드 조각 | `code_snippet` | TEXT | NULL |

</details>

<details>
<summary><strong> +@ . 🔒 확정된 ENUM 정의 (탐지된 취약점) </strong></summary>

## 🔒 확정된 ENUM 정의

| 적용 컬럼 | 허용 값 (상수) | 설명 |
| --- | --- | --- |
| `analysis_requests.status` | `PENDING`, `SCANNING`, `EXPLAINING`, `COMPLETED`, `FAILED` | AI 설명 단계를 포함한 분석 파이프라인 상태 |
| `vulnerability_finding.severity` | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` | 취약점 심각도 (INFO 제외 확정) |
| `vulnerability_finding.vulnerability_type` | `SQL_INJECTION`, `HARDCODED_SECRET`, `XSS` | MVP 탐지 대상 3종 |

</details>

---



## 🛠️ 추후 도입 예정 / 미확정 사안

*현재는 `EXPLAINING` 상태 로직만 존재하며, 추후 AI가 생성한 고도화된 응답과 수정 코드를 별도로 저장할 필요가 있을 때 테이블로 분리할 예정입니다.*

| 설명 | 컬럼명 | 타입 | 제약 사항 |
| --- | --- | --- | --- |
| LLM 분석 ID | `id` | BIGINT | PK, AUTO_INCREMENT |
| 1:1 대상 취약점 | `finding_id` | BIGINT | FK → `vulnerability_finding.id` |
| 취약점 원인 | `cause` | TEXT | NULL |
| 위험성 설명 | `risk_description` | TEXT | NULL |
| 개선 방법 및 코드 | `fixed_code` | TEXT | NULL |
| 생성 일시 | `created_at` | DATETIME(6) | NOT NULL |

* **`project` 및 `project_file` 테이블:** 분석 단위를 `analysis_requests`로 일원화하면서 구조 단순화를 위해 제거되었습니다.
* **`security_rule` 테이블:** DB 조회의 비효율성을 줄이고자, DB 테이블에서 삭제하고 코드 내부의 불변 상수 클래스(`RuleCatalog.java`)로 편입되었습니다.
* **`Severity`의 `INFO` 레벨:** 스캐너 MVP 목적(위협 탐지)에 맞지 않아 데이터 무결성을 위해 제거되었습니다.


