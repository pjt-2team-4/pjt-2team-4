
# api 주소 각 요청 시 json 형태


## GET `/api/v1/analyses/{analysisId}/files

 ** 2-1. 특정 분석 요청 ID에 속한 전체 파일 목록 조회 **

<details>
<summary><strong> 2-1. 특정 분석 요청 ID에 속한 전체 파일 목록 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analyses/1/files
```

 ### Response

```
[
    {
        "fileId": 1,
        "fileName": "BankingService.java",
        "fileSizeBytes": 187,
        "findingCount": 0,
        "highestSeverity": null,
        "language": "java",
        "lineCount": 6,
        "relativePath": "src/main/java/BankingService.java"
    },
    {
        "fileId": 2,
        "fileName": "AccountController.java",
        "fileSizeBytes": 148,
        "findingCount": 0,
        "highestSeverity": null,
        "language": "java",
        "lineCount": 5,
        "relativePath": "src/main/java/AccountController.java"
    },
    {
        "fileId": 3,
        "fileName": "TransactionDao.java",
        "fileSizeBytes": 122,
        "findingCount": 0,
        "highestSeverity": null,
        "language": "java",
        "lineCount": 4,
        "relativePath": "src/main/java/TransactionDao.java"
    }
]
```


</details>



 ### 주요 필드

 | 필드 | 타입 | 설명 |
| --- | --- | --- |
| `fileId` | Long | 파일 고유 ID |
| `fileName` | String | 파일 이름 |
| `relativePath` | String | 프로젝트 기준 상대 경로 |
| `language` | String | 파일의 프로그래밍 언어 |
| `lineCount` | int | 파일 전체 라인 수 |
| `findingCount` | int | 해당 파일에서 발견된 취약점 개수 |


---
