# REST API Documentation & Endpoint Specification

## 1. Authentication & Security Overview

All protected endpoints require an `Authorization` header containing a valid Bearer JSON Web Token:

```http
Authorization: Bearer <jwt_token>
```

OpenAPI Swagger UI is accessible at: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON Specification is accessible at: `http://localhost:8080/v3/api-docs`

---

## 2. Authentication API (`/api/auth`)

### 2.1 Register New Account
- **Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "student@example.com",
    "password": "Password@123",
    "fullName": "Jane Doe",
    "role": "STUDENT",
    "phone": "+91 9876543210",
    "location": "Bengaluru, India"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "type": "Bearer",
    "user": {
      "id": 2,
      "email": "student@example.com",
      "fullName": "Jane Doe",
      "role": "STUDENT"
    }
  }
  ```

### 2.2 User Login
- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "admin@resumeanalyzer.com",
    "password": "Admin@123456"
  }
  ```
- **Response**: `200 OK` (returns JWT token and authenticated user details)

### 2.3 Get Current Authenticated Profile
- **Endpoint**: `GET /api/auth/me`
- **Access**: Authenticated

### 2.4 Change Password
- **Endpoint**: `POST /api/auth/change-password`
- **Access**: Authenticated
- **Request Body**:
  ```json
  {
    "currentPassword": "OldPassword@123",
    "newPassword": "NewPassword@123"
  }
  ```

---

## 3. Resume Processing API (`/api/resumes`)

### 3.1 Upload & Parse Resume
- **Endpoint**: `POST /api/resumes/upload`
- **Access**: `STUDENT`
- **Content-Type**: `multipart/form-data`
- **Parameters**: `file` (Binary PDF or DOCX file, max 5 MB)
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "resumeId": 1,
    "candidateName": "JOHN DOE",
    "email": "john.doe@example.com",
    "phone": "+91 9876543210",
    "degree": "BACHELOR OF TECHNOLOGY",
    "graduationYear": 2024,
    "cgpa": "8.8",
    "technicalSkills": ["Java", "Spring Boot", "React", "Docker", "MySQL"],
    "experience": ["Backend Developer Intern - TechCorp Solutions"],
    "projects": ["E-Commerce Microservices Platform"],
    "certifications": ["AWS Certified Cloud Practitioner"]
  }
  ```

### 3.2 List User Resumes
- **Endpoint**: `GET /api/resumes`
- **Access**: `STUDENT`

### 3.3 Get Extracted Data
- **Endpoint**: `GET /api/resumes/{id}/extracted`
- **Access**: `STUDENT`

### 3.4 Update Extracted Data (Human-in-the-Loop Verification)
- **Endpoint**: `PUT /api/resumes/{id}/extracted`
- **Access**: `STUDENT`
- **Request Body**: Updated JSON payload of skills and education details.

---

## 4. Resume Quality Scoring API (`/api/analysis`)

### 4.1 Run Quality Scoring
- **Endpoint**: `POST /api/analysis/resume/{resumeId}`
- **Access**: `STUDENT`
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "resumeId": 1,
    "overallScore": 92,
    "completenessScore": 100,
    "skillCoverageScore": 95,
    "educationScore": 100,
    "experienceScore": 90,
    "projectScore": 85,
    "formattingScore": 90,
    "keywordRelevanceScore": 100,
    "strengths": [
      "Strong technical skill breadth with 25 verified technologies detected",
      "Document length and text density conform to standard hiring conventions"
    ],
    "weaknesses": [],
    "improvementSuggestions": []
  }
  ```

### 4.2 Get AI Phrasing Improvement Suggestions
- **Endpoint**: `GET /api/analysis/resume/{resumeId}/improvements`
- **Access**: `STUDENT`
- **Response**: `200 OK`
  ```json
  [
    {
      "originalBullet": "Worked on backend APIs using Spring Boot and SQL",
      "suggestedBullet": "Architected high-throughput RESTful microservices in Spring Boot, decreasing API response latency by 25%",
      "rationale": "Incorporates action verb and quantifiable performance impact",
      "impactFactor": "High"
    }
  ]
  ```

---

## 5. Job Matching Engine API (`/api/matching`)

