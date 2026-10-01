

 ### `AnalysisRequest` 주요 필드

 | 필드 | 타입 | DB 컬럼 | 역할 |
| --- | --- | --- | --- |
| `id` | `Long` | `id` | 분석 요청 고유 ID |
| `user` | `User` | `user_id` | 분석을 요청한 사용자 |
| `title` | `String` | `title` | 분석 제목 |
| `language` | `String` | `language` | 분석 대상 언어 |
| `status` | `AnalysisStatus` | `status` | 현재 분석 상태 |
| `estimatedDurationSeconds` | `Integer` | `estimated_duration_seconds` | 예상 분석 시간 |
| `errorMessage` | `String` | `error_message` | 분석 실패 시 오류 메시지 |
| `analysisFiles` | `List<AnalysisFile>` | FK 관계 | 해당 분석에 포함된 파일 목록 |


