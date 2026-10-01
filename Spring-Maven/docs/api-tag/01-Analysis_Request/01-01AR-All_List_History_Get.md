
# api 주소 각 요청 시 json 형태


## GET `/api/v1/analyses`

 ** 1-1. 전체 분석 히스토리 목록 조회**

<details>
<summary><strong> 1-1. 전체 분석 히스토리 목록 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analyses
```

 ### Response

```
[
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
    },
    {
        "analysisId": 2,
        "createdAt": "2026-09-30T11:05:14.326968",
        "criticalCount": 0,
        "files": [
            {
                "id": 4,
                "filePath": "src/main/java/PaymentController.java",
                "content": "import org.springframework.web.bind.annotation.*;\n@RestController\npublic class PaymentController {\n    @GetMapping(\"/receipt\")\n    public String getReceipt(@RequestParam String userName) {\n        return \"<html><body>Receipt for: \" + userName + \"</body></html>\";\n    }\n}"
            },
            {
                "id": 5,
                "filePath": "src/main/java/PaymentService.java",
                "content": "public class PaymentService {\n    public void pay() {\n        System.out.println(\"Payment gateway connecting...\");\n    }\n}"
            }
        ],
        "highCount": 0,
        "language": "java",
        "lowCount": 0,
        "mediumCount": 0,
        "status": "SCANNING",
        "title": "이커머스 결제 API 서버 보안 검사",
        "totalCount": 0,
        "vulnerabilities": []
    },
    {
        "analysisId": 3,
        "createdAt": "2026-09-30T11:05:14.331225",
        "criticalCount": 0,
        "files": [
            {
                "id": 6,
                "filePath": "src/main/java/AuthRepository.java",
                "content": "import java.sql.*;\npublic class AuthRepository {\n    public boolean login(String email, String password) throws SQLException {\n        Connection conn = DriverManager.getConnection(\"jdbc:mysql://localhost/db\", \"root\", \"\");\n        Statement stmt = conn.createStatement();\n        String query = \"SELECT * FROM users WHERE email = '\" + email + \"' AND password = '\" + password + \"'\";\n        ResultSet rs = stmt.executeQuery(query);\n        return rs.next();\n    }\n}"
            }
        ],
        "highCount": 0,
        "language": "java",
        "lowCount": 0,
        "mediumCount": 0,
        "status": "PENDING",
        "title": "사용자 인증 마이크로서비스 코드 스캔",
        "totalCount": 0,
        "vulnerabilities": []
    },
    {
        "analysisId": 4,
        "createdAt": "2026-09-30T11:05:14.334107",
        "criticalCount": 0,
        "files": [
            {
                "id": 7,
                "filePath": "src/main/java/FileController.java",
                "content": "import org.springframework.web.bind.annotation.*;\n@RestController\npublic class FileController {\n    @GetMapping(\"/search\")\n    public String search(@RequestParam String keyword) {\n        return \"<div>Result: \" + keyword + \"</div>\";\n    }\n}"
            }
        ],
        "highCount": 0,
        "language": "java",
        "lowCount": 0,
        "mediumCount": 0,
        "status": "FAILED",
        "title": "클라우드 파일 관리 시스템 진단",
        "totalCount": 0,
        "vulnerabilities": []
    }
]
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
