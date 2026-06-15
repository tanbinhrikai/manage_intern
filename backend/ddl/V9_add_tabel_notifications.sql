CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               receiver_id CHAR(36) COLLATE utf8mb4_unicode_ci,
                               title VARCHAR(255),
                               content VARCHAR(255),
                               type TINYINT,
                               is_read BIT(1) NOT NULL DEFAULT 0,
                               reference_id BIGINT,
                               created_at DATETIME(6),
                               PRIMARY KEY (id),
                               CONSTRAINT fk_notifications_receiver
                                   FOREIGN KEY (receiver_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;