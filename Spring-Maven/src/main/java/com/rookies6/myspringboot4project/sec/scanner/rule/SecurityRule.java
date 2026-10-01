package com.rookies6.myspringboot4project.sec.scanner.rule;

import com.rookies6.myspringboot4project.sec.common.enums.Language;

import com.rookies6.myspringboot4project.sec.common.enums.Severity;

import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 탐지 규칙 1개의 정의. DB 테이블이 아니라 코드 상수다(불변).
 * severity / cweId 는 여기서 확정되며 LLM이 바꾸지 못한다.
 */
@Getter
public class SecurityRule {

    private final String ruleId;
    private final VulnerabilityType vulnerabilityType;
    private final String cweId;
    private final Severity severity;
    private final Set<Language> targetLanguages;
    private final Pattern pattern;
    private final List<Pattern> excludePatterns;
    private final String title;
    private final String description;

    public SecurityRule(String ruleId,
                        VulnerabilityType vulnerabilityType,
                        Severity severity,
                        Set<Language> targetLanguages,
                        String regex,
                        List<String> excludeRegexes,
                        String title,
                        String description) {
        this.ruleId = ruleId;
        this.vulnerabilityType = vulnerabilityType;
        this.cweId = vulnerabilityType.getCweId();
        this.severity = severity;
        this.targetLanguages = Set.copyOf(targetLanguages);
        this.pattern = Pattern.compile(regex);
        this.excludePatterns = List.copyOf(excludeRegexes.stream().map(Pattern::compile).toList());
        this.title = title;
        this.description = description;
    }

    public boolean appliesTo(Language language) {
        return targetLanguages.contains(language);
    }

    public boolean matches(String line) {
        return pattern.matcher(line).find();
    }

    /** 오탐 제외 조건에 걸리는지 (매개변수화 쿼리, 환경변수 참조 등) */
    public boolean isExcluded(String line) {
        return excludePatterns.stream().anyMatch(p -> p.matcher(line).find());
    }
}

