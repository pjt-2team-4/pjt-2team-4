
# api 주소 각 요청 시 json 형태


## GET `http://localhost:8080/api/v1/analyses/{id}`

 ** 1-2. ID로 분석 결과 상세 조회**

<details>
<summary><strong> 1-2. ID로 분석 결과 상세 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analyses/1
```

 ### Response

```
{
    "analysisId": 1,
    "createdAt": "2026-09-30T11:05:14.237497",
    "criticalCount": 0,
    "files": [
        {
            "id": 1,
            "filePath": "src/main/java/BankingService.java",
            "content": "public class BankingService {\n    private String awsSecretKey = \"AKIAIOSFODNN7EXAMPLE\";\n    public void processTransaction() {\n        System.out.println(\"Processing banking...\");\n    }\n}"
        },
        {
            "id": 2,
            "filePath": "src/main/java/AccountController.java",
            "content": "public class AccountController {\n    public void getAccount(String accountNumber) {\n        System.out.println(\"Account: \" + accountNumber);\n    }\n}"
        },
        {
            "id": 3,
            "filePath": "src/main/java/TransactionDao.java",
            "content": "public class TransactionDao {\n    // TODO: Use parameterized queries\n    public String query = \"SELECT * FROM accounts\";\n}"
        }
    ],
    "highCount": 0,
    "language": "java",
    "lowCount": 0,
    "mediumCount": 0,
    "status": "COMPLETED",
    "title": "스프링 시큐어 뱅킹 시스템 취약점 분석",
    "totalCount": 0,
    "vulnerabilities": []
}
```


</details>



 ### 주요 필드

 | 필드 | 설명 |
| --- | --- |
| `analysisId` | 분석 ID |
| `createdAt` | 분석 생성 시간 |
| `title` | 분석 제목 |
| `language` | 분석 언어 |
| `status` | 분석 상태 |
| `totalCount` | 전체 취약점 수 |
| `criticalCount` | Critical 취약점 수 |
| `highCount` | High 취약점 수 |
| `mediumCount` | Medium 취약점 수 |
| `lowCount` | Low 취약점 수 |
| `files` | 분석 대상 파일 목록 |
| `vulnerabilities` | 발견된 취약점 목록 |

### `status`

 - `PENDING` — 대기
- `SCANNING` — 분석 중
- `COMPLETED` — 완료
- `FAILED` — 실패

---
