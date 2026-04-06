-- ============================================================
-- FitnessCenterDB — DDL & DML  
-- ============================================================

--CREATE DATABASE FitnessDB;
GO
USE FitnessDB;
GO

DROP TABLE IF EXISTS MemberTrainer;
DROP TABLE IF EXISTS TrainerCertification;
DROP TABLE IF EXISTS MemberGoal;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS Attendance;
DROP TABLE IF EXISTS MemberMembership;
DROP TABLE IF EXISTS ClassSession;
DROP TABLE IF EXISTS Class;
DROP TABLE IF EXISTS Trainer;
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS MembershipPlan;
GO

-- ------------------------------------------------------------
-- CREATE TABLES (DDL)
-- ------------------------------------------------------------

CREATE TABLE MembershipPlan (
    plan_id         INT           IDENTITY(1,1) PRIMARY KEY,
    plan_name       VARCHAR(100)  NOT NULL UNIQUE,
    price           DECIMAL(10,2) NOT NULL CHECK (price > 0),
    duration_months INT           NOT NULL CHECK (duration_months > 0),
    description     TEXT
);

CREATE TABLE Member (
    member_id     INT          IDENTITY(1,1) PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    date_of_birth DATE,
    join_date     DATE         NOT NULL DEFAULT GETDATE(),
    address       VARCHAR(255)
);

CREATE TABLE MemberMembership (
    membership_id  INT           IDENTITY(1,1) PRIMARY KEY,
    member_id      INT           NOT NULL,
    plan_id        INT           NOT NULL,
    start_date     DATE          NOT NULL,
    end_date       DATE          NOT NULL,
    status         VARCHAR(10)   NOT NULL DEFAULT 'active'
                                 CHECK (status IN ('active', 'expired', 'canceled')),
    payment_amount DECIMAL(10,2) CHECK (payment_amount >= 0),
    CONSTRAINT FK_Membership_Member FOREIGN KEY (member_id)
        REFERENCES Member(member_id) ON DELETE CASCADE,
    CONSTRAINT FK_Membership_Plan FOREIGN KEY (plan_id)
        REFERENCES MembershipPlan(plan_id),
    CONSTRAINT CHK_Membership_Dates CHECK (end_date > start_date)
);

CREATE TABLE Trainer (
    trainer_id     INT          IDENTITY(1,1) PRIMARY KEY,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(20),
    specialization VARCHAR(100),
    hire_date      DATE         NOT NULL
);

CREATE TABLE Class (
    class_id     INT          IDENTITY(1,1) PRIMARY KEY,                                  
    class_name   VARCHAR(100) NOT NULL,
    description  TEXT,
    max_capacity INT          NOT NULL CHECK (max_capacity > 0),
);

CREATE TABLE ClassSession (
    session_id         INT         IDENTITY(1,1) PRIMARY KEY,
    class_id           INT         NOT NULL,
    trainer_id         INT         NOT NULL,
    session_date       DATE        NOT NULL,
    start_time         TIME        NOT NULL,
    end_time           TIME        NOT NULL,
    room               VARCHAR(50),
    current_enrollment INT         NOT NULL DEFAULT 0 CHECK (current_enrollment >= 0),
    CONSTRAINT FK_Session_Class   FOREIGN KEY (class_id)   REFERENCES Class(class_id),
    CONSTRAINT FK_Session_Trainer FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id),
    CONSTRAINT CHK_Session_Times  CHECK (end_time > start_time)
);

CREATE TABLE Attendance (
    attendance_id   INT         IDENTITY(1,1) PRIMARY KEY,
    member_id       INT         NOT NULL,
    session_id      INT         NOT NULL,
    attendance_date DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'attended'
                                CHECK (status IN ('attended', 'no-show')),
    CONSTRAINT UK_Attendance         UNIQUE (member_id, session_id),
    CONSTRAINT FK_Attendance_Member  FOREIGN KEY (member_id)  REFERENCES Member(member_id),
    CONSTRAINT FK_Attendance_Session FOREIGN KEY (session_id) REFERENCES ClassSession(session_id) ON DELETE CASCADE
);

-- Payment
CREATE TABLE Payment (
    payment_id      INT           IDENTITY(1,1) PRIMARY KEY,
    member_id       INT           NOT NULL,
    membership_id   INT           NOT NULL,
    payment_date    DATE          NOT NULL DEFAULT GETDATE(),
    amount          DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method          VARCHAR(30)   NOT NULL CHECK (method IN ('cash', 'card', 'bank_transfer', 'online')),
    CONSTRAINT FK_Payment_Member     FOREIGN KEY (member_id)    REFERENCES Member(member_id),
    CONSTRAINT FK_Payment_Membership FOREIGN KEY (membership_id) REFERENCES MemberMembership(membership_id)
);

