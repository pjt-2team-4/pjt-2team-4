package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainViewController {

    private final AnalysisRequestRepository analysisRequestRepository;

    // 루트 홈 (대시보드) 화면 예시 (만약 존재한다면)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "index"; // templates/index.html 또는 대시보드 템플릿명
    }

    // 7. API 가이드 화면
    @GetMapping("/api-guide")
    public String apiGuide(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "api-guide"; // templates/api-guide.html
    }

    // 8. 코드 보안 분석 메인 화면
    @GetMapping("/user/analysis")
    public String analysis(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analysis"; // templates/analysis.html
    }
}