package com.rookies6.myspringboot4project.sec.scanner;

import com.rookies6.myspringboot4project.sec.common.enums.Language;

import com.rookies6.myspringboot4project.sec.analysis.entity.Severity;

import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import com.rookies6.myspringboot4project.sec.scanner.rule.RuleCatalog;
import com.rookies6.myspringboot4project.sec.scanner.rule.SecurityRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 규칙 기반 보안 취약점 탐지기.
 * 외부 호출(LLM, 네트워크, DB)이 전혀 없으므로 순수 단위 테스트가 가능하다.
 */
@Component
public class SecurityScanner {

    private static final String SQLI_STATEMENT_RULE_ID = "SQLI-002";
    private static final Pattern STATEMENT_DECLARATION = Pattern.compile(
            "\\bStatement\\s+([a-zA-Z_$][\\w$]*)\\s*(?:=|;)");

    /** 테스트 코드는 실제 위험이 아니므로 심각도를 1단계 낮춘다 */
    private static final Pattern TEST_PATH = Pattern.compile(
            "(?i)(^|/)(test|tests|__tests__|spec)/|\\.(test|spec)\\.[a-z]+$|Test\\.java$|Tests\\.java$");

    /**
     * @param relativePath 파일 상대 경로 (예: src/routes/users.ts)
     * @param language     파일 언어
     * @param content      코드 본문
     * @return 탐지된 취약점 목록 (없으면 빈 리스트)
     */
    public List<RawFinding> scan(String relativePath, Language language, String content) {
        if (language == null || !language.isSupported() || content == null || content.isBlank()) {
            return List.of();
        }

        String[] originalLines = content.split("\n", -1);
        String[] scanLines = stripComments(originalLines, language);
        List<SecurityRule> rules = RuleCatalog.forLanguage(language);
        if (rules.isEmpty()) {
            return List.of();
        }

        Map<Integer, SecurityRule> hitByLine = collectHits(scanLines, rules, language);
        List<RawFinding> findings = mergeConsecutive(hitByLine, originalLines, relativePath);
        return findings;
    }

    // ── 1) 라인별 매칭: 한 라인에 여러 룰이 걸리면 심각도가 높은 1건만 남긴다 ──
    private Map<Integer, SecurityRule> collectHits(String[] lines, List<SecurityRule> rules,
                                                   Language language) {
        Map<Integer, SecurityRule> hits = new TreeMap<>();
        Set<String> statementVariables = new HashSet<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            if (language == Language.JAVA) {
                Matcher declaration = STATEMENT_DECLARATION.matcher(line);
                while (declaration.find()) {
                    statementVariables.add(declaration.group(1));
                }
            }
            int lineNumber = i + 1;

            for (SecurityRule rule : rules) {
                if (!rule.matches(line) || rule.isExcluded(line)) {
                    continue;
                }
                if (SQLI_STATEMENT_RULE_ID.equals(rule.getRuleId())
                        && !matchesDeclaredStatement(rule, line, statementVariables)) {
                    continue;
                }
                SecurityRule previous = hits.get(lineNumber);
                if (previous == null
                        || rule.getSeverity().getWeight() > previous.getSeverity().getWeight()) {
                    hits.put(lineNumber, rule);
                }
            }
        }
        return hits;
    }

    private boolean matchesDeclaredStatement(SecurityRule rule, String line,
                                             Set<String> statementVariables) {
        Matcher execution = rule.getPattern().matcher(line);
        while (execution.find()) {
            if (statementVariables.contains(execution.group(1))) {
                return true;
            }
        }
        return false;
    }

    // ── 2) 같은 룰이 연속된 라인에서 걸리면 하나의 구간으로 병합 ──
    private List<RawFinding> mergeConsecutive(Map<Integer, SecurityRule> hits,
                                              String[] originalLines,
                                              String relativePath) {
        List<RawFinding> findings = new ArrayList<>();
        SecurityRule current = null;
        int start = 0;
        int end = 0;

        for (Map.Entry<Integer, SecurityRule> entry : hits.entrySet()) {
            int lineNumber = entry.getKey();
            SecurityRule rule = entry.getValue();

            boolean continuation = current != null
                    && current.getRuleId().equals(rule.getRuleId())
                    && lineNumber == end + 1;

            if (continuation) {
                end = lineNumber;
                continue;
            }
            if (current != null) {
                findings.add(toFinding(current, start, end, originalLines, relativePath));
            }
            current = rule;
            start = lineNumber;
            end = lineNumber;
        }
        if (current != null) {
            findings.add(toFinding(current, start, end, originalLines, relativePath));
        }
        return findings;
    }

    private RawFinding toFinding(SecurityRule rule, int startLine, int endLine,
                                 String[] originalLines, String relativePath) {
        Severity severity = isTestFile(relativePath)
                ? rule.getSeverity().downgrade()
                : rule.getSeverity();

        return new RawFinding(
                relativePath,
                rule.getRuleId(),
                rule.getVulnerabilityType(),
                rule.getCweId(),
                severity,
                startLine,
                endLine,
                extractSnippet(originalLines, startLine, endLine)
        );
    }

    private boolean isTestFile(String relativePath) {
        return relativePath != null
                && TEST_PATH.matcher(relativePath.replace('\\', '/')).find();
    }

    /** 1-based, 양끝 포함 */
    private String extractSnippet(String[] lines, int startLine, int endLine) {
        int from = Math.max(1, startLine);
        int to = Math.min(lines.length, endLine);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if (i > from) {
                sb.append("\n");
            }
            sb.append(lines[i - 1].strip());
        }
        return sb.toString();
    }

    // ── 주석 라인 제거: 라인 번호를 유지하기 위해 빈 문자열로 치환한다 ──
    private String[] stripComments(String[] lines, Language language) {
        String[] result = new String[lines.length];
        String blockCommentEnd = null;
        boolean supportsSlashComments = language != Language.PYTHON;
        boolean supportsHashComments = language == Language.PYTHON;
        boolean supportsHtmlComments = language == Language.HTML;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].strip();

            if (blockCommentEnd != null) {
                result[i] = "";
                if (trimmed.contains(blockCommentEnd)) {
                    blockCommentEnd = null;
                }
                continue;
            }
            if ((supportsSlashComments && trimmed.startsWith("//"))
                    || (supportsHashComments && trimmed.startsWith("#"))) {
                result[i] = "";
                continue;
            }
            if (supportsSlashComments && trimmed.startsWith("/*")) {
                result[i] = "";
                if (!trimmed.contains("*/")) {
                    blockCommentEnd = "*/";
                }
                continue;
            }
            if (supportsHtmlComments && trimmed.startsWith("<!--")) {
                result[i] = "";
                if (!trimmed.contains("-->")) {
                    blockCommentEnd = "-->";
                }
                continue;
            }
            result[i] = lines[i];
        }
        return result;
    }

    /** 파일별 탐지 결과를 합쳐 최고 심각도를 구한다 (Worker에서 사용) */
    public static Severity overallSeverity(List<RawFinding> findings) {
        return Severity.highest(findings.stream().map(RawFinding::severity).toList());
    }

    /** 룰별 탐지 건수 집계 */
    public static Map<String, Integer> countByRule(List<RawFinding> findings) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (RawFinding finding : findings) {
            counts.merge(finding.ruleId(), 1, Integer::sum);
        }
        return counts;
    }
}