-- MemberGoal
CREATE TABLE MemberGoal (
    goal_id      INT          IDENTITY(1,1) PRIMARY KEY,
    member_id    INT          NOT NULL,
    trainer_id   INT,
    goal_type    VARCHAR(50)  NOT NULL CHECK (goal_type IN (
                     'weight_loss', 'muscle_gain', 'endurance',
                     'flexibility', 'general_fitness', 'rehabilitation', 'other')),
    description  VARCHAR(255),
    target_value VARCHAR(50),
    target_date  DATE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'in_progress'
                              CHECK (status IN ('in_progress', 'achieved', 'abandoned')),
    created_date DATE         NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Goal_Member  FOREIGN KEY (member_id)  REFERENCES Member(member_id),
    CONSTRAINT FK_Goal_Trainer FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id)
);

-- TrainerCertification
CREATE TABLE TrainerCertification (
    cert_id      INT          IDENTITY(1,1) PRIMARY KEY,
    trainer_id   INT          NOT NULL,
    cert_name    VARCHAR(100) NOT NULL,
    issuing_body VARCHAR(100),
    issue_date   DATE         NOT NULL,
    expiry_date  DATE,
    CONSTRAINT FK_Cert_Trainer FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id),
    CONSTRAINT CHK_Cert_Dates CHECK (expiry_date IS NULL OR expiry_date > issue_date)
);

-- MemberTrainer 
CREATE TABLE MemberTrainer (
    assignment_id INT         IDENTITY(1,1) PRIMARY KEY,
    member_id     INT         NOT NULL,
    trainer_id    INT         NOT NULL,
    assigned_date DATE        NOT NULL DEFAULT GETDATE(),
    end_date      DATE,
    session_type  VARCHAR(50) NOT NULL DEFAULT 'personal_training'
                              CHECK (session_type IN ('personal_training', 'nutrition_coaching', 'rehabilitation', 'goal_coaching')),
    notes         VARCHAR(255),
    CONSTRAINT UK_MemberTrainer     UNIQUE (member_id, trainer_id, session_type),
    CONSTRAINT FK_MT_Member         FOREIGN KEY (member_id)  REFERENCES Member(member_id),
    CONSTRAINT FK_MT_Trainer        FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id),
    CONSTRAINT CHK_MT_Dates         CHECK (end_date IS NULL OR end_date > assigned_date)
);
GO

-- ------------------------------------------------------------
-- INDEXES
-- ------------------------------------------------------------
CREATE INDEX idx_member_email        ON Member(email);
CREATE INDEX idx_membership_member   ON MemberMembership(member_id);
CREATE INDEX idx_membership_plan     ON MemberMembership(plan_id);
CREATE INDEX idx_session_date        ON ClassSession(session_date);
CREATE INDEX idx_session_class       ON ClassSession(class_id);
CREATE INDEX idx_session_trainer     ON ClassSession(trainer_id);
CREATE INDEX idx_attendance_member   ON Attendance(member_id);
CREATE INDEX idx_attendance_session  ON Attendance(session_id);
CREATE INDEX idx_trainer_email       ON Trainer(email);
CREATE INDEX idx_payment_member      ON Payment(member_id);
CREATE INDEX idx_payment_date        ON Payment(payment_date);
CREATE INDEX idx_goal_member         ON MemberGoal(member_id);
CREATE INDEX idx_cert_trainer        ON TrainerCertification(trainer_id);
CREATE INDEX idx_mt_member           ON MemberTrainer(member_id);
CREATE INDEX idx_mt_trainer          ON MemberTrainer(trainer_id);
GO







-- ============================================================
-- INSERT SAMPLE DATA (DML)
-- ============================================================

-- ------------------------------------------------------------
-- MembershipPlan 
-- ------------------------------------------------------------
INSERT INTO MembershipPlan (plan_name, price, duration_months, description) VALUES
('Gym — 1 Month',               29.99,  1,  'Full gym floor access for one month.'),
('Gym — 3 Months',              79.99,  3,  'Full gym floor access, 3-month term. Save 11%.'),
('Gym — 6 Months',             139.99,  6,  'Full gym floor access, 6-month term. Save 22%.'),
('Gym — Annual',               199.99, 12,  'Full gym floor access, annual commitment. Best value.'),
('Gym + Pool — 1 Month',        49.99,  1,  'Gym floor and swimming pool access.'),
('Gym + Pool — 3 Months',      134.99,  3,  'Gym + pool, 3-month term.'),
('Gym + Pool — Annual',        449.99, 12,  'Gym + pool, annual commitment.'),
('All-Inclusive — 1 Month',     79.99,  1,  'Gym, pool, all group classes, and sauna.'),
('All-Inclusive — 3 Months',   219.99,  3,  'All-inclusive, 3-month term.'),
('All-Inclusive — Annual',     749.99, 12,  'All-inclusive annual — maximum savings.'),
('Student — 1 Month',           19.99,  1,  'Gym floor access. Valid student ID required.'),
('Senior — 1 Month',            24.99,  1,  'Gym + group classes for members 60+.');
GO

