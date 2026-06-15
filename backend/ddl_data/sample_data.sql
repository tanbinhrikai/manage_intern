INSERT INTO intern_hub_db.users
(id, email, password_hash, full_name, date_of_birth, is_active, role_name, department_id, created_at, updated_at)
VALUES
    -- Department 1
    ('a3f91b72-1c4a-4e28-8d01-5e6b7d8a9c01', 'an.nguyen.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Nguyen Van An', '1990-05-12', 1, 'MENTOR', 1, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('b4f91b72-1c4a-4e28-8d01-5e6b7d8a9c02', 'lan.bui.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Bui Thi Lan', '1993-07-19', 1, 'MENTOR', 1, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),

    -- Department 2
    ('c5f91b72-1c4a-4e28-8d01-5e6b7d8a9c03', 'bich.tran.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Tran Thi Bich', '1988-11-20', 1, 'MENTOR', 2, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('d6f91b72-1c4a-4e28-8d01-5e6b7d8a9c04', 'nam.hoang.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Hoang Tuan Nam', '1994-09-22', 1, 'MENTOR', 2, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),

    -- Department 3
    ('e7f91b72-1c4a-4e28-8d01-5e6b7d8a9c05', 'cuong.le.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Le Hoang Cuong', '1995-02-28', 1, 'MENTOR', 3, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('f8f91b72-1c4a-4e28-8d01-5e6b7d8a9c06', 'oanh.ngo.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Ngo Thi Oanh', '1990-01-10', 1, 'MENTOR', 3, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),

    -- Department 4
    ('09f91b72-1c4a-4e28-8d01-5e6b7d8a9c07', 'giang.pham.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Pham Truong Giang', '1992-08-08', 1, 'MENTOR', 4, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('1af91b72-1c4a-4e28-8d01-5e6b7d8a9c08', 'phong.trinh.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Trinh Khac Phong', '1987-04-05', 1, 'MENTOR', 4, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),

    -- Department 5
    ('2bf91b72-1c4a-4e28-8d01-5e6b7d8a9c09', 'huong.vu.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Vu Thi Huong', '1991-03-15', 1, 'MENTOR', 5, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('3cf91b72-1c4a-4e28-8d01-5e6b7d8a9c10', 'quynh.dang.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Dang Thi Quynh', '1996-10-30', 1, 'MENTOR', 5, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),

    -- Department 6
    ('4df91b72-1c4a-4e28-8d01-5e6b7d8a9c11', 'khoi.doan.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Doan Minh Khoi', '1989-12-01', 1, 'MENTOR', 6, '2026-06-11 10:00:00', '2026-06-11 10:00:00'),
    ('5ef91b72-1c4a-4e28-8d01-5e6b7d8a9c12', 'son.ly.mentor@rikai.technology', '$2a$10$/FCCD3XobH2fRjEH9EGfWO30kj1zCMn0udpx/HIhMPVWcSDfe2QOa', 'Ly Bao Son', '1992-06-14', 1, 'MENTOR', 6, '2026-06-11 10:00:00', '2026-06-11 10:00:00');


INSERT INTO intern_hub_db.internship_batches
(id, name, start_date, end_date, description, status, created_at)
VALUES
    -- Kỳ thực tập đã hoàn thành năm ngoái
    (1, 'Summer Internship 2025', '2025-06-01', '2025-08-31', 'Chương trình thực tập sinh mùa hè 2025 tập trung vào Web Development.', 'COMPLETED', '2025-05-10 08:30:00'),
    (2, 'Fall Internship 2025', '2025-09-15', '2025-12-15', 'Kỳ thực tập mùa thu 2025 dành cho khối sinh viên năm cuối thực tập tốt nghiệp.', 'COMPLETED', '2025-08-25 09:15:00'),

    -- Kỳ thực tập bị hủy bỏ
    (3, 'Winter Bootcamp 2025', '2025-12-01', '2026-02-28', 'Chương trình đào tạo mùa đông (Đã hủy do điều chỉnh định hướng công ty).', 'CANCEL', '2025-11-05 10:00:00'),

    -- Kỳ thực tập đầu năm nay đã hoàn thành
    (4, 'Spring Internship 2026', '2026-02-15', '2026-05-15', 'Kỳ thực tập mùa xuân 2026 với đa dạng vị trí: Frontend, Backend, QA.', 'COMPLETED', '2026-01-20 14:00:00'),

    -- Kỳ thực tập đang diễn ra
    (5, 'Summer Internship 2026', '2026-06-01', '2026-08-31', 'Chương trình thực tập sinh mùa hè 2026 quy mô lớn, đào tạo Fullstack.', 'ONGOING', '2026-05-15 08:00:00'),
    (6, 'AI Research Batch 2026', '2026-05-01', '2026-10-31', 'Chương trình thực tập chuyên sâu 6 tháng mảng Trí tuệ nhân tạo (AI/ML).', 'ONGOING', '2026-04-10 10:30:00');

INSERT INTO intern_hub_db.interns
(full_name, email, phone, position_id, batch_id, mentor_id, start_date, end_date, intern_status, offer_status, offer_date, offer_notes)
VALUES
    -- ==========================================
    -- KỲ THỰC TẬP CŨ ĐÃ KẾT THÚC (Batch 1 & 2 - 2025)
    -- ==========================================
    -- Mentor a3f... (RTE) hướng dẫn Java (1)
    ('Tran Viet Anh', 'anh.tran@gmail.com', '0901234567', 1, 1, 'a3f91b72-1c4a-4e28-8d01-5e6b7d8a9c01', '2025-06-01', '2025-08-31', 'COMPLETED', 'ACCEPTED', '2025-08-25', 'Thái độ tốt, kỹ năng Java vững. Đã nhận offer Junior.'),
    -- Mentor b4f... (RTE) hướng dẫn ReactJS (5)
    ('Nguyen Mai Ly', 'ly.nguyen@gmail.com', '0987654321', 5, 1, 'b4f91b72-1c4a-4e28-8d01-5e6b7d8a9c02', '2025-06-01', '2025-08-31', 'COMPLETED', 'REJECTED', '2025-08-30', 'Chưa đạt yêu cầu về logic component. Cần trau dồi thêm.'),
    -- Mentor 9e5... (RTW) hướng dẫn PHP (3)
    ('Le Huy Hoang', 'hoang.le@gmail.com', '0912345678', 3, 2, '9e54425d-1ce7-4d6f-881e-1c874862e3d0', '2025-09-15', '2025-12-15', 'COMPLETED', 'ACCEPTED', '2025-12-10', 'Nắm bắt framework Laravel nhanh.'),
    -- Mentor e7f... (X-Team) hướng dẫn Python (2) -> Bỏ ngang
    ('Bui Thuy Linh', 'linh.bui@gmail.com', '0934567890', 2, 2, 'e7f91b72-1c4a-4e28-8d01-5e6b7d8a9c05', '2025-09-15', '2025-10-20', 'DROPPED', 'NONE', null, 'Nghỉ giữa chừng do bận lịch học trên trường.'),

    -- ==========================================
    -- KỲ THỰC TẬP BỊ HỦY HOẶC KỲ XUÂN (Batch 3 & 4)
    -- ==========================================
    -- Mentor f8f... (QA/X-team) hướng dẫn Python (2) (Automation) -> Batch 3 bị CANCEL nên dropped
    ('Pham Van Kien', 'kien.pham@gmail.com', '0945678901', 2, 3, 'f8f91b72-1c4a-4e28-8d01-5e6b7d8a9c06', '2025-12-01', '2025-12-10', 'DROPPED', 'NONE', null, 'Chương trình bootcampt bị công ty hủy bỏ.'),
    -- Mentor 2bf... (QC) hướng dẫn Java (1) (Automation Test)
    ('Doan Thao My', 'my.doan@gmail.com', '0956789012', 1, 4, '2bf91b72-1c4a-4e28-8d01-5e6b7d8a9c09', '2026-02-15', '2026-05-15', 'COMPLETED', 'PROPOSED', null, 'Đang đợi duyệt headcount từ ban giám đốc cho vị trí QA Automation.'),

    -- ==========================================
    -- KỲ THỰC TẬP ĐANG DIỄN RA (Batch 5 & 6 - Hiện tại là Tháng 6/2026)
    -- ==========================================
    -- Mentor c5f... (RTW) hướng dẫn ReactJS (5)
    ('Vu Minh Quan', 'quan.vu@gmail.com', '0967890123', 5, 5, 'c5f91b72-1c4a-4e28-8d01-5e6b7d8a9c03', '2026-06-01', '2026-08-31', 'ACTIVE', 'NONE', null, null),
    -- Mentor d6f... (RTW) hướng dẫn PHP (3) -> Có nguy cơ
    ('Ngo Tuan Kiet', 'kiet.ngo@gmail.com', '0978901234', 3, 5, 'd6f91b72-1c4a-4e28-8d01-5e6b7d8a9c04', '2026-06-01', '2026-08-31', 'WARNING', 'NONE', null, 'Điểm danh thiếu nhiều buổi, tiến độ task chậm.'),
    -- Mentor 09f... (CSD) hướng dẫn Python (2)
    ('Truong Hai Yen', 'yen.truong@gmail.com', '0989012345', 2, 5, '09f91b72-1c4a-4e28-8d01-5e6b7d8a9c07', '2026-06-01', '2026-08-31', 'ACTIVE', 'NONE', null, null),
    -- Mentor 4df... (Phòng 6) hướng dẫn AI (4) - Batch 6 (Research)
    ('Hoang Dinh Nam', 'nam.hoang@gmail.com', '0990123456', 4, 6, '4df91b72-1c4a-4e28-8d01-5e6b7d8a9c11', '2026-05-01', '2026-10-31', 'ACTIVE', 'NONE', null, 'Đang làm research về mô hình LLM nội bộ.'),
    -- Mentor 5ef... (Phòng 6) hướng dẫn AI (4) - Batch 6
    ('Dang Quoc Bao', 'bao.dang@gmail.com', '0923456789', 4, 6, '5ef91b72-1c4a-4e28-8d01-5e6b7d8a9c12', '2026-05-01', '2026-10-31', 'ACTIVE', 'NONE', null, null);