# CareerMatch Backend

> **Know Your Fit. Build What's Missing.**

The backend service for **CareerMatch**, an AI-powered career intelligence platform designed to help university students, fresh graduates, and entry-level job seekers understand how well their CV matches a target job, identify skill gaps, and receive actionable improvement recommendations.

---

## 📌 Overview

CareerMatch addresses a common problem faced by early-career job seekers: having a CV but not knowing whether it is actually a good fit for a specific job.

The backend provides the core API and business layer for the CareerMatch platform. It handles authentication, user management, CV management, job data, match analysis, improvement recommendations, persistence, validation, and API security.

The backend is designed to serve the Angular frontend through REST APIs while remaining ready for future integration with a dedicated AI service.

---

## 🎯 Core Product Flow

```text
Discover Job
     ↓
Understand Job Requirements
     ↓
Upload CV
     ↓
Analyze Career Fit
     ↓
Understand Skill Gaps
     ↓
Get Improvement Recommendations
     ↓
Improve & Recalculate Fit
````

The backend supports the APIs and data structures required to power this journey.

---

## 🏗️ Architecture

The current backend follows a layered Spring Boot architecture:

```text
┌─────────────────────────┐
│    Angular Frontend     │
│                         │
│      REST / HTTP        │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────────────┐
│       CareerMatch Backend       │
│                                 │
│  Controllers                    │
│       ↓                         │
│  Services                       │
│       ↓                         │
│  Repositories                   │
│       ↓                         │
│  PostgreSQL                     │
└─────────────────────────────────┘
             │
             │ Future Integration
             ▼
