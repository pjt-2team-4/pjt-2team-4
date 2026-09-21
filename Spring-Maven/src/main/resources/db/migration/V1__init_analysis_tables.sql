-- 1. 코드 분석 요청 메타데이터를 저장하는 부모 테이블
CREATE TABLE analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    language VARCHAR(50) NOT NULL,
    original_code TEXT NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    high_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 분석된 개별 취약점 상세 정보를 저장하는 자식 테이블
CREATE TABLE vulnerability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT,
    type VARCHAR(255),
    severity VARCHAR(255),
    line_number INT,
    problem_code TEXT,
    description TEXT,
    ai_explanation TEXT,
    after_code TEXT,
    CONSTRAINT fk_vulnerability_analysis 
        FOREIGN KEY (analysis_id) REFERENCES analysis(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;