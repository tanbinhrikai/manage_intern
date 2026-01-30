-- Update evaluation_sessions: Add FIRST_TERM to session_type constraint
ALTER TABLE evaluation_sessions
    DROP CHECK chk_session_type;

ALTER TABLE evaluation_sessions
    ADD CONSTRAINT chk_session_type CHECK (session_type IN ('WEEKLY', 'FIRST_TERM', 'MID_TERM', 'FINAL'));

-- Update evaluation_scores: Change score from TINYINT to DECIMAL(4,2) to support decimal scores
ALTER TABLE evaluation_scores
    MODIFY score DECIMAL(4, 2);

-- Update score range constraint to support decimal values
ALTER TABLE evaluation_scores
    DROP CHECK chk_score_range;

ALTER TABLE evaluation_scores
    ADD CONSTRAINT chk_score_range CHECK (score IS NULL OR (score >= 1.0 AND score <= 10.0));