┌─────────────────────────────────┐
│          AI Service             │
│                                 │
│ CV Extraction                   │
│ Job Requirement Extraction      │
│ Skill Normalization             │
│ Semantic Matching               │
│ Embeddings                      │
│ AI Recommendations              │
└─────────────────────────────────┘
```

### Architecture Responsibilities

#### CareerMatch Backend

The main backend is responsible for:

* REST API exposure
* Authentication and authorization
* JWT token management
* Business logic orchestration
* Database persistence
* CV and job management
* Match result management
* Improvement recommendation management
* Validation and error handling
* Frontend integration
* Future AI-service orchestration

#### AI Service — Planned

A separate AI service is planned for AI-specific workloads such as:

* CV text and profile extraction
* Job requirement extraction
* Skill normalization
* Semantic similarity
* Embedding-based matching
* AI-generated recommendation wording
* Structured AI output

The AI service is intentionally separated from the main backend so that AI-specific processing does not become tightly coupled to the REST/API layer.

---

## 🚀 Features

### Authentication

The backend provides authentication capabilities including:

* User registration
* User login
* JWT-based authentication
* Refresh token support
* User identity management
* Protected API endpoints

Authentication is implemented using Spring Security and JWT.

---

### 👤 User Management

User-related functionality includes:

* User profile information
* User preferences
* Onboarding status
* Email verification status
* User roles
* Account-related settings

---

### 📄 CV Management

The CV module provides the backend foundation for:

* CV upload
* CV metadata storage
* CV file information
* Extracted/raw CV text storage
* Parsed state tracking
* Structured skills/profile data
* CV history and ownership

CV-related data is associated with the authenticated user.

---

### 💼 Job Management

The job module supports:

* Job listings
* Job details
* Company information
* Job descriptions
* Required skills
* Preferred skills
* Experience requirements
* Employment type
* Remote type
* Salary information
* Job source information
* Job URLs
* Job synchronization metadata

The data model is designed to support external job providers in future iterations.

---

### 🎯 Match Analysis

The match analysis layer provides the data structure required to compare a candidate CV with a target job.

A match result can contain:

* Overall match score
* Score breakdown
* Matched skills
* Missing skills
* Strengths
* Improvement plan
* Evidence
* Extraction confidence
* Human review flag
* Job snapshot information

The architecture allows deterministic business rules and future AI-derived signals to work together.

---

### 📈 Improvement Recommendations

CareerMatch goes beyond simply producing a match score.

The recommendation model supports:

* Skill gap identification
* Gap category
* Required vs preferred classification
* Importance weighting
* Job evidence
* CV evidence
* Related strengths
* Recommended actions
* Deliverables
* Estimated effort
* Expected score gain
* Priority score
* Priority label
* Confidence
* Recommendation status

This supports the product's core concept:

> **Don't just tell the user where they stand — show them what to improve.**

---

## 🛠️ Technology Stack

| Technology          | Purpose                          |
| ------------------- | -------------------------------- |
| Java                | Backend programming language     |
| Spring Boot 3.2.0   | Application framework            |
| Spring Data JPA     | Persistence and data access      |
| Hibernate           | ORM and database mapping         |
| PostgreSQL          | Relational database              |
| Spring Security     | Authentication and authorization |
| JWT                 | Stateless authentication         |
| Lombok              | Boilerplate reduction            |
| Maven Wrapper       | Build and dependency management  |
| SpringDoc / OpenAPI | API documentation                |

---

## 📂 Project Structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── backend/
│   │   │               ├── config/
│   │   │               │   ├── CorsConfig.java
│   │   │               │   ├── OpenApiConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── CVController.java
│   │   │               │   ├── JobController.java
│   │   │               │   ├── MatchController.java
│   │   │               │   └── UserController.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── CVUploadRequest.java
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── MatchAnalysisRequest.java
│   │   │               │   │   ├── RegisterRequest.java
│   │   │               │   │   ├── UpdateProfileRequest.java
│   │   │               │   │   └── UpdateSettingsRequest.java
│   │   │               │   │
│   │   │               │   └── response/
│   │   │               │       ├── ApiResponse.java
│   │   │               │       ├── AuthResponse.java
│   │   │               │       ├── CVProfileResponse.java
│   │   │               │       ├── CVResponse.java
│   │   │               │       ├── ErrorResponse.java
│   │   │               │       ├── ImprovementPlanResponse.java
│   │   │               │       ├── JobListResponse.java
│   │   │               │       ├── JobResponse.java
│   │   │               │       ├── MatchAnalysisResponse.java
│   │   │               │       ├── MatchHistoryResponse.java
│   │   │               │       └── UserResponse.java
│   │   │               │
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── ResourceNotFoundException.java
│   │   │               │   ├── UnauthorizedException.java
│   │   │               │   └── ValidationException.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── CV.java
│   │   │               │   ├── ImprovementRecommendation.java
│   │   │               │   ├── Job.java
│   │   │               │   ├── MatchResult.java
│   │   │               │   ├── RefreshToken.java
│   │   │               │   └── User.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── CVRepository.java
│   │   │               │   ├── ImprovementRecommendationRepository.java
│   │   │               │   ├── JobRepository.java
│   │   │               │   ├── MatchResultRepository.java
│   │   │               │   ├── RefreshTokenRepository.java
│   │   │               │   └── UserRepository.java
│   │   │               │
│   │   │               ├── security/
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── JwtService.java
│   │   │               │   └── UserDetailsServiceImpl.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── CVService.java
│   │   │               │   ├── FileStorageService.java
│   │   │               │   ├── JobService.java
│   │   │               │   ├── MatchService.java
│   │   │               │   ├── RecommendationService.java
│   │   │               │   └── UserService.java
│   │   │               │
│   │   │               ├── util/
│   │   │               │   ├── DateUtils.java
│   │   │               │   └── StringUtils.java
│   │   │               │
│   │   │               └── BackendApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── backend/
│                       └── BackendApplicationTests.java
│
├── README.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

## 🗄️ Database

CareerMatch currently uses PostgreSQL as its primary relational database.

### Current Tables

```text
users
  │
  ├───────────────┐
  │               │
  ▼               ▼
cvs         refresh_tokens
  │
  │
  ▼
match_results ◄──────── jobs
  │
  ▼
improvement_recommendations
```

### Entity Relationships

```text
users
 ├── 1:N → cvs
 ├── 1:N → match_results
 └── 1:N → refresh_tokens

cvs
 └── 1:N → match_results

jobs
 └── 1:N → match_results

match_results
 └── 1:N → improvement_recommendations
