
# api 주소 각 요청 시 json 형태


## POST `http://localhost:8080/api/v1/analyses`

 ** 1-4. 새 분석 요청 생성**

<details>
<summary><strong> 1-4. 새 분석 요청 생성 </strong></summary>

```
POST http://localhost:8080/api/v1/analyses
```

 ### Body

```
{
  "userId": 4,
  "title": "가나다",
  "language": "JAVA",
  "files": [
    {
      "filePath": "repository/StudentDetailRepository.java",
      "content": "package com.rookies6.myspringboot4project.repository;\n\nimport com.rookies6.myspringboot4project.entity.StudentDetail;\nimport org.springframework.data.jpa.repository.JpaRepository;\n\n@Repository\npublic interface StudentDetailRepository extends JpaRepository<StudentDetail, Long> {\n    Optional<StudentDetail> findByStudentId(Long studentId);\n}"
    },
    {
      "filePath": "repository/StudentRepository.java",
      "content": "package com.rookies6.myspringboot4project.repository;\n\nimport com.rookies6.myspringboot4project.entity.Student;\nimport org.springframework.data.jpa.repository.JpaRepository;\n\n@Repository\npublic interface StudentRepository extends JpaRepository<Student, Long> {\n    boolean existsByStudentNumber(String studentNumber);\n}"
    }
  ]
}

```

### Response

```
{
    "analysisId": 9,
    "createdAt": "2026-10-01T10:16:08.432142135",
    "criticalCount": 0,
    "files": [
        {
            "id": 15,
            "filePath": "repository/StudentDetailRepository.java",
            "content": "package com.rookies6.myspringboot4project.repository;\n\nimport com.rookies6.myspringboot4project.entity.StudentDetail;\nimport org.springframework.data.jpa.repository.JpaRepository;\n\n@Repository\npublic interface StudentDetailRepository extends JpaRepository<StudentDetail, Long> {\n    Optional<StudentDetail> findByStudentId(Long studentId);\n}"
        },
        {
            "id": 16,
            "filePath": "repository/StudentRepository.java",
            "content": "package com.rookies6.myspringboot4project.repository;\n\nimport com.rookies6.myspringboot4project.entity.Student;\nimport org.springframework.data.jpa.repository.JpaRepository;\n\n@Repository\npublic interface StudentRepository extends JpaRepository<Student, Long> {\n    boolean existsByStudentNumber(String studentNumber);\n}"
        }
    ],
    "highCount": 0,
    "language": "JAVA",
    "lowCount": 0,
    "mediumCount": 0,
    "status": "PENDING",
    "title": "가나다",
    "totalCount": 0,
    "vulnerabilities": []
}

```

</details>

 ### Request 필드

 | 필드 | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 분석을 요청한 사용자 ID |
| `title` | String | 분석 제목 |
| `language` | String | 분석 대상 언어 |
| `files` | Array | 분석할 파일 목록 |
| `files.filePath` | String | 파일 경로 |
| `files.content` | String | 파일 소스 코드 |

### Response 필드

 | 필드 | 타입 | 설명 |
| --- | --- | --- |
| `analysisId` | Long | 분석 고유 ID |
| `createdAt` | LocalDateTime | 분석 생성 시간 |
| `title` | String | 분석 제목 |
| `language` | String | 분석 대상 언어 |
| `status` | String | 분석 상태 |
| `totalCount` | int | 전체 취약점 개수 |
| `criticalCount` | long | Critical 취약점 개수 |
| `highCount` | long | High 취약점 개수 |
| `mediumCount` | long | Medium 취약점 개수 |
| `lowCount` | long | Low 취약점 개수 |
| `files` | Array | 분석 대상 파일 목록 |
| `vulnerabilities` | Array | 발견된 취약점 목록 |

### 분석 상태

 | 상태 | 설명 |
| --- | --- |
| `PENDING` | 분석 대기 |
| `SCANNING` | 분석 진행 중 |
| `COMPLETED` | 분석 완료 |
| `FAILED` | 분석 실패 |

**요청:** `userId`, `title`, `language`, `files`\
 **응답:** 요청 데이터 \+ `analysisId`, `createdAt`, `status`, 취약점 통계 및 결과 추가

---


