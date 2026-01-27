use intern_hub_db;
# set foreign_key_checks  = 0 ;
# truncate table plan_tasks;
# truncate table plan_modules;
#
# truncate table learning_plans;

DROP PROCEDURE IF EXISTS generate_comprehensive_data;

DELIMITER $$

CREATE PROCEDURE generate_comprehensive_data()
BEGIN
    DECLARE v_i INT DEFAULT 41;
    DECLARE v_max_id INT DEFAULT 540;
    DECLARE v_intern_id INT;
    DECLARE v_full_name VARCHAR(100);
    DECLARE v_email VARCHAR(100);
    DECLARE v_phone VARCHAR(20);
    DECLARE v_position_id INT;
    DECLARE v_batch_id INT;
    DECLARE v_mentor_id CHAR(36);
    DECLARE v_start_date DATE;
    DECLARE v_status ENUM('PENDING', 'INTERVIEWING', 'ACTIVE', 'COMPLETED', 'DROPPED', 'FAILED', 'WARNING');
    DECLARE v_random_surname VARCHAR(20);
    DECLARE v_random_middle VARCHAR(20);
    DECLARE v_random_name VARCHAR(20);

    DECLARE v_week INT;
    DECLARE v_report_count INT;
    DECLARE v_score DECIMAL(4,2);

    WHILE v_i <= v_max_id DO
        SET v_random_surname = ELT(FLOOR(1 + (RAND() * 8)), 'Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ');
        SET v_random_middle = ELT(FLOOR(1 + (RAND() * 6)), 'Văn', 'Thị', 'Đức', 'Thanh', 'Minh', 'Ngọc');
        SET v_random_name = ELT(FLOOR(1 + (RAND() * 10)), 'Tâm', 'Tín', 'Thành', 'Trung', 'Hùng', 'Dũng', 'Lan', 'Mai', 'Cúc', 'Trúc');

        SET v_full_name = CONCAT(v_random_surname, ' ', v_random_middle, ' ', v_random_name);
        SET v_email = CONCAT(LOWER(v_random_name), '.', LOWER(v_random_surname), v_i, '@rikai.technology');
        SET v_phone = CONCAT('09', LPAD(v_i, 8, '0'));
        SET v_position_id = FLOOR(1 + (RAND() * 5));
        SET v_batch_id = FLOOR(1 + (RAND() * 5));

        SET v_mentor_id = CASE FLOOR(1 + (RAND() * 6))
            WHEN 1 THEN '5e3ba91a-126b-42bb-b8ce-4a09cc2abf7a'
            WHEN 2 THEN '536e0b3a-9974-4a46-9c08-22b6a3ae1f9b'
            WHEN 3 THEN 'c8412e92-4541-4af1-a1b7-3e9e4e32477b'
            WHEN 4 THEN '2c638c0d-f4bc-4a53-b88d-1a75e9620830'
            WHEN 5 THEN 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d'
            ELSE 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e'
END;

        IF v_batch_id = 1 THEN SET v_start_date = '2025-01-01';
        ELSEIF v_batch_id = 2 THEN SET v_start_date = '2025-02-01';
        ELSEIF v_batch_id = 3 THEN SET v_start_date = '2025-03-01';
        ELSEIF v_batch_id = 4 THEN SET v_start_date = '2025-04-01';
ELSE SET v_start_date = '2025-05-01';
END IF;

        SET v_status = ELT(FLOOR(1 + (RAND() * 5)), 'COMPLETED', 'ACTIVE', 'DROPPED', 'WARNING', 'COMPLETED');

INSERT INTO interns (id, full_name, email, phone, position_id, batch_id, mentor_id, start_date, end_date, intern_status, created_at, updated_at)
VALUES (v_i, v_full_name, v_email, v_phone, v_position_id, v_batch_id, v_mentor_id, v_start_date, DATE_ADD(v_start_date, INTERVAL 6 MONTH), v_status, NOW(), NOW());

SET v_week = 1;
        SET v_report_count = FLOOR(5 + (RAND() * 10));

        IF v_status = 'COMPLETED' THEN
            SET v_report_count = 24;
END IF;

        WHILE v_week <= v_report_count DO
            SET v_score = 5 + (RAND() * 5);
            IF v_score > 10 THEN SET v_score = 10; END IF;

INSERT INTO weekly_reports (intern_id, mentor_id, week_number, week_start_date, tasks_assigned, tasks_completed, issues_risks, mentor_overall_comment, average_score, created_at, updated_at)
VALUES (
           v_i,
           v_mentor_id,
           v_week,
           DATE_ADD(v_start_date, INTERVAL (v_week - 1) WEEK),
           CONCAT('Tasks for week ', v_week, ' generated automatically'),
           CONCAT('Completed tasks for week ', v_week),
           IF(RAND() < 0.2, 'Minor issues encountered', NULL),
           CONCAT('Auto comment for ', v_full_name, ' week ', v_week),
           ROUND(v_score, 2),
           NOW(),
           NOW()
       );

SET v_week = v_week + 1;
END WHILE;

        SET v_i = v_i + 1;
END WHILE;
END$$

DELIMITER ;

CALL generate_comprehensive_data();

DROP PROCEDURE IF EXISTS generate_comprehensive_data;