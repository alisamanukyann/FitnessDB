USE FitnessDB;
GO

-- Active members with their current membership plan
SELECT
    m.first_name + ' ' + m.last_name   AS full_name,
    p.plan_name,
    mm.start_date,
    mm.end_date
FROM Member m
JOIN MemberMembership mm ON m.member_id = mm.member_id
JOIN MembershipPlan   p  ON mm.plan_id  = p.plan_id
WHERE mm.status = 'active'
ORDER BY mm.end_date;

-- Members and their assigned trainers 
SELECT
    m.first_name + ' ' + m.last_name   AS member_name,
    t.first_name + ' ' + t.last_name   AS trainer_name,
    t.specialization,
    mt.session_type,
    mt.assigned_date
FROM MemberTrainer mt
JOIN Member  m ON mt.member_id  = m.member_id
JOIN Trainer t ON mt.trainer_id = t.trainer_id
ORDER BY m.last_name, mt.assigned_date;

