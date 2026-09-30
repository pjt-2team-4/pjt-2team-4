# sec/ 폴더 하위 구조



## 1. analysis/
```md
analysis/
├── controller/
│   └── AnalysisController.java
│
├── service/
│   ├── AnalysisWorker.java
│   └── AnalysisService.java
│
├── entity/
│   ├── AnalysisRequest.java
│   └── AnalysisStatus.java
│
├── repository/
│   └── AnalysisRequestRepository.java
│
└── dto/
    └── AnalysisDTO.java

```


## 2. analysisfile/
```md
analysisfile/
├── controller/
│   └── AnalysisFileController.java
│
├── service/
│   └── AnalysisFileService.java
│
├── entity/
│   └── AnalysisFile.java
│
├── repository/
│   └── AnalysisFileRepository.java
│
└── dto/
    └── AnalysisFileDTO.java
```


## 3. common/enums/
```md
sec/common/enums/
│
├── FindingStatus.java
│
├── FindingVulnerability.java
│
├── Language.java
│
├── Severity.java
│
└── VulnerabilityType.java
```


## 4. scanner/
```md
scanner/
├── SecurityScanner.java
│
├── rule/
│   ├── SecurityRule.java
│   └── RuleCatalog.java
│
└── dto/
    └── RawFinding.java
```

