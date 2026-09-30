package com.rookies6.myspringboot4project.sec.scanner.rule;

import com.rookies6.myspringboot4project.sec.common.enums.Language;
import com.rookies6.myspringboot4project.sec.analysis.entity.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;

import java.util.List;
import java.util.Set;


/**
 * MVP 탐지 룰 카탈로그 (SQL Injection 4 / Hardcoded Secret 5 / XSS 5).
 * 룰을 추가할 때는 반드시 SecurityScannerTest에 취약 코드·안전 코드 쌍을 함께 추가한다.
 */
public final class RuleCatalog {

    private RuleCatalog() {
    }

    private static final Set<Language> JS_TS = Set.of(Language.JAVASCRIPT, Language.TYPESCRIPT);
    private static final Set<Language> ALL = Set.of(
            Language.JAVA, Language.JAVASCRIPT, Language.TYPESCRIPT, Language.PYTHON, Language.HTML);

    /** 매개변수화 쿼리를 쓰고 있으면 탐지하지 않는다 */
    private static final List<String> SQL_EXCLUDES = List.of(
            "(?i)(?=.*\\bPreparedStatement\\b)(?=.*(?:=\\s*\\?|\\$\\d+|:\\w+))"
    );

    /** 환경변수 참조 / 플레이스홀더 값은 탐지하지 않는다 */
    private static final List<String> SECRET_EXCLUDES = List.of(
            "(?i)process\\.env|System\\.getenv|os\\.environ|\\$\\{",
            "(?i)[:=]\\s*(?:\"(?:changeme|change_me|your[-_\\w]*|xxx+|example|dummy|sample|placeholder|<[^>]*>)\"|'(?:changeme|change_me|your[-_\\w]*|xxx+|example|dummy|sample|placeholder|<[^>]*>)'|\"\"|'')"
    );