### 5.1 Calculate Explainable Match Score
- **Endpoint**: `POST /api/matching/analyze`
- **Access**: `STUDENT`
- **Request Body**:
  ```json
  {
    "resumeId": 1,
    "jobId": 1
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "overallScore": 87,
    "requiredSkillsScore": 100,
    "preferredSkillsScore": 100,
    "semanticScore": 43,
    "experienceScore": 90,
    "educationScore": 95,
    "matchingSkills": ["Java", "Spring Boot", "REST API", "MySQL", "Git"],
    "missingSkills": [],
    "components": [
      {
        "name": "Required Skills",
        "rawScore": 100,
        "effectiveWeightPercentage": 40.0,
        "contributedPoints": 40.0
      },
      {
        "name": "Preferred Skills",
        "rawScore": 100,
        "effectiveWeightPercentage": 20.0,
        "contributedPoints": 20.0
      },
      {
        "name": "Semantic / TF-IDF Similarity",
        "rawScore": 43,
        "effectiveWeightPercentage": 20.0,
        "contributedPoints": 8.6
      }
    ],
    "explanation": "Job Match Score Calculation Breakdown: Final Calculated Score: 87 / 100"
  }
  ```

### 5.2 Dynamic Skill Gap & Learning Roadmap
- **Endpoint**: `GET /api/matching/skill-gap?resumeId={id}&jobId={id}`
- **Access**: `STUDENT`
- **Response**: Returns 5-phase career milestone roadmap and customized project portfolio ideas.

### 5.3 Automated Job Recommendations
- **Endpoint**: `GET /api/matching/recommendations?resumeId={id}`
- **Access**: `STUDENT`
- **Response**: Array of jobs ranked by match score descending.

---

## 6. Recruiter Portal API (`/api/recruiter`)

### 6.1 Get Recruiter Dashboard Metrics
- **Endpoint**: `GET /api/recruiter/dashboard`
- **Access**: `RECRUITER`
- **Response**: `200 OK` (`totalJobs`, `activeJobs`, `totalApplications`, `shortlistedCount`)

### 6.2 Post New Vacancy
- **Endpoint**: `POST /api/recruiter/jobs`
- **Access**: `RECRUITER`
- **Request Body**:
  ```json
  {
    "title": "Senior Spring Boot Architect",
    "companyName": "TechCorp Solutions",
    "location": "Bengaluru, India",
    "workMode": "HYBRID",
    "employmentType": "FULL_TIME",
    "salaryRange": "₹25,00,000 - ₹35,00,000",
    "minExperienceYears": 3,
    "description": "Lead microservices engineering team...",
    "requiredSkills": "Java, Spring Boot, Microservices, Docker",
    "preferredSkills": "Kafka, AWS, Kubernetes"
  }
  ```

### 6.3 Review Job Applicants
- **Endpoint**: `GET /api/recruiter/jobs/{jobId}/applications`
- **Access**: `RECRUITER` (Only for author of job)
- **Response**: Array of candidate submissions with match scores, student contact, and resume ID.

### 6.4 Update Application Status
- **Endpoint**: `PUT /api/recruiter/applications/{id}/status`
- **Access**: `RECRUITER`
- **Request Body**:
  ```json
  {
    "status": "SHORTLISTED",
    "notes": "Candidate exhibits strong microservices competency. Schedule interview."
  }
  ```

---

## 7. Platform Administration API (`/api/admin`)

### 7.1 System Analytics
- **Endpoint**: `GET /api/admin/analytics`
- **Access**: `ADMIN`
- **Response**: Platform-wide totals (Users, Resumes, Jobs, Skills).

### 7.2 Paginated User Directory
- **Endpoint**: `GET /api/admin/users?page=0&size=10&search=`
- **Access**: `ADMIN`

### 7.3 Toggle User Status
- **Endpoint**: `PUT /api/admin/users/{id}/status`
- **Access**: `ADMIN`
- **Request Body**: `{"active": false}`

### 7.4 Skill Taxonomy Management
- **List Skills**: `GET /api/admin/skills`
- **Add Skill & Aliases**: `POST /api/admin/skills`
- **Delete Skill**: `DELETE /api/admin/skills/{id}`

---

## 8. System Diagnostics (`/api/health`)

- **Endpoint**: `GET /api/health`
- **Access**: Public
- **Response**:
  ```json
  {
    "status": "UP",
    "service": "ai-resume-analyzer-backend",
    "version": "1.0.0",
    "timestamp": "14-10-2026 17:29:36"
  }
  ```
