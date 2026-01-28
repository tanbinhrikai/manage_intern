ALTER TABLE positions DROP FOREIGN KEY positions_ibfk_1;
ALTER TABLE positions DROP INDEX idx_position_department;
ALTER TABLE positions DROP COLUMN department_id;