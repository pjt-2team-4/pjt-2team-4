

### `AnalysisFile` 엔티티

 | 필드 | 타입 | DB 컬럼 | 역할 |
| --- | --- | --- | --- |
| `id` | `Long` | `id` | 분석 파일 고유 ID |
| `analysisRequest` | `AnalysisRequest` | `analysis_request_id` | 어떤 분석 요청에 속한 파일인지 |
| `relativePath` | `String` | `relative_path` | 프로젝트 기준 상대 경로 |
| `fileName` | `String` | `file_name` | 파일명 |
| `language` | `String` | `language` | 파일 언어 |
| `content` | `String` | `content` | 분석 대상 소스 코드 |
| `lineCount` | `Integer` | `line_count` | 전체 코드 줄 수 |
| `findings` | `List<FindingVulnerability>` | 관계 매핑 | 해당 파일에서 발견된 취약점 목록 |
