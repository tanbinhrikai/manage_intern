CREATE TABLE IF NOT EXISTS roadmaps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position_id INT,
    batch_id INT,
    duration_month INT,
    status ENUM('DRAFT', 'PUBLISHED') DEFAULT 'DRAFT',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmaps_pos FOREIGN KEY (position_id) REFERENCES positions(id),
    CONSTRAINT fk_roadmaps_bat FOREIGN KEY (batch_id) REFERENCES internship_batches(id)
);

ALTER TABLE roadmap_nodes
ADD COLUMN roadmap_id INT;

ALTER TABLE roadmap_nodes
ADD CONSTRAINT fk_roadmap_node_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id);
