USE FitnessDB;
GO
-- Most Active Members
DROP VIEW IF EXISTS vw_MostActiveMembers;
GO
CREATE VIEW vw_MostActiveMembers AS
SELECT
    m.member_id,
    m.first_name + ' ' + m.last_name   AS full_name,
    COUNT(a.attendance_id)              AS total_sessions_attended
FROM Member m
JOIN Attendance a ON m.member_id = a.member_id
WHERE a.status = 'attended'
GROUP BY m.member_id, m.first_name, m.last_name;
GO