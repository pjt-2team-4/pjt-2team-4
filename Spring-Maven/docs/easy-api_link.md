
#  연결 및 api


<details>
<summary><strong> 1. users (회원) </strong></summary>


[User.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/BE/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/user/entity/User.java)


## 1-1. 데이터베이스 테이블 명세 users (회원)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 회원고유ID | id | BIGINT | PK, AUTO_INCREMENT |
| 이메일 | email | VARCHAR(100) | NOT NULL, UNIQUE |
| 해시된 비밀번호 | password | VARCHAR(255) | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |


## 1-2. API 명세 (User REST API)

| 기능 | Method | URI | 파라미터 / 요청 본문 | 설명 |
| --- | --- | --- | --- | --- |
| **회원가입** | ==POST== | `/api/user` | **Body:** email, password | 새로운 회원을 등록합니다. (이메일 중복 체크 포함) |
| **전체 회원 조회** | ==GET== | `/api/user` | *없음* | 등록된 모든 회원 목록을 조회합니다. |
| **ID로 회원 조회** | ==GET== | `/api/user/{id}` | **Path:** id (BIGINT) | 특정 ID의 회원 정보를 단건 조회합니다. |
| **이메일로 회원 조회** | ==GET== | `/api/user/email/{email}` | **Path:** email (VARCHAR) | 특정 이메일을 가진 회원의 정보를 조회합니다. |
| **회원 정보 수정** | ==PUT== | `/api/user/{id}` | **Path:** id (BIGINT)<br>

<br>**Body:** email, password | 기존 회원의 이메일 및 비밀번호 정보를 수정합니다. |
| **회원 삭제** | ==DELETE== | `/api/user/{id}` | **Path:** id (BIGINT) | 특정 회원을 삭제합니다. (존재하지 않을 경우 예외 처리) |

</details>

<details>
<summary><strong> 2. project (프로젝트) </strong></summary>


[Project.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/BE/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/project/entity/Project.java)


### 2-1. 데이터베이스 테이블 명세 project (프로젝트)

| 설명 | 컬럼 | 타입 | 제약 |
| --- | --- | --- | --- |
| 프로젝트ID | id | BIGINT | PK |
| 유저ID | user_id | BIGINT | FK → users.id, NOT NULL |
| 프로젝트 이름 | title | VARCHAR(100) | NOT NULL |
| 언어 | language | VARCHAR(20) | NOT NULL |
| 생성일시 | created_at | DATETIME(6) | NOT NULL |
| 수정일시 | updated_at | DATETIME(6) | NOT NULL |


### 2-2. 프로젝트 API 명세

| 분류 | 기능 | Method | Endpoint (URI) | 요청 데이터 |
| --- | --- | --- | --- | --- |
| **프로젝트** | 등록 | `POST` | `/api/project` | `[Body]` 이름, 설명, 언어 |
|  | 단건 조회 | `GET` | `/api/project/{id}` | `[Path]` id |
|  | 목록 조회 | `GET` | `/api/project` | 없음 |
|  | 삭제 | `DELETE` | `/api/project/{id}` | `[Path]` id |



</details>

<details>
<summary><strong> 3. CodeFile (프로젝트_파일) </strong></summary>


[CodeFile.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/BE/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/codefile/entity/CodeFile.java)


### 3-1. 데이터베이스 테이블 명세 project_file

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


### 3-2. 프로젝트 API 명세

| 분류 | 기능 | Method | Endpoint (URI) | 요청 데이터 |
| --- | --- | --- | --- | --- |
| **코드 파일** | 생성 | `POST` | `/api/code-file` | `[Body]` 프로젝트 ID, 파일명, 언어, 소스 코드 |
|  | 목록 조회 | `GET` | `/api/code-file` | `[Query]` projectId |
|  | 상세 조회 | `GET` | `/api/code-file/{id}` | `[Path]` id |
|  | 삭제 | `DELETE` | `/api/code-file/{id}` | `[Path]` id |


</details>

<details>
<summary><strong> 4. analysis_request (분석 요청) </strong></summary>


[AnalysisRequest.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/BE/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysis/entity/AnalysisRequest.java)


### 4-1. 데이터베이스 테이블 명세 analysis_request

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


### 4-2. 프로젝트 API 명세

| **보안 분석** | 분석 요청 | `POST` | `/api/analysis` | `[Body]` 프로젝트 ID, 언어, 소스 코드 |
|  | 결과 조회 | `GET` | `/api/analysis/{id}` | `[Path]` id |

</details>

[VulnerabilityFinding.java](https://github.com/pjt-2team-4/pjt-2team-4/blob/BE/Spring-Maven/src/main/java/com/rookies6/myspringboot4project/sec/analysis/entity/VulnerabilityFinding.java)





