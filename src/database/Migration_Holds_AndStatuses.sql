USE OneDesk;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.students') AND name = 'financial_hold'
)
BEGIN
    ALTER TABLE dbo.students ADD financial_hold BIT NOT NULL DEFAULT 0;
    PRINT 'Added students.financial_hold';
END
ELSE PRINT 'students.financial_hold already exists — skipped';
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.students') AND name = 'disciplinary_hold'
)
BEGIN
    ALTER TABLE dbo.students ADD disciplinary_hold BIT NOT NULL DEFAULT 0;
    PRINT 'Added students.disciplinary_hold';
END
ELSE PRINT 'students.disciplinary_hold already exists — skipped';
GO

UPDATE dbo.students SET enrollment_status = 'ACTIVE'    WHERE student_id IN (SELECT user_id FROM users WHERE username = 'zahra');
UPDATE dbo.students SET enrollment_status = 'GRADUATED' WHERE student_id IN (SELECT user_id FROM users WHERE username = 'rameen');
UPDATE dbo.students SET enrollment_status = 'ACTIVE'    WHERE student_id IN (SELECT user_id FROM users WHERE username = 'aizah');


PRINT '';
PRINT 'Migration complete. Test scenarios:';
PRINT '  zahra  (ACTIVE)    — can submit any of the 12 use cases';
PRINT '  aizah  (ACTIVE)    — can submit any of the 12 use cases';
PRINT '  rameen (GRADUATED) — can ONLY submit UC9-UC12 (Certification & Documentation)';
PRINT '                       Standard academic/support requests will be blocked with a';
PRINT '                       clear "Not Eligible" message.';
PRINT '';
PRINT 'To test holds, uncomment the UPDATE rows above and re-run.';
GO
