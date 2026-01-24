create database  if not exists  intern_hub_db;
Use intern_hub_db;

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
                                           title VARCHAR(255) NOT NULL,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
CREATE TABLE IF NOT EXISTS positions (
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
                                       intern_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
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
                                       INDEX idx_intern_status (intern_status),
                                       INDEX idx_intern_offer_status (offer_status),
                                       FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE RESTRICT,
                                       FOREIGN KEY (batch_id) REFERENCES internship_batches(id) ON DELETE SET NULL,
                                       FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE SET NULL,
                                       CONSTRAINT chk_intern_status CHECK (intern_status IN ('ACTIVE', 'WARNING', 'COMPLETED', 'DROPPED')),
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
                                              FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. EVALUATION CRITERIA TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_criteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DECIMAL(5,2) DEFAULT 1.00,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    INDEX idx_criteria_category (category),
    INDEX idx_criteria_active (is_active)
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
                                                    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
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
                                                   FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
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
INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('MENTOR'),
                                  ('HR');
-- =====================================================
-- UPDATE WEEKLY REPORTS TABLE
-- =====================================================
ALTER TABLE weekly_reports
    ADD COLUMN average_score DECIMAL(4,2) DEFAULT 0 AFTER status;

-- =====================================================
-- CRITERIA SCORE DEFINITIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS criteria_score_definitions (
                                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                                          criteria_id INT NOT NULL,
                                                          score_label VARCHAR(50),
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

alter table weekly_reports drop column status;
