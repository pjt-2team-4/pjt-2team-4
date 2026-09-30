package com.rookies6.myspringboot4project.sec.scanner;

// 공통 enums 컴포넌트 import 경로 수정
import com.rookies6.myspringboot4project.sec.common.enums.Language;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.sec.scanner.SecurityScanner;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Scanner 단위 테스트. Spring 컨텍스트·DB 없이 동작한다.
 * 룰을 추가할 때는 반드시 취약 코드 / 안전 코드 쌍을 함께 추가한다.
 */
@DisplayName("SecurityScanner 테스트")
class SecurityScannerTest {

    private final SecurityScanner scanner = new SecurityScanner();

    // ─────────────────────────── 탐지되어야 하는 코드 ───────────────────────────
    @ParameterizedTest(name = "[{0}] {1}")
    @DisplayName("취약 코드는 해당 룰로 탐지되어야 한다")
    @CsvSource(delimiter = '|', value = {
            "SQLI-001  | JAVA       | String sql = \"SELECT * FROM users WHERE id = \" + userId;",
            "SQLI-002  | JAVA       | Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql);",
            "SQLI-003  | TYPESCRIPT | const query = `SELECT * FROM users WHERE id = ${userId}`;",
            "SQLI-004  | JAVASCRIPT | db.query(\"SELECT * FROM users WHERE id = \" + id);",
            "SECRET-001| JAVA       | String password = \"P@ssw0rd123!\";",
            "SECRET-002| TYPESCRIPT | const apiKey = \"sk-live-9f2b7c1d4a8e\";",
            "SECRET-003| JAVA       | String awsKey = \"AKIAIOSFODNN7EXAMPLE\";",
            "SECRET-004| JAVA       | String key = \"-----BEGIN RSA PRIVATE KEY-----\";",
            "SECRET-005| JAVA       | String url = \"jdbc:mysql://localhost:3306/db?password=root1234\";",
            "XSS-001   | TYPESCRIPT | el.innerHTML = userInput;",
            "XSS-002   | JAVASCRIPT | document.write(location.search);",
            "XSS-003   | TYPESCRIPT | <div dangerouslySetInnerHTML={{ __html: comment }} />",
            "XSS-004   | JAVA       | response.getWriter().print(request.getParameter(\"name\"));",
            "XSS-005   | HTML       | <p th:utext=\"${comment}\"></p>"
    })
    void shouldDetectVulnerableCode(String expectedRuleId, Language language, String code) {
        List<RawFinding> findings = scanner.scan("src/Sample.txt", language, code);

        assertThat(findings)
                .extracting(RawFinding::ruleId)
                .containsExactly(expectedRuleId.strip());
    }

    // ─────────────────────────── 탐지되면 안 되는 코드 ───────────────────────────
    @ParameterizedTest(name = "[{0}] {1}")
    @DisplayName("안전한 코드는 탐지되지 않아야 한다")
    @CsvSource(delimiter = '|', value = {
            "매개변수화 쿼리   | JAVA       | String sql = \"SELECT * FROM users WHERE id = ?\";",
            "PreparedStatement| JAVA       | PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM users WHERE id = ?\"); ps.executeQuery();",
            "바인딩 파라미터   | TYPESCRIPT | const query = `SELECT * FROM users WHERE id = $1`;",
            "인자 바인딩       | JAVASCRIPT | db.query(\"SELECT * FROM users WHERE id = $1\", [id]);",
            "환경변수 참조     | JAVA       | String password = System.getenv(\"DB_PASSWORD\");",
            "env 참조          | TYPESCRIPT | const apiKey = import.meta.env.VITE_API_KEY;",
            "플레이스홀더      | JAVA       | String password = \"changeme\";",
            "플레이스홀더2     | TYPESCRIPT | const apiKey = \"your-api-key\";",
            "빈 문자열         | TYPESCRIPT | const token = \"\";",
            "유효하지 않은 AWS 키| JAVA      | String awsKey = \"AKIA123\";",
            "공개 키 블록       | JAVA       | String key = \"-----BEGIN PUBLIC KEY-----\";",
            "비밀번호 없는 DB URL| JAVA       | String url = \"jdbc:mysql://localhost:3306/db\";",
            "고정 문자열 대입  | TYPESCRIPT | container.innerHTML = \"<b>loading</b>\";",
            "고정 문자열 출력  | JAVASCRIPT | document.write(\"hello\");",
            "textContent 사용  | TYPESCRIPT | container.textContent = comment;",
            "인코딩 후 출력    | JAVA       | response.getWriter().print(escapeHtml(name));",
            "이스케이프 출력   | HTML       | <p th:text=\"${comment}\"></p>"
    })
    void shouldNotDetectSafeCode(String caseName, Language language, String code) {
        List<RawFinding> findings = scanner.scan("src/Sample.txt", language, code);

        assertThat(findings).isEmpty();
    }

