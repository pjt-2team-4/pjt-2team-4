
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
    "errorMessage": null
}
```


</details>



 ### 주요 필드

 | 필드 | 설명 |
| --- | --- |
| `analysisId` | 분석 ID |
| `status` | 분석 상태 |


### `status`

 - `PENDING` — 대기
- `SCANNING` — 분석 중
- `COMPLETED` — 완료
- `FAILED` — 실패

---
