ALTER TABLE internship_batches
    ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT' AFTER description;