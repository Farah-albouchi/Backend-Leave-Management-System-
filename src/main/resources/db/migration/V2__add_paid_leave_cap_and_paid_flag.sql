-- Add paid leave cap days to users table
ALTER TABLE users ADD COLUMN paid_leave_cap_days INTEGER NULL;

-- Add is_paid flag to leave_requests table  
ALTER TABLE leave_requests ADD COLUMN is_paid BOOLEAN NOT NULL DEFAULT true;

-- Update existing leave requests to be paid by default
UPDATE leave_requests SET is_paid = true WHERE is_paid IS NULL;

-- Add comment explaining the cap column
COMMENT ON COLUMN users.paid_leave_cap_days IS 'Maximum paid leave days per year. NULL uses system default (30 days)';
COMMENT ON COLUMN leave_requests.is_paid IS 'Whether this leave request counts towards paid leave quota';


