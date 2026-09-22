
# 해당 api 설명
```md
templates/
├──api-guide.html	# 하위 도메인-관리 에 연결해주는 중심 page
└── fragments/
    └── api/
        ├── user-management.html	(유저 관련)
        ├── project-management.html	(프로젝트 관련)
        ├── analysis-management.html	(분석 관련)
        ├── code-file-management.html	(코드 관련)
        │
        ├── user/
        │   ├── get-user.html      (유저 목록/단건 조회용 HTML 조각)
        │   ├── post-user.html     (유저 생성 폼 조각)
        │   ├── put-user.html      (유저 수정 폼 조각)
        │   └── delete-user.html   (유저 삭제 버튼/모달 조각)
        │
        ├── project/
        │   ├── get-project.html
        │   ├── post-project.html
        │   └── delete-project.html
        │
        ├── analysis/
        │   ├── get-analysis.html  (분석 결과 대시보드 뷰 조각)
        │   └── post-analysis.html (코드 입력 및 분석 요청 폼 조각)
        │
        └── code-file/
              ├── get-code-file.html
              ├── post-code-file.html
              └── delete-code-file.html

```
