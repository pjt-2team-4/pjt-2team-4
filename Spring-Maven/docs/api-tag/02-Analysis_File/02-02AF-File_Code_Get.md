
# api 주소 각 요청 시 json 형태


## GET `/api/v1/analyses/{analysisId}/files/{fileId}

 ** 2-2. 단일 분석 파일 상세 조회 **

<details>
<summary><strong> 2-2. 단일 분석 파일 상세 조회 </strong></summary>

```
GET http://localhost:8080/api/v1/analyses/1/files/1
```

 ### Response

```
{
    "content": "public class BankingService {\n    private String awsSecretKey = \"AKIAIOSFODNN7EXAMPLE\";\n    public void processTransaction() {\n        System.out.println(\"Processing banking...\");\n    }\n}",
    "fileId": 1,
    "fileName": "BankingService.java",
    "findingMarkers": [],
    "language": "java",
    "lineCount": 6,
    "relativePath": "src/main/java/BankingService.java"
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

