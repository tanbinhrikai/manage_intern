-- Disable foreign key checks for truncate
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate existing evaluation criteria data
TRUNCATE TABLE evaluation_criteria;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

use intern_hub_db;

-- Create criteria_groups table
CREATE TABLE IF NOT EXISTS criteria_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name TEXT,
    display_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Drop category column (it's nullable so no constraint to drop)
ALTER TABLE evaluation_criteria DROP COLUMN category;

-- Add group_id column and foreign key
ALTER TABLE evaluation_criteria ADD COLUMN group_id BIGINT;
ALTER TABLE evaluation_criteria ADD CONSTRAINT fk_criteria_group FOREIGN KEY (group_id) REFERENCES criteria_groups(id);


INSERT INTO criteria_groups (id, name, display_order) VALUES
                                                          (1, 'WORK PERFORMANCE', 1),
                                                          (2, 'ATTITUDE AND SOFT SKILLS' , 2),
                                                          (3, 'KNOWLEDGE APPLICATION', 3);

-- =====================================================
-- I. WORK PERFORMANCE (GROUP ID = 1)
-- =====================================================

-- Main Criteria 1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (1, 1, 'Practical Project Skills', 'Evaluates practical skills and real-world task implementation', 1.00, NULL, 1, true);

-- Sub-criteria 1.1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (2, 1, 'Technical Competence & Execution Quality', 'Evaluates understanding of requirements, solution design, and implementation quality', 1.00, 1, 1, true);

-- Sub-criteria 1.2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (3, 1, 'Proactivity & Task Management', 'Evaluates self-management of progress and level of proactivity', 1.00, 1, 2, true);

-- =====================================================
-- II. ATTITUDE AND SOFT SKILLS (GROUP ID = 2)
-- =====================================================

-- Main Criteria 2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (4, 2, 'Work Attitude & Spirit', 'Evaluates work attitude and collaborative spirit', 1.00, NULL, 1, true);

-- Sub-criteria 2.1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (5, 2, 'Work Attitude & Collaboration', 'Evaluates positive, professional attitude and willingness to cooperate', 1.00, 4, 1, true);

-- Sub-criteria 2.2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (6, 2, 'Communication & Reporting', 'Evaluates communication skills and progress reporting', 1.00, 4, 2, true);

-- Main Criteria 3
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (7, 2, 'Teamwork Ability', 'Evaluates cooperation and teamwork capabilities', 1.00, NULL, 2, true);

-- Sub-criteria 3.1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (8, 2, 'Team Collaboration & Communication', 'Evaluates team integration, coordination, and internal communication', 1.00, 7, 1, true);

-- Sub-criteria 3.2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (9, 2, 'Contribution Level & Team Impact', 'Evaluates level of contribution and positive influence on the team', 1.00, 7, 2, true);

-- Main Criteria 4
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (10, 2, 'Deadline Compliance & Responsibility', 'Evaluates time management and sense of responsibility', 1.00, NULL, 3, true);

-- Sub-criteria 4.1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (11, 2, 'Deadline Compliance & Task Management', 'Evaluates ability to meet deadlines and manage time effectively', 1.00, 10, 1, true);

-- Sub-criteria 4.2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (12, 2, 'Responsibility & Commitment', 'Evaluates responsibility and commitment to assigned tasks', 1.00, 10, 2, true);

-- =====================================================
-- III. KNOWLEDGE APPLICATION (GROUP ID = 3)
-- =====================================================

-- Main Criteria 5
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (13, 3, 'Critical Thinking & Problem Solving', 'Evaluates analytical and problem-solving abilities', 1.00, NULL, 1, true);

-- Sub-criteria 5.1
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (14, 3, 'Problem Analysis & Solution Proposal', 'Evaluates ability to identify problems and propose feasible solutions', 1.00, 13, 1, true);

-- Sub-criteria 5.2
INSERT INTO evaluation_criteria (id, group_id, name, description, weight, parent_id, display_order, is_active)
VALUES (15, 3, 'Solution Implementation & Result Improvement', 'Evaluates solution implementation and ability to learn from results', 1.00, 13, 2, true);

-- =====================================================
-- INSERT MENTORS (USERS)
-- =====================================================
-- INSERT INTO users (id, email, password_hash, full_name, date_of_birth, is_active, role_name, department_id) VALUES
-- ('5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'mentor1@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Nguyen Van Mentor1', '1990-01-15', TRUE, 'MENTOR', 1),
-- ('536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 'mentor2@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Tran Thi Mentor2', '1988-05-20', TRUE, 'MENTOR', 1),
-- ('c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'mentor3@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Le Van Mentor3', '1992-08-10', TRUE, 'MENTOR', 2),
-- ('2c638c0d-f4bc-4a53-b88d-1a75e9620830', 'mentor4@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Pham Thi Mentor4', '1991-03-25', TRUE, 'MENTOR', 4);

-- =====================================================
-- INSERT INTERNS DATA
-- =====================================================
-- INSERT INTO interns (id, full_name, email, phone, position_id, batch_id, mentor_id, start_date,
-- end_date, intern_status, offer_status, offer_date, offer_notes, created_at, updated_at)
-- VALUES
-- (1, 'Nguyễn Văn An', 'an.nguyen@rikai.technology', '0901000001', 1, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a',
-- '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-07', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (2, 'Trần Thị Bình', 'binh.tran@rikai.technology', '0901000002', 1, 1, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b',
-- '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (3, 'Lê Văn Cường', 'cuong.le@rikai.technology', '0901000003', 2, 1, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-01-01', '2025-06-30',
-- 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (4, 'Phạm Thị Dung', 'dung.pham@rikai.technology', '0901000004', 2, 1,
-- '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-01-01', '2025-02-28', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (5, 'Hoàng Văn Em', 'em.hoang@rikai.technology', '0901000005', 3, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a',
-- '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-07', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (6, 'Vũ Thị Giang', 'giang.vu@rikai.technology', '0901000006', 3, 1, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-01-01',
-- '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (7, 'Đặng Văn Hùng', 'hung.dang@rikai.technology', '0901000007', 4, 1, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b',
-- '2025-01-01', '2025-06-30', 'COMPLETED', 'REJECTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (8, 'Bùi Thị Khanh', 'khanh.bui@rikai.technology', '0901000008', 4, 1, '2c638c0d-f4bc-4a53-b88d-1a75e9620830',
-- '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (9, 'Cao Văn Lâm', 'lam.cao@rikai.technology', '0902000001', 1, 2, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-10', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (10, 'Đỗ Thị Mai', 'mai.do@rikai.technology', '0902000002', 1, 2, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b',
-- '2025-02-01', '2025-07-31', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (11, 'Ngô Văn Nam', 'nam.ngo@rikai.technology', '0902000003', 2, 2, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-05', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (12, 'Hồ Thị Oanh', 'oanh.ho@rikai.technology', '0902000004', 2, 2, '2c638c0d-f4bc-4a53-b88d-1a75e9620830',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (13, 'Dương Văn Phú', 'phu.duong@rikai.technology', '0902000005', 3, 2, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a',
-- '2025-02-01', '2025-04-15', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (14, 'Lý Thị Quyên', 'quyen.ly@rikai.technology', '0902000006', 3, 2, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (15, 'Mai Văn Duyên', 'duyen.mai@rikai.technology', '0902000007', 4, 2, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-12', NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (16, 'Trương Thị Sương', 'suong.truong@rikai.technology', '0902000008', 4, 2, '2c638c0d-f4bc-4a53-b88d-1a75e9620830',
-- '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (17, 'Võ Văn Tài', 'tai.vo@rikai.technology', '0903000001', 1, 3, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-03-01',
-- '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-10', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (18, 'Đinh Thị Uyên', 'uyen.dinh@rikai.technology', '0903000002', 1, 3, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-03-01',
-- '2025-08-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (19, 'Bạch Văn Vinh', 'vinh.bach@rikai.technology', '0903000003', 2, 3, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-03-01',
-- '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-07', NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (20, 'Phùng Thanh Tài', 'tai.phung@rikai.technology', '0903000004', 2, 3, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-03-01',
-- '2025-08-31', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (21, 'Đoàn Văn Ý', 'y.doan@rikai.technology', '0903000005', 3, 3, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-03-01',
-- '2025-08-31', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (22, 'Hà Thị Xuân', 'xuan.ha@rikai.technology', '0903000006', 3, 3, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-03-01',
-- '2025-08-31', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (23, 'Lê Văn Anh', 'anh.le@rikai.technology', '0903000007', 4, 3, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-03-01',
-- '2025-05-15', 'DROPPED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (24, 'Lâm Thị Bích Ngọc', 'ngoc.lam@rikai.technology', '0903000008', 4, 3, '2c638c0d-f4bc-4a53-b88d-1a75e9620830',
-- '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-05', NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (25, 'Mạc Văn Chiến', 'chien.mac@rikai.technology', '0904000001', 1, 4, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (26, 'Ninh Thị Duyên', 'duyen.ninh@rikai.technology', '0904000002', 1, 4, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (27, 'Ông Cao Thắng', 'thang.ong@rikai.technology', '0904000003', 2, 4, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-10', NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (28, 'Quách Ngọc Tuyên', 'tuyen.quach@rikai.technology', '0904000004', 2, 4, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-04-01',
-- '2025-09-30', 'WARNING', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (29, 'Phạm Lê Tú', 'tu.pham@rikai.technology', '0904000005', 3, 4, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (30, 'Lê Thị Hoa', 'hoa.le@rikai.technology', '0904000006', 3, 4, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-07', NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (31, 'Đoàn Văn Hậu', 'hau.doan@rikai.technology', '0904000007', 4, 4, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (32, 'Vũ Thị Kim', 'kim.vu@rikai.technology', '0904000008', 4, 4, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-04-01',
-- '2025-09-30', 'COMPLETED', 'REJECTED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (33, 'Vương Văn Lộc', 'loc.vuong@rikai.technology', '0905000001', 1, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (34, 'Cao Thị Minh', 'minh.cao@rikai.technology', '0905000002', 1, 5, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (35, 'Đoàn Trung Sơn', 'son.doan@rikai.technology', '0905000003', 2, 5, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'PROPOSED', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (36, 'Châu Thị Ngọc', 'ngoc.chau@rikai.technology', '0905000004', 2, 5, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (37, 'Đàm Văn Phúc', 'phuc.dam@rikai.technology', '0905000005', 3, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (38, 'Nguyễn Thị Quỳnh', 'quynh.nguyen@rikai.technology', '0905000006', 3, 5, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'ACCEPTED', '2025-11-10', NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14'),
-- (39, 'Lại Văn Sang', 'sang.lai@rikai.technology', '0905000007', 4, 5, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:50:23'),
-- (40, 'Trần Thị Thảo', 'thao.tran@rikai.technology', '0905000008', 4, 5, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-05-01',
-- '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, '2026-01-16 02:50:23', '2026-01-16 02:54:14');
