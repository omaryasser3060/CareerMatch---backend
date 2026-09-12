ALTER TABLE jobs
    ADD COLUMN IF NOT EXISTS requirements_json TEXT;