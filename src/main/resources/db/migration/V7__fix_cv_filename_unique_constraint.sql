-- ============================================================
-- Fix CV filename uniqueness for soft-deleted CVs
-- ============================================================
--
-- Rule:
--   The same user cannot have two ACTIVE CVs
--   with the same filename.
--
--   Different users CAN have the same filename.
--
--   A soft-deleted CV does NOT block re-uploading
--   the same filename by the same user.
-- ============================================================

ALTER TABLE cvs
DROP CONSTRAINT IF EXISTS uk_user_cv_filename;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_cv_filename_active
    ON cvs (user_id, filename)
    WHERE deleted = false;