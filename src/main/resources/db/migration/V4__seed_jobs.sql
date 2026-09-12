-- ============================================================
-- CareerMatch Database - Seed Jobs
-- Version: V4
-- Description: Inserts sample jobs for development/demo
-- ============================================================

INSERT INTO jobs (
    id, external_id, title, company, location, description,
    source, required_skills_json, preferred_skills_json,
    expected_experience_months, salary_min, salary_max, currency,
    employment_type, remote_type, url, posted_date
) VALUES

-- ============================================================
-- Backend Jobs
-- ============================================================
(
    'job_1', 'adzuna_1001',
    'Senior Backend Developer', 'TechCorp Inc.', 'Remote',
    'We are looking for a Senior Backend Developer with experience in Java, Spring Boot, and microservices architecture. You will be responsible for designing and implementing scalable backend services, working with PostgreSQL, and deploying to AWS. Experience with Docker and Kubernetes is essential.',
    'adzuna',
    '["Java", "Spring Boot", "Microservices", "PostgreSQL", "Docker"]',
    '["AWS", "Kubernetes", "Redis", "Kafka"]',
    48, 80000, 120000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1001', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'job_2', 'adzuna_1002',
    'Backend Developer', 'StartupXYZ', 'Cairo, Egypt',
    'Join our fast-growing startup as a Backend Developer. You will build REST APIs using Python and FastAPI, work with PostgreSQL, and help us scale our platform. Experience with Docker and CI/CD is a plus.',
    'adzuna',
    '["Python", "FastAPI", "PostgreSQL", "REST APIs"]',
    '["Docker", "CI/CD", "AWS", "Redis"]',
    24, 30000, 50000, 'USD', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1002', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'job_3', 'adzuna_1003',
    'Java Backend Engineer', 'FinTech Solutions', 'London, UK',
    'We are seeking a Java Backend Engineer to work on our financial platform. Required skills: Java 17, Spring Boot, Spring Security, PostgreSQL, and microservices. You will work in an agile team and contribute to architectural decisions.',
    'adzuna',
    '["Java", "Spring Boot", "Spring Security", "PostgreSQL", "Microservices"]',
    '["Kafka", "Redis", "AWS", "Docker", "Kubernetes"]',
    36, 70000, 100000, 'GBP', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1003', CURRENT_TIMESTAMP - INTERVAL '3 days'
),

-- ============================================================
-- Frontend Jobs
-- ============================================================
(
    'job_4', 'adzuna_1004',
    'Frontend Developer', 'WebSolutions', 'New York, NY',
    'We are looking for a Frontend Developer with strong Angular skills. You will build responsive web applications, work closely with designers, and ensure high performance. Experience with TypeScript, RxJS, and SCSS is required.',
    'adzuna',
    '["Angular", "TypeScript", "SCSS", "RxJS"]',
    '["React", "Vue.js", "TailwindCSS", "Jest"]',
    24, 70000, 95000, 'USD', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1004', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'job_5', 'adzuna_1005',
    'React Developer', 'Digital Agency', 'Remote',
    'Join our team as a React Developer. You will build modern web applications using React, Redux, and TypeScript. Experience with Next.js and Tailwind CSS is a plus.',
    'adzuna',
    '["React", "Redux", "TypeScript", "JavaScript"]',
    '["Next.js", "Tailwind CSS", "GraphQL", "Jest"]',
    18, 60000, 85000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1005', CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'job_6', 'adzuna_1006',
    'Vue.js Developer', 'EuroTech', 'Berlin, Germany',
    'We are hiring a Vue.js Developer to work on our SaaS platform. Required: Vue.js, Vuex, TypeScript, and REST APIs. Experience with Nuxt.js is a plus.',
    'adzuna',
    '["Vue.js", "TypeScript", "JavaScript", "REST APIs"]',
    '["Nuxt.js", "Vuex", "Tailwind CSS", "Jest"]',
    24, 55000, 75000, 'EUR', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1006', CURRENT_TIMESTAMP - INTERVAL '2 days'
),

-- ============================================================
-- Full Stack Jobs
-- ============================================================
(
    'job_7', 'adzuna_1007',
    'Full Stack Developer', 'Innovation Labs', 'Remote',
    'We are looking for a Full Stack Developer proficient in both frontend and backend. Required: React, Node.js, Express, PostgreSQL, and AWS. You will work on end-to-end features and contribute to our microservices architecture.',
    'adzuna',
    '["React", "Node.js", "Express.js", "PostgreSQL", "AWS"]',
    '["TypeScript", "Docker", "GraphQL", "Redis"]',
    36, 90000, 130000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1007', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'job_8', 'adzuna_1008',
    'Full Stack Engineer', 'CloudSystems', 'Dubai, UAE',
    'Join our cloud team as a Full Stack Engineer. You will work with Angular, Spring Boot, and PostgreSQL. Experience with Docker and Kubernetes is required.',
    'adzuna',
    '["Angular", "Spring Boot", "PostgreSQL", "Docker"]',
    '["Kubernetes", "AWS", "Redis", "Kafka"]',
    30, 80000, 110000, 'AED', 'FULL_TIME', 'ONSITE',
    'https://example.com/jobs/1008', CURRENT_TIMESTAMP - INTERVAL '5 days'
),

