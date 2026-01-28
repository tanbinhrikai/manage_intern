DROP TABLE IF EXISTS intern_roadmap_progress;

CREATE TABLE IF NOT EXISTS roadmap_nodes(
    id INT AUTO_INCREMENT PRIMARY KEY,
    -- Liên kết với vị trí công việc (VD: Java Backend Intern)
    -- Chỉ cần set ở Node cha cao nhất (Root), các node con có thể null hoặc thừa kế
    position_id INT,
    -- Cấu trúc đệ quy: Node con trỏ về Node cha
    parent_id INT,
    -- Tên bài học / Task
    title VARCHAR(255) NOT NULL,
    -- Phân loại node để AI và Code xử lý logic
    node_type VARCHAR(20) NOT NULL,
    -- Các giá trị: 'PHASE' (Tháng), 'MODULE' (Tuần), 'LESSON' (Bài), 'TASK' (Nhiệm vụ)
    description TEXT,
    -- Điều kiện Pass (Quan trọng cho AI Agent check)
    pass_condition TEXT,
    -- Context cho AI biết mục tiêu đầu ra của node này là gì
    learning_outcome TEXT,
    -- Ước lượng thời gian (giờ)
    estimated_hours DECIMAL(5,2),
    -- Thứ tự sắp xếp (1.1, 1.2...)
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Indexes & FK
    INDEX idx_node_parent (parent_id),
    INDEX idx_node_position (position_id),
    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES roadmap_nodes(id) ON DELETE CASCADE,
    CONSTRAINT chk_node_type CHECK (node_type IN ('PHASE', 'MODULE', 'LESSON', 'TASK'))
) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;

CREATE TABLE IF NOT EXISTS intern_roadmap_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT NOT NULL,
    node_id INT NOT NULL, -- Link tới roadmap_nodes
    -- Trạng thái chi tiết hơn
    status VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    -- LOCKED: Chưa học tới (do chưa xong bài trước)
    -- OPEN: Đã mở, có thể học
    -- IN_PROGRESS: Đang làm
    -- SUBMITTED: Đã nộp bài (chờ mentor duyệt)
    -- COMPLETED: Đã xong
    submitted_proof TEXT, -- Link Github PR, hoặc câu trả lời
    mentor_feedback TEXT, -- Mentor nhận xét task này
    completed_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_progress_intern_node (intern_id, node_id),
    INDEX idx_progress_status (status),
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES roadmap_nodes(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_status CHECK (status IN ('LOCKED', 'OPEN', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED', 'REJECTED'))
) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;