-- ------------------------------------------------------------
-- Member 
-- ------------------------------------------------------------
INSERT INTO Member (first_name, last_name, email, phone, date_of_birth, join_date) VALUES
('John',      'Doe',        'john.doe@email.com',       '123456780', '1990-05-12', '2025-01-11'),
('Emily',     'Johnson',    'emily.j@email.com',        '123456781', '1992-07-23', '2025-01-12'),
('Michael',   'Brown',      'michael.b@email.com',      '123456782', '1988-11-02', '2025-01-13'),
('Sarah',     'Davis',      'sarah.d@email.com',        '123456783', '1996-01-30', '2025-01-14'),
('David',     'Wilson',     'david.w@email.com',        '123456784', '1991-09-18', '2025-01-15'),
('Laura',     'Taylor',     'laura.t@email.com',        '123456785', '1993-06-25', '2025-01-16'),
('Daniel',    'Anderson',   'daniel.a@email.com',       '123456786', '1987-04-09', '2025-01-17'),
('Sophia',    'Thomas',     'sophia.t@email.com',       '123456787', '1998-12-01', '2025-01-18'),
('James',     'Jackson',    'james.j@email.com',        '123456788', '1989-08-14', '2025-01-19'),
('Olivia',    'White',      'olivia.w@email.com',       '123456789', '1997-10-05', '2025-01-20'),
('Liam',      'Harris',     'liam.h@email.com',         '123456790', '1994-03-11', '2025-01-21'),
('Ava',       'Martin',     'ava.m@email.com',          '123456791', '1999-02-17', '2025-01-22'),
('Noah',      'Thompson',   'noah.t@email.com',         '123456792', '1990-07-29', '2025-01-23'),
('Isabella',  'Garcia',     'isabella.g@email.com',     '123456793', '1995-05-06', '2025-01-24'),
('William',   'Martinez',   'william.m@email.com',      '123456794', '1986-09-21', '2025-01-25'),
('Mia',       'Robinson',   'mia.r@email.com',          '123456795', '1993-12-13', '2025-01-26'),
('Benjamin',  'Clark',      'benjamin.c@email.com',     '123456796', '1988-06-03', '2025-01-27'),
('Charlotte', 'Rodriguez',  'charlotte.r@email.com',    '123456797', '1996-08-27', '2025-01-28'),
('Lucas',     'Lewis',      'lucas.l@email.com',        '123456798', '1992-01-19', '2025-01-29'),
('Amelia',    'Lee',        'amelia.l@email.com',       '123456799', '1997-04-22', '2025-01-30'),
('Henry',     'Walker',     'henry.w@email.com',        '123456800', '1991-11-08', '2025-02-01'),
('Evelyn',    'Hall',       'evelyn.h@email.com',       '123456801', '1994-10-15', '2025-02-02'),
('Alexander', 'Allen',      'alex.a@email.com',         '123456802', '1989-02-26', '2025-02-03'),
('Harper',    'Young',      'harper.y@email.com',       '123456803', '1998-06-09', '2025-02-04'),
('Mason',     'Hernandez',  'mason.h@email.com',        '123456804', '1993-03-03', '2025-02-05'),
('Ella',      'King',       'ella.k@email.com',         '123456805', '1995-09-12', '2025-02-06'),
('Logan',     'Wright',     'logan.w@email.com',        '123456806', '1990-12-28', '2025-02-07'),
('Abigail',   'Lopez',      'abigail.l@email.com',      '123456807', '1996-05-17', '2025-02-08'),
('Ethan',     'Hill',       'ethan.h@email.com',        '123456808', '1987-07-07', '2025-02-09'),
('Emily',     'Scott',      'emily.s@email.com',        '123456809', '1992-11-19', '2025-02-10'),
('Jacob',     'Green',      'jacob.g@email.com',        '123456810', '1991-04-01', '2025-02-11'),
('Madison',   'Adams',      'madison.a@email.com',      '123456811', '1999-08-23', '2025-02-12'),
('Aiden',     'Baker',      'aiden.b@email.com',        '123456812', '1994-01-14', '2025-02-13'),
('Chloe',     'Nelson',     'chloe.n@email.com',        '123456813', '1997-03-27', '2025-02-14'),
('Matthew',   'Carter',     'matthew.c@email.com',      '123456814', '1988-10-30', '2025-02-15'),
('Lily',      'Mitchell',   'lily.m@email.com',         '123456815', '1995-06-06', '2025-02-16'),
('Sebastian', 'Perez',      'sebastian.p@email.com',    '123456816', '1993-02-02', '2025-02-17'),
('Grace',     'Roberts',    'grace.r@email.com',        '123456817', '1998-09-09', '2025-02-18'),
('Jack',      'Turner',     'jack.t@email.com',         '123456818', '1990-05-25', '2025-02-19'),
('Zoe',       'Phillips',   'zoe.p@email.com',          '123456819', '1996-12-12', '2025-02-20'),
('Nathan',    'Campbell',   'nathan.c@email.com',       '123456820', '1992-03-18', '2025-02-21'),
('Hannah',    'Parker',     'hannah.p@email.com',       '123456821', '1995-07-11', '2025-02-22'),
('Ryan',      'Evans',      'ryan.e@email.com',         '123456822', '1991-01-05', '2025-02-23'),
('Sofia',     'Edwards',    'sofia.e@email.com',        '123456823', '1998-04-14', '2025-02-24'),
('Caleb',     'Collins',    'caleb.c@email.com',        '123456824', '1989-06-29', '2025-02-25'),
('Victoria',  'Stewart',    'victoria.s@email.com',     '123456825', '1996-10-10', '2025-02-26'),
('Isaac',     'Sanchez',    'isaac.s@email.com',        '123456826', '1993-08-08', '2025-02-27'),
('Aria',      'Morris',     'aria.m@email.com',         '123456827', '1997-12-03', '2025-02-28'),
('Julian',    'Rogers',     'julian.r@email.com',       '123456828', '1990-09-22', '2025-03-01'),
('Scarlett',  'Reed',       'scarlett.r@email.com',     '123456829', '1994-11-16', '2025-03-02'),
('Gabriel',   'Cook',       'gabriel.c@email.com',      '123456830', '1988-02-27', '2025-03-03'),
('Layla',     'Morgan',     'layla.m@email.com',        '123456831', '1999-05-19', '2025-03-04'),
('Anthony',   'Bell',       'anthony.b@email.com',      '123456832', '1991-07-30', '2025-03-05'),
('Penelope',  'Murphy',     'penelope.m@email.com',     '123456833', '1995-01-21', '2025-03-06'),
('Dylan',     'Bailey',     'dylan.b@email.com',        '123456834', '1992-06-12', '2025-03-07'),
('Riley',     'Rivera',     'riley.r@email.com',        '123456835', '1998-03-09', '2025-03-08'),
('Aaron',     'Cooper',     'aaron.c@email.com',        '123456836', '1987-10-04', '2025-03-09'),
('Nora',      'Richardson', 'nora.r@email.com',         '123456837', '1996-02-15', '2025-03-10'),
('Eli',       'Cox',        'eli.c@email.com',          '123456838', '1993-09-01', '2025-03-11'),
('Luna',      'Howard',     'luna.h@email.com',         '123456839', '1997-08-20', '2025-03-12'),
('Connor',    'Ward',       'connor.w@email.com',       '123456840', '1990-04-07', '2025-03-13'),
('Avery',     'Torres',     'avery.t@email.com',        '123456841', '1994-12-25', '2025-03-14'),
('Jordan',    'Peterson',   'jordan.p@email.com',       '123456842', '1991-05-02', '2025-03-15'),
('Bella',     'Gray',       'bella.g@email.com',        '123456843', '1999-11-11', '2025-03-16'),
('Owen',      'Ramirez',    'owen.r@email.com',         '123456844', '1988-07-17', '2025-03-17'),
('Skylar',    'James',      'skylar.j@email.com',       '123456845', '1996-01-09', '2025-03-18'),
('Wyatt',     'Watson',     'wyatt.w@email.com',        '123456846', '1992-10-23', '2025-03-19'),
('Claire',    'Brooks',     'claire.b@email.com',       '123456847', '1995-03-28', '2025-03-20'),
('Luke',      'Kelly',      'luke.k@email.com',         '123456848', '1993-06-14', '2025-03-21'),
('Paisley',   'Sanders',    'paisley.s@email.com',      '123456849', '1998-09-05', '2025-03-22'),
('Leo',       'Price',      'leo.p@email.com',          '123456850', '1990-02-11', '2025-03-23'),
('Violet',    'Bennett',    'violet.b@email.com',       '123456851', '1997-07-07', '2025-03-24'),
('Hudson',    'Wood',       'hudson.w@email.com',       '123456852', '1991-12-19', '2025-03-25'),
('Stella',    'Barnes',     'stella.b@email.com',       '123456853', '1994-04-30', '2025-03-26'),
('Ezra',      'Ross',       'ezra.r@email.com',         '123456854', '1989-08-16', '2025-03-27'),
('Hazel',     'Henderson',  'hazel.h@email.com',        '123456855', '1996-05-25', '2025-03-28'),
('Thomas',    'Coleman',    'thomas.c@email.com',       '123456856', '1992-11-03', '2025-03-29'),
('Aurora',    'Jenkins',    'aurora.j@email.com',       '123456857', '1999-01-27', '2025-03-30'),
('Charles',   'Perry',      'charles.p@email.com',      '123456858', '1987-03-13', '2025-03-31'),
('Savannah',  'Powell',     'savannah.p@email.com',     '123456859', '1995-10-08', '2025-04-01');
GO

