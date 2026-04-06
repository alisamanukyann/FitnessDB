USE FitnessDB;
GO

CREATE LOGIN fitnessApp WITH PASSWORD = 'Fitness@123!'; 

CREATE USER  fitnessApp FOR LOGIN fitnessApp; 

    ALTER ROLE db_owner ADD MEMBER fitnessApp; 

 

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'admin_user') 

    CREATE USER admin_user FOR LOGIN gym_admin; 

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'staff_user') 

    CREATE USER staff_user FOR LOGIN gym_staff; 

GO 

 

-- Admin: full access to all tables 

GRANT SELECT, INSERT, UPDATE, DELETE ON Member               TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON MemberMembership     TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON Trainer              TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON Class                TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON ClassSession         TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON Attendance           TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON MembershipPlan       TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON Payment              TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON MemberGoal           TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON TrainerCertification TO admin_user; 

GRANT SELECT, INSERT, UPDATE, DELETE ON MemberTrainer        TO admin_user; 

 

-- Staff: read + limited write, no deletes on sensitive tables 

GRANT SELECT, INSERT, UPDATE ON Member         TO staff_user; 

GRANT SELECT, INSERT         ON Attendance     TO staff_user; 

GRANT SELECT                 ON ClassSession   TO staff_user; 

GRANT SELECT                 ON Class          TO staff_user; 

GRANT SELECT                 ON Trainer        TO staff_user; 

GRANT SELECT                 ON MembershipPlan TO staff_user; 

GRANT SELECT                 ON MemberGoal     TO staff_user; 

GRANT SELECT                 ON MemberTrainer  TO staff_user; 

GRANT SELECT, INSERT         ON Payment        TO staff_user; 

 

DENY DELETE                  ON Member           TO staff_user; 

DENY INSERT, UPDATE, DELETE  ON MemberMembership TO staff_user; 

DENY DELETE                  ON Payment          TO staff_user; 

 

GRANT EXECUTE ON sp_AddAttendance         TO staff_user; 

GRANT EXECUTE ON sp_AssignTrainerToMember TO staff_user; 

GO 