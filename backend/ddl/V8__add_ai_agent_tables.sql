-- V8: Add missing columns and tables for AI Agent

-- 1. Add is_expanded column to roadmap_nodes
ALTER TABLE roadmap_nodes
ADD COLUMN IF NOT EXISTS is_expanded TINYINT(1) DEFAULT 0 AFTER difficulty;

CREATE TABLE IF NOT EXISTS spring_ai_chat_memory (
    conversation_id VARCHAR(256) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(10) NOT NULL,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_memory_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
