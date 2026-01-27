use intern_hub_db;

-- =====================================================
-- V6: INSERT COMPREHENSIVE SAMPLE DATA
-- =====================================================
-- This migration inserts sample data for testing the intern management system
-- Includes: Users (Admin, HR, Mentors), Interns, Weekly Reports, Evaluations
-- Using INSERT IGNORE to skip duplicates

-- =====================================================
-- 1. INSERT ADMIN AND HR USERS
-- Password: Admin@123 (BCrypt hash)
-- =====================================================
INSERT IGNORE INTO users (id, email, password_hash, full_name, date_of_birth, is_active, role_name, department_id, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'admin@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'System Admin', '1985-01-01', TRUE, 'ADMIN', 1, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000002', 'hr@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Trần Thị HR Manager', '1988-03-15', TRUE, 'HR', 1, NOW(), NOW());

-- =====================================================
-- 2. INSERT MENTORS
-- =====================================================
INSERT IGNORE INTO users (id, email, password_hash, full_name, date_of_birth, is_active, role_name, department_id, created_at, updated_at)
VALUES
    ('5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'mentor1@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Nguyễn Văn Mentor1', '1990-01-15', TRUE, 'MENTOR', 1, NOW(), NOW()),
    ('536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 'mentor2@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Trần Thị Mentor2', '1988-05-20', TRUE, 'MENTOR', 1, NOW(), NOW()),
    ('c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'mentor3@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Lê Văn Mentor3', '1992-08-10', TRUE, 'MENTOR', 2, NOW(), NOW()),
    ('2c638c0d-f4bc-4a53-b88d-1a75e9620830', 'mentor4@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Phạm Thị Mentor4', '1991-03-25', TRUE, 'MENTOR', 3, NOW(), NOW()),
    ('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'mentor5@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Hoàng Văn Mentor5', '1989-11-30', TRUE, 'MENTOR', 4, NOW(), NOW()),
    ('b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 'mentor6@rikai.technology', '$2a$10$rZpqQYWLO6wB2VKxU4qJHeG4Xd6h8WA7HzG.jVXKy3UHqTxjpCX6q', 'Vũ Thị Mentor6', '1993-07-22', TRUE, 'MENTOR', 5, NOW(), NOW());

-- =====================================================
-- 3. INSERT INTERNS
-- =====================================================
INSERT IGNORE INTO interns (id, full_name, email, phone, position_id, batch_id, mentor_id, start_date, end_date, intern_status, offer_status, offer_date, offer_notes, created_at, updated_at)
VALUES
    -- Batch 1 (RINT 01) - All COMPLETED
    (1, 'Nguyễn Văn An', 'an.nguyen@rikai.technology', '0901000001', 1, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-07', 'Excellent performance, strong Java skills', NOW(), NOW()),
    (2, 'Trần Thị Bình', 'binh.tran@rikai.technology', '0901000002', 1, 1, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-10', 'Good teamwork and communication', NOW(), NOW()),
    (3, 'Lê Văn Cường', 'cuong.le@rikai.technology', '0901000003', 2, 1, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-01-01', '2025-06-30', 'COMPLETED', 'PROPOSED', NULL, NULL, NOW(), NOW()),
    (4, 'Phạm Thị Dung', 'dung.pham@rikai.technology', '0901000004', 2, 1, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-01-01', '2025-02-28', 'DROPPED', 'NONE', NULL, 'Left for personal reasons', NOW(), NOW()),
    (5, 'Hoàng Văn Em', 'em.hoang@rikai.technology', '0901000005', 3, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-07', 'Outstanding PHP developer', NOW(), NOW()),
    (6, 'Vũ Thị Giang', 'giang.vu@rikai.technology', '0901000006', 3, 1, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-01-01', '2025-06-30', 'COMPLETED', 'REJECTED', NULL, 'Need more experience', NOW(), NOW()),
    (7, 'Đặng Văn Hùng', 'hung.dang@rikai.technology', '0901000007', 4, 1, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-01-01', '2025-06-30', 'COMPLETED', 'ACCEPTED', '2025-07-15', 'Promising AI researcher', NOW(), NOW()),
    (8, 'Bùi Thị Khanh', 'khanh.bui@rikai.technology', '0901000008', 5, 1, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-01-01', '2025-06-30', 'COMPLETED', 'NONE', NULL, NULL, NOW(), NOW()),

    -- Batch 2 (RINT 02) - Mixed status
    (9, 'Cao Văn Lâm', 'lam.cao@rikai.technology', '0902000001', 1, 2, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-10', 'Strong Spring Boot skills', NOW(), NOW()),
    (10, 'Đỗ Thị Mai', 'mai.do@rikai.technology', '0902000002', 1, 2, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, NOW(), NOW()),
    (11, 'Ngô Văn Nam', 'nam.ngo@rikai.technology', '0902000003', 2, 2, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-05', 'Python ML expert', NOW(), NOW()),
    (12, 'Hồ Thị Oanh', 'oanh.ho@rikai.technology', '0902000004', 2, 2, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-02-01', '2025-07-31', 'COMPLETED', 'PROPOSED', NULL, NULL, NOW(), NOW()),
    (13, 'Dương Văn Phú', 'phu.duong@rikai.technology', '0902000005', 3, 2, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-02-01', '2025-04-15', 'DROPPED', 'NONE', NULL, 'Health issues', NOW(), NOW()),
    (14, 'Lý Thị Quyên', 'quyen.ly@rikai.technology', '0902000006', 3, 2, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-12', NULL, NOW(), NOW()),
    (15, 'Mai Văn Duyên', 'duyen.mai@rikai.technology', '0902000007', 4, 2, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-02-01', '2025-07-31', 'COMPLETED', 'ACCEPTED', '2025-08-12', 'AI/ML specialist', NOW(), NOW()),
    (16, 'Trương Thị Sương', 'suong.truong@rikai.technology', '0902000008', 5, 2, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-02-01', '2025-07-31', 'COMPLETED', 'NONE', NULL, NULL, NOW(), NOW()),

    -- Batch 3 (RINT 03) - Mixed status
    (17, 'Võ Văn Tài', 'tai.vo@rikai.technology', '0903000001', 1, 3, 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-10', NULL, NOW(), NOW()),
    (18, 'Đinh Thị Uyên', 'uyen.dinh@rikai.technology', '0903000002', 1, 3, 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', '2025-03-01', '2025-08-31', 'COMPLETED', 'NONE', NULL, NULL, NOW(), NOW()),
    (19, 'Bạch Văn Vinh', 'vinh.bach@rikai.technology', '0903000003', 2, 3, 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-07', 'Top performer', NOW(), NOW()),
    (20, 'Phùng Thanh Tùng', 'tung.phung@rikai.technology', '0903000004', 2, 3, 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', '2025-03-01', '2025-08-31', 'COMPLETED', 'PROPOSED', NULL, NULL, NOW(), NOW()),
    (21, 'Đoàn Văn Ý', 'y.doan@rikai.technology', '0903000005', 3, 3, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-08', NULL, NOW(), NOW()),
    (22, 'Hà Thị Xuân', 'xuan.ha@rikai.technology', '0903000006', 4, 3, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-03-01', '2025-08-31', 'WARNING', 'NONE', NULL, 'Need improvement in communication', NOW(), NOW()),
    (23, 'Lê Văn Anh', 'anh.le2@rikai.technology', '0903000007', 5, 3, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-03-01', '2025-05-15', 'DROPPED', 'NONE', NULL, 'Found full-time job', NOW(), NOW()),
    (24, 'Lâm Thị Bích Ngọc', 'ngoc.lam@rikai.technology', '0903000008', 5, 3, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-03-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-09-05', 'Excellent ReactJS skills', NOW(), NOW()),

    -- Batch 4 (RINT 04) - Mostly COMPLETED
    (25, 'Mạc Văn Chiến', 'chien.mac@rikai.technology', '0904000001', 1, 4, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-05', NULL, NOW(), NOW()),
    (26, 'Ninh Thị Duyên', 'duyen.ninh@rikai.technology', '0904000002', 1, 4, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-04-01', '2025-09-30', 'COMPLETED', 'PROPOSED', NULL, NULL, NOW(), NOW()),
    (27, 'Ông Cao Thắng', 'thang.ong@rikai.technology', '0904000003', 2, 4, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-10', NULL, NOW(), NOW()),
    (28, 'Quách Ngọc Tuyên', 'tuyen.quach@rikai.technology', '0904000004', 3, 4, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-04-01', '2025-09-30', 'COMPLETED', 'NONE', NULL, NULL, NOW(), NOW()),
    (29, 'Phạm Lê Tú', 'tu.pham@rikai.technology', '0904000005', 4, 4, 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-08', 'Strong AI fundamentals', NOW(), NOW()),
    (30, 'Lê Thị Hoa', 'hoa.le@rikai.technology', '0904000006', 5, 4, 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', '2025-04-01', '2025-09-30', 'COMPLETED', 'ACCEPTED', '2025-10-07', NULL, NOW(), NOW()),

    -- Batch 5 (RINT 05) - Currently ACTIVE
    (31, 'Vương Văn Lộc', 'loc.vuong@rikai.technology', '0905000001', 1, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (32, 'Cao Thị Minh', 'minh.cao@rikai.technology', '0905000002', 1, 5, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (33, 'Đoàn Trung Sơn', 'son.doan@rikai.technology', '0905000003', 2, 5, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (34, 'Châu Thị Ngọc', 'ngoc.chau@rikai.technology', '0905000004', 2, 5, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (35, 'Đàm Văn Phúc', 'phuc.dam@rikai.technology', '0905000005', 3, 5, 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (36, 'Nguyễn Thị Quỳnh', 'quynh.nguyen2@rikai.technology', '0905000006', 4, 5, 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (37, 'Lại Văn Sang', 'sang.lai@rikai.technology', '0905000007', 5, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (38, 'Trần Thị Thảo', 'thao.tran2@rikai.technology', '0905000008', 5, 5, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', '2025-05-01', '2025-10-31', 'WARNING', 'NONE', NULL, 'Deadline issues reported', NOW(), NOW()),
    (39, 'Bùi Minh Tuấn', 'tuan.bui@rikai.technology', '0905000009', 1, 5, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', '2025-05-01', '2025-10-31', 'ACTIVE', 'NONE', NULL, NULL, NOW(), NOW()),
    (40, 'Đỗ Thị Vân', 'van.do@rikai.technology', '0905000010', 2, 5, '2c638c0d-f4bc-4a53-b88d-1a75e9620830', '2025-05-01', '2025-10-31', 'ACTIVE', 'PROPOSED', NULL, NULL, NOW(), NOW());

-- =====================================================
-- 4. INSERT WEEKLY REPORTS (For ACTIVE interns)
-- =====================================================
INSERT IGNORE INTO weekly_reports (id, intern_id, mentor_id, week_number, week_start_date, tasks_assigned, tasks_completed, issues_risks, mentor_overall_comment, average_score, created_at, updated_at)
VALUES
    -- Intern 31 (Vương Văn Lộc) - Week 1-8
    (1, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 1, '2025-05-05', 'Setup development environment, Read project documentation', 'Environment setup completed, Documentation reviewed', NULL, 'Bạn Lộc có tinh thần học hỏi tốt, nhanh chóng làm quen với môi trường làm việc. Cần cải thiện kỹ năng đặt câu hỏi.', 7.50, NOW(), NOW()),
    (2, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 2, '2025-05-12', 'Complete Spring Boot tutorial, Write first API endpoint', 'Tutorial completed, First API working', NULL, 'Tiến bộ đáng kể trong tuần này. Code quality tốt, cần học thêm về best practices.', 8.00, NOW(), NOW()),
    (3, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 3, '2025-05-19', 'Implement user authentication, Write unit tests', 'Authentication module done, 80% test coverage', NULL, 'Excellent work on authentication. Test coverage is impressive. Keep it up!', 8.50, NOW(), NOW()),
    (4, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 4, '2025-05-26', 'Integrate with database, Implement CRUD operations', 'Database integration complete, CRUD working', 'Minor issues with complex queries', 'Good progress. SQL skills improving. Need to focus on query optimization.', 7.80, NOW(), NOW()),
    (5, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 5, '2025-06-02', 'Build REST API endpoints, Add validation', 'All endpoints completed, Validation added', NULL, 'API design is clean and follows RESTful principles. Very satisfied with the progress.', 8.20, NOW(), NOW()),
    (6, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 6, '2025-06-09', 'Implement security features, Add error handling', 'Security layer done, Custom exceptions implemented', NULL, 'Great attention to security details. Error handling is comprehensive.', 8.50, NOW(), NOW()),
    (7, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 7, '2025-06-16', 'Performance optimization, Code review', 'Optimizations applied, Code refactored', NULL, 'Significant performance improvements. Shows deep understanding of the codebase.', 9.00, NOW(), NOW()),
    (8, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 8, '2025-06-23', 'Documentation, Knowledge transfer', 'Full documentation completed', NULL, 'Excellent documentation skills. Ready for more complex tasks.', 8.80, NOW(), NOW()),

    -- Intern 32 (Cao Thị Minh) - Week 1-8
    (9, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 1, '2025-05-05', 'Onboarding, Environment setup', 'Setup completed', NULL, 'Minh hòa nhập nhanh với team, có thái độ học hỏi tích cực.', 7.00, NOW(), NOW()),
    (10, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 2, '2025-05-12', 'Learn Java basics, Complete exercises', 'All exercises completed', NULL, 'Good foundation. Need to practice more with OOP concepts.', 7.20, NOW(), NOW()),
    (11, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 3, '2025-05-19', 'Database design, SQL queries', 'Database design approved', NULL, 'Solid understanding of database normalization. SQL skills are good.', 7.80, NOW(), NOW()),
    (12, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 4, '2025-05-26', 'Implement service layer', 'Services implemented', NULL, 'Good separation of concerns. Code is clean and readable.', 8.00, NOW(), NOW()),
    (13, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 5, '2025-06-02', 'API integration', 'Integration completed', NULL, 'Successfully integrated external APIs. Good problem-solving skills.', 8.20, NOW(), NOW()),
    (14, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 6, '2025-06-09', 'Testing and debugging', 'Bug fixes completed', 'Some complex bugs took longer', 'Improved debugging skills. Learning to use proper tools.', 7.50, NOW(), NOW()),
    (15, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 7, '2025-06-16', 'Feature development', 'New features added', NULL, 'Taking initiative on new features. Good communication.', 8.00, NOW(), NOW()),
    (16, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 8, '2025-06-23', 'Code review and refactoring', 'Refactoring completed', NULL, 'Shows maturity in code review. Accepts feedback well.', 8.30, NOW(), NOW()),

    -- Intern 33 (Đoàn Trung Sơn) - Week 1-6
    (17, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 1, '2025-05-05', 'Python environment setup', 'Setup done', NULL, 'Sơn có background tốt về Python, adapt nhanh với project.', 7.50, NOW(), NOW()),
    (18, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 2, '2025-05-12', 'Data preprocessing tasks', 'Data cleaned and processed', NULL, 'Good attention to data quality. Pandas skills are solid.', 8.00, NOW(), NOW()),
    (19, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 3, '2025-05-19', 'ML model implementation', 'Basic model working', NULL, 'Shows understanding of ML fundamentals. Need more practice with tuning.', 7.80, NOW(), NOW()),
    (20, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 4, '2025-05-26', 'Model optimization', 'Model accuracy improved', NULL, 'Significant improvement in model performance. Good research skills.', 8.50, NOW(), NOW()),
    (21, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 5, '2025-06-02', 'API deployment', 'Model deployed as API', NULL, 'Successfully deployed ML model. MLOps skills developing well.', 8.20, NOW(), NOW()),
    (22, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 6, '2025-06-09', 'Documentation and testing', 'Docs completed', NULL, 'Comprehensive documentation. Ready for production.', 8.80, NOW(), NOW()),

    -- Intern 38 (Trần Thị Thảo - WARNING status) - Week 1-4
    (23, 38, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 1, '2025-05-05', 'Learn ReactJS basics', 'Partially completed', 'Slow progress', 'Thảo cần cố gắng hơn, tiến độ chậm so với kế hoạch.', 5.50, NOW(), NOW()),
    (24, 38, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 2, '2025-05-12', 'Complete React components', 'Some components done', 'Missing deadline', 'Đã có cải thiện nhưng vẫn cần theo sát hơn. Phải hoàn thành đúng deadline.', 6.00, NOW(), NOW()),
    (25, 38, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 3, '2025-05-19', 'State management with Redux', 'Redux basic done', 'Communication issues', 'Cần chủ động hơn trong việc báo cáo. Technical skills đang cải thiện.', 6.20, NOW(), NOW()),
    (26, 38, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 4, '2025-05-26', 'API integration', 'In progress', 'Deadline at risk', 'Vẫn chưa đạt được kỳ vọng. Cần có kế hoạch cải thiện cụ thể.', 5.80, NOW(), NOW());

-- =====================================================
-- 5. INSERT WEEKLY REPORT DETAILS (Scores for each criteria)
-- =====================================================
INSERT IGNORE INTO weekly_report_details (weekly_report_id, criteria_id, score, comment, created_at, updated_at)
VALUES
    -- For report 1 (Intern 31, Week 1)
    (1, 2, 7, 'Good understanding of requirements', NOW(), NOW()),
    (1, 3, 8, 'Proactive in learning', NOW(), NOW()),
    (1, 5, 8, 'Positive attitude', NOW(), NOW()),
    (1, 6, 7, 'Good communication', NOW(), NOW()),
    (1, 8, 7, 'Works well with team', NOW(), NOW()),
    (1, 9, 7, 'Contributing to discussions', NOW(), NOW()),
    (1, 11, 8, 'On time with tasks', NOW(), NOW()),
    (1, 12, 8, 'Takes responsibility', NOW(), NOW()),
    (1, 14, 7, 'Identifies issues well', NOW(), NOW()),
    (1, 15, 7, 'Implements solutions', NOW(), NOW()),

    -- For report 7 (Intern 31, Week 7 - highest performing week)
    (7, 2, 9, 'Excellent code quality', NOW(), NOW()),
    (7, 3, 9, 'Very proactive', NOW(), NOW()),
    (7, 5, 9, 'Outstanding attitude', NOW(), NOW()),
    (7, 6, 9, 'Clear communication', NOW(), NOW()),
    (7, 8, 9, 'Great team player', NOW(), NOW()),
    (7, 9, 9, 'High contribution level', NOW(), NOW()),
    (7, 11, 9, 'Always on time', NOW(), NOW()),
    (7, 12, 9, 'Highly responsible', NOW(), NOW()),
    (7, 14, 9, 'Strong problem analysis', NOW(), NOW()),
    (7, 15, 9, 'Effective implementation', NOW(), NOW()),

    -- For report 23 (Intern 38, Week 1 - lower scores)
    (23, 2, 5, 'Needs improvement in technical skills', NOW(), NOW()),
    (23, 3, 5, 'Lacks proactivity', NOW(), NOW()),
    (23, 5, 6, 'Acceptable attitude', NOW(), NOW()),
    (23, 6, 5, 'Communication needs work', NOW(), NOW()),
    (23, 8, 5, 'Limited collaboration', NOW(), NOW()),
    (23, 9, 5, 'Low contribution', NOW(), NOW()),
    (23, 11, 5, 'Deadline issues', NOW(), NOW()),
    (23, 12, 6, 'Basic responsibility', NOW(), NOW()),
    (23, 14, 5, 'Weak problem analysis', NOW(), NOW()),
    (23, 15, 5, 'Implementation issues', NOW(), NOW());

-- =====================================================
-- 6. INSERT EVALUATION SESSIONS
-- =====================================================
INSERT IGNORE INTO evaluation_sessions (id, intern_id, mentor_id, session_type, evaluation_date, final_score, level_assessment, conclusion, overall_comment, created_at, updated_at)
VALUES
    -- Intern 1 (Nguyễn Văn An) - All three evaluations
    (1, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'FIRST_TERM', '2025-03-01', 7.50, 'Junior Developer', 'PASS', 'An có nền tảng tốt, làm việc chăm chỉ, cần cải thiện communication skills.', NOW(), NOW()),
    (2, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'MID_TERM', '2025-04-30', 8.20, 'Junior Developer', 'PASS', 'Có tiến bộ rõ rệt, kỹ năng technical đang phát triển tốt.', NOW(), NOW()),
    (3, 1, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'FINAL', '2025-06-30', 8.80, 'Mid-level Developer', 'PASS', 'Xuất sắc! Đã sẵn sàng làm full-time developer, recommend strongly for offer.', NOW(), NOW()),

    -- Intern 5 (Hoàng Văn Em)
    (4, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'FIRST_TERM', '2025-03-01', 8.00, 'Junior Developer', 'PASS', 'Em nắm bắt rất nhanh, có tư duy logic tốt.', NOW(), NOW()),
    (5, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'MID_TERM', '2025-04-30', 8.50, 'Junior Developer', 'PASS', 'Tiếp tục phát triển tốt, đã có thể độc lập trong công việc.', NOW(), NOW()),
    (6, 5, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'FINAL', '2025-06-30', 9.00, 'Mid-level Developer', 'PASS', 'Outstanding performance throughout the internship!', NOW(), NOW()),

    -- Intern 7 (Đặng Văn Hùng) - AI track
    (7, 7, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'FIRST_TERM', '2025-03-01', 7.80, 'AI Junior', 'PASS', 'Good foundation in AI/ML concepts.', NOW(), NOW()),
    (8, 7, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'MID_TERM', '2025-04-30', 8.30, 'AI Junior', 'PASS', 'Strong progress in deep learning projects.', NOW(), NOW()),
    (9, 7, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'FINAL', '2025-06-30', 8.70, 'AI Engineer', 'PASS', 'Ready to contribute to production AI systems.', NOW(), NOW()),

    -- Active interns - FIRST_TERM only
    (10, 31, '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'FIRST_TERM', '2025-06-30', 8.25, 'Junior Developer', 'PASS', 'Lộc có tiềm năng cao, làm việc có trách nhiệm.', NOW(), NOW()),
    (11, 32, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 'FIRST_TERM', '2025-06-30', 7.80, 'Junior Developer', 'PASS', 'Minh đang phát triển đúng hướng.', NOW(), NOW()),
    (12, 33, 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 'FIRST_TERM', '2025-06-30', 8.10, 'ML Junior', 'PASS', 'Shows strong potential in ML field.', NOW(), NOW()),

    -- Warning intern - struggling
    (13, 38, '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 'FIRST_TERM', '2025-06-30', 5.80, 'Trainee', 'NEED_IMPROVEMENT', 'Cần cải thiện nhiều về kỹ năng và thái độ làm việc.', NOW(), NOW());

-- =====================================================
-- 7. INSERT EVALUATION SCORES
-- =====================================================
INSERT IGNORE INTO evaluation_scores (session_id, criteria_id, score, comment)
VALUES
    -- Intern 1 - Final Evaluation (high scores)
    (3, 1, 9, 'Excellent practical skills'),
    (3, 4, 9, 'Great work attitude'),
    (3, 7, 8, 'Good teamwork'),
    (3, 10, 9, 'Always meets deadlines'),
    (3, 13, 8, 'Strong problem solving'),

    -- Intern 38 - First Term (low scores)
    (13, 1, 5, 'Needs significant improvement'),
    (13, 4, 6, 'Attitude improving slowly'),
    (13, 7, 5, 'Limited team contribution'),
    (13, 10, 5, 'Deadline compliance issues'),
    (13, 13, 6, 'Basic problem solving only');

-- =====================================================
-- 8. INSERT INTERNSHIP ROADMAPS
-- =====================================================
INSERT IGNORE INTO internship_roadmaps (id, position_id, stage_name, stage_order, description, duration_weeks, expected_outcomes, created_at)
VALUES
    (1, 1, 'ONBOARDING', 1, 'Environment setup and team introduction', 2, 'Familiar with tools and team', NOW()),
    (2, 1, 'TRAINING', 2, 'Core Java and Spring Boot training', 4, 'Basic Spring Boot application', NOW()),
    (3, 1, 'PROJECT', 3, 'Contribute to real project', 12, 'Independent feature development', NOW()),
    (4, 1, 'EVALUATION', 4, 'Final evaluation and review', 2, 'Ready for full-time offer', NOW()),
    (5, 2, 'ONBOARDING', 1, 'Python environment and tools', 2, 'Setup complete', NOW()),
    (6, 2, 'TRAINING', 2, 'Python and ML fundamentals', 4, 'Basic ML models', NOW()),
    (7, 2, 'PROJECT', 3, 'ML project development', 12, 'Production-ready model', NOW()),
    (8, 2, 'EVALUATION', 4, 'Final evaluation', 2, 'Ready for offer', NOW());

-- =====================================================
-- 9. INSERT INTERN ROADMAP PROGRESS
-- =====================================================
INSERT IGNORE INTO intern_roadmap_progress (intern_id, roadmap_id, status, start_date, expected_end_date, actual_completion_date, mentor_notes)
VALUES
    -- Intern 31 (Java)
    (31, 1, 'COMPLETED', '2025-05-01', '2025-05-14', '2025-05-10', 'Finished onboarding early'),
    (31, 2, 'COMPLETED', '2025-05-15', '2025-06-11', '2025-06-08', 'Training completed successfully'),
    (31, 3, 'IN_PROGRESS', '2025-06-12', '2025-09-03', NULL, 'Contributing well to main project'),

    -- Intern 33 (Python/ML)
    (33, 5, 'COMPLETED', '2025-05-01', '2025-05-14', '2025-05-12', 'Quick setup'),
    (33, 6, 'COMPLETED', '2025-05-15', '2025-06-11', '2025-06-10', 'Good ML fundamentals'),
    (33, 7, 'IN_PROGRESS', '2025-06-12', '2025-09-03', NULL, 'Working on NLP project'),

    -- Intern 38 (Warning - behind schedule)
    (38, 1, 'COMPLETED', '2025-05-01', '2025-05-14', '2025-05-20', 'Delayed onboarding'),
    (38, 2, 'OVERDUE', '2025-05-21', '2025-06-17', NULL, 'Still in training phase, behind schedule');

-- =====================================================
-- 10. INSERT SYSTEM ALERTS
-- =====================================================
INSERT IGNORE INTO system_alerts (alert_type, target_user_id, intern_id, title, message, severity, is_read, created_at)
VALUES
    ('INTERN_AT_RISK', '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 38, 'Intern At Risk Alert', 'Trần Thị Thảo đang có điểm trung bình thấp (5.80) và cần được hỗ trợ thêm.', 'warning', FALSE, NOW()),
    ('EVALUATION_DUE', '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 31, 'Mid-term Evaluation Due', 'Đánh giá giữa kỳ cho Vương Văn Lộc sẽ đến hạn vào tuần tới.', 'info', FALSE, NOW()),
    ('DEADLINE_APPROACHING', 'c8412e92-4541-4af1-a1b7-3e9e4e32477b', 33, 'Project Milestone Coming Up', 'Đoàn Trung Sơn có deadline milestone vào ngày 2025-07-15.', 'info', TRUE, NOW()),
    ('MENTOR_OVERLOADED', '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', NULL, 'Mentor Workload Alert', 'Mentor Nguyễn Văn Mentor1 đang hướng dẫn 5 intern, có thể cần hỗ trợ.', 'warning', FALSE, NOW());

-- =====================================================
-- 11. INSERT AUDIT LOGS
-- =====================================================
INSERT IGNORE INTO audit_logs (user_id, action, entity_type, entity_id, old_value, new_value, details, ip_address, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'CREATE', 'INTERN', 31, NULL, 'Vương Văn Lộc', 'Created new intern for Batch RINT 05', '192.168.1.1', NOW()),
    ('5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a', 'UPDATE', 'WEEKLY_REPORT', 1, NULL, NULL, 'Submitted weekly report for intern 31', '192.168.1.10', NOW()),
    ('536e0b3a-9974-4a46-9c08-22b6a3ae1f9b', 'UPDATE', 'INTERN', 38, 'ACTIVE', 'WARNING', 'Changed status due to performance concerns', '192.168.1.11', NOW()),
    ('00000000-0000-0000-0000-000000000002', 'CREATE', 'EVALUATION_SESSION', 10, NULL, NULL, 'Created first term evaluation for intern 31', '192.168.1.2', NOW());
