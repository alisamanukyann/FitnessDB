-- ============================================================
--  Run this in SSMS BEFORE launching the Java app
--  Creates TWO logins:
--    1. fitnessdb_user  — Admin (db_owner, full access)
--    2. fitnessdb_member — Member portal (read-only + limited write)
-- ============================================================

USE master;
GO

-- ──────────────────────────────────────────────────────────────
-- ADMIN LOGIN (unchanged)
-- ──────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'fitnessdb_user')
BEGIN
    CREATE LOGIN fitnessdb_user WITH PASSWORD = 'Fitness@123!';
    PRINT 'Login fitnessdb_user created.';
END
ELSE
    PRINT 'Login fitnessdb_user already exists.';
GO

-- ──────────────────────────────────────────────────────────────
-- MEMBER PORTAL LOGIN
-- ──────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'fitnessdb_member')
BEGIN
    CREATE LOGIN fitnessdb_member WITH PASSWORD = 'Member@Portal1!';
    PRINT 'Login fitnessdb_member created.';
END
ELSE
    PRINT 'Login fitnessdb_member already exists.';
GO

USE FitnessDB;
GO

-- ── Admin database user ──────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'fitnessdb_user')
BEGIN
    CREATE USER fitnessdb_user FOR LOGIN fitnessdb_user;
    PRINT 'Database user fitnessdb_user created.';
END
ELSE
    PRINT 'Database user fitnessdb_user already exists.';
GO

ALTER ROLE db_owner ADD MEMBER fitnessdb_user;
PRINT 'fitnessdb_user granted db_owner on FitnessDB.';
GO

-- ── Member portal database user ──────────────────────────────
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'fitnessdb_member')
BEGIN
    CREATE USER fitnessdb_member FOR LOGIN fitnessdb_member;
    PRINT 'Database user fitnessdb_member created.';
END
ELSE
    PRINT 'Database user fitnessdb_member already exists.';
GO

-- SELECT permissions (read their own data via app logic)
GRANT SELECT ON Member               TO fitnessdb_member;
GRANT SELECT ON MemberMembership     TO fitnessdb_member;
GRANT SELECT ON MembershipPlan       TO fitnessdb_member;
GRANT SELECT ON MemberGoal           TO fitnessdb_member;
GRANT SELECT ON MemberTrainer        TO fitnessdb_member;
GRANT SELECT ON Attendance           TO fitnessdb_member;
GRANT SELECT ON ClassSession         TO fitnessdb_member;
GRANT SELECT ON Class                TO fitnessdb_member;
GRANT SELECT ON Trainer              TO fitnessdb_member;
GRANT SELECT ON TrainerCertification TO fitnessdb_member;
GRANT SELECT ON Payment              TO fitnessdb_member;

-- UPDATE own Member row (profile edit)
GRANT UPDATE ON Member               TO fitnessdb_member;

-- EXECUTE sp_AddAttendance (class registration)
GRANT EXECUTE ON sp_AddAttendance    TO fitnessdb_member;

PRINT 'fitnessdb_member granted SELECT + limited UPDATE + EXECUTE on FitnessDB.';
GO

PRINT '';
PRINT '========================================';
PRINT ' Setup complete!';
PRINT ' Admin login:  fitnessdb_user / Fitness@123!';
PRINT ' Member login: uses email + member_id';
PRINT '========================================';
