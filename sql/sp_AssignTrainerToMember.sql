USE FitnessDB;
GO
-- Assign a personal trainer to a member
DROP PROCEDURE IF EXISTS sp_AssignTrainerToMember;
GO
CREATE PROCEDURE sp_AssignTrainerToMember
    @member_id    INT,
    @trainer_id   INT,
    @session_type VARCHAR(50)  = 'personal_training',
    @notes        VARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM Member  WHERE member_id  = @member_id)
        RAISERROR('Member %d not found.',  16, 1, @member_id);
    ELSE IF NOT EXISTS (SELECT 1 FROM Trainer WHERE trainer_id = @trainer_id)
        RAISERROR('Trainer %d not found.', 16, 1, @trainer_id);
    ELSE IF EXISTS (
        SELECT 1 FROM MemberTrainer
        WHERE member_id = @member_id AND trainer_id = @trainer_id AND session_type = @session_type
    )
        RAISERROR('This member-trainer-type combination already exists.', 16, 1);
    ELSE
        INSERT INTO MemberTrainer (member_id, trainer_id, assigned_date, session_type, notes)
        VALUES (@member_id, @trainer_id, CAST(GETDATE() AS DATE), @session_type, @notes);
END;
GO