-- ------------------------------------------------------------
-- Trainer 
-- ------------------------------------------------------------
INSERT INTO Trainer (first_name, last_name, email, phone, specialization, hire_date) VALUES
('Alex',      'Morgan',    'alex.morgan@fitness.com',      '555100001', 'Personal Training',      '2023-01-10'),
('Chris',     'Walker',    'chris.walker@fitness.com',     '555100002', 'Strength Training',      '2022-11-05'),
('Megan',     'Hall',      'megan.hall@fitness.com',       '555100003', 'Yoga',                   '2024-02-14'),
('Brian',     'Allen',     'brian.allen@fitness.com',      '555100004', 'CrossFit',               '2021-09-20'),
('Jessica',   'Young',     'jessica.young@fitness.com',    '555100005', 'Pilates',                '2023-06-18'),
('Kevin',     'King',      'kevin.king@fitness.com',       '555100006', 'Bodybuilding',           '2020-03-12'),
('Rachel',    'Scott',     'rachel.scott@fitness.com',     '555100007', 'Cardio Training',        '2022-07-25'),
('Jason',     'Green',     'jason.green@fitness.com',      '555100008', 'HIIT',                   '2023-04-30'),
('Laura',     'Baker',     'laura.baker@fitness.com',      '555100009', 'Rehabilitation',         '2021-12-01'),
('Daniel',    'Adams',     'daniel.adams@fitness.com',     '555100010', 'Sports Conditioning',    '2022-10-09'),
('Sophia',    'Nelson',    'sophia.nelson@fitness.com',    '555100011', 'Zumba',                  '2024-01-15'),
('Matthew',   'Carter',    'matthew.carter@fitness.com',   '555100012', 'Powerlifting',           '2020-08-08'),
('Olivia',    'Mitchell',  'olivia.mitchell@fitness.com',  '555100013', 'Stretching & Mobility',  '2023-03-22'),
('Andrew',    'Perez',     'andrew.perez@fitness.com',     '555100014', 'Functional Training',    '2022-05-17'),
('Emma',      'Roberts',   'emma.roberts@fitness.com',     '555100015', 'Aerobics',               '2021-11-11'),
('Joshua',    'Turner',    'joshua.turner@fitness.com',    '555100016', 'Endurance Training',     '2020-06-06'),
('Chloe',     'Phillips',  'chloe.phillips@fitness.com',   '555100017', 'Dance Fitness',          '2024-02-01'),
('Ethan',     'Campbell',  'ethan.campbell@fitness.com',   '555100018', 'Weight Loss Coaching',   '2023-07-19'),
('Ava',       'Parker',    'ava.parker@fitness.com',       '555100019', 'Core Training',          '2022-09-27'),
('Noah',      'Evans',     'noah.evans@fitness.com',       '555100020', 'Martial Arts Fitness',   '2021-04-14'),
('Lily',      'Edwards',   'lily.edwards@fitness.com',     '555100021', 'Flexibility Training',   '2023-08-05'),
('William',   'Collins',   'william.collins@fitness.com',  '555100022', 'Circuit Training',       '2020-02-20'),
('Grace',     'Stewart',   'grace.stewart@fitness.com',    '555100023', 'Prenatal Fitness',       '2024-03-01'),
('James',     'Sanchez',   'james.sanchez@fitness.com',    '555100024', 'Postnatal Fitness',      '2022-12-12'),
('Ella',      'Morris',    'ella.morris@fitness.com',      '555100025', 'Balance Training',       '2023-05-10'),
('Marcus',    'Rivera',    'marcus.rivera@fitness.com',    '555100026', 'Olympic Weightlifting',  '2021-07-07'),
('Natalie',   'Cruz',      'natalie.cruz@fitness.com',     '555100027', 'Aqua Fitness',           '2022-03-15'),
('Tyler',     'Reed',      'tyler.reed@fitness.com',       '555100028', 'Kickboxing',             '2023-09-01'),
('Samantha',  'Price',     'samantha.price@fitness.com',   '555100029', 'Senior Fitness',         '2020-11-20'),
('Victor',    'Ross',      'victor.ross@fitness.com',      '555100030', 'Sports Nutrition',       '2021-05-25'),
('Hannah',    'Wood',      'hannah.wood@fitness.com',      '555100031', 'Spinning',               '2022-08-10'),
('Carlos',    'Flores',    'carlos.flores@fitness.com',    '555100032', 'TRX & Suspension',       '2023-02-28'),
('Stephanie', 'Bennett',   'stephanie.b@fitness.com',      '555100033', 'Boxing Fitness',         '2020-09-05'),
('Derek',     'Cox',       'derek.cox@fitness.com',        '555100034', 'Calisthenics',           '2024-01-08'),
('Amanda',    'Ward',      'amanda.ward@fitness.com',      '555100035', 'Mind-Body Wellness',     '2021-10-30'),
('Patrick',   'Bell',      'patrick.bell@fitness.com',     '555100036', 'Athletic Performance',   '2022-06-14'),
('Jasmine',   'Torres',    'jasmine.torres@fitness.com',   '555100037', 'Body Pump',              '2023-11-11'),
('Ryan',      'Gray',      'ryan.gray@fitness.com',        '555100038', 'Triathlon Coaching',     '2020-04-22'),
('Monica',    'James',     'monica.james@fitness.com',     '555100039', 'Barre Fitness',          '2021-08-18'),
('Trevor',    'Kelly',     'trevor.kelly@fitness.com',     '555100040', 'Functional Movement',    '2024-04-05'),
('Brianna',   'Sanders',   'brianna.sanders@fitness.com',  '555100041', 'HIIT & Tabata',          '2022-01-17'),
('Oscar',     'Coleman',   'oscar.coleman@fitness.com',    '555100042', 'Strength & Conditioning','2023-10-03');
GO

