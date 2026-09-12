-- ============================================================
-- CareerMatch Database - Initial Schema
-- Version: V1
-- Description: Creates all core tables with enhanced integrity constraints
-- ============================================================

-- ============================================================
-- Drop existing tables (in correct order for FK dependencies)
-- ============================================================
DROP TABLE IF EXISTS improvement_recommendations CASCADE;
DROP TABLE IF EXISTS match_results CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS cvs CASCADE;
DROP TABLE IF EXISTS jobs CASCADE;
DROP TABLE IF EXISTS skills CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       role VARCHAR(50) NOT NULL DEFAULT 'USER',
                       preferences TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(36),
                       last_modified_by VARCHAR(36),
                       version BIGINT NOT NULL DEFAULT 0,
                       deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       deleted_at TIMESTAMP
);

-- ============================================================
-- REFRESH TOKENS TABLE
-- ============================================================
CREATE TABLE refresh_tokens (
                                id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                user_id VARCHAR(36) NOT NULL,
                                token VARCHAR(500) UNIQUE NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                revoked_at TIMESTAMP,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                version BIGINT NOT NULL DEFAULT 0,
                                deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                deleted_at TIMESTAMP,
                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- CVS TABLE
-- ============================================================
CREATE TABLE cvs (
                     id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                     user_id VARCHAR(36) NOT NULL,
                     filename VARCHAR(255) NOT NULL,
                     file_url VARCHAR(500) NOT NULL,
                     file_size BIGINT NOT NULL,
                     file_type VARCHAR(100) NOT NULL,
                     raw_text TEXT,
                     skills_json TEXT,
                     profile_json TEXT,
                     parsed BOOLEAN NOT NULL DEFAULT FALSE,
                     extraction_confidence VARCHAR(50),
                     uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     parsed_at TIMESTAMP,
                     created_by VARCHAR(36),
                     last_modified_by VARCHAR(36),
                     version BIGINT NOT NULL DEFAULT 0,
                     deleted BOOLEAN NOT NULL DEFAULT FALSE,
                     deleted_at TIMESTAMP,
                     CONSTRAINT fk_cvs_user
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                     CONSTRAINT uk_user_cv_filename UNIQUE (user_id, filename)
);

-- ============================================================
-- JOBS TABLE
-- ============================================================
CREATE TABLE jobs (
                      id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                      external_id VARCHAR(255),
                      title VARCHAR(500) NOT NULL,
                      company VARCHAR(255) NOT NULL,
                      location VARCHAR(255),
                      description TEXT NOT NULL,
                      source VARCHAR(100) DEFAULT 'adzuna',
                      required_skills_json TEXT,
                      preferred_skills_json TEXT,
                      expected_experience_months INTEGER,
                      salary_min DECIMAL(15,2),
                      salary_max DECIMAL(15,2),
                      currency VARCHAR(10) DEFAULT 'USD',
                      employment_type VARCHAR(50),
                      remote_type VARCHAR(50),
                      url VARCHAR(500),
                      posted_date TIMESTAMP,
                      last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      created_by VARCHAR(36),
                      last_modified_by VARCHAR(36),
                      version BIGINT NOT NULL DEFAULT 0,
                      deleted BOOLEAN NOT NULL DEFAULT FALSE,
                      deleted_at TIMESTAMP,

                      CONSTRAINT chk_salary_range CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

-- ============================================================
-- SKILLS TABLE (for normalization)
-- ============================================================
CREATE TABLE skills (
                        id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                        canonical_name VARCHAR(255) UNIQUE NOT NULL,
                        aliases TEXT,
                        category VARCHAR(100),
                        proficiency_level VARCHAR(50),
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        version BIGINT NOT NULL DEFAULT 0
);

-- ============================================================
-- MATCH RESULTS TABLE
-- ============================================================
CREATE TABLE match_results (
                               id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                               user_id VARCHAR(36) NOT NULL,
                               cv_id VARCHAR(36) NOT NULL,
                               job_id VARCHAR(36) NOT NULL,
                               overall_match_score INTEGER NOT NULL,
                               score_breakdown_json TEXT,
                               matched_skills_json TEXT,
                               missing_skills_json TEXT,
                               strengths_json TEXT,
                               improvement_plan_json TEXT,
                               evidence_json TEXT,
                               extraction_confidence VARCHAR(50) NOT NULL,
                               human_review_flag BOOLEAN NOT NULL DEFAULT FALSE,
                               job_title VARCHAR(500),
                               company_name VARCHAR(255),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               created_by VARCHAR(36),
                               last_modified_by VARCHAR(36),
                               version BIGINT NOT NULL DEFAULT 0,
                               deleted BOOLEAN NOT NULL DEFAULT FALSE,
                               deleted_at TIMESTAMP,
                               CONSTRAINT fk_match_results_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_match_results_cv
                                   FOREIGN KEY (cv_id) REFERENCES cvs(id) ON DELETE CASCADE,
                               CONSTRAINT fk_match_results_job
                                   FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                               CONSTRAINT chk_match_score_range
                                   CHECK (overall_match_score >= 0 AND overall_match_score <= 100)
);

-- ============================================================
-- IMPROVEMENT RECOMMENDATIONS TABLE
-- ============================================================
CREATE TABLE improvement_recommendations (
                                             id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                             match_result_id VARCHAR(36) NOT NULL,
                                             gap_name VARCHAR(255) NOT NULL,
                                             gap_category VARCHAR(100) NOT NULL,
                                             required_or_preferred VARCHAR(20) NOT NULL,
                                             importance_weight INTEGER NOT NULL,
                                             job_evidence TEXT,
                                             cv_evidence TEXT,
                                             related_existing_strengths TEXT,
                                             recommended_action TEXT NOT NULL,
                                             deliverable TEXT,
                                             estimated_effort VARCHAR(100) NOT NULL,
                                             expected_score_gain INTEGER NOT NULL,
                                             priority_score INTEGER NOT NULL,
                                             priority_label VARCHAR(50) NOT NULL,
                                             confidence VARCHAR(50) NOT NULL,
                                             resources_json TEXT,
                                             status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             completed_at TIMESTAMP,
                                             version BIGINT NOT NULL DEFAULT 0,
                                             deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                             deleted_at TIMESTAMP,
                                             CONSTRAINT fk_recommendations_match_result
                                                 FOREIGN KEY (match_result_id) REFERENCES match_results(id) ON DELETE CASCADE,
                                             CONSTRAINT chk_importance_weight
                                                 CHECK (importance_weight >= 1 AND importance_weight <= 10),
                                             CONSTRAINT chk_expected_score_gain
                                                 CHECK (expected_score_gain >= 0 AND expected_score_gain <= 100),
                                             CONSTRAINT chk_priority_score
                                                 CHECK (priority_score >= 0)
);

-- ============================================================
-- INDEXES
-- ============================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_deleted ON users(deleted);

-- Refresh tokens indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
CREATE INDEX idx_refresh_tokens_deleted ON refresh_tokens(deleted);

-- CVs indexes
CREATE INDEX idx_cvs_user_id ON cvs(user_id);
CREATE INDEX idx_cvs_parsed ON cvs(parsed);
CREATE INDEX idx_cvs_uploaded_at ON cvs(uploaded_at DESC);
CREATE INDEX idx_cvs_deleted ON cvs(deleted);

-- Jobs indexes
CREATE INDEX idx_jobs_external_id ON jobs(external_id);
CREATE INDEX idx_jobs_title ON jobs(title);
CREATE INDEX idx_jobs_company ON jobs(company);
CREATE INDEX idx_jobs_location ON jobs(location);
CREATE INDEX idx_jobs_source ON jobs(source);
CREATE INDEX idx_jobs_posted_date ON jobs(posted_date DESC);
CREATE INDEX idx_jobs_last_sync_at ON jobs(last_sync_at);
CREATE INDEX idx_jobs_deleted ON jobs(deleted);

-- Skills indexes
CREATE INDEX idx_skills_canonical_name ON skills(canonical_name);
CREATE INDEX idx_skills_category ON skills(category);
CREATE INDEX idx_skills_is_active ON skills(is_active);

-- Match results indexes
CREATE INDEX idx_match_results_user_id ON match_results(user_id);
CREATE INDEX idx_match_results_cv_id ON match_results(cv_id);
CREATE INDEX idx_match_results_job_id ON match_results(job_id);
CREATE INDEX idx_match_results_created_at ON match_results(created_at DESC);
CREATE INDEX idx_match_results_score ON match_results(overall_match_score DESC);
CREATE INDEX idx_match_results_deleted ON match_results(deleted);

-- Recommendations indexes
CREATE INDEX idx_recommendations_match_id ON improvement_recommendations(match_result_id);
CREATE INDEX idx_recommendations_priority ON improvement_recommendations(priority_score DESC);
CREATE INDEX idx_recommendations_status ON improvement_recommendations(status);
CREATE INDEX idx_recommendations_deleted ON improvement_recommendations(deleted);

-- ============================================================
-- COMMENTS
-- ============================================================
COMMENT ON TABLE users IS 'Application users (candidates)';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens for authentication';
COMMENT ON TABLE cvs IS 'Uploaded CV files with extracted text and parsed profile';
COMMENT ON TABLE jobs IS 'Job postings from Adzuna or local sources';
COMMENT ON TABLE skills IS 'Canonical skills for normalization';
COMMENT ON TABLE match_results IS 'AI-generated match results between CV and Job';
COMMENT ON TABLE improvement_recommendations IS 'Actionable recommendations for skill gaps';