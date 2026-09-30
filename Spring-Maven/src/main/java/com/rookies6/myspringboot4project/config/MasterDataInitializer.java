package com.rookies6.myspringboot4project.config;

import com.rookies6.myspringboot4project.sec/common/enums.AnalysisRequest;
import com.rookies6.myspringboot4project.sec/common/enums.AnalysisStatus;
import com.rookies6.myspringboot4project.sec/common/enums.Severity;
import com.rookies6.myspringboot4project.sec/common/enums.VulnerabilityFinding;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MasterDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AnalysisRequestRepository analysisRequestRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. 유저 초기화
        User user1 = userRepository.findByEmail("admin@codeguard.com")
                .orElseGet(() -> userRepository.save(
                        User.builder().email("admin@codeguard.com").password("hashed_password_123").build()
                ));

        User user2 = userRepository.findByEmail("developer1@codeguard.com")
                .orElseGet(() -> userRepository.save(
                        User.builder().email("developer1@codeguard.com").password("hashed_password_456").build()
                ));

        User user3 = userRepository.findByEmail("security@codeguard.com")
                .orElseGet(() -> userRepository.save(
                        User.builder().email("security@codeguard.com").password("hashed_password_789").build()
                ));

        // 2. 분석 요청(AnalysisRequest) 및 파일/취약점 초기화
        if (analysisRequestRepository.count() == 0) {
            
            // --- [분석 1] 스프링 시큐어 뱅킹 시스템 (COMPLETED) ---
            AnalysisRequest request1 = AnalysisRequest.builder()
                    .user(user1)
                    .title("스프링 시큐어 뱅킹 시스템 취약점 분석")
                    .language("java")
                    .status(AnalysisStatus.COMPLETED)
                    .estimatedDurationSeconds(10)
                    .build();

            AnalysisFile file1_1 = createFile("src/main/java/BankingService.java", "BankingService.java",
                    "public class BankingService {\n" +
                            "    private String awsSecretKey = \"AKIAIOSFODNN7EXAMPLE\";\n" +
                            "    public void processTransaction() {\n" +
                            "        System.out.println(\"Processing banking...\");\n" +
                            "    }\n" +
                            "}");
            
            // HARDCODED_SECRET 사용 (MVP 3종 중 하나)
            file1_1.addFinding(VulnerabilityFinding.builder()
                    .title("Hardcoded AWS Secret Key")
                    .ruleId("SEC-001")
                    .vulnerabilityType(VulnerabilityType.HARDCODED_SECRET)
                    .severity(Severity.CRITICAL)
                    .description("소스코드에 AWS Secret Key가 하드코딩되어 있습니다. 환경 변수나 Secret Manager를 사용하세요.")
                    .startLine(2).endLine(2)
                    .codeSnippet("private String awsSecretKey = \"AKIAIOSFODNN7EXAMPLE\";")
                    .build());

            AnalysisFile file1_2 = createFile("src/main/java/AccountController.java", "AccountController.java",
                    "public class AccountController {\n" +
                            "    public void getAccount(String accountNumber) {\n" +
                            "        System.out.println(\"Account: \" + accountNumber);\n" +
                            "    }\n" +
                            "}");

            AnalysisFile file1_3 = createFile("src/main/java/TransactionDao.java", "TransactionDao.java",
                    "public class TransactionDao {\n" +
                            "    // TODO: Use parameterized queries\n" +
                            "    public String query = \"SELECT * FROM accounts\";\n" +
                            "}");

            request1.addFile(file1_1);
            request1.addFile(file1_2);
            request1.addFile(file1_3);

            // --- [분석 2] 이커머스 결제 API 서버 (SCANNING) ---
            AnalysisRequest request2 = AnalysisRequest.builder()
                    .user(user1)
                    .title("이커머스 결제 API 서버 보안 검사")
                    .language("java")
                    .status(AnalysisStatus.SCANNING)
                    .estimatedDurationSeconds(15)
                    .build();

            request2.addFile(createFile("src/main/java/PaymentController.java", "PaymentController.java",
                    "import org.springframework.web.bind.annotation.*;\n" +
                            "@RestController\n" +
                            "public class PaymentController {\n" +
                            "    @GetMapping(\"/receipt\")\n" +
                            "    public String getReceipt(@RequestParam String userName) {\n" +
                            "        return \"<html><body>Receipt for: \" + userName + \"</body></html>\";\n" +
                            "    }\n" +
                            "}"));
            request2.addFile(createFile("src/main/java/PaymentService.java", "PaymentService.java",
                    "public class PaymentService {\n" +
                            "    public void pay() {\n" +
                            "        System.out.println(\"Payment gateway connecting...\");\n" +
                            "    }\n" +
                            "}"));

            // --- [분석 3] 사용자 인증 마이크로서비스 (PENDING) ---
            AnalysisRequest request3 = AnalysisRequest.builder()
                    .user(user2)
                    .title("사용자 인증 마이크로서비스 코드 스캔")
                    .language("java")
                    .status(AnalysisStatus.PENDING)
                    .estimatedDurationSeconds(12)
                    .build();

            request3.addFile(createFile("src/main/java/AuthRepository.java", "AuthRepository.java",
                    "import java.sql.*;\n" +
                            "public class AuthRepository {\n" +
                            "    public boolean login(String email, String password) throws SQLException {\n" +
                            "        Connection conn = DriverManager.getConnection(\"jdbc:mysql://localhost/db\", \"root\", \"\");\n" +
                            "        Statement stmt = conn.createStatement();\n" +
                            "        String query = \"SELECT * FROM users WHERE email = '\" + email + \"' AND password = '\" + password + \"'\";\n" +
                            "        ResultSet rs = stmt.executeQuery(query);\n" +
                            "        return rs.next();\n" +
                            "    }\n" +
                            "}"));
            
            // --- [분석 4] 클라우드 파일 관리 시스템 (FAILED) ---
            AnalysisRequest request4 = AnalysisRequest.builder()
                    .user(user3)
                    .title("클라우드 파일 관리 시스템 진단")
                    .language("java")
                    .status(AnalysisStatus.FAILED)
                    .estimatedDurationSeconds(8)
                    .build();

            AnalysisFile file4_1 = createFile("src/main/java/FileController.java", "FileController.java",
                    "import org.springframework.web.bind.annotation.*;\n" +
                            "@RestController\n" +
                            "public class FileController {\n" +
                            "    @GetMapping(\"/search\")\n" +
                            "    public String search(@RequestParam String keyword) {\n" +
                            "        return \"<div>Result: \" + keyword + \"</div>\";\n" +
                            "    }\n" +
                            "}");
            
            // XSS 사용 (MVP 3종 중 하나)
            file4_1.addFinding(VulnerabilityFinding.builder()
                    .title("Reflected XSS Vulnerability")
                    .ruleId("SEC-002")
                    .vulnerabilityType(VulnerabilityType.XSS)
                    .severity(Severity.HIGH)
                    .description("사용자 입력값(keyword)이 적절한 검증이나 인코딩 없이 응답에 포함되어 스크립트가 실행될 수 있습니다.")
                    .startLine(5).endLine(5)
                    .codeSnippet("return \"<div>Result: \" + keyword + \"</div>\";")
                    .build());

            request4.addFile(file4_1);

            // 4. 연쇄 저장 처리
            analysisRequestRepository.saveAll(List.of(request1, request2, request3, request4));
        }
    }

    private AnalysisFile createFile(String relativePath, String fileName, String content) {
        int lineCount = content.split("\r\n|\r|\n").length;

        return AnalysisFile.builder()
                .relativePath(relativePath)
                .fileName(fileName)
                .language("java")
                .content(content)
                .lineCount(lineCount)
                .build();
    }
}