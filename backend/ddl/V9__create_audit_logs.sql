CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL, -- UUID of the actor (foreign key to users)
    action VARCHAR(50) NOT NULL, -- e.g., CREATE, UPDATE, DELETE, EVALUATE, STATUS_CHANGE
    entity_type VARCHAR(100) NOT NULL, -- e.g., INTERN, WEEKLY_REPORT, EVALUATION_SESSION
    entity_id BIGINT, -- ID of target entity (using BIGINT to handle Long/Integer primary keys)
    details TEXT NOT NULL, -- Detailed, HTML-ready, user-friendly description
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
