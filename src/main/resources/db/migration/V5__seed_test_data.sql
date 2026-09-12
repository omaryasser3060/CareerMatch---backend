-- ============================================================
-- CareerMatch Database - Seed Test Data (CVs + Match Results)
-- Version: V5
-- Description: Inserts sample CVs and match results for testing
-- ============================================================

-- ============================================================
-- Sample CVs for test user
-- ============================================================
INSERT INTO cvs (
    id, user_id, filename, file_url, file_size, file_type,
    raw_text, skills_json, profile_json, parsed, extraction_confidence
) VALUES
      (
          'cv_test_1',
          'user_test_1',
          'john_doe_backend_cv.pdf',
          './uploads/cvs/user_test_1/sample1.pdf',
          245760,
          'application/pdf',
          'John Doe - Backend Developer
          Email: john.doe@example.com
          Phone: +20 123 456 7890

          EXPERIENCE:
          Backend Developer Intern at TechCorp (2023 - Present)
          - Built REST APIs using Python and FastAPI
          - Worked with PostgreSQL for data storage
          - Used Docker for containerization
          - Implemented CI/CD pipelines

          SKILLS:
          Python, FastAPI, PostgreSQL, Docker, Git, REST APIs, Linux

          PROJECTS:
          - E-commerce API: Built with FastAPI and PostgreSQL
          - Task Manager: Python CLI tool with SQLite

          EDUCATION:
          Bachelor of Computer Science, Cairo University, 2023
          GPA: 3.5/4.0',
          '["Python", "FastAPI", "PostgreSQL", "Docker", "Git", "REST APIs", "Linux"]',
          '{"skills": ["Python", "FastAPI", "PostgreSQL", "Docker", "Git", "REST APIs", "Linux"], "experience_months": 12, "experience_summary": "1 year backend internship experience", "past_titles": ["Backend Developer Intern"], "confidence": "HIGH"}',
          TRUE,
          'HIGH'
      ),
      (
          'cv_test_2',
          'user_test_1',
          'john_doe_frontend_cv.pdf',
          './uploads/cvs/user_test_1/sample2.pdf',
          198656,
          'application/pdf',
          'John Doe - Frontend Developer
          Email: john.doe@example.com

          EXPERIENCE:
          Frontend Developer Freelance (2023 - Present)
          - Built responsive websites using React
          - Worked with TypeScript and Tailwind CSS
          - Integrated REST APIs

          SKILLS:
          JavaScript, TypeScript, React, HTML, CSS, Tailwind CSS, Git

          PROJECTS:
          - Portfolio Website: React + TypeScript
          - Weather App: React + OpenWeather API

          EDUCATION:
          Bachelor of Computer Science, Cairo University, 2023',
          '["JavaScript", "TypeScript", "React", "HTML", "CSS", "Tailwind CSS", "Git"]',
          '{"skills": ["JavaScript", "TypeScript", "React", "HTML", "CSS", "Tailwind CSS", "Git"], "experience_months": 8, "experience_summary": "Freelance frontend development", "past_titles": ["Frontend Developer"], "confidence": "HIGH"}',
          TRUE,
          'HIGH'
      )
    ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Sample Match Results
-- ============================================================
INSERT INTO match_results (
    id, user_id, cv_id, job_id,
    overall_match_score, score_breakdown_json,
    matched_skills_json, missing_skills_json,
    strengths_json, improvement_plan_json, evidence_json,
    extraction_confidence, human_review_flag,
    job_title, company_name
) VALUES
      (
          'match_test_1',
          'user_test_1',
          'cv_test_1',
          'job_2',
          82,
          '{"requiredSkills": 75, "preferredSkills": 50, "semanticSimilarityAvg": 0.88}',
          '["Python", "FastAPI", "PostgreSQL", "REST APIs"]',
          '["Docker", "AWS"]',
          '["Strong backend/API development experience", "Good database knowledge"]',
          '{"recommendations": [{"gap_name": "Docker", "recommended_action": "Build a small project using Docker", "priority": "HIGH"}]}',
          '{"Python": {"cvEvidence": "Built REST APIs using Python and FastAPI", "jobEvidence": "Required Python"}}',
          'HIGH',
          FALSE,
          'Backend Developer',
          'StartupXYZ'
      ),
      (
          'match_test_2',
          'user_test_1',
          'cv_test_2',
          'job_5',
          68,
          '{"requiredSkills": 50, "preferredSkills": 25, "semanticSimilarityAvg": 0.75}',
          '["React", "TypeScript", "JavaScript"]',
          '["Redux", "Next.js"]',
          '["Strong React skills", "Good TypeScript knowledge"]',
          '{"recommendations": [{"gap_name": "Redux", "recommended_action": "Learn Redux by building a state-heavy app", "priority": "HIGH"}]}',
          '{"React": {"cvEvidence": "Built responsive websites using React", "jobEvidence": "Required React"}}',
          'HIGH',
          FALSE,
          'React Developer',
          'Digital Agency'
      )
    ON CONFLICT (id) DO NOTHING;