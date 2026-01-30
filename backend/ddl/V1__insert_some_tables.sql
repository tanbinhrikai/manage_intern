ALTER TABLE evaluation_criteria ADD COLUMN parent_id INT;

-- =====================================================
-- I. WORK PERFORMANCE (ĐÁNH GIÁ KẾT QUẢ CÔNG VIỆC)
-- =====================================================

-- Main Criteria 1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (1, 'WORK_PERFORMANCE', 'Practical Project Skills', 'Evaluates practical skills and real-world task implementation', 1.00, NULL, 1, true);

-- Sub-criteria 1.1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (2, 'WORK_PERFORMANCE', 'Technical Competence & Execution Quality', 'Evaluates understanding of requirements, solution design, and implementation quality', 1.00, 1, 1, true);

-- Sub-criteria 1.2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (3, 'WORK_PERFORMANCE', 'Proactivity & Task Management', 'Evaluates self-management of progress and level of proactivity', 1.00, 1, 2, true);

-- =====================================================
-- II. ATTITUDE AND SOFT SKILLS (THÁI ĐỘ VÀ KỸ NĂNG MỀM)
-- =====================================================

-- Main Criteria 2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (4, 'ATTITUDE_SOFT_SKILLS', 'Work Attitude & Spirit', 'Evaluates work attitude and collaborative spirit', 1.00, NULL, 1, true);

-- Sub-criteria 2.1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (5, 'ATTITUDE_SOFT_SKILLS', 'Work Attitude & Collaboration', 'Evaluates positive, professional attitude and willingness to cooperate', 1.00, 4, 1, true);

-- Sub-criteria 2.2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (6, 'ATTITUDE_SOFT_SKILLS', 'Communication & Reporting', 'Evaluates communication skills and progress reporting', 1.00, 4, 2, true);

-- Main Criteria 3
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (7, 'ATTITUDE_SOFT_SKILLS', 'Teamwork Ability', 'Evaluates cooperation and teamwork capabilities', 1.00, NULL, 2, true);

-- Sub-criteria 3.1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (8, 'ATTITUDE_SOFT_SKILLS', 'Team Collaboration & Communication', 'Evaluates team integration, coordination, and internal communication', 1.00, 7, 1, true);

-- Sub-criteria 3.2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (9, 'ATTITUDE_SOFT_SKILLS', 'Contribution Level & Team Impact', 'Evaluates level of contribution and positive influence on the team', 1.00, 7, 2, true);

-- Main Criteria 4
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (10, 'ATTITUDE_SOFT_SKILLS', 'Deadline Compliance & Responsibility', 'Evaluates time management and sense of responsibility', 1.00, NULL, 3, true);

-- Sub-criteria 4.1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (11, 'ATTITUDE_SOFT_SKILLS', 'Deadline Compliance & Task Management', 'Evaluates ability to meet deadlines and manage time effectively', 1.00, 10, 1, true);

-- Sub-criteria 4.2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (12, 'ATTITUDE_SOFT_SKILLS', 'Responsibility & Commitment', 'Evaluates responsibility and commitment to assigned tasks', 1.00, 10, 2, true);

-- =====================================================
-- III. KNOWLEDGE APPLICATION (KHẢ NĂNG ỨNG DỤNG KIẾN THỨC)
-- =====================================================

-- Main Criteria 5
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (13, 'KNOWLEDGE_APPLICATION', 'Critical Thinking & Problem Solving', 'Evaluates analytical and problem-solving abilities', 1.00, NULL, 1, true);

-- Sub-criteria 5.1
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (14, 'KNOWLEDGE_APPLICATION', 'Problem Analysis & Solution Proposal', 'Evaluates ability to identify problems and propose feasible solutions', 1.00, 13, 1, true);

-- Sub-criteria 5.2
INSERT INTO evaluation_criteria (id, category, name, description, weight, parent_id, display_order, is_active)
VALUES (15, 'KNOWLEDGE_APPLICATION', 'Solution Implementation & Result Improvement', 'Evaluates solution implementation and ability to learn from results', 1.00, 13, 2, true);

