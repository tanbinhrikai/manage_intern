CREATE TABLE IF NOT EXISTS learning_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_id INT, -- or BIGINT if error --
    title TEXT,
    description TEXT,
    generated_from_prompt TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_learning_plans_intern_id
    FOREIGN KEY (intern_id) REFERENCES interns(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS plan_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    title TEXT,
    focus_topic TEXT,
    order_index INT DEFAULT 0,

    CONSTRAINT fk_plan_modules_plan_id
    FOREIGN KEY (plan_id) REFERENCES learning_plans(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plan_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    title TEXT,
    description TEXT,
    resource_link TEXT,
    estimated_minutes INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'TODO',
    order_index INT DEFAULT 0,

    sub_plan_id BIGINT,

    CONSTRAINT fk_plan_tasks_module_id
    FOREIGN KEY (module_id) REFERENCES plan_modules(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_plan_tasks_sub_plan
    FOREIGN KEY (sub_plan_id) REFERENCES learning_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_learning_plan_intern ON learning_plans(intern_id);
CREATE INDEX idx_plan_module_plan ON plan_modules(plan_id);
CREATE INDEX idx_plan_task_module ON plan_tasks(module_id);