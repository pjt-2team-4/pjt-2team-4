-- 1. Analysis 테이블에 분석 내역 1건 추가 (자동으로 id=1 할당)
INSERT INTO analysis (language, original_code, total_count, high_count, created_at) 
VALUES (
    'JAVA', 
    'public class Test {\n    public void getUser(String userId) {\n        String sql = "SELECT * FROM users WHERE id = " + userId;\n    }\n}', 
    1, 
    1, 
    NOW()
);

-- 2. Vulnerability 테이블에 위 분석(analysis_id = 1)에 속하는 취약점 데이터 1건 추가
INSERT INTO vulnerability (
    analysis_id, type, severity, line_number, 
    problem_code, description, ai_explanation, after_code
) VALUES (
    1, 
    'SQL Injection', 
    'HIGH', 
    3, 
    'String sql = "SELECT * FROM users WHERE id = " + userId;', 
    '사용자 입력값이 SQL 문장과 직접 연결되어 DB 유출 위험이 있습니다.', 
    'PreparedStatement 또는 Parameter Binding 방식을 사용하여 입력값과 SQL 문장을 분리하세요.', 
    'String sql = "SELECT * FROM users WHERE id = ?";\nPreparedStatement pstmt = conn.prepareStatement(sql);\npstmt.setString(1, userId);'
);