-- ============================================================
-- DevOps Jobs
-- ============================================================
(
    'job_9', 'adzuna_1009',
    'DevOps Engineer', 'CloudNative', 'London, UK',
    'We are seeking a DevOps Engineer to manage our cloud infrastructure. Experience with AWS, Docker, Kubernetes, and CI/CD pipelines is essential. Terraform experience is a strong plus.',
    'adzuna',
    '["AWS", "Docker", "Kubernetes", "CI/CD", "Terraform"]',
    '["Python", "Go", "Prometheus", "Grafana"]',
    36, 75000, 105000, 'GBP', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1009', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'job_10', 'adzuna_1010',
    'Site Reliability Engineer', 'ScaleUp', 'Remote',
    'We are looking for an SRE to ensure our platform is reliable and scalable. Required: Kubernetes, Docker, Terraform, AWS, and strong Linux skills. Experience with Prometheus and Grafana is a plus.',
    'adzuna',
    '["Kubernetes", "Docker", "Terraform", "AWS", "Linux"]',
    '["Prometheus", "Grafana", "Python", "Go", "CI/CD"]',
    48, 100000, 140000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1010', CURRENT_TIMESTAMP - INTERVAL '3 days'
),

-- ============================================================
-- Data & AI Jobs
-- ============================================================
(
    'job_11', 'adzuna_1011',
    'Data Scientist', 'AI Innovations', 'San Francisco, CA',
    'We are hiring a Data Scientist to work on cutting-edge AI projects. Required: Python, Machine Learning, TensorFlow, Pandas, and NumPy. Experience with NLP is a strong plus.',
    'adzuna',
    '["Python", "Machine Learning", "TensorFlow", "Pandas", "NumPy"]',
    '["PyTorch", "NLP", "Scikit-learn", "AWS"]',
    36, 110000, 160000, 'USD', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1011', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'job_12', 'adzuna_1012',
    'Machine Learning Engineer', 'DeepTech', 'Remote',
    'Join our ML team as a Machine Learning Engineer. You will build and deploy ML models at scale. Required: Python, PyTorch, TensorFlow, Docker, and AWS.',
    'adzuna',
    '["Python", "PyTorch", "TensorFlow", "Docker", "AWS"]',
    '["Kubernetes", "MLflow", "Airflow", "SQL"]',
    42, 120000, 170000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1012', CURRENT_TIMESTAMP - INTERVAL '4 days'
),

-- ============================================================
-- Entry-Level Jobs
-- ============================================================
(
    'job_13', 'adzuna_1013',
    'Junior Software Developer', 'TechStart', 'Cairo, Egypt',
    'Great opportunity for a junior developer to start their career. We are looking for someone with knowledge of JavaScript, HTML, CSS, and basic React. You will receive mentorship and training.',
    'adzuna',
    '["JavaScript", "HTML", "CSS", "React"]',
    '["TypeScript", "Git", "REST APIs", "Node.js"]',
    6, 15000, 25000, 'USD', 'FULL_TIME', 'ONSITE',
    'https://example.com/jobs/1013', CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'job_14', 'adzuna_1014',
    'Graduate Backend Developer', 'BigCorp', 'Remote',
    'We are looking for recent graduates to join our backend team. Required: basic knowledge of Java or Python, understanding of databases, and willingness to learn.',
    'adzuna',
    '["Java", "Python", "SQL", "Git"]',
    '["Spring Boot", "Django", "Docker", "REST APIs"]',
    0, 40000, 55000, 'USD', 'FULL_TIME', 'REMOTE',
    'https://example.com/jobs/1014', CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'job_15', 'adzuna_1015',
    'Junior Frontend Developer', 'WebAgency', 'Alexandria, Egypt',
    'We are seeking a Junior Frontend Developer to join our team. Required: HTML, CSS, JavaScript, and React basics. Experience with Git and responsive design is a plus.',
    'adzuna',
    '["JavaScript", "HTML", "CSS", "React"]',
    '["TypeScript", "Tailwind CSS", "Git", "Figma"]',
    12, 12000, 20000, 'USD', 'FULL_TIME', 'HYBRID',
    'https://example.com/jobs/1015', CURRENT_TIMESTAMP - INTERVAL '3 days'
)

    ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Verify seed data
-- ============================================================
-- SELECT COUNT(*) FROM jobs;
-- SELECT COUNT(*) FROM jobs;
-- SELECT COUNT(*) FROM users;
-- SELECT COUNT(*) FROM skills;