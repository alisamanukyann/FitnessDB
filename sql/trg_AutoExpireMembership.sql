USE FitnessDB;
GO
-- Automatically expire memberships whose end_date has passed
DROP TRIGGER IF EXISTS trg_AutoExpireMembership;
GO
CREATE TRIGGER trg_AutoExpireMembership
ON MemberMembership
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE MemberMembership
    SET status = 'expired'
    WHERE membership_id IN (SELECT membership_id FROM inserted)
      AND end_date < CAST(GETDATE() AS DATE)
      AND status   = 'active';
END;
GO