-- ------------------------------------------------------------
-- Class 
-- ------------------------------------------------------------
INSERT INTO Class (class_name, description, max_capacity) VALUES
('Yoga Basics',          'Introductory yoga focusing on flexibility and breathing.',          20),
('Advanced Yoga',        'Challenging poses and flows for experienced practitioners.',        15),
('HIIT Blast',           'High-intensity interval training for maximum fat burning.',         25),
('Pilates Core',         'Core strengthening and posture improvement through Pilates.',       18),
('Zumba Dance',          'Fun Latin-inspired dance-based cardio workout.',                    30),
('Strength Training',    'Progressive weight training to build muscle and strength.',         20),
('CrossFit WOD',         'High-intensity functional movements and workout of the day.',       15),
('Cardio Burn',          'Steady-state and interval cardio for stamina and heart health.',    25),
('Boxing Fitness',       'Boxing techniques combined with full-body cardio training.',        20),
('Spinning',             'Indoor cycling sessions for endurance and calorie burn.',           22),
('Stretch & Relax',      'Guided light stretching and deep relaxation techniques.',           18),
('Body Sculpt',          'Full-body toning and muscle definition exercises.',                 20),
('Functional Fitness',   'Compound exercises mimicking real-life movements.',                 15),
('Dance Fitness',        'Energetic choreographed dance routines for full-body workout.',     25),
('Kickboxing',           'Martial arts-inspired high-energy cardio workout.',                 20),
('Beginner Gym',         'Introductory class for new gym members learning the basics.',       30),
('Core & Abs',           'Focused abdominal and deep core strengthening session.',            18),
('Endurance Training',   'Improve aerobic stamina with long-duration progressive exercises.', 20),
('Balance & Stability',  'Exercises to improve proprioception and coordination.',             15),
('Senior Fitness',       'Low-impact functional exercises designed for adults 60+.',          20),
('Olympic Lifting',      'Technique-focused snatch and clean & jerk training.',               10),
('Aqua Aerobics',        'Pool-based low-impact aerobic workout.',                            20),
('TRX Suspension',       'Full-body workout using TRX suspension straps.',                    15),
('Calisthenics',         'Bodyweight strength and skill training.',                           18),
('Mind-Body Wellness',   'Meditation, breathwork, and gentle movement session.',              20),
('Athletic Performance', 'Sport-specific conditioning and agility drills.',                   16),
('Body Pump',            'Barbell-based high-rep group strength training.',                   25),
('Triathlon Prep',       'Swim, bike, and run conditioning for triathletes.',                 12),
('Barre Fitness',        'Ballet-inspired low-impact toning and flexibility class.',          20),
('Functional Movement',  'Movement screening and corrective exercise session.',               15),
('HIIT Tabata',          'Strict Tabata-protocol HIIT for advanced cardio conditioning.',     20),
('Strength & Conditioning','Periodized strength and athletic conditioning program.',          18),
('Sports Conditioning',  'Event-specific training for competitive athletes.',                 16),
('Powerlifting Club',    'Squat, bench, and deadlift technique and programming.',             12),
('Martial Arts Fitness', 'Fitness conditioning inspired by martial arts disciplines.',        20),
('Flexibility Flow',     'Dynamic and static stretching flows for full-body flexibility.',    18),
('Circuit Training',     'Timed multi-station full-body circuit for all levels.',             25),
('Prenatal Fitness',     'Safe, trimester-appropriate exercise for expectant mothers.',       12),
('Postnatal Recovery',   'Gentle recovery and core restoration for new mothers.',             12),
('Aerobics Classic',     'Traditional aerobics routines set to upbeat music.',               25);
GO

