package com.rookies6.myspringboot4project.pages;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/analyses")
@RequiredArgsConstructor
public class AnalysisViewController {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final UserRepository userRepository;

    // ==========================================
    // [Flow A] 기존 파일 중 선택해서 "재분석" 하는 화면
    // ==========================================
    @GetMapping
    public String analysisPage(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analysis"; // templates/analysis.html
    }

    // ==========================================
    // [Flow B] 로컬에서 파일을 드래그앤드롭하여 "새로 업로드/분석" 하는 화면
    // ==========================================
    @GetMapping("/new")
    public String uploadAnalysisForm(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analyses/analysis-upload"; // templates/analyses/analysis-upload.html
    }

    // ==========================================
    // 공통 조회 및 관리 화면
    // ==========================================
    @GetMapping("/list")
    public String listAnalyses(Model model) {
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analyses/list"; // templates/analyses/list.html
    }

    @GetMapping("/{id}")
    public String analysisDetail(@PathVariable("id") Long id, Model model) {
        AnalysisRequest analysis = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석 요청입니다. ID: " + id));

        model.addAttribute("analysis", analysis);
        model.addAttribute("analyses", analysisRequestRepository.findAll());

        return "analyses/detail"; // templates/analyses/detail.html
    }

    // ==========================================
    // 단순 폼 Submit 처리용 (JS Fetch를 안 쓸 때 대비한 Fallback)
    // ==========================================
    @PostMapping
    public String createAnalysis(@RequestParam("title") String title, 
                                 @RequestParam(value = "language", defaultValue = "Java") String language) {
        
        User defaultUser = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("등록된 유저가 없습니다."));

        AnalysisRequest newRequest = AnalysisRequest.builder()
                .user(defaultUser)
                .title(title)
                .language(language)
                .status(com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus.PENDING)
                .estimatedDurationSeconds(60)
                .build();
                
        analysisRequestRepository.save(newRequest);
        return "redirect:/analyses/list";
    }

    @GetMapping("/{id}/edit")
    public String editAnalysisForm(@PathVariable("id") Long id, Model model) {
        AnalysisRequest analysis = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석입니다."));
        model.addAttribute("analysis", analysis);
        model.addAttribute("analyses", analysisRequestRepository.findAll());
        return "analyses/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAnalysis(@PathVariable("id") Long id, @RequestParam("title") String title) {
        AnalysisRequest analysis = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석입니다."));
        // title 수정 로직 필요시 추가
        analysisRequestRepository.save(analysis);
        return "redirect:/analyses/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteAnalysis(@PathVariable("id") Long id) {
        analysisRequestRepository.deleteById(id);
        return "redirect:/analyses/list";
    }
}