    // ─────────────────────────── 알고리즘 동작 ───────────────────────────
    @Test
    @DisplayName("주석 안의 취약 코드는 탐지하지 않는다")
    void shouldIgnoreComments() {
        String code = """
                // const q = `SELECT * FROM users WHERE id = ${id}`;
                /*
                  const q2 = `DELETE FROM users WHERE id = ${id}`;
                */
                const safe = 1;
                """;

        List<RawFinding> findings = scanner.scan("src/a.ts", Language.TYPESCRIPT, code);

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("Python의 # 주석은 제외한다")
    void shouldIgnorePythonHashComments() {
        List<RawFinding> findings = scanner.scan(
                "src/sample.py", Language.PYTHON, "# password = \"P@ssw0rd123!\"");

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("JavaScript private field의 #는 주석으로 취급하지 않는다")
    void shouldScanJavaScriptPrivateField() {
        List<RawFinding> findings = scanner.scan(
                "src/Config.js", Language.JAVASCRIPT, "#apiKey = \"sk-live-9f2b7c1d4a8e\";");

        assertThat(findings).extracting(RawFinding::ruleId).containsExactly("SECRET-002");
    }

    @Test
    @DisplayName("HTML 여러 줄 주석 안의 취약 코드는 탐지하지 않는다")
    void shouldIgnoreMultilineHtmlComments() {
        String code = """
                <!--
                <p th:utext="${comment}"></p>
                -->
                <p th:text="${comment}"></p>
                """;

        List<RawFinding> findings = scanner.scan("src/page.html", Language.HTML, code);

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("Statement 선언과 executeQuery 호출이 다른 줄이어도 SQL Injection을 탐지한다")
    void shouldDetectStatementQueryAcrossLines() {
        String code = """
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                """;

        List<RawFinding> findings = scanner.scan("src/Database.java", Language.JAVA, code);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId()).isEqualTo("SQLI-002");
        assertThat(findings.get(0).startLine()).isEqualTo(2);
        assertThat(findings.get(0).endLine()).isEqualTo(2);
    }

    @Test
    @DisplayName("Statement가 아닌 객체의 executeQuery 호출은 SQLI-002로 탐지하지 않는다")
    void shouldNotTreatOtherObjectAsStatement() {
        String code = """
                CustomQuery query = new CustomQuery();
                query.executeQuery(sql);
                """;

        List<RawFinding> findings = scanner.scan("src/Database.java", Language.JAVA, code);

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("같은 룰이 연속된 라인에 걸리면 하나로 병합된다")
    void shouldMergeConsecutiveLines() {
        String code = """
                const a = `SELECT * FROM users WHERE id = ${id}`;
                const b = `SELECT * FROM orders WHERE uid = ${id}`;
                const c = 1;
                """;

        List<RawFinding> findings = scanner.scan("src/a.ts", Language.TYPESCRIPT, code);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).startLine()).isEqualTo(1);
        assertThat(findings.get(0).endLine()).isEqualTo(2);
    }

    @Test
    @DisplayName("룰 메타데이터(유형·CWE·심각도)가 그대로 반영된다")
    void shouldInheritRuleMetadata() {
        String code = "const query = `SELECT * FROM users WHERE id = ${userId}`;";

        RawFinding finding = scanner.scan("src/routes/users.ts", Language.TYPESCRIPT, code).get(0);

        assertThat(finding.ruleId()).isEqualTo("SQLI-003");
        assertThat(finding.vulnerabilityType()).isEqualTo(VulnerabilityType.SQL_INJECTION);
        assertThat(finding.cweId()).isEqualTo("CWE-89");
        assertThat(finding.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(finding.relativePath()).isEqualTo("src/routes/users.ts");
    }

    @Test
    @DisplayName("테스트 파일에서 탐지되면 심각도가 1단계 낮아진다")
    void shouldDowngradeSeverityForTestFile() {
        String code = "const query = `SELECT * FROM users WHERE id = ${userId}`;";

        RawFinding finding = scanner.scan("src/routes/users.test.ts", Language.TYPESCRIPT, code).get(0);

        assertThat(finding.severity()).isEqualTo(Severity.HIGH);   // CRITICAL -> HIGH
    }

    @Test
    @DisplayName("Windows 테스트 경로에서도 심각도를 1단계 낮춘다")
    void shouldDowngradeSeverityForWindowsTestPath() {
        RawFinding finding = scanner.scan(
                "src\\test\\java\\Sample.java", Language.JAVA,
                "String password = \"P@ssw0rd123!\";").get(0);

        assertThat(finding.severity()).isEqualTo(Severity.MEDIUM); // HIGH -> MEDIUM
    }

    @Test
    @DisplayName("탐지 결과는 원본의 1-based 라인 번호와 코드 조각을 보존한다")
    void shouldPreserveLineNumbersAndSnippet() {
        String code = "\nString password = \"P@ssw0rd123!\";\n";

        RawFinding finding = scanner.scan("src/Config.java", Language.JAVA, code).get(0);

        assertThat(finding.startLine()).isEqualTo(2);
        assertThat(finding.endLine()).isEqualTo(2);
        assertThat(finding.endLine()).isLessThanOrEqualTo(code.split("\\n", -1).length);
        assertThat(finding.codeSnippet()).isEqualTo("String password = \"P@ssw0rd123!\";");
    }

    @Test
    @DisplayName("RawFinding은 역전되거나 0인 라인 범위를 거부한다")
    void shouldRejectInvalidRawFindingLineRange() {
        assertThatIllegalArgumentException().isThrownBy(() -> new RawFinding(
                "src/Config.java", "SECRET-001", VulnerabilityType.HARDCODED_SECRET,
                "CWE-798", Severity.HIGH, 0, 1, "password = \"secret\""));
        assertThatIllegalArgumentException().isThrownBy(() -> new RawFinding(
                "src/Config.java", "SECRET-001", VulnerabilityType.HARDCODED_SECRET,
                "CWE-798", Severity.HIGH, 3, 2, "password = \"secret\""));
    }

    @Test
    @DisplayName("지원하지 않는 언어는 오류 없이 빈 결과를 반환한다")
    void shouldReturnEmptyForUnsupportedLanguage() {
        List<RawFinding> findings = scanner.scan("README.md", Language.UNKNOWN, "SELECT * FROM users");

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("언어에 맞는 룰만 적용된다 (Java 파일에 JS 룰 미적용)")
    void shouldApplyOnlyLanguageSpecificRules() {
        String code = "const query = `SELECT * FROM users WHERE id = ${userId}`;";

        List<RawFinding> findings = scanner.scan("Sample.java", Language.JAVA, code);

        assertThat(findings).isEmpty();   // SQLI-003 은 JS/TS 전용
    }

    @Test
    @DisplayName("한 라인에 여러 룰이 걸리면 심각도가 높은 1건만 남는다")
    void shouldKeepHighestSeverityPerLine() {
        String code = "const secret = \"AKIAIOSFODNN7EXAMPLE\";";   // SECRET-002(HIGH) + SECRET-003(CRITICAL)

        List<RawFinding> findings = scanner.scan("src/a.ts", Language.TYPESCRIPT, code);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).severity()).isEqualTo(Severity.CRITICAL);
    }
}