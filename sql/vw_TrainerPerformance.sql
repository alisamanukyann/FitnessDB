USE FitnessDB;
GO
-- Trainer Performance
DROP VIEW IF EXISTS vw_TrainerPerformance;
GO
CREATE VIEW vw_TrainerPerformance AS
SELECT
    t.trainer_id,
    t.first_name + ' ' + t.last_name   AS trainer_name,
    t.specialization,
    COUNT(DISTINCT cs.session_id)       AS total_sessions,
    COUNT(DISTINCT a.member_id)         AS unique_members_taught,
    COUNT(DISTINCT mt.member_id)        AS personal_training_clients
FROM Trainer t
LEFT JOIN ClassSession  cs ON t.trainer_id  = cs.trainer_id
LEFT JOIN Attendance    a  ON cs.session_id = a.session_id AND a.status = 'attended'
LEFT JOIN MemberTrainer mt ON t.trainer_id  = mt.trainer_id
GROUP BY t.trainer_id, t.first_name, t.last_name, t.specialization;
GO
