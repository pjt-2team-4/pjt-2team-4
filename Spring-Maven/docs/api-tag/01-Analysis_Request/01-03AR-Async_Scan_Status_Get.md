
# api 주소 각 요청 시 json 형태


## GET `http://localhost:8080/api/v1/analyses/{id}/status`

 ** 1-3. 비동기 스캔 진행 상태 조회**

<details>
<summary><strong> 1-3. 비동기 스캔 진행 상태 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analyses/1/status
```

 ### Response

```
{
    "analysisId": 1,
    "status": "COMPLETED",
    "stage": null,
    "stageLabel": null,
    "progress": 0,
    "currentFile": null,
    "processedFiles": 0,
    "totalFiles": 0,
    "findingsSoFar": 0,
    "totalFindings": 0,
    "explainedFindings": 0,
    "recentLogs": [],
    "startedAt": null,
    "durationSeconds": 0,
    "overallSeverity": null,
    "completedAt": null,
    "errorMessage": null
}
```


</details>


### 진행 상태 관련 필드

 | 필드 | 타입 | 의미 |
| --- | --- | --- |
| `status` | `AnalysisStatus` | 전체 분석 상태 |
| `stage` | `String` | 현재 처리 단계 |
| `progress` | `Integer` | 전체 진행률 |
| `currentFile` | `String` | 현재 분석 중인 파일 |
| `processedFiles` | `Integer` | 처리 완료 파일 수 |
| `totalFiles` | `Integer` | 전체 파일 수 |
| `findingsSoFar` | `Integer` | 현재까지 탐지된 취약점 수 |
| `totalFindings` | `Integer` | 전체 취약점 수 |
| `explainedFindings` | `Integer` | 설명 완료된 취약점 수 |
| `startedAt` | `LocalDateTime` | 분석 시작 시각 |
| `completedAt` | `LocalDateTime` | 분석 종료 시각 |

### `AnalysisStatus` 상태값

 | 상태 | 의미 | Worker 흐름 |
| --- | --- | --- |
| `PENDING` | 분석 대기 | 최초 생성 |
| `SCANNING` | 취약점 탐지 중 | Scanner 실행 |
| `EXPLAINING` | 취약점 설명 생성 중 | LLM 처리 |
| `COMPLETED` | 분석 완료 | 정상 종료 |
| `FAILED` | 분석 실패 | 예외 발생 |

