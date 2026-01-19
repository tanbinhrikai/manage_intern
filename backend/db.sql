create database  if not exists  mydb;
Use mydb;
-- =====================================================
-- 1. ROLES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    role_name VARCHAR(50) PRIMARY KEY NOT NULL,
    description TEXT,
    INDEX idx_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. DEPARTMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_department_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. USERS TABLE (Mentors, Admins)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    is_active BOOLEAN DEFAULT TRUE,
    role_name VARCHAR(50) NOT NULL,
    department_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role_name),
    INDEX idx_user_department (department_id),
    INDEX idx_user_active (is_active),
    FOREIGN KEY (role_name) REFERENCES roles(role_name) ON DELETE RESTRICT,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. JOB POSITIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS job_positions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    department_id INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_position_title (title),
    INDEX idx_position_department (department_id),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. INTERNSHIP BATCHES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS internship_batches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE ,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch_name (name),
    INDEX idx_batch_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. INTERNS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS interns (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    position_id INT NOT NULL,
    batch_id INT,
    mentor_id CHAR(36),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    offer_status VARCHAR(20) DEFAULT 'NONE',
    offer_date DATE,
    offer_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_intern_name (full_name),
    INDEX idx_intern_email (email),
    INDEX idx_intern_position (position_id),
    INDEX idx_intern_batch (batch_id),
    INDEX idx_intern_mentor (mentor_id),
    INDEX idx_intern_status (status),
    INDEX idx_intern_offer_status (offer_status),
    FOREIGN KEY (position_id) REFERENCES job_positions(id) ON DELETE RESTRICT,
    FOREIGN KEY (batch_id) REFERENCES internship_batches(id) ON DELETE SET NULL,
    FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_intern_status CHECK (status IN ('ACTIVE', 'WARNING', 'COMPLETED', 'DROPPED')),
    CONSTRAINT chk_intern_offer_status CHECK (offer_status IN ('NONE', 'PROPOSED', 'ACCEPTED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. WEEKLY REPORTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS weekly_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    mentor_id CHAR(36) NOT NULL,
    week_number INT,
    week_start_date DATE NOT NULL,
    tasks_assigned TEXT,
    tasks_completed TEXT,
    output_quality TEXT,
    proactivity_score TINYINT,
    progress_score TINYINT,
    issues_risks TEXT,
    mentor_overall_comment TEXT,
    status VARCHAR(20) DEFAULT 'submitted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_intern (intern_id),
    INDEX idx_report_mentor (mentor_id),
    INDEX idx_report_week (week_start_date),
    INDEX idx_report_status (status),
    UNIQUE KEY uk_report_intern_week (intern_id, week_start_date),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_proactivity_score CHECK (proactivity_score IS NULL OR (proactivity_score >= 1 AND proactivity_score <= 10)),
    CONSTRAINT chk_progress_score CHECK (progress_score IS NULL OR (progress_score >= 1 AND progress_score <= 10))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. EVALUATION CRITERIA TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_criteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DECIMAL(5,2) DEFAULT 1.00,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    INDEX idx_criteria_category (category),
    INDEX idx_criteria_active (is_active),
    CONSTRAINT chk_criteria_category CHECK (category IN ('EXPERTISE', 'MINDSET', 'SKILLS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. EVALUATION TEMPLATES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_templates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    position_id INT NOT NULL,
    criteria_id INT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    weight_override DECIMAL(5,2),
    INDEX idx_template_position (position_id),
    INDEX idx_template_criteria (criteria_id),
    UNIQUE KEY uk_template_position_criteria (position_id, criteria_id),
    FOREIGN KEY (position_id) REFERENCES job_positions(id) ON DELETE CASCADE,
    FOREIGN KEY (criteria_id) REFERENCES evaluation_criteria(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. EVALUATION SESSIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    mentor_id CHAR(36) NOT NULL,
    session_type VARCHAR(20) NOT NULL,
    evaluation_date DATE NOT NULL,
    final_score DECIMAL(5,2),
    level_assessment VARCHAR(255),
    conclusion VARCHAR(50),
    overall_comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_intern (intern_id),
    INDEX idx_session_mentor (mentor_id),
    INDEX idx_session_type (session_type),
    INDEX idx_session_date (evaluation_date),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_session_type CHECK (session_type IN ('WEEKLY', 'MID_TERM', 'FINAL')),
    CONSTRAINT chk_session_conclusion CHECK (conclusion IS NULL OR conclusion IN ('PASS', 'NEED_IMPROVEMENT', 'FAIL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. EVALUATION SCORES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    criteria_id INT NOT NULL,
    score TINYINT,
    comment TEXT,
    INDEX idx_score_session (session_id),
    INDEX idx_score_criteria (criteria_id),
    UNIQUE KEY uk_score_session_criteria (session_id, criteria_id),
    FOREIGN KEY (session_id) REFERENCES evaluation_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (criteria_id) REFERENCES evaluation_criteria(id) ON DELETE RESTRICT,
    CONSTRAINT chk_score_range CHECK (score IS NULL OR (score >= 1 AND score <= 10))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. INTERNSHIP ROADMAPS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS internship_roadmaps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    position_id INT NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    stage_order INT DEFAULT 0,
    description TEXT,
    duration_weeks INT,
    expected_outcomes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_roadmap_position (position_id),
    INDEX idx_roadmap_stage (stage_name),
    FOREIGN KEY (position_id) REFERENCES job_positions(id) ON DELETE CASCADE,
    CONSTRAINT chk_roadmap_stage CHECK (stage_name IN ('ONBOARDING', 'TRAINING', 'PROJECT', 'EVALUATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. INTERN ROADMAP PROGRESS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS intern_roadmap_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    roadmap_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_date DATE,
    expected_end_date DATE,
    actual_completion_date DATE,
    mentor_notes TEXT,
    INDEX idx_progress_intern (intern_id),
    INDEX idx_progress_roadmap (roadmap_id),
    INDEX idx_progress_status (status),
    UNIQUE KEY uk_progress_intern_roadmap (intern_id, roadmap_id),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (roadmap_id) REFERENCES internship_roadmaps(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 14. INTERN STATUS HISTORY TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS intern_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by CHAR(36) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_intern (intern_id),
    INDEX idx_history_changed_by (changed_by),
    INDEX idx_history_new_status (new_status),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_history_old_status CHECK (old_status IS NULL OR old_status IN ('ACTIVE', 'WARNING', 'COMPLETED', 'DROPPED')),
    CONSTRAINT chk_history_new_status CHECK (new_status IN ('ACTIVE', 'WARNING', 'COMPLETED', 'DROPPED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 15. BUSINESS METRICS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS business_metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_category VARCHAR(100),
    target_value DECIMAL(10,2),
    actual_value DECIMAL(10,2),
    unit VARCHAR(50),
    recorded_date DATE NOT NULL,
    notes TEXT,
    created_by CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_metric_intern (intern_id),
    INDEX idx_metric_category (metric_category),
    INDEX idx_metric_date (recorded_date),
    INDEX idx_metric_created_by (created_by),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 16. SYSTEM ALERTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS system_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    target_user_id CHAR(36),
    intern_id INT,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) DEFAULT 'info',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_type (alert_type),
    INDEX idx_alert_target_user (target_user_id),
    INDEX idx_alert_intern (intern_id),
    INDEX idx_alert_read (is_read),
    INDEX idx_alert_created (created_at),
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    CONSTRAINT chk_alert_type CHECK (alert_type IN ('INTERN_AT_RISK', 'MENTOR_OVERLOADED', 'DEADLINE_APPROACHING', 'EVALUATION_DUE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 17. AUDIT LOGS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id INT,
    old_value TEXT,
    new_value TEXT,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 18. REFRESH TOKENS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id CHAR(36) NOT NULL,
    INDEX idx_refresh_token (refresh_token),
    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expiry (expiry_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT DEFAULT DATA
-- =====================================================

-- Insert default roles
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'System Administrator with full access'),
('MENTOR', 'Mentor who manages interns')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insert default departments (example)
INSERT INTO departments (name) VALUES
('Backend Development'),
('Frontend Development'),
('Mobile Development'),
('Marketing'),
('Sales')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- END OF SCHEMA
-- =====================================================

INSERT INTO internship_batches (name, start_date, end_date, description)
VALUES 
    ('RINT 01', '2025-01-01', '2025-06-30', 'Đợt thực tập RINT 01.'),
    ('RINT 02', '2025-02-01', '2025-07-31', 'Đợt thực tập RINT 02.'),
    ('RINT 03', '2025-03-01', '2025-08-31', 'Đợt thực tập RINT 03.'),
    ('RINT 04', '2025-04-01', '2025-09-30', 'Đợt thực tập RINT 04.'),
    ('RINT 05', '2025-05-01', '2025-10-31', 'Đợt thực tập RINT 05.');
    
-- '1', 'Nguyễn Văn An', 'an.nguyen@rikai.technology', '0901000001', '1', '1', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '2', 'Trần Thị Bình', 'binh.tran@rikai.technology', '0901000002', '1', '1', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '3', 'Lê Văn Cường', 'cuong.le@rikai.technology', '0901000003', '2', '1', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-01-01', '2025-06-30', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '4', 'Phạm Thị Dung', 'dung.pham@rikai.technology', '0901000004', '2', '1', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-01-01', '2025-06-30', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '5', 'Hoàng Văn Em', 'em.hoang@rikai.technology', '0901000005', '3', '1', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '6', 'Vũ Thị Giang', 'giang.vu@rikai.technology', '0901000006', '3', '1', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '7', 'Đặng Văn Hùng', 'hung.dang@rikai.technology', '0901000007', '4', '1', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-01-01', '2025-06-30', 'COMPLETED', 'REJECTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '8', 'Bùi Thị Khanh', 'khanh.bui@rikai.technology', '0901000008', '4', '1', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '9', 'Cao Văn Lâm', 'lam.cao@rikai.technology', '0902000001', '1', '2', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '10', 'Đỗ Thị Mai', 'mai.do@rikai.technology', '0902000002', '1', '2', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-02-01', '2025-07-31', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '11', 'Ngô Văn Nam', 'nam.ngo@rikai.technology', '0902000003', '2', '2', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '12', 'Hồ Thị Oanh', 'oanh.ho@rikai.technology', '0902000004', '2', '2', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-02-01', '2025-07-31', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '13', 'Dương Văn Phú', 'phu.duong@rikai.technology', '0902000005', '3', '2', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-02-01', '2025-07-31', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '14', 'Lý Thị Quyên', 'quyen.ly@rikai.technology', '0902000006', '3', '2', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '15', 'Mai Văn Duyên', 'duyen.mai@rikai.technology', '0902000007', '4', '2', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '16', 'Trương Thị Sương', 'suong.truong@rikai.technology', '0902000008', '4', '2', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '17', 'Võ Văn Tài', 'tai.vo@rikai.technology', '0903000001', '1', '3', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '18', 'Đinh Thị Uyên', 'uyen.dinh@rikai.technology', '0903000002', '1', '3', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-03-01', '2025-08-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '19', 'Bạch Văn Vinh', 'vinh.bach@rikai.technology', '0903000003', '2', '3', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '20', 'Phùng Thanh Tài', 'tai.phung@rikai.technology', '0903000004', '2', '3', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-03-01', '2025-08-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '21', 'Đoàn Văn Ý', 'y.doan@rikai.technology', '0903000005', '3', '3', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-03-01', '2025-08-31', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '22', 'Hà Thị Xuân', 'xuan.ha@rikai.technology', '0903000006', '3', '3', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-03-01', '2025-08-31', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '23', 'Lê Văn Anh', 'anh.le@rikai.technology', '0903000007', '4', '3', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-03-01', '2025-08-31', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '24', 'Lâm Thị Bích Ngọc', 'ngoc.lam@rikai.technology', '0903000008', '4', '3', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '25', 'Mạc Văn Chiến', 'chien.mac@rikai.technology', '0904000001', '1', '4', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-04-01', '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '26', 'Ninh Thị Duyên', 'duyen.ninh@rikai.technology', '0904000002', '1', '4', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-04-01', '2025-09-30', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '27', 'Ông Cao Thắng', 'thang.ong@rikai.technology', '0904000003', '2', '4', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '28', 'Quách Ngọc Tuyên', 'tuyen.quach@rikai.technology', '0904000004', '2', '4', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-04-01', '2025-09-30', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '29', 'Phạm Lê Tú', 'tu.pham@rikai.technology', '0904000005', '3', '4', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-04-01', '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '30', 'Lê Thị Hoa', 'hoa.le@rikai.technology', '0904000006', '3', '4', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '31', 'Đoàn Văn Hậu', 'hau.doan@rikai.technology', '0904000007', '4', '4', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-04-01', '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '32', 'Vũ Thị Kim', 'kim.vu@rikai.technology', '0904000008', '4', '4', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-04-01', '2025-09-30', 'COMPLETED', 'REJECTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '33', 'Vương Văn Lộc', 'loc.vuong@rikai.technology', '0905000001', '1', '5', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '34', 'Cao Thị Minh', 'minh.cao@rikai.technology', '0905000002', '1', '5', '04b5f9d5-6475-459c-97da-6acd0cdcbbde', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '35', 'Đoàn Trung Sơn', 'son.doan@rikai.technology', '0905000003', '2', '5', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-05-01', '2025-10-31', 'ACTIVE', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '36', 'Châu Thị Ngọc', 'ngoc.chau@rikai.technology', '0905000004', '2', '5', '42b3746a-41ad-4f79-8e3e-7022a96490c1', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '37', 'Đàm Văn Phúc', 'phuc.dam@rikai.technology', '0905000005', '3', '5', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '38', 'Nguyễn Thị Quỳnh', 'quynh.nguyen@rikai.technology', '0905000006', '3', '5', 'f9ba0e01-8ca0-426b-ac17-3e92b698a1ea', '2025-05-01', '2025-10-31', 'ACTIVE', 'ACCEPTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'
-- '39', 'Lại Văn Sang', 'sang.lai@rikai.technology', '0905000007', '4', '5', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'
-- '40', 'Trần Thị Thảo', 'thao.tran@rikai.technology', '0905000008', '4', '5', 'e9fc1dde-6bc5-450f-ab83-82bc10178a93', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'

-- =====================================================
-- UPDATE EVALUATION CRITERIA CONSTRAINT
-- =====================================================
ALTER TABLE evaluation_criteria DROP CONSTRAINT chk_criteria_category;

ALTER TABLE weekly_reports
    DROP COLUMN output_quality,
    DROP COLUMN proactivity_score,
    DROP COLUMN progress_score;

ALTER TABLE weekly_reports
    ADD COLUMN average_score DECIMAL(4,2) DEFAULT 0 AFTER status;

ALTER TABLE evaluation_criteria ADD CONSTRAINT chk_criteria_category CHECK (category IN ('EXPERTISE', 'SKILLS', 'MINDSET'));

-- =====================================================
-- CRITERIA SCORE DEFINITIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS criteria_score_definitions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    criteria_id INT NOT NULL,
    score_label VARCHAR(50),
    min_score TINYINT NOT NULL,
    max_score TINYINT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (criteria_id) REFERENCES evaluation_criteria(id) ON DELETE CASCADE,
    INDEX idx_def_criteria (criteria_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- WEEKLY REPORT DETAILS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS weekly_report_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    weekly_report_id INT NOT NULL,
    criteria_id INT NOT NULL,
    score TINYINT,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (criteria_id) REFERENCES evaluation_criteria(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_report_criteria (weekly_report_id, criteria_id),
    CONSTRAINT chk_detail_score CHECK (score >= 0 AND score <= 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT EVALUATION CRITERIA DATA
-- =====================================================
INSERT INTO evaluation_criteria (id, category, name, weight ) VALUES
(1, 'EXPERTISE', 'Professional Competence & Execution Quality', 1.00),
(2, 'EXPERTISE', 'Proactivity & Task Management (Outcome)', 1.00 ),
(3, 'SKILLS', 'Work Attitude & Collaborative Spirit', 1.00),
(4, 'SKILLS', 'Communication & Job Reporting', 1.00),
(5, 'SKILLS', 'Teamwork & Group Communication', 1.00),
(6, 'SKILLS', 'Contribution Level & Team Impact', 1.00),
(7, 'SKILLS', 'Deadline Compliance & Task Management (Skill)', 1.00),
(8, 'SKILLS', 'Responsibility & Work Commitment', 1.00),
(9, 'MINDSET', 'Problem Analysis & Solution Proposal', 1.00),
(10, 'MINDSET', 'Solution Implementation & Result Improvement', 1.00)
ON DUPLICATE KEY UPDATE name = VALUES(name), category = VALUES(category);

-- =====================================================
-- INSERT CRITERIA SCORE DEFINITIONS DATA
-- =====================================================
INSERT INTO criteria_score_definitions (criteria_id, score_label, description) VALUES
(1, 'Excellent',  'Understands requirements correctly from the start, independently designs and implements complete solutions; high-quality product, stable, few bugs, clear code/documentation.'),
(1, 'Good',  'Understands requirements reasonably well, implements quite independently; product meets requirements, minor bugs but fixed quickly.'),
(1, 'Average', 'Understands requirements at a basic level, implementation needs guidance; results work but have many bugs, requiring significant review and editing.'),
(1, 'Weak', 'Frequently misunderstands requirements, poor implementation; product quality is low, heavily dependent on mentor or not completed.'),

(2, 'Excellent',  'Proactively seeks information, self-manages progress, completes on time or early, minimal dependence on mentor.'),
(2, 'Good',  'Works relatively independently, only needs minor suggestions from mentor.'),
(2, 'Average',  'Can do the work but needs step-by-step guidance from mentor.'),
(2, 'Weak',  'Can only do very basic parts, main parts require mentor support; sometimes cannot complete tasks.'),

(3, 'Excellent',  'Always maintains a positive, professional attitude; respects others, listens and accepts feedback quickly; good cooperative spirit, positive contribution to the team.'),
(3, 'Good',  'Serious attitude, eager to learn; listens and adjusts when receiving feedback; stable work spirit.'),
(3, 'Average',  'Attitude generally stable, complies with rules but not truly positive; participates in work just enough.'),
(3, 'Weak',  'Passive, lacks seriousness; shows negative signs (doing personal work, lack of focus, complaining); affects team spirit.'),

(4, 'Excellent',  'Proactive, clear communication; asks focused questions; reports progress fully and timely, proactively informs about issues.'),
(4, 'Good', 'Communication relatively clear; exchanges and reports fully, sometimes needs reminders from mentor.'),
(4, 'Average', 'Communication still shy, not coherent; rarely proactively reports, usually only responds when asked.'),
(4, 'Weak', 'Poor communication, vague feedback or avoiding exchange; frequently reports late or missing, affecting work progress.'),

(5, 'Excellent', 'Integrates quickly, proactively coordinates; clear, coherent communication; creates an open exchange environment, supports team to work effectively.'),
(5, 'Good',  ' cooperates well, communication quite clear; ready to participate and coordinate when the team needs, sometimes still reserved.'),
(5, 'Average',  'Coordinates at a basic level; communication infrequent or unclear, mainly focuses on individual work.'),
(5, 'Weak',  'Little exchange, poor communication; does not proactively coordinate, updates information late, causing delays to general progress.'),

(6, 'Excellent',  'Frequently contributes valuable opinions, shares knowledge; creates positive influence, helps bond and improve team performance.'),
(6, 'Good',  'Actively participates in discussions, has useful contributions; supports teammates when requested, contributes to achieving common goals.'),
(6, 'Average',  'Contribution still limited; rarely proactively shares, influence on the team not yet distinct.'),
(6, 'Weak',  'Little or no participation in discussion; lacks information sharing, sometimes causing difficulties or negative influence on the team.'),

(7, 'Excellent',  'Always completes on time or before deadline; knows how to prioritize, manages time scientifically; handles multiple parallel tasks effectively, no need for reminders.'),
(7, 'Good',  'Mostly completes on time; manages time quite well, sometimes slightly slow but notifies in advance.'),
(7, 'Average',  'Completes work but often needs reminders about deadlines; time management not effective when workload increases.'),
(7, 'Weak',  'Frequently misses deadlines, does not notify in advance; poor work arrangement, easy to miss tasks, needs mentor to follow closely.'),

(8, 'Excellent',  'Demonstrates high sense of responsibility; proactively accepts additional work when team needs; always ensures output quality.'),
(8, 'Good',  'Responsible for assigned work; rarely needs reminders; fulfills commitments properly.'),
(8, 'Average',  'Responsible at a basic level but unstable; sometimes hesitant or lacks decisiveness.'),
(8, 'Weak',  'Shows signs of avoiding difficult work, pushing responsibility; affects progress and general efficiency of the team.'),

(9, 'Excellent',  'Quickly identifies the true nature of problems; analyzes causes - consequences clearly; proposes many feasible solutions, weighs pros - cons.'),
(9, 'Good',  'Identifies most problems correctly; proposes suitable solutions, sometimes needs mentor feedback to optimize.'),
(9, 'Average',  'Understands problems at a basic level; solutions are monotonous, lack feasibility, dependent on mentor.'),
(9, 'Weak',  'Frequently misunderstands problems; rarely proposes clear solutions, waits for mentor instructions.'),

(10, 'Excellent',  'Implements logically, adjusts flexibly; resolves problems thoroughly, draws experience and shares with the group.'),
(10, 'Good',  'Performs quite well; problems are handled, draws experience but not systematically.'),
(10, 'Average',  'Needs significant mentor guidance; handling is temporary, problems easily recur.'),
(10, 'Weak',  'Passive, easily gives up; problems not solved or handled incorrectly, repeating old errors.');

-- =====================================================
-- UPDATE CRITERIA SCORE DEFINITIONS CONSTRAINT
-- =====================================================
ALTER TABLE criteria_score_definitions ADD CONSTRAINT chk_score_label CHECK (score_label IN ('Excellent', 'Good', 'Average', 'Weak'));

ALTER table evaluation_criteria drop column display_order;

ALTER table evaluation_criteria drop column is_active;


-- Update criteria_score_definitions --

alter  table  criteria_score_definitions drop  column  min_score;

alter table  criteria_score_definitions drop column  max_score;

truncate  table  weekly_report_details;
truncate table  weekly_reports;




