# CREATE TABLE IF NOT EXISTS chat_memory (
#     conversation_id VARCHAR(36) NOT NULL,
#     message_id VARCHAR(36) NOT NULL,
#     message_type VARCHAR(36) NOT NULL,
#     message_content TEXT NOT NULL,
#     message_metadata TEXT,
#     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
#     PRIMARY KEY (conversation_id, message_id)
#     );
#
# CREATE INDEX idx_chat_memory_conversation_id ON chat_memory(conversation_id);