```

### Database Tables

| Table                         | Purpose                                    |
| ----------------------------- | ------------------------------------------ |
| `users`                       | User accounts and profile information      |
| `cvs`                         | Uploaded CVs and extracted CV-related data |
| `jobs`                        | Job postings and job requirements          |
| `match_results`               | CV-to-job matching results                 |
| `improvement_recommendations` | Recommended actions for identified gaps    |
| `refresh_tokens`              | Refresh token persistence                  |

The database uses foreign-key relationships with cascading deletes where appropriate to maintain ownership and data integrity.

---

## 🔐 Authentication

CareerMatch uses JWT-based authentication.

The general authentication flow is:

```text
Client
  │
  │ Login credentials
  ▼
AuthController
  │
  ▼
AuthService
  │
  ▼
UserRepository
  │
  ▼
Password Verification
  │
  ▼
JWT Generation
  │
  ▼
Authentication Response
```

For authenticated requests:

```text
Client
  │
  │ Authorization: Bearer <JWT>
  ▼
JwtAuthenticationFilter
  │
  ▼
JWT Validation
  │
  ▼
Spring Security Context
  │
  ▼
Protected Controller
```

Refresh tokens are stored separately to support token renewal.

> Never commit real passwords, JWT secrets, API keys, tokens, or other credentials to the repository.

---

## 📚 API Documentation

The backend exposes REST APIs for the main CareerMatch domains.

Current API areas include:

* Authentication
* Users
* CVs
* Jobs
* Match Analysis

API documentation is provided through **Swagger / OpenAPI**.

When the backend is running locally, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI specification is available through the configured API documentation endpoint.

---

## ⚙️ Configuration

The application uses Spring Boot configuration files:

```text
src/main/resources/
├── application.properties
├── application-dev.properties
└── application-prod.properties
```

The configuration includes settings for:

* Server port
* PostgreSQL datasource
* JPA / Hibernate
* JWT
* CORS
* File uploads
* Logging
* OpenAPI documentation

### Environment Variables

For local or production environments, sensitive values should be supplied securely rather than committed to source control.

Example configuration values:

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=careermatch
DB_USERNAME=postgres
DB_PASSWORD=<your-password>

JWT_SECRET=<your-secret>
```

Do not use the placeholder values above as actual credentials.

---

## 💻 Local Development

### Prerequisites

Make sure the following are installed:

* JDK compatible with the project's Java configuration
* PostgreSQL
* Git

Maven does not need to be installed globally because the repository includes the Maven Wrapper.

---

### 1. Clone the Repository

```bash
git clone https://github.com/omaryasser3060/CareerMatch---backend.git
cd CareerMatch---backend
```

---

### 2. Configure PostgreSQL

Create the required PostgreSQL database:

```text
careermatch
```

Then configure the local datasource credentials in your local configuration.

Do not commit personal database credentials.

---

### 3. Build the Project

On Windows:

```powershell
./mvnw.cmd clean compile
```

Alternatively:

```powershell
./mvnw clean compile
```

---

### 4. Run the Application

On Windows:

```powershell
./mvnw.cmd spring-boot:run
```

Alternatively:

```powershell
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## 🧪 Testing

The project includes a test structure under:

```text
src/test/java/
```

The Maven test command can be used to execute the test suite:

```powershell
./mvnw test
```

As the project evolves, additional unit, integration, controller, security, and repository tests can be added around the core CareerMatch workflows.

---

## 🛡️ Error Handling

The backend includes centralized exception handling through:

```text
GlobalExceptionHandler.java
```

Custom exception types are used for common application-level failures, including:

* Resource not found
* Unauthorized access
* Validation failures

The API also provides structured error responses to make frontend error handling more predictable.

---

## 🔄 Frontend Integration

The Angular frontend communicates with the backend through REST APIs.

The intended communication flow is:

```text
Angular
   │
   │ HTTP / REST
   ▼
CareerMatch Backend
   │
   ├── Authentication
   ├── Users
   ├── CVs
   ├── Jobs
   └── Match Analysis
```

The frontend should communicate with the **main backend**, rather than directly accessing internal services or external providers.

This keeps authentication, business rules, data access, and integration logic centralized in the backend.

---

## 🤖 AI Integration Roadmap

AI is a core part of the CareerMatch product, but AI-specific processing is intended to live in a dedicated service rather than being tightly coupled to the Spring Boot REST layer.

### Planned AI Capabilities

```text
CV
 │
 ▼