-- =====================================================
-- SCORE DEFINITIONS
-- =====================================================

-- Sub-criteria 2 (id=2): Technical Competence & Execution Quality
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(2, 'EXCELLENT', 'Understands requirements correctly from the start, independently designs and implements complete solutions; high-quality output, stable, few bugs, clear code/documentation.', NOW()),
(2, 'GOOD', 'Understands requirements relatively well, implements quite independently; product meets requirements with minor bugs that are fixed quickly.', NOW()),
(2, 'AVERAGE', 'Understands requirements at a basic level, needs guidance for implementation; results run but contain multiple errors, requiring significant review and edits.', NOW()),
(2, 'WEAK', 'Frequently misunderstands requirements, poor implementation; product quality is substandard, relies heavily on mentors, or fails to complete tasks.', NOW());

-- Sub-criteria 3 (id=3): Proactivity & Task Management
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(3, 'EXCELLENT', 'Proactively researches, self-manages progress, completes tasks on or before deadlines, minimal dependence on mentors.', NOW()),
(3, 'GOOD', 'Works relatively independently, requires only minor feedback from mentors.', NOW()),
(3, 'AVERAGE', 'Can perform tasks but needs step-by-step guidance from mentors.', NOW()),
(3, 'WEAK', 'Can only perform very basic parts, major tasks require mentor assistance; sometimes fails to complete tasks.', NOW());

-- Sub-criteria 5 (id=5): Work Attitude & Collaboration
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(5, 'EXCELLENT', 'Always maintains a positive, professional attitude; respects others, listens and absorbs feedback quickly; excellent cooperative spirit, contributes positively to the collective.', NOW()),
(5, 'GOOD', 'Serious and receptive attitude; listens and adjusts when receiving feedback; stable work spirit.', NOW()),
(5, 'AVERAGE', 'Generally stable attitude, follows regulations but lacks genuine enthusiasm; participates in work at a sufficient level.', NOW()),
(5, 'WEAK', 'Passive, lacks seriousness; displays negative behavior (distracted, lack of focus, complaining); negatively affects team morale.', NOW());

-- Sub-criteria 6 (id=6): Communication & Reporting
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(6, 'EXCELLENT', 'Proactive, clear communication; asks focused questions; reports progress fully and timely, proactively informs about issues.', NOW()),
(6, 'GOOD', 'Relatively clear communication; exchanges and reports adequately, occasionally needs reminders from mentors.', NOW()),
(6, 'AVERAGE', 'Communication is timid or unclear; rarely reports proactively, usually only responds when asked.', NOW()),
(6, 'WEAK', 'Poor communication, vague responses or avoids exchange; frequently reports late or missing information, affecting work progress.', NOW());

-- Sub-criteria 8 (id=8): Team Collaboration & Communication
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(8, 'EXCELLENT', 'Integrates quickly, coordinates proactively; clear and coherent communication; creates an open exchange environment, effectively supports team efficiency.', NOW()),
(8, 'GOOD', 'Good cooperation, relatively clear communication; ready to participate and coordinate when needed, sometimes hesitant.', NOW()),
(8, 'AVERAGE', 'Basic coordination; communication is infrequent or unclear, mainly focuses on individual tasks.', NOW()),
(8, 'WEAK', 'Little exchange, poor communication; does not proactively coordinate, updates information late, negatively impacting overall progress.', NOW());

-- Sub-criteria 9 (id=9): Contribution Level & Team Impact
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(9, 'EXCELLENT', 'Frequently provides valuable input, shares knowledge; creates positive influence, helps bond the team and improve performance.', NOW()),
(9, 'GOOD', 'Actively participates in discussions, provides useful contributions; supports teammates when asked, contributing to common goals.', NOW()),
(9, 'AVERAGE', 'Limited contribution; rarely shares proactively, impact on the team is not distinct.', NOW()),
(9, 'WEAK', 'Little or no participation in discussions; lack of information sharing, sometimes causes difficulties or negative impact on the team.', NOW());