-- ------------------------------------------------------------
-- ClassSession 
-- ------------------------------------------------------------
INSERT INTO ClassSession (class_id, trainer_id, session_date, start_time, end_time, room, current_enrollment)
SELECT
    s.class_id,
    s.trainer_id,
    s.session_date,
    s.start_time,
    DATEADD(HOUR, 1, s.start_time) AS end_time,
    s.room,
    s.current_enrollment
FROM (
    SELECT
        ((rn - 1) % 40) + 1                                        AS class_id,
        ((rn - 1) % 42) + 1                                        AS trainer_id,
        DATEADD(DAY, (rn - 1) % 60, '2025-04-01')                  AS session_date,
        CAST(DATEADD(HOUR, 7 + ((rn - 1) % 12), '1900-01-01') AS TIME) AS start_time,
        CONCAT('Room ', ((rn - 1) % 8) + 1)                        AS room,
        ((rn - 1) % 18) + 1                                        AS current_enrollment
    FROM (
        SELECT ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS rn
        FROM (SELECT TOP 80 object_id FROM sys.objects ORDER BY object_id) x
    ) n
) s;
GO

-- ------------------------------------------------------------
-- MemberMembership 
-- ------------------------------------------------------------
INSERT INTO MemberMembership (member_id, plan_id, start_date, end_date, status, payment_amount)
SELECT
    m.member_id,
    ((m.member_id - 1) % 12) + 1                                            AS plan_id,
    DATEADD(DAY, -(m.member_id % 90), GETDATE())                             AS start_date,
    DATEADD(MONTH,
        CASE ((m.member_id - 1) % 12) + 1
            WHEN  1 THEN 1   WHEN  2 THEN 3   WHEN  3 THEN 6
            WHEN  4 THEN 12  WHEN  5 THEN 1   WHEN  6 THEN 3
            WHEN  7 THEN 12  WHEN  8 THEN 1   WHEN  9 THEN 3
            WHEN 10 THEN 12  WHEN 11 THEN 1   ELSE       1
        END,
        DATEADD(DAY, -(m.member_id % 90), GETDATE())
    )                                                                         AS end_date,
    CASE (m.member_id % 3)
        WHEN 0 THEN 'active'
        WHEN 1 THEN 'expired'
        ELSE        'canceled'
    END                                                                       AS status,
    p.price                                                                   AS payment_amount
