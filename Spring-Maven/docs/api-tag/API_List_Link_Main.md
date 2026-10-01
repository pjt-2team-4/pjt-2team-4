
 # API 목록 링크 정리

 ## 01 - Analysis Request

 | 파일 | 요청구분 | API 주소 | 목적 |
| --- | --- | --- | --- |
| [01-01AR-All_List_History_Get.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/01-Analysis_Request/01-01AR-All_List_History_Get.md) | GET | `/api/v1/analyses` | 전체 분석 히스토리 목록 조회 |
| [01-02AR-ID_Once_Get.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/01-Analysis_Request/01-02AR-ID_Once_Get.md) | GET | `/api/v1/analyses/{id}` | ID로 분석 결과 상세 조회 |
| [01-03AR-Async_Scan_Status_Get.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/01-Analysis_Request/01-03AR-Async_Scan_Status_Get.md) | GET | `/api/v1/analyses/{id}/status` | 비동기 스캔 진행 상태 조회 |
| [01-04AR-Create_Analysis.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/01-Analysis_Request/01-04AR-Create_Analysis.md) | POST | `/api/v1/analyses` | 새 분석 요청 생성 |
| [01-05AR-Delete_Analysis.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/01-Analysis_Request/01-05AR-Delete_Analysis.md) | DELETE | `/api/v1/analyses/{id}` | 분석 히스토리 삭제 |

---

 ## 02 - Analysis File

 | 파일 | 요청구분 | API 주소 | 목적 |
| --- | --- | --- | --- |
| [02-01AF-File_List_Get.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/02-Analysis_File/02-01AF-File_List_Get.md) | GET | `/api/v1/analysis-files/request/{requestId}` | 특정 분석 요청의 파일 목록 조회 |
| [02-02AF-File_Code_Get.md](https://github.com/pjt-2team-4/pjt-2team-4/blob/Inchan/Spring-Maven/docs/api-tag/02-Analysis_File/02-02AF-File_Code_Get.md) | GET | `/api/v1/analysis-files/{id}` | 단일 분석 파일 상세 조회 |

---

 ### 전체 구조

```
01-Analysis_Request
├── 01-01AR-All_List_History_Get.md
├── 01-02AR-ID_Once_Get.md
├── 01-03AR-Async_Scan_Status_Get.md
├── 01-04AR-Create_Analysis.md
└── 01-05AR-Delete_Analysis.md

02-Analysis_File
├── 02-01AF-File_List_Get.md
└── 02-02AF-File_Code_Get.md
```
