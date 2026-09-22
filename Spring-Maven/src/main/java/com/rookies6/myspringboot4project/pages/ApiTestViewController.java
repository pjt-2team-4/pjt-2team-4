package com.rookies6.myspringboot4project.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiTestViewController {

    // 2. 유저 관리 화면
    @GetMapping("/user-management")
    public String userManagementPage() {
        return "fragments/api/user-management"; 
    }

    // 3. 프로젝트 만 관리 화면
    @GetMapping("/project-management")
    public String projectManagementPage() {
        return "fragments/api/project-management";
    }

    // 4. 코드 파일 관리 화면
    @GetMapping("/code-file-management")
    public String codeFileManagementPage() {
        return "fragments/api/code-file-management";
    }

    // 5. 보안 분석 관리 화면
    @GetMapping("/analysis-management")
    public String analysisManagementPage() {
        return "fragments/api/analysis-management";
    }
}