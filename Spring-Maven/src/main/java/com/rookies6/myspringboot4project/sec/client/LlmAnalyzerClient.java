package com.rookies6.myspringboot4project.sec.client;

import com.rookies6.myspringboot4project.sec.entity.Vulnerability;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LlmAnalyzerClient {
    public List<Vulnerability> requestAnalysisToAi(String language, String code) {
        return List.of(
            Vulnerability.builder()
                .type("SQL Injection").severity("HIGH").lineNumber(1)
                .problemCode("String sql = \"SELECT * FROM users WHERE id = \" + userId;")
                .description("사용자 입력값이 SQL 문장과 직접 연결되어 DB 유출 위험이 있습니다.")
                .aiExplanation("PreparedStatement를 사용하여 바인딩 처리하세요.")
                .afterCode("String sql = \"SELECT * FROM users WHERE id = ?\";")
                .build()
        );
    }
}