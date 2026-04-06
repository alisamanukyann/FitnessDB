USE FitnessDB;
GO
-- Block booking when a session is fully booked; increment current_enrollment on success
DROP TRIGGER IF EXISTS trg_CheckCapacity;
GO
CREATE TRIGGER trg_CheckCapacity
ON Attendance
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN ClassSession cs ON i.session_id = cs.session_id
        JOIN Class        c  ON cs.class_id  = c.class_id
        WHERE cs.current_enrollment >= c.max_capacity
    )
    BEGIN
        RAISERROR('Enrollment failed: session is fully booked.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    INSERT INTO Attendance (member_id, session_id, attendance_date, status)
    SELECT member_id, session_id, attendance_date, status FROM inserted;

    UPDATE cs
    SET cs.current_enrollment = cs.current_enrollment + 1
    FROM ClassSession cs
    JOIN inserted i ON cs.session_id = i.session_id;
END;
GO
