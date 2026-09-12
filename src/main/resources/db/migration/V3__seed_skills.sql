-- ============================================================
-- CareerMatch Database - Seed Skills
-- Version: V3
-- Description: Inserts canonical skills for normalization
-- ============================================================

INSERT INTO skills (id, canonical_name, aliases, category) VALUES
-- ============================================================
-- Programming Languages
-- ============================================================
('skill_1', 'JavaScript', '["JS", "Javascript", "ECMAScript", "ES6", "ES2015"]', 'LANGUAGE'),
('skill_2', 'TypeScript', '["TS", "Typescript"]', 'LANGUAGE'),
('skill_3', 'Python', '["Python3", "py", "python3"]', 'LANGUAGE'),
('skill_4', 'Java', '["Java8", "Java11", "Java17", "Java21"]', 'LANGUAGE'),
('skill_5', 'C#', '["CSharp", "C Sharp", "DotNet", "C#.NET"]', 'LANGUAGE'),
('skill_6', 'C++', '["CPP", "C Plus Plus", "cpp"]', 'LANGUAGE'),
('skill_7', 'C', '["c"]', 'LANGUAGE'),
('skill_8', 'Go', '["Golang", "go"]', 'LANGUAGE'),
('skill_9', 'Rust', '["Rustlang", "rust"]', 'LANGUAGE'),
('skill_10', 'PHP', '["php", "PHP7", "PHP8"]', 'LANGUAGE'),
('skill_11', 'Ruby', '["ruby", "Ruby on Rails", "Rails"]', 'LANGUAGE'),
('skill_12', 'Kotlin', '["kotlin"]', 'LANGUAGE'),
('skill_13', 'Swift', '["swift", "SwiftUI"]', 'LANGUAGE'),
('skill_14', 'Scala', '["scala"]', 'LANGUAGE'),
('skill_15', 'R', '["r", "Rlang"]', 'LANGUAGE'),
('skill_16', 'MATLAB', '["matlab"]', 'LANGUAGE'),
('skill_17', 'Perl', '["perl"]', 'LANGUAGE'),
('skill_18', 'Dart', '["dart"]', 'LANGUAGE'),
('skill_19', 'Elixir', '["elixir"]', 'LANGUAGE'),
('skill_20', 'Haskell', '["haskell"]', 'LANGUAGE'),

-- ============================================================
-- Frontend Frameworks & Libraries
-- ============================================================
('skill_30', 'React', '["React.js", "ReactJS", "react"]', 'FRAMEWORK'),
('skill_31', 'Angular', '["Angular.js", "AngularJS", "Angular 2+", "angular"]', 'FRAMEWORK'),
('skill_32', 'Vue.js', '["Vue", "VueJS", "Vue.js", "vue"]', 'FRAMEWORK'),
('skill_33', 'Next.js', '["NextJS", "Next", "nextjs"]', 'FRAMEWORK'),
('skill_34', 'Svelte', '["SvelteJS", "svelte"]', 'FRAMEWORK'),
('skill_35', 'Nuxt.js', '["Nuxt", "NuxtJS"]', 'FRAMEWORK'),
('skill_36', 'jQuery', '["jquery"]', 'FRAMEWORK'),
('skill_37', 'Redux', '["redux"]', 'FRAMEWORK'),
('skill_38', 'MobX', '["mobx"]', 'FRAMEWORK'),
('skill_39', 'Tailwind CSS', '["Tailwind", "TailwindCSS", "tailwind"]', 'FRAMEWORK'),
('skill_40', 'Bootstrap', '["bootstrap"]', 'FRAMEWORK'),
('skill_41', 'SASS/SCSS', '["SASS", "SCSS", "sass", "scss"]', 'FRAMEWORK'),

-- ============================================================
-- Backend Frameworks
-- ============================================================
('skill_50', 'Spring Boot', '["SpringBoot", "Spring", "spring boot"]', 'FRAMEWORK'),
('skill_51', 'Spring Security', '["SpringSecurity", "spring security"]', 'FRAMEWORK'),
('skill_52', 'Django', '["django"]', 'FRAMEWORK'),
('skill_53', 'Flask', '["flask"]', 'FRAMEWORK'),
('skill_54', 'FastAPI', '["Fast API", "fastapi"]', 'FRAMEWORK'),
('skill_55', 'Express.js', '["Express", "ExpressJS", "express"]', 'FRAMEWORK'),
('skill_56', 'NestJS', '["Nest.js", "Nest", "nestjs"]', 'FRAMEWORK'),
('skill_57', '.NET', '["DotNet", "ASP.NET", "NET Core", ".NET Core"]', 'FRAMEWORK'),
('skill_58', 'Ruby on Rails', '["Rails", "RoR"]', 'FRAMEWORK'),
('skill_59', 'Laravel', '["laravel"]', 'FRAMEWORK'),
('skill_60', 'Symfony', '["symfony"]', 'FRAMEWORK'),
('skill_61', 'Micronaut', '["micronaut"]', 'FRAMEWORK'),
('skill_62', 'Quarkus', '["quarkus"]', 'FRAMEWORK'),

