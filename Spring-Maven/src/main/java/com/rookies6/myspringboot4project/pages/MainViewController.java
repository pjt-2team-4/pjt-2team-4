package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainViewController {

    private final AnalysisRequestRepository analysisRequestRepository;

    // 💡 공통 UI 설정 값을 만들어주는 헬퍼 메서드 (중복 코드 제거)
    // Map 타입을 Object로 변경하여 중첩 구조 허용
private Map<String, Object> getUiSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.put("title", "내 프로젝트 메인 화면");
    settings.put("favicon", "/images/favicon.ico");
    
    // 💡 theme.color 에러를 막기 위한 중첩 맵 추가
    Map<String, String> theme = new HashMap<>();
    theme.put("color", "default"); // 또는 원하는 색상 값
    settings.put("theme", theme);
    
    return settings;
}

    // 루트 홈 (대시보드) 화면 예시 (만약 존재한다면)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("uiSettings", getUiSettings());

        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "index"; // templates/index.html 또는 대시보드 템플릿명
    }

    // 7. API 가이드 화면
    @GetMapping("/api-guide")
    public String apiGuide(Model model) {
        model.addAttribute("uiSettings", getUiSettings());

        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "api-guide"; // templates/api-guide.html
    }

    // 8. 코드 보안 분석 메인 화면
    @GetMapping("/user/analysis")
    public String analysis(Model model) {
        model.addAttribute("uiSettings", getUiSettings());

        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analysis"; // templates/analysis.html
    }
}