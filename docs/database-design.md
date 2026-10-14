# Database Schema & Entity Relationship Design

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o| STUDENT_PROFILES : "has one"
    USERS ||--o| RECRUITER_PROFILES : "has one"
    USERS ||--o{ RESUMES : "uploads"
    USERS ||--o{ JOBS : "publishes (as Recruiter)"
    USERS ||--o{ JOB_APPLICATIONS : "submits (as Student)"
    USERS ||--o{ SAVED_JOBS : "bookmarks"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"

    RESUMES ||--|| EXTRACTED_RESUME_DATA : "extracts"
    RESUMES ||--o{ RESUME_SKILLS : "contains"
    RESUMES ||--o{ RESUME_ANALYSES : "evaluates"
    RESUMES ||--o{ JOB_MATCH_RESULTS : "matches against"
    RESUMES ||--o{ JOB_APPLICATIONS : "attached to"

    SKILLS ||--o{ RESUME_SKILLS : "categorized by"

    JOBS ||--o{ JOB_MATCH_RESULTS : "matched with"
    JOBS ||--o{ JOB_APPLICATIONS : "receives"
    JOBS ||--o{ SAVED_JOBS : "saved in"

    USERS {
        bigint id PK
        varchar email UK
        varchar password
        varchar full_name
        varchar role
        varchar phone
        varchar location
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    STUDENT_PROFILES {
        bigint id PK
        bigint user_id FK
        varchar current_degree
        varchar institution
        int graduation_year
        varchar cgpa
        varchar target_job_title
        text bio
    }

    RECRUITER_PROFILES {
        bigint id PK
        bigint user_id FK
        varchar company_name
        varchar company_website
        varchar designation
    }

    RESUMES {
        bigint id PK
        bigint user_id FK
        varchar original_filename
        varchar stored_filename
        varchar file_path
        varchar content_type
        bigint file_size
        boolean is_active
        timestamp uploaded_at
    }

    EXTRACTED_RESUME_DATA {
        bigint id PK
        bigint resume_id FK
        varchar candidate_name
        varchar email
        varchar phone
        varchar location
        varchar degree
        varchar institution
        int graduation_year
        varchar cgpa
        longtext raw_text
        text technical_skills_json
        text experience_json
        text education_json
        text projects_json
        text certifications_json
        timestamp extracted_at
    }

    SKILLS {
        bigint id PK
        varchar name UK
        varchar category
        text aliases_json
    }

    JOBS {
        bigint id PK
        bigint recruiter_id FK
        varchar title
        varchar company_name
        varchar location
        varchar work_mode
        varchar employment_type
        varchar salary_range
        varchar experience_level
        int min_experience_years
        text description
        text requirements
        text required_skills
        text preferred_skills
        boolean active
        timestamp created_at
    }

    JOB_MATCH_RESULTS {
        bigint id PK
        bigint resume_id FK
        bigint job_id FK
        int overall_score
        int required_skills_score
        int preferred_skills_score
        int semantic_score
        int experience_score
        int education_score
        text matching_skills_json
        text missing_skills_json
        text score_explanation
        timestamp matched_at
    }

    JOB_APPLICATIONS {
        bigint id PK
        bigint job_id FK
        bigint student_id FK
        bigint resume_id FK
        int match_score
        varchar status
        text cover_letter
        text recruiter_notes
        timestamp applied_at
    }

    RESUME_ANALYSES {
        bigint id PK
        bigint resume_id FK
        int overall_score
        int completeness_score
        int skill_coverage_score
        int education_score
        int experience_score
        int project_score
        int formatting_score
        int keyword_relevance_score
        text strengths_json
        text weaknesses_json
        text missing_info_json
        text suggestions_json
        timestamp analyzed_at
    }
```

---

## 2. Table Specifications & Schema Details

### 2.1 `users`
Stores user identities, hashed passwords, and assigned system roles.
- `id` (BIGINT, Primary Key, Auto-increment)
- `email` (VARCHAR(150), Unique, Not Null, Indexed)
- `password` (VARCHAR(255), Not Null) — BCrypt hash
- `full_name` (VARCHAR(100), Not Null)
- `role` (VARCHAR(20), Not Null) — `STUDENT`, `RECRUITER`, `ADMIN`
- `phone` (VARCHAR(30))
- `location` (VARCHAR(100))
- `active` (BOOLEAN, Default: TRUE)
- `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2.2 `resumes`
Tracks uploaded resume documents with filesystem storage paths.
- `id` (BIGINT, Primary Key, Auto-increment)
- `user_id` (BIGINT, Foreign Key to `users(id)`, Indexed)
- `original_filename` (VARCHAR(255), Not Null)
- `stored_filename` (VARCHAR(255), Not Null, Unique)
- `file_path` (VARCHAR(500), Not Null)
- `content_type` (VARCHAR(100), Not Null) — `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- `file_size` (BIGINT, Not Null)
- `is_active` (BOOLEAN, Default: TRUE)
- `uploaded_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)

### 2.3 `extracted_resume_data`
Stores parsed textual data, normalized skills, and section extracts.
- `id` (BIGINT, Primary Key, Auto-increment)
- `resume_id` (BIGINT, Foreign Key to `resumes(id)`, Unique)
- `candidate_name` (VARCHAR(150))
- `email` (VARCHAR(150))
- `phone` (VARCHAR(50))
- `degree` (VARCHAR(150))
- `graduation_year` (INT)
- `cgpa` (VARCHAR(20))
- `raw_text` (LONGTEXT, Not Null)
- `technical_skills` (TEXT) — Comma-separated or JSON array of canonical skills
- `experience_json` (TEXT)
- `education_json` (TEXT)
- `projects_json` (TEXT)
- `certifications_json` (TEXT)
- `extracted_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)

### 2.4 `jobs`
Maintains corporate job vacancy listings authored by recruiters or seeded as sample engineering openings.
- `id` (BIGINT, Primary Key, Auto-increment)
- `recruiter_id` (BIGINT, Nullable Foreign Key to `users(id)`)
- `title` (VARCHAR(200), Not Null)
- `company_name` (VARCHAR(150), Not Null)
- `location` (VARCHAR(100), Not Null)
- `work_mode` (VARCHAR(20), Not Null) — `REMOTE`, `HYBRID`, `ON_SITE`
- `employment_type` (VARCHAR(30), Not Null) — `FULL_TIME`, `INTERNSHIP`, `CONTRACT`, `PART_TIME`
- `salary_range` (VARCHAR(100))
- `experience_level` (VARCHAR(50))
- `min_experience_years` (INT, Default: 0)
- `description` (TEXT, Not Null)
- `requirements` (TEXT)
- `required_skills` (TEXT, Not Null)
- `preferred_skills` (TEXT)
- `active` (BOOLEAN, Default: TRUE)
- `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)

### 2.5 `job_match_results`
Stores calculated match scores and component weights for auditability and history.
- `id` (BIGINT, Primary Key, Auto-increment)
- `resume_id` (BIGINT, Foreign Key to `resumes(id)`)
- `job_id` (BIGINT, Foreign Key to `jobs(id)`)
- `overall_score` (INT, Not Null) — 0 to 100
- `required_skills_score` (INT)
- `preferred_skills_score` (INT)
- `semantic_score` (INT)
- `experience_score` (INT)
- `education_score` (INT)
- `matching_skills` (TEXT)
- `missing_skills` (TEXT)
- `score_explanation` (TEXT)
- `matched_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)

### 2.6 `job_applications`
Tracks candidate applications to corporate positions with recruiter decisions.
- `id` (BIGINT, Primary Key, Auto-increment)
- `job_id` (BIGINT, Foreign Key to `jobs(id)`, Composite Unique with `student_id`)
- `student_id` (BIGINT, Foreign Key to `users(id)`)
- `resume_id` (BIGINT, Foreign Key to `resumes(id)`)
- `match_score` (INT, Default: 0)
- `status` (VARCHAR(30), Not Null) — `APPLIED`, `UNDER_REVIEW`, `SHORTLISTED`, `REJECTED`
- `cover_letter` (TEXT)
- `recruiter_notes` (TEXT)
- `applied_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)

---

## 3. Dual-Profile Database Compatibility

The application is architected to seamlessly transition between local development and production environments without changing entity definitions or application code:

1. **`local` Profile (Default)**:
   - Uses embedded persistent H2 database stored at `./data/resumedb`.
   - Initialized with `MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`.
   - Zero installation prerequisites; ensures immediate execution and automated testing.
2. **`mysql` Profile (Production)**:
   - Connects to native MySQL 8.0 server on port `3306`.
   - Uses `org.hibernate.dialect.MySQLDialect`.
   - Automatically builds tables via `spring.jpa.hibernate.ddl-auto: update`.