FROM Member m
JOIN MembershipPlan p ON p.plan_id = ((m.member_id - 1) % 12) + 1;
GO

-- ------------------------------------------------------------
-- Attendance
-- ------------------------------------------------------------
INSERT INTO Attendance (member_id, session_id, attendance_date, status)
SELECT
    m.member_id,
    cs.session_id,
    cs.session_date,
    CASE WHEN (m.member_id + cs.session_id) % 7 = 0 THEN 'no-show' ELSE 'attended' END
FROM Member m
CROSS JOIN ClassSession cs
WHERE (m.member_id + cs.session_id) % 5 IN (0, 1, 2);
GO

-- ------------------------------------------------------------
-- Payment 
-- ------------------------------------------------------------
INSERT INTO Payment (member_id, membership_id, payment_date, amount, method)
SELECT
    mm.member_id,
    mm.membership_id,
    DATEADD(DAY, 1, mm.start_date)                                AS payment_date,
    mm.payment_amount                                             AS amount,
    CASE (mm.membership_id % 4)
        WHEN 0 THEN 'card'
        WHEN 1 THEN 'cash'
        WHEN 2 THEN 'online'
        ELSE        'bank_transfer'
    END                                                           AS method                                                
FROM MemberMembership mm;
GO

-- ------------------------------------------------------------
-- MemberGoal 
-- ------------------------------------------------------------
INSERT INTO MemberGoal (member_id, trainer_id, goal_type, description, target_value, target_date, status, created_date)
SELECT
    m.member_id,
    CASE WHEN m.member_id % 5 = 0 THEN NULL
         ELSE ((m.member_id - 1) % 42) + 1
    END,
    CASE (m.member_id % 7)
        WHEN 0 THEN 'weight_loss'
        WHEN 1 THEN 'muscle_gain'
        WHEN 2 THEN 'endurance'
        WHEN 3 THEN 'flexibility'
        WHEN 4 THEN 'general_fitness'
        WHEN 5 THEN 'rehabilitation'
        ELSE        'other'
    END,
    CASE (m.member_id % 7)
        WHEN 0 THEN 'Lose body fat and improve BMI.'
        WHEN 1 THEN 'Increase bench press by 20 kg.'
        WHEN 2 THEN 'Complete a 10K run without stopping.'
        WHEN 3 THEN 'Touch toes and improve hamstring flexibility.'
        WHEN 4 THEN 'Exercise at least 3 times per week consistently.'
        WHEN 5 THEN 'Recover from injury and regain full mobility.'
        ELSE        'Improve sleep quality through evening workouts.'
    END,
    CASE (m.member_id % 7)
        WHEN 0 THEN 'Lose 8 kg'
        WHEN 1 THEN 'Bench press 100 kg'
        WHEN 2 THEN '10K in under 55 min'
        WHEN 3 THEN 'Flat-palm floor touch'
        WHEN 4 THEN '3 sessions/week for 2 months'
        WHEN 5 THEN 'Full pain-free range of motion'
        ELSE        '7–8 hours sleep average'
    END,
    DATEADD(MONTH, 3 + (m.member_id % 6), GETDATE()),
    CASE (m.member_id % 3)
        WHEN 0 THEN 'in_progress'
        WHEN 1 THEN 'achieved'
        ELSE        'in_progress'
    END,
    DATEADD(DAY, -((m.member_id % 60) + 1), GETDATE())
