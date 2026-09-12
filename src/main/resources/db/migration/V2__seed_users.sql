-- ============================================================
-- CareerMatch Database - Seed Users
-- Version: V2
-- Description: Inserts test users for development/demo
-- ============================================================

-- Password for all test users: "Password123!"
-- BCrypt hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiLZ8Qq5XQ5K

INSERT INTO users (id, email, name, password_hash, onboarding_completed, email_verified, role, preferences)
VALUES
    (
        'user_test_1',
        'test@careermatch.com',
        'Test User',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiLZ8Qq5XQ5K',
        TRUE,
        TRUE,
        'USER',
        '{"defaultLocation": "Cairo, Egypt", "jobAlerts": true, "jobAlertFrequency": "DAILY", "language": "en", "theme": "SYSTEM"}'
    ),
    (
        'user_premium_1',
        'premium@careermatch.com',
        'Premium User',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiLZ8Qq5XQ5K',
        TRUE,
        TRUE,
        'PREMIUM',
        '{"defaultLocation": "Remote", "jobAlerts": true, "jobAlertFrequency": "DAILY", "language": "en", "theme": "DARK"}'
    ),
    (
        'user_admin_1',
        'admin@careermatch.com',
        'Admin User',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiLZ8Qq5XQ5K',
        TRUE,
        TRUE,
        'ADMIN',
        '{"language": "en", "theme": "LIGHT"}'
    )
    ON CONFLICT (email) DO NOTHING;