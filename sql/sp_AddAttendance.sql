USE FitnessDB;
GO
-- Register a member for a class session
DROP PROCEDURE IF EXISTS sp_AddAttendance;
GO
CREATE PROCEDURE sp_AddAttendance
    @member_id  INT,
    @session_id INT
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM Member       WHERE member_id  = @member_id)
        RAISERROR('Member %d not found.',  16, 1, @member_id);
    ELSE IF NOT EXISTS (SELECT 1 FROM ClassSession WHERE session_id = @session_id)
        RAISERROR('Session %d not found.', 16, 1, @session_id);
    ELSE IF EXISTS (SELECT 1 FROM Attendance  WHERE member_id = @member_id AND session_id = @session_id)
        RAISERROR('Member is already registered for this session.', 16, 1);
    ELSE
        INSERT INTO Attendance (member_id, session_id, attendance_date, status)
        SELECT @member_id, @session_id, session_date, 'attended'
        FROM   ClassSession WHERE session_id = @session_id;
END;
GO