-- Sub-criteria 11 (id=11): Deadline Compliance & Task Management
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(11, 'EXCELLENT', 'Always completes on or before deadlines; prioritizes and manages time scientifically; handles parallel tasks effectively without reminders.', NOW()),
(11, 'GOOD', 'Mostly completes on time; manages time reasonably well, occasionally slightly late but gives advance notice.', NOW()),
(11, 'AVERAGE', 'Completes tasks but often needs reminders about deadlines; time management is ineffective when workload increases.', NOW()),
(11, 'WEAK', 'Frequently misses deadlines without notice; poor task organization, easily misses tasks, requires close monitoring by mentors.', NOW());

-- Sub-criteria 12 (id=12): Responsibility & Commitment
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(12, 'EXCELLENT', 'Demonstrates high sense of responsibility; proactively takes on extra work when needed; always ensures output quality.', NOW()),
(12, 'GOOD', 'Responsible for assigned work; rarely needs reminders; fulfills commitments.', NOW()),
(12, 'AVERAGE', 'Basic level of responsibility but unstable; sometimes hesitant or indecisive.', NOW()),
(12, 'WEAK', 'Avoids difficult tasks, pushes responsibility to others; affects the team progress and overall efficiency.', NOW());

-- Sub-criteria 14 (id=14): Problem Analysis & Solution Proposal
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(14, 'EXCELLENT', 'Quickly identifies the core nature of the problem; clearly analyzes causes and consequences; proposes multiple feasible solutions, considers pros and cons.', NOW()),
(14, 'GOOD', 'Correctly identifies most problems; proposes suitable solutions, occasionally needs mentor feedback for optimization.', NOW()),
(14, 'AVERAGE', 'Understands problems at a basic level; solutions are simplistic, lack feasibility, relies on mentors.', NOW()),
(14, 'WEAK', 'Frequently misunderstands problems; rarely proposes clear solutions, waits for mentor instructions.', NOW());

-- Sub-criteria 15 (id=15): Solution Implementation & Result Improvement
INSERT INTO criteria_score_definitions (criteria_id, score_label, description, created_at)
VALUES
(15, 'EXCELLENT', 'Implements logically, adjusts flexibly; resolves issues thoroughly, learns from experience, and shares knowledge with the team.', NOW()),
(15, 'GOOD', 'Implements reasonably well; issues are resolved, learns from experience but not systematically.', NOW()),
(15, 'AVERAGE', 'Needs significant mentor guidance; fixes are temporary, issues likely to reoccur.', NOW()),
(15, 'WEAK', 'Passive, easily gives up; problems are unresolved or handled incorrectly, repeats old errors.', NOW());


INSERT INTO departments(title)
VALUES
('RTE'),
('RTW'),
('X-Team'),
('QA'),
('CSD'),
('QC');

INSERT INTO positions(title)
VALUES
('Java'),
('Python'),
('PHP'),
('AI'),
('ReactJS');

INSERT INTO internship_batches (id, name, start_date, end_date, description, created_at)
VALUES
(1, 'RINT 01', '2025-01-01', '2025-06-30', 'Internship Batch RINT 01.', '2026-01-16 02:42:02'),
(2, 'RINT 02', '2025-02-01', '2025-07-31', 'Internship Batch RINT 02.', '2026-01-16 02:42:02'),
(3, 'RINT 03', '2025-03-01', '2025-08-31', 'Internship Batch RINT 03.', '2026-01-16 02:42:02'),
(4, 'RINT 04', '2025-04-01', '2025-09-30', 'Internship Batch RINT 04.', '2026-01-16 02:42:02'),
(5, 'RINT 05', '2025-05-01', '2025-10-31', 'Internship Batch RINT 05.', '2026-01-16 02:42:02');