FROM Member m;
GO

-- ------------------------------------------------------------
-- TrainerCertification 
-- ------------------------------------------------------------
INSERT INTO TrainerCertification (trainer_id, cert_name, issuing_body, issue_date, expiry_date)
SELECT
    t.trainer_id,
    CASE c.cert_num
        WHEN 1 THEN CASE (t.trainer_id % 8)
            WHEN 0 THEN 'NASM Certified Personal Trainer (CPT)'
            WHEN 1 THEN 'ACE Personal Trainer Certification'
            WHEN 2 THEN 'NSCA Certified Strength & Conditioning Specialist'
            WHEN 3 THEN 'ACSM Certified Exercise Physiologist'
            WHEN 4 THEN 'CrossFit Level 2 Trainer'
            WHEN 5 THEN 'RYT-200 Yoga Alliance Certification'
            WHEN 6 THEN 'AFAA Group Fitness Instructor'
            ELSE        'ISSA Certified Fitness Trainer'
        END
        ELSE 'CPR & AED Certification'
    END,
    CASE c.cert_num
        WHEN 1 THEN CASE (t.trainer_id % 8)
            WHEN 0 THEN 'NASM'         WHEN 1 THEN 'ACE'
            WHEN 2 THEN 'NSCA'         WHEN 3 THEN 'ACSM'
            WHEN 4 THEN 'CrossFit HQ'  WHEN 5 THEN 'Yoga Alliance'
            WHEN 6 THEN 'AFAA'         ELSE        'ISSA'
        END
        ELSE 'American Red Cross'
    END,
    CASE c.cert_num
        WHEN 1 THEN DATEADD(MONTH, -(t.trainer_id * 7 % 36), GETDATE())
        ELSE        DATEADD(MONTH, -(t.trainer_id % 18), GETDATE())
    END,
    CASE c.cert_num
        WHEN 1 THEN DATEADD(YEAR, 2, DATEADD(MONTH, -(t.trainer_id * 7 % 36), GETDATE()))
        ELSE        DATEADD(YEAR, 2, DATEADD(MONTH, -(t.trainer_id % 18), GETDATE()))
    END
FROM Trainer t
CROSS JOIN (VALUES (1), (2)) AS c(cert_num);
GO

-- ------------------------------------------------------------
-- MemberTrainer 
-- ------------------------------------------------------------
INSERT INTO MemberTrainer (member_id, trainer_id, assigned_date, end_date, session_type, notes)
SELECT
    m.member_id,
    ((m.member_id - 1) % 42) + 1,
    DATEADD(DAY, -((m.member_id % 60) + 1), GETDATE()),
    CASE WHEN m.member_id % 4 = 0
         THEN DATEADD(MONTH, 3, DATEADD(DAY, -((m.member_id % 60) + 1), GETDATE()))
         ELSE NULL
    END,
    CASE (m.member_id % 4)
        WHEN 0 THEN 'personal_training'
        WHEN 1 THEN 'nutrition_coaching'
        WHEN 2 THEN 'goal_coaching'
        ELSE        'personal_training'
    END,
    'Assigned at registration.'
FROM Member m
WHERE m.member_id % 3 <> 0;  
GO