    public static final List<SecurityRule> RULES = List.of(

            // ─────────── SQL Injection (CWE-89) ───────────
            new SecurityRule("SQLI-001", VulnerabilityType.SQL_INJECTION, Severity.CRITICAL,
                    Set.of(Language.JAVA),
                    "(?i)\"[^\"]*\\b(SELECT|INSERT|UPDATE|DELETE)\\b[^\"]*\"\\s*\\+",
                    SQL_EXCLUDES,
                    "문자열 결합으로 SQL 생성",
                    "SQL 문자열에 + 연산자로 외부 입력을 직접 결합하고 있습니다."),

            new SecurityRule("SQLI-002", VulnerabilityType.SQL_INJECTION, Severity.CRITICAL,
                    Set.of(Language.JAVA),
                    "(?i)\\b([a-zA-Z_$][\\w$]*)\\s*\\.\\s*executeQuery\\s*\\(\\s*[a-zA-Z_$][\\w$]*\\s*\\)",
                    SQL_EXCLUDES,
                    "Statement에 조립된 쿼리 실행",
                    "PreparedStatement 없이 변수로 조립한 SQL을 그대로 실행하고 있습니다."),

            new SecurityRule("SQLI-003", VulnerabilityType.SQL_INJECTION, Severity.CRITICAL,
                    JS_TS,
                    "(?i)`[^`]*\\b(SELECT|INSERT|UPDATE|DELETE)\\b[^`]*\\$\\{",
                    SQL_EXCLUDES,
                    "템플릿 리터럴 SQL 삽입",
                    "SQL 문자열 안에서 ${} 보간으로 외부 입력을 직접 결합하고 있습니다."),

            new SecurityRule("SQLI-004", VulnerabilityType.SQL_INJECTION, Severity.CRITICAL,
                    JS_TS,
                    "(?i)\\bdb\\.query\\s*\\(\\s*['\"][^'\"]*\\b(SELECT|INSERT|UPDATE|DELETE)\\b[^'\"]*['\"]\\s*\\+",
                    SQL_EXCLUDES,
                    "쿼리 인자에 문자열 결합",
                    "db.query() 인자에서 문자열을 결합해 쿼리를 만들고 있습니다."),

            // ─────────── Hardcoded Secret (CWE-798) ───────────
            new SecurityRule("SECRET-001", VulnerabilityType.HARDCODED_SECRET, Severity.HIGH,
                    ALL,
                    "(?i)\\b(password|pwd)\\b\\s*[:=]\\s*[\"'][^\"']+[\"']",
                    SECRET_EXCLUDES,
                    "비밀번호 하드코딩",
                    "비밀번호가 소스 코드에 평문 문자열로 기입되어 있습니다."),

            new SecurityRule("SECRET-002", VulnerabilityType.HARDCODED_SECRET, Severity.HIGH,
                    ALL,
                    "(?i)\\b(api[-_]?key|secret|token)\\b\\s*[:=]\\s*[\"'][^\"']{8,}[\"']",
                    SECRET_EXCLUDES,
                    "API 키·토큰 하드코딩",
                    "API 키 또는 토큰이 소스 코드에 직접 기입되어 있습니다."),

            new SecurityRule("SECRET-003", VulnerabilityType.HARDCODED_SECRET, Severity.CRITICAL,
                    ALL,
                    "\\bAKIA[0-9A-Z]{16}\\b",
                    SECRET_EXCLUDES,
                    "AWS 액세스 키 노출",
                    "AWS 액세스 키 형식의 문자열이 코드에 포함되어 있습니다."),

            new SecurityRule("SECRET-004", VulnerabilityType.HARDCODED_SECRET, Severity.CRITICAL,
                    ALL,
                    "-----BEGIN\\s+([A-Z]+\\s+)?PRIVATE KEY-----",
                    List.of(),
                    "개인키 블록 노출",
                    "개인키(Private Key) 전문이 소스 코드에 포함되어 있습니다."),

            new SecurityRule("SECRET-005", VulnerabilityType.HARDCODED_SECRET, Severity.HIGH,
                    ALL,
                    "(?i)\\b(jdbc|mongodb(\\+srv)?|postgres(ql)?|mysql|redis):[^\\s\"']*(password\\s*=\\s*[^\\s\"'&;]+|//[^:\\s\"'/]+:[^@\\s\"']+@)",
                    SECRET_EXCLUDES,
                    "접속 URL에 비밀번호 평문",
                    "DB 접속 문자열에 비밀번호가 평문으로 포함되어 있습니다."),

            // ─────────── XSS (CWE-79) ───────────
            new SecurityRule("XSS-001", VulnerabilityType.XSS, Severity.HIGH,
                    JS_TS,
                    "(?i)\\.(innerHTML|outerHTML)\\s*=",
                    List.of("(?i)\\.(innerHTML|outerHTML)\\s*=\\s*[\"'`][^\"'`]*[\"'`]\\s*;?\\s*$"),
                    "innerHTML에 검증되지 않은 값 대입",
                    "외부 입력을 innerHTML로 직접 삽입하면 스크립트가 실행될 수 있습니다."),

            new SecurityRule("XSS-002", VulnerabilityType.XSS, Severity.HIGH,
                    JS_TS,
                    "(?i)\\bdocument\\.(write|writeln)\\s*\\(",
                    List.of("(?i)\\bdocument\\.(write|writeln)\\s*\\(\\s*[\"'`][^\"'`]*[\"'`]\\s*\\)"),
                    "document.write로 값 출력",
                    "document.write에 외부 입력이 들어가면 임의 스크립트가 실행될 수 있습니다."),

            new SecurityRule("XSS-003", VulnerabilityType.XSS, Severity.MEDIUM,
                    JS_TS,
                    "dangerouslySetInnerHTML",
                    List.of(),
                    "React dangerouslySetInnerHTML 사용",
                    "React의 XSS 방어를 우회하는 API를 사용하고 있습니다."),

            new SecurityRule("XSS-004", VulnerabilityType.XSS, Severity.HIGH,
                    Set.of(Language.JAVA, Language.HTML),
                    "(?i)(getWriter\\(\\)\\s*\\.\\s*(print|println|write)|out\\.(print|println)|<%=)[^;]*request\\.getParameter",
                    List.of(),
                    "요청 파라미터를 그대로 출력",
                    "요청 파라미터를 이스케이프 없이 응답 본문에 출력하고 있습니다."),

            new SecurityRule("XSS-005", VulnerabilityType.XSS, Severity.MEDIUM,
                    Set.of(Language.HTML),
                    "th:utext\\s*=",
                    List.of(),
                    "Thymeleaf th:utext 사용",
                    "th:utext는 HTML 이스케이프를 하지 않아 XSS에 노출될 수 있습니다.")
    );

    public static List<SecurityRule> forLanguage(Language language) {
        return RULES.stream().filter(rule -> rule.appliesTo(language)).toList();
    }

    public static SecurityRule byId(String ruleId) {
        return RULES.stream()
                .filter(rule -> rule.getRuleId().equals(ruleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 룰: " + ruleId));
    }
}