-- ============================================================
-- Databases
-- ============================================================
('skill_70', 'PostgreSQL', '["Postgres", "PG", "psql", "postgresql"]', 'DATABASE'),
('skill_71', 'MySQL', '["mysql", "MariaDB"]', 'DATABASE'),
('skill_72', 'MongoDB', '["Mongo", "mongo", "mongodb"]', 'DATABASE'),
('skill_73', 'Redis', '["redis"]', 'DATABASE'),
('skill_74', 'Elasticsearch', '["ES", "Elastic", "elasticsearch"]', 'DATABASE'),
('skill_75', 'Oracle', '["oracle", "OracleDB"]', 'DATABASE'),
('skill_76', 'SQL Server', '["MSSQL", "Microsoft SQL Server", "mssql"]', 'DATABASE'),
('skill_77', 'SQLite', '["sqlite"]', 'DATABASE'),
('skill_78', 'Cassandra', '["cassandra"]', 'DATABASE'),
('skill_79', 'DynamoDB', '["dynamodb", "Dynamo"]', 'DATABASE'),
('skill_80', 'Neo4j', '["neo4j"]', 'DATABASE'),
('skill_81', 'Firebase', '["firebase", "Firestore"]', 'DATABASE'),

-- ============================================================
-- DevOps & CI/CD
-- ============================================================
('skill_90', 'Docker', '["docker", "Dockerfile"]', 'DEVOPS'),
('skill_91', 'Kubernetes', '["K8s", "kubernetes", "k8s"]', 'DEVOPS'),
('skill_92', 'CI/CD', '["CICD", "Continuous Integration", "Continuous Delivery", "ci/cd"]', 'DEVOPS'),
('skill_93', 'Jenkins', '["jenkins"]', 'DEVOPS'),
('skill_94', 'GitLab CI', '["GitLabCI", "gitlab ci"]', 'DEVOPS'),
('skill_95', 'GitHub Actions', '["GHA", "github actions"]', 'DEVOPS'),
('skill_96', 'Terraform', '["terraform"]', 'DEVOPS'),
('skill_97', 'Ansible', '["ansible"]', 'DEVOPS'),
('skill_98', 'Puppet', '["puppet"]', 'DEVOPS'),
('skill_99', 'Chef', '["chef"]', 'DEVOPS'),
('skill_100', 'Prometheus', '["prometheus"]', 'DEVOPS'),
('skill_101', 'Grafana', '["grafana"]', 'DEVOPS'),
('skill_102', 'Nginx', '["nginx"]', 'DEVOPS'),
('skill_103', 'Apache', '["apache", "Apache HTTP Server"]', 'DEVOPS'),

-- ============================================================
-- Cloud Platforms
-- ============================================================
('skill_110', 'AWS', '["Amazon Web Services", "aws", "Amazon AWS"]', 'CLOUD'),
('skill_111', 'Azure', '["Microsoft Azure", "azure", "Azure Cloud"]', 'CLOUD'),
('skill_112', 'Google Cloud', '["GCP", "Google Cloud Platform", "gcp"]', 'CLOUD'),
('skill_113', 'Heroku', '["heroku"]', 'CLOUD'),
('skill_114', 'DigitalOcean', '["digitalocean", "DO"]', 'CLOUD'),
('skill_115', 'Vercel', '["vercel"]', 'CLOUD'),
('skill_116', 'Netlify', '["netlify"]', 'CLOUD'),

-- ============================================================
-- Tools
-- ============================================================
('skill_120', 'Git', '["git", "GitHub", "GitLab", "Bitbucket"]', 'TOOL'),
('skill_121', 'Jira', '["jira"]', 'TOOL'),
('skill_122', 'Confluence', '["confluence"]', 'TOOL'),
('skill_123', 'Slack', '["slack"]', 'TOOL'),
('skill_124', 'Linux', '["linux", "Unix", "Ubuntu", "CentOS", "Debian"]', 'TOOL'),
('skill_125', 'REST APIs', '["REST", "RESTful", "REST API", "rest api"]', 'TOOL'),
('skill_126', 'GraphQL', '["graphql", "gql"]', 'TOOL'),
('skill_127', 'gRPC', '["grpc"]', 'TOOL'),
('skill_128', 'WebSockets', '["websockets", "ws", "socket.io"]', 'TOOL'),
('skill_129', 'Postman', '["postman"]', 'TOOL'),
('skill_130', 'Swagger', '["swagger", "OpenAPI"]', 'TOOL'),
('skill_131', 'Maven', '["maven"]', 'TOOL'),
('skill_132', 'Gradle', '["gradle"]', 'TOOL'),
('skill_133', 'npm', '["npm", "yarn", "pnpm"]', 'TOOL'),
('skill_134', 'Webpack', '["webpack"]', 'TOOL'),
('skill_135', 'Vite', '["vite"]', 'TOOL'),

-- ============================================================
-- Testing
-- ============================================================
('skill_140', 'JUnit', '["junit", "JUnit5"]', 'TOOL'),
('skill_141', 'Mockito', '["mockito"]', 'TOOL'),
('skill_142', 'Jest', '["jest"]', 'TOOL'),
('skill_143', 'Cypress', '["cypress"]', 'TOOL'),
('skill_144', 'Selenium', '["selenium"]', 'TOOL'),
('skill_145', 'Playwright', '["playwright"]', 'TOOL'),
('skill_146', 'TestNG', '["testng"]', 'TOOL'),

-- ============================================================
-- Soft Skills
-- ============================================================
('skill_150', 'Communication', '["communication skills", "communication"]', 'SOFT_SKILL'),
('skill_151', 'Teamwork', '["team work", "collaboration", "team player"]', 'SOFT_SKILL'),
('skill_152', 'Problem Solving', '["problem-solving", "analytical thinking", "problem solving"]', 'SOFT_SKILL'),
('skill_153', 'Leadership', '["leadership skills", "team lead", "leadership"]', 'SOFT_SKILL'),
('skill_154', 'Time Management', '["time management"]', 'SOFT_SKILL'),
('skill_155', 'Adaptability', '["adaptability", "flexibility"]', 'SOFT_SKILL'),
('skill_156', 'Critical Thinking', '["critical thinking"]', 'SOFT_SKILL'),
('skill_157', 'Creativity', '["creativity", "creative thinking"]', 'SOFT_SKILL'),

-- ============================================================
-- Data & AI
-- ============================================================
('skill_160', 'Machine Learning', '["ML", "machine learning"]', 'OTHER'),
('skill_161', 'Deep Learning', '["DL", "deep learning"]', 'OTHER'),
('skill_162', 'TensorFlow', '["tensorflow", "tf"]', 'FRAMEWORK'),
('skill_163', 'PyTorch', '["pytorch", "torch"]', 'FRAMEWORK'),
('skill_164', 'Pandas', '["pandas"]', 'FRAMEWORK'),
('skill_165', 'NumPy', '["numpy"]', 'FRAMEWORK'),
('skill_166', 'Scikit-learn', '["sklearn", "scikit-learn"]', 'FRAMEWORK'),
('skill_167', 'Data Analysis', '["data analysis", "data analytics"]', 'OTHER'),
('skill_168', 'NLP', '["nlp", "Natural Language Processing"]', 'OTHER'),
('skill_169', 'Computer Vision', '["cv", "computer vision"]', 'OTHER'),

-- ============================================================
-- Mobile
-- ============================================================
('skill_170', 'React Native', '["react native", "RN"]', 'FRAMEWORK'),
('skill_171', 'Flutter', '["flutter"]', 'FRAMEWORK'),
('skill_172', 'Android', '["android", "Android Development"]', 'OTHER'),
('skill_173', 'iOS', '["ios", "iOS Development"]', 'OTHER'),
('skill_174', 'Xamarin', '["xamarin"]', 'FRAMEWORK'),

-- ============================================================
-- Security
-- ============================================================
('skill_180', 'OAuth', '["oauth", "OAuth2"]', 'TOOL'),
('skill_181', 'JWT', '["jwt", "JSON Web Tokens"]', 'TOOL'),
('skill_182', 'SSL/TLS', '["ssl", "tls", "https"]', 'TOOL'),
('skill_183', 'Penetration Testing', '["pentest", "penetration testing"]', 'OTHER'),
('skill_184', 'Cryptography', '["cryptography", "encryption"]', 'OTHER')

    ON CONFLICT (canonical_name) DO NOTHING;

-- ============================================================
-- Verify seed data
-- ============================================================
-- SELECT COUNT(*) FROM skills;