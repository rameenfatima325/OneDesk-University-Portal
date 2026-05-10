-- =====================================================================
-- OneDesk — MASTER DATABASE SETUP
--   admin     /  admin123     (admin)
--   zahra     /  zahra123     (student, student_id = 2)
--   aizah     /  aizah123     (student, student_id = 3)
--   rameen    /  rameen123    (student, student_id = 4)
-- =====================================================================

USE master;
GO

IF DB_ID('OneDesk') IS NOT NULL
BEGIN
    ALTER DATABASE OneDesk SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE OneDesk;
END
GO

CREATE DATABASE OneDesk;
GO

USE OneDesk;
GO

-- ---------------------------------------------------------------------
-- Shared tables: users, students, audit_log, payments, notifications
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT             IDENTITY(1,1) PRIMARY KEY,
    username       VARCHAR(50)     NOT NULL UNIQUE,
    password_hash  VARCHAR(255)    NOT NULL,
    role           VARCHAR(10)     NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
    full_name      VARCHAR(100)    NOT NULL,
    email          VARCHAR(100)    NOT NULL UNIQUE,
    is_active      BIT             DEFAULT 1,
    created_at     DATETIME2       DEFAULT SYSDATETIME(),
    last_login     DATETIME2       NULL
);
GO

CREATE TABLE students (
    student_id         INT            PRIMARY KEY,
    roll_number        VARCHAR(20)    NOT NULL UNIQUE,
    degree_program     VARCHAR(50)    NOT NULL,
    semester           INT            NOT NULL CHECK (semester BETWEEN 1 AND 12),
    cgpa               DECIMAL(3,2)   DEFAULT 0.00 CHECK (cgpa BETWEEN 0.00 AND 4.00),
    enrollment_status  VARCHAR(20)    DEFAULT 'ACTIVE',
    CONSTRAINT fk_student_user FOREIGN KEY (student_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);
GO

CREATE TABLE audit_log (
    log_id            INT             IDENTITY(1,1) PRIMARY KEY,
    admin_username    VARCHAR(50)     NOT NULL,
    request_id        INT             NOT NULL,
    request_type      VARCHAR(50)     NOT NULL,
    old_status        VARCHAR(50)     NULL,
    new_status        VARCHAR(50)     NULL,
    action_timestamp  DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    remarks           NVARCHAR(1000)  NULL
);
CREATE INDEX idx_audit_request ON audit_log(request_id, request_type);
GO

CREATE TABLE payments (
    payment_id      INT             IDENTITY(1,1) PRIMARY KEY,
    student_id      INT             NOT NULL,
    request_id      INT             NOT NULL,
    request_type    VARCHAR(50)     NOT NULL,
    method          VARCHAR(20)     NOT NULL CHECK (method IN ('CASH','CARD','BANK_TRANSFER','ONLINE')),
    amount          DECIMAL(10,2)   NOT NULL CHECK (amount > 0),
    status          VARCHAR(15)     DEFAULT 'PENDING' CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED')),
    paid_at         DATETIME2       NULL,
    receipt_number  VARCHAR(50)     NULL UNIQUE,
    created_at      DATETIME2       DEFAULT SYSDATETIME(),
    CONSTRAINT fk_payment_student FOREIGN KEY (student_id) REFERENCES students(student_id)
);
GO

CREATE TABLE notifications (
    notification_id INT             IDENTITY(1,1) PRIMARY KEY,
    recipient_id    INT             NOT NULL,
    recipient_type  VARCHAR(10)     NOT NULL CHECK (recipient_type IN ('STUDENT','ADMIN')),
    message         NVARCHAR(MAX)   NOT NULL,
    channel         VARCHAR(10)     DEFAULT 'IN_APP' CHECK (channel IN ('IN_APP','EMAIL','SMS')),
    request_id      INT             NULL,
    request_type    VARCHAR(50)     NULL,
    sent_at         DATETIME2       DEFAULT SYSDATETIME(),
    is_read         BIT             DEFAULT 0,
    CONSTRAINT fk_notif_user FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE
);
GO

INSERT INTO users (username, password_hash, role, full_name, email) VALUES
    ('admin',  'admin123',  'ADMIN',   'Academic Office Admin', 'admin@nu.edu.pk'),
    ('zahra',  'zahra123',  'STUDENT', 'Zahra Arshad',          'zahra@nu.edu.pk'),
    ('aizah',  'aizah123',  'STUDENT', 'Aizah Atif',            'aizah@nu.edu.pk'),
    ('rameen', 'rameen123', 'STUDENT', 'Rameen Fatima',         'rameen@nu.edu.pk');
GO

INSERT INTO students (student_id, roll_number, degree_program, semester, cgpa)
SELECT user_id, '24i-0630', 'BS CS', 4, 3.82 FROM users WHERE username = 'zahra'
UNION ALL
SELECT user_id, '24i-0840', 'BS CS', 4, 3.65 FROM users WHERE username = 'aizah'
UNION ALL
SELECT user_id, '24i-0782', 'BS CS', 4, 3.70 FROM users WHERE username = 'rameen';
GO

-- ---------------------------------------------------------------------
-- UC1-UC4 (academic requests)
-- ---------------------------------------------------------------------
CREATE TABLE attendance_requests (
    request_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    course_name         VARCHAR(100)    NOT NULL,
    semester            VARCHAR(20)     NOT NULL,
    class_date          DATE            NOT NULL,
    reason              NVARCHAR(MAX)   NOT NULL,
    supporting_doc_path VARCHAR(255)    NULL,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date     DATETIME2       DEFAULT SYSDATETIME(),
    admin_remarks       NVARCHAR(MAX)   NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE course_registration_issues (
    issue_id        INT             IDENTITY(1,1) PRIMARY KEY,
    student_id      INT             NOT NULL,
    course_code     VARCHAR(20)     NOT NULL,
    section         VARCHAR(10)     NULL,
    semester        VARCHAR(20)     NOT NULL,
    issue_type      VARCHAR(20)     NOT NULL
                    CHECK (issue_type IN ('NOT_REGISTERED','WRONG_SECTION','TIMETABLE_CLASH','PREREQ_BLOCKED','OTHER')),
    description     NVARCHAR(MAX)   NOT NULL,
    urgency         VARCHAR(10)     DEFAULT 'MEDIUM' CHECK (urgency IN ('LOW','MEDIUM','HIGH')),
    status          VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    admin_remarks   NVARCHAR(MAX)   NULL,
    created_at      DATETIME2       DEFAULT SYSDATETIME(),
    CONSTRAINT fk_course_issue_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE add_drop_requests (
    request_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    request_type        VARCHAR(10)     NOT NULL CHECK (request_type IN ('ADD','DROP','WITHDRAW')),
    course_code         VARCHAR(20)     NOT NULL,
    reason              NVARCHAR(MAX)   NOT NULL,
    supporting_doc_path VARCHAR(255)    NULL,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date     DATETIME2       DEFAULT SYSDATETIME(),
    processed_date      DATETIME2       NULL,
    admin_remarks       NVARCHAR(MAX)   NULL,
    CONSTRAINT fk_addrop_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE academic_record_corrections (
    correction_id       INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    record_type         VARCHAR(30)     NOT NULL
                        CHECK (record_type IN ('GRADE','CGPA','ATTENDANCE','TRANSCRIPT_DATA','PERSONAL_INFO','OTHER')),
    incorrect_value     VARCHAR(255)    NOT NULL,
    correct_value       VARCHAR(255)    NOT NULL,
    justification       NVARCHAR(MAX)   NOT NULL,
    evidence_path       VARCHAR(255)    NULL,
    priority            VARCHAR(10)     DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date     DATETIME2       DEFAULT SYSDATETIME(),
    admin_remarks       NVARCHAR(MAX)   NULL,
    CONSTRAINT fk_record_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

-- ---------------------------------------------------------------------
-- UC5-UC8 (support requests)
-- ---------------------------------------------------------------------
CREATE TABLE fee_support_requests (
    request_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    fee_type            VARCHAR(20)     NOT NULL
                        CHECK (fee_type IN ('TUITION','HOSTEL','EXAM','LIBRARY','OTHER')),
    outstanding_amount  DECIMAL(10,2)   NOT NULL CHECK (outstanding_amount > 0),
    request_type        VARCHAR(15)     NOT NULL
                        CHECK (request_type IN ('INSTALLMENT','EXTENSION','WAIVER','SCHOLARSHIP')),
    justification       NVARCHAR(MAX)   NOT NULL,
    doc_path            VARCHAR(255)    NULL,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    admin_remarks       NVARCHAR(MAX)   NULL,
    submission_date     DATETIME2       DEFAULT SYSDATETIME(),
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE lost_items (
    item_id      INT             IDENTITY(1,1) PRIMARY KEY,
    student_id   INT             NOT NULL,
    item_name    VARCHAR(100)    NOT NULL,
    category     VARCHAR(20)     NOT NULL
                 CHECK (category IN ('ELECTRONICS','STATIONERY','BOOKS','CLOTHING','KEYS','WALLET','BAG','OTHER')),
    location     VARCHAR(100)    NOT NULL,
    report_date  DATE            NOT NULL,
    description  NVARCHAR(MAX)   NOT NULL,
    contact      VARCHAR(50)     NOT NULL,
    status       VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED','MATCHED','CLOSED')),
    CONSTRAINT fk_lost_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE found_items (
    item_id      INT             IDENTITY(1,1) PRIMARY KEY,
    student_id   INT             NOT NULL,
    item_name    VARCHAR(100)    NOT NULL,
    category     VARCHAR(20)     NOT NULL
                 CHECK (category IN ('ELECTRONICS','CLOTHING','STATIONERY','BOOKS','KEYS','WALLET','BAG','OTHER')),
    location     VARCHAR(100)    NOT NULL,
    report_date  DATE            NOT NULL,
    description  NVARCHAR(MAX)   NOT NULL,
    contact      VARCHAR(50)     NOT NULL,
    status       VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED','MATCHED','CLOSED')),
    CONSTRAINT fk_found_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE scholarship_queries (
    query_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id        INT             NOT NULL,
    scholarship_type  VARCHAR(15)     NOT NULL
                      CHECK (scholarship_type IN ('MERIT','NEED_BASED','SPORTS','MINORITY','OTHER')),
    cgpa              DECIMAL(3,2)    NOT NULL CHECK (cgpa BETWEEN 0.00 AND 4.00),
    family_income     DECIMAL(12,2)   NOT NULL CHECK (family_income >= 0),
    query_text        NVARCHAR(MAX)   NOT NULL,
    doc_path          VARCHAR(255)    NULL,
    eligibility_flag  BIT             DEFAULT 0,
    status            VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    admin_remarks     NVARCHAR(MAX)   NULL,
    submission_date   DATETIME2       DEFAULT SYSDATETIME(),
    CONSTRAINT fk_scholarship_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
GO

CREATE TABLE general_complaints (
    complaint_id        INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NULL,
    category            VARCHAR(20)     NOT NULL
                        CHECK (category IN ('FACILITY','STAFF','HARASSMENT','ACADEMIC','HOSTEL','TRANSPORT','OTHER')),
    severity            VARCHAR(10)     NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    subject             VARCHAR(200)    NOT NULL,
    description         NVARCHAR(MAX)   NOT NULL,
    is_anonymous        BIT             DEFAULT 0,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED','ESCALATED')),
    submission_date     DATETIME2       DEFAULT SYSDATETIME(),
    resolution_remarks  NVARCHAR(MAX)   NULL,
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE SET NULL
);
GO

-- ---------------------------------------------------------------------
--  UC9-UC12 (document requests)
-- ---------------------------------------------------------------------
CREATE TABLE transcript_requests (
    request_id       INT             IDENTITY(1,1) PRIMARY KEY,
    student_id       INT             NOT NULL,
    transcript_type  VARCHAR(20)     NOT NULL
                     CHECK (transcript_type IN ('OFFICIAL_SEALED','UNOFFICIAL')),
    purpose          VARCHAR(20)     NOT NULL
                     CHECK (purpose IN ('JOB_APPLICATION','HIGHER_STUDIES','VISA','OTHER')),
    copies           INT             NOT NULL CHECK (copies BETWEEN 1 AND 10),
    delivery_mode    VARCHAR(10)     NOT NULL CHECK (delivery_mode IN ('PICKUP','POSTAL')),
    mailing_address  NVARCHAR(MAX)   NULL,
    status           VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date  DATETIME2       NULL DEFAULT SYSDATETIME(),
    expected_date    DATE            NULL,
    admin_remarks    NVARCHAR(MAX)   NULL,
    is_collected     INT             NULL DEFAULT 0,
    CONSTRAINT fk_transcript_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX idx_transcript_student ON transcript_requests(student_id, status);
GO

CREATE TABLE id_card_requests (
    request_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    request_type        VARCHAR(15)     NOT NULL
                        CHECK (request_type IN ('NEW_ISSUANCE','REPLACEMENT','DUPLICATE')),
    replacement_reason  VARCHAR(15)     NULL,
    delivery_address    NVARCHAR(500)   NOT NULL,
    photo_path          VARCHAR(255)    NULL,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date     DATETIME2       NULL DEFAULT SYSDATETIME(),
    dispatch_date       DATETIME2       NULL,
    admin_remarks       NVARCHAR(MAX)   NULL,
    is_collected        INT             NULL DEFAULT 0,
    delivery_mode       VARCHAR(50)     NULL DEFAULT 'Pickup',
    CONSTRAINT fk_idcard_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX idx_idcard_student ON id_card_requests(student_id, status);
GO

CREATE TABLE enrollment_letter_requests (
    request_id      INT             IDENTITY(1,1) PRIMARY KEY,
    student_id      INT             NOT NULL,
    letter_type     VARCHAR(30)     NOT NULL
                    CHECK (letter_type IN ('ENROLLMENT_CONFIRMATION','STUDENT_STATUS','BONAFIDE','BANK_LETTER','VISA_SUPPORT')),
    addressed_to    VARCHAR(150)    NOT NULL,
    purpose         NVARCHAR(500)   NOT NULL,
    language        VARCHAR(10)     NULL DEFAULT 'ENGLISH' CHECK (language IN ('ENGLISH','URDU')),
    copies          INT             NOT NULL CHECK (copies BETWEEN 1 AND 10),
    status          VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    submission_date DATETIME2       NULL DEFAULT SYSDATETIME(),
    ready_date      DATETIME2       NULL,
    collected_flag  BIT             NULL DEFAULT 0,
    admin_remarks   NVARCHAR(MAX)   NULL,
    is_collected    INT             NULL DEFAULT 0,
    delivery_mode   VARCHAR(50)     NULL DEFAULT 'Pickup',
    CONSTRAINT fk_enroll_letter_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX idx_enroll_letter_ready ON enrollment_letter_requests(status, collected_flag);
GO

CREATE TABLE degree_verification_requests (
    request_id          INT             IDENTITY(1,1) PRIMARY KEY,
    student_id          INT             NOT NULL,
    graduation_year     INT             NOT NULL CHECK (graduation_year BETWEEN 1985 AND 2099),
    degree_program      VARCHAR(50)     NOT NULL,
    verification_type   VARCHAR(15)     NOT NULL
                        CHECK (verification_type IN ('HEC','EMBASSY','EMPLOYER','IBCC')),
    purpose             NVARCHAR(500)   NOT NULL,
    copies              INT             NOT NULL CHECK (copies BETWEEN 1 AND 10),
    is_urgent           BIT             NULL DEFAULT 0,
    status              VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','IN_REVIEW','RESOLVED','REJECTED')),
    processing_deadline DATE            NULL,
    submission_date     DATETIME2       NULL DEFAULT SYSDATETIME(),
    admin_remarks       NVARCHAR(MAX)   NULL,
    is_collected        INT             NULL DEFAULT 0,
    delivery_mode       VARCHAR(50)     NULL DEFAULT 'Pickup',
    CONSTRAINT fk_degree_verif_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX idx_degree_verif_urgent ON degree_verification_requests(is_urgent, status);
GO

PRINT '';
PRINT '====================================================================';
PRINT 'OneDesk database setup complete.';
PRINT '====================================================================';
PRINT '';
PRINT 'Demo logins:';
PRINT '  admin   / admin123     (admin)';
PRINT '  zahra   / zahra123     (UC1-UC4 — academic requests)';
PRINT '  aizah   / aizah123     (UC5-UC8 — support requests)';
PRINT '  rameen  / rameen123    (UC9-UC12 — document requests)';
PRINT '';

SELECT 'users'        AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'students',     COUNT(*) FROM students
UNION ALL SELECT 'audit_log',    COUNT(*) FROM audit_log
UNION ALL SELECT 'attendance_requests',          COUNT(*) FROM attendance_requests
UNION ALL SELECT 'course_registration_issues',   COUNT(*) FROM course_registration_issues
UNION ALL SELECT 'add_drop_requests',            COUNT(*) FROM add_drop_requests
UNION ALL SELECT 'academic_record_corrections',  COUNT(*) FROM academic_record_corrections
UNION ALL SELECT 'fee_support_requests',         COUNT(*) FROM fee_support_requests
UNION ALL SELECT 'lost_items',                   COUNT(*) FROM lost_items
UNION ALL SELECT 'found_items',                  COUNT(*) FROM found_items
UNION ALL SELECT 'scholarship_queries',          COUNT(*) FROM scholarship_queries
UNION ALL SELECT 'general_complaints',           COUNT(*) FROM general_complaints
UNION ALL SELECT 'transcript_requests',          COUNT(*) FROM transcript_requests
UNION ALL SELECT 'id_card_requests',             COUNT(*) FROM id_card_requests
UNION ALL SELECT 'enrollment_letter_requests',   COUNT(*) FROM enrollment_letter_requests
UNION ALL SELECT 'degree_verification_requests', COUNT(*) FROM degree_verification_requests;
GO