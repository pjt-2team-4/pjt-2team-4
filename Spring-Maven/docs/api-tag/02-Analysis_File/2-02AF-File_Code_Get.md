
# api 주소 각 요청 시 json 형태


## GET `/api/v1/analysis-files/{id}`

 ** 2-2. 단일 분석 파일 상세 조회 **

<details>
<summary><strong> 2-2. 단일 분석 파일 상세 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analysis-files/7
```

 ### Response

```
{
    "content": "import org.springframework.web.bind.annotation.*;\n@RestController\npublic class FileController {\n    @GetMapping(\"/search\")\n    public String search(@RequestParam String keyword) {\n        return \"<div>Result: \" + keyword + \"</div>\";\n    }\n}",
    "fileId": 7,
    "fileName": "FileController.java",
    "relativePath": "src/main/java/FileController.java",
    "vulnerabilities": []
}
```


</details>



 ### 주요 필드

 | 필드 | 타입 | 설명 |
| --- | --- | --- |
| `fileId` | Long | 파일 고유 ID |
| `fileName` | String | 파일 이름 |
| `relativePath` | String | 프로젝트 기준 상대 경로 |
| `content` | String | 파일의 전체 소스 코드 |
| `vulnerabilities` | Array | 해당 파일에서 발견된 취약점 목록 |

### 예시

```
{
  "fileId": 7,
  "fileName": "FileController.java",
  "relativePath": "src/main/java/FileController.java",
  "content": "...",
  "vulnerabilities": []
}
```

 - `content` → 분석 대상 파일의 실제 소스 코드
- `vulnerabilities` → 해당 파일에서 발견된 취약점 목록
- `vulnerabilities: []` → 발견된 취약점이 없음
---