CV Extraction
 │
 ▼
Candidate Profile
 │
 ▼
Skill Normalization
 │
 ├───────────────┐
 │               │
 ▼               ▼
Job Requirements  Candidate Skills
 │               │
 └───────┬───────┘
         ▼
Semantic Matching
         │
         ▼
Match Signals
         │
         ▼
Backend Business Rules
         │
         ▼
Match Result
         │
         ▼
Improvement Recommendations
```

Potential future AI capabilities include:

* Structured CV extraction
* Job requirement extraction
* Skill normalization
* Semantic similarity
* Embedding-based matching
* AI-assisted evidence generation
* Personalized improvement recommendations

These capabilities are part of the planned architecture and should not be considered fully integrated into the current backend unless explicitly implemented.

---

## 📊 Matching Model

The CareerMatch product is designed around a structured matching model that evaluates multiple dimensions of candidate-job fit.

The planned scoring dimensions include:

| Dimension        | Weight |
| ---------------- | -----: |
| Required Skills  |    40% |
| Experience       |    20% |
| Projects         |    15% |
| Education        |    10% |
| Preferred Skills |    15% |

The final matching implementation may evolve as the AI and evaluation pipeline is validated.

Semantic similarity thresholds and AI-derived scoring signals should remain configurable and subject to validation rather than being treated as fixed product truth.

---

## 📌 Current Status

### Implemented

* Spring Boot backend foundation
* REST API structure
* PostgreSQL integration
* JPA/Hibernate persistence
* Core domain models
* Controllers
* Services
* Repositories
* DTO-based API contracts
* JWT authentication structure
* Refresh token persistence
* CV management foundation
* Job management foundation
* Match analysis data model
* Improvement recommendation data model
* Global exception handling
* Swagger/OpenAPI documentation
* CORS configuration
* File upload configuration

### Planned / Future

* Dedicated AI service integration
* AI-powered CV extraction
* AI-powered job requirement extraction
* Semantic skill matching
* Embedding-based similarity
* Advanced AI recommendation generation
* External job-provider integration
* More comprehensive automated testing
* Production deployment infrastructure

---

## 🧭 Development Principles

CareerMatch follows several principles to keep the backend maintainable and integration-ready:

### Separation of Responsibilities

Controllers handle HTTP/API concerns.

Services handle business logic and orchestration.

Repositories handle persistence.

Security components handle authentication and authorization.

The future AI service handles AI-specific processing.

### API-First Integration

The Angular frontend communicates with the backend through documented REST APIs.

### Data Integrity

Relational constraints, foreign keys, validation, and JPA mappings are used to maintain consistent data.

### Security by Design

Authentication, authorization, password protection, JWT validation, and secure configuration are treated as core backend responsibilities.

### AI as a Supporting Intelligence Layer

AI should provide meaningful intelligence such as extraction, semantic understanding, and recommendations rather than replacing deterministic business rules unnecessarily.

---

## 🗺️ Roadmap

```text
Phase 1
Backend Foundation
        │
        ▼
Phase 2
Frontend Integration
        │
        ▼
Phase 3
AI Service Integration
        │
        ▼
Phase 4
Semantic Matching & Recommendations
        │
        ▼
Phase 5
Testing, Optimization & Production Readiness
```

---

## 📄 Project Information

**Project:** CareerMatch

**Component:** Backend API

**Version:** v1.0.0

**Architecture:** Layered Spring Boot REST API

**Database:** PostgreSQL

**Authentication:** JWT

**API Documentation:** Swagger / OpenAPI

**Frontend:** Angular

**AI Service:** Planned separate service

---

## 👨‍💻 Development

CareerMatch is being developed as an AI Product Engineering project with a focus on solving a real career-development problem through a combination of:

* Practical backend engineering
* Structured data modeling
* Secure API design
* AI-assisted intelligence
* Explainable matching
* Actionable career recommendations
* User-centered product design

The goal is not simply to produce a compatibility score, but to help users understand:

> **Where they fit, what they are missing, and what they can do next.**

---

