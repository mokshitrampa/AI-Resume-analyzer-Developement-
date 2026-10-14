# AI-Powered Resume Analyzer & Intelligent Job Matcher

> **"Analyze Your Resume. Discover Your Potential. Find Your Perfect Career Match."**

A complete, production-grade, enterprise recruitment intelligence platform built with **Java 21, Spring Boot 3, and React 19**. Developed for university Java laboratory evaluation with SaaS design quality and pure Java algorithmic matching.

---

## 🌟 Key Platform Capabilities

- 📄 **Automated Resume Parsing**: High-fidelity text stream extraction from **PDF** (Apache PDFBox 3.0.2) and **DOCX** (Apache POI 5.2.5) files with magic-byte validation and secure UUID storage.
- 🧠 **NLP Technical Skill Taxonomy**: Canonical skill normalization with alias mapping across 150+ technologies (e.g., `SpringBoot` $\rightarrow$ `Spring Boot`, `JS` $\rightarrow$ `JavaScript`, `K8s` $\rightarrow$ `Kubernetes`, `Postgres` $\rightarrow$ `PostgreSQL`).
- 📊 **Deterministic Quality Scoring Rubric**: 0–100 quality score evaluating Completeness, Technical Breadth, Education, Experience, Projects, Formatting, and Keyword Relevance.
- 🎯 **Explainable Job Matching Engine**: Pure Java weighted formula combining **Required Skills Overlap (40%)**, **Preferred Skills Overlap (20%)**, **TF-IDF Vector Cosine Similarity (20%)**, **Experience Match (10%)**, and **Education Match (10%)** with **dynamic weight redistribution** when optional criteria are omitted.
- 🗺️ **Dynamic 5-Phase Career Roadmap**: Milestone-based learning pathway with estimated study durations and targeted project portfolio ideas generated from detected missing skills.
- ✍️ **AI Phrasing Improvement Assistant**: Side-by-side action-verb and metric-driven bullet point enhancements with clear distinction between deterministic baseline recommendations and LLM integrations.
- 🏢 **Dedicated Multi-Role Portals**:
  - **Student Portal**: Resume management, interactive analysis view, job matcher, skill gap roadmap, job recommendations, and vacancy applications.
  - **Recruiter Portal**: Vacancy authoring, applicant review dashboard, candidate profile inspection, and Shortlist/Review/Reject status controls.
  - **Admin Console**: Platform analytics, user directory management (activation/suspension/deletion), job moderation, and dynamic skill taxonomy editing.

---

## 🛠️ Technology Stack

| Domain | Technology |
| :--- | :--- |
| **Backend Core** | Java 21 LTS, Spring Boot 3.2.5, Spring Web, Spring Security 6, Spring Data JPA, Spring Validation |
| **Authentication** | Stateless JWT (JJWT 0.12.5), BCrypt Password Hashing, Role-Based Access Control (RBAC) |
| **Database** | MySQL 8.0 & Hibernate 6 (with persistent H2-MySQL dialect for zero-setup local execution) |
| **Document Extraction**| Apache PDFBox 3.0.2 & Apache POI 5.2.5 |
| **Algorithms** | In-Memory Tokenizer, Stopword Pruning, TF-IDF Vectorizer, Cosine Similarity, Dynamic Weighting |
| **API Documentation** | SpringDoc OpenAPI 2.5.0 / Swagger UI |
| **Frontend Framework** | React 19, Vite, React Router 7, Axios, Lucide React Icons |
| **UI Styling** | Tailwind CSS (Dark navy SaaS theme, gradient cards, fully responsive layouts) |
| **Data Analytics** | Recharts (Score rings, interactive radar/bar comparisons) |
| **DevOps / Containers** | Docker, Docker Compose, Multi-stage builds, Nginx SPA proxy |

---

## 🚀 Quick Start Guide (Local Execution)

### Prerequisites
- **Java 21 LTS** installed (Verified at: `C:\Users\moksh\.jdks\jdk-21.0.12.1+1`)
- **Apache Maven 3.9+** (Verified at: `C:\Users\moksh\.jdks\apache-maven-3.9.6`)
- **Node.js 18+** and **npm**

---

### Step 1: Start the Spring Boot Backend

Open a PowerShell terminal in the project root:

```powershell
# Set Java 21 Home
$env:JAVA_HOME = "C:\Users\moksh\.jdks\jdk-21.0.12.1+1"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Navigate to backend directory and start
cd backend
mvn spring-boot:run
```

*Or launch the pre-packaged executable JAR:*
```powershell
java -jar backend/target/ai-resume-analyzer-backend-1.0.0.jar
```

The backend starts on port **`8080`**:
- **Health Actuator**: `http://localhost:8080/api/health`
- **OpenAPI Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

### Step 2: Start the React Frontend

Open a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

The web application opens at: **`http://localhost:5173`** (proxies `/api` requests to backend port 8080).

---

## 👥 Default Test Credentials & Sample Resumes

| Role | Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@resumeanalyzer.com` | `Admin@123456` | Platform analytics, user accounts moderation, job moderation, NLP skill taxonomy. |
| **Recruiter** | `recruiter@tech.com` | `Password@123` | Post vacancies, manage openings, review applicants, shortlist candidates. |
| **Student** | `student@test.com` | `Password@123` | Upload resumes, analyze scores, match jobs, generate roadmap, apply for jobs. |

*(New Students and Recruiters can also register directly from the website at `/register`)*

### Ready-to-Upload Sample Resumes:
Pre-generated binary resumes are located in the `samples/` directory:
- 📄 `samples/John_Doe_FullStack_Resume.pdf` — Senior full-stack candidate with Java, Spring Boot, React, and Cloud skills.
- 📄 `samples/Jane_Smith_Junior_Developer.docx` — Junior engineering graduate profile in OpenXML Word format.

---

## 🧪 Automated Testing & Verification

Execute the backend automated test suite:

```powershell
$env:JAVA_HOME = "C:\Users\moksh\.jdks\jdk-21.0.12.1+1"
cd backend
mvn test
```

### Verified Test Suites:
- `AlgorithmAndMatchingTest`: Verifies TF-IDF calculations, cosine angle matrix, dynamic weight redistribution, and alias resolution.
- `AuthAndSecurityTest`: Verifies BCrypt password encoding, JWT token generation, expiration claims, and role authorization.
- `SampleGeneratorTest`: Verifies PDFBox and Apache POI streaming document generation.

---

## 🐳 Docker Compose Deployment (Optional)

To spin up MySQL 8, the Spring Boot backend, and the React production Nginx container:

```bash
docker-compose up --build
```

- **Frontend Application**: `http://localhost` (Port 80)
- **Backend API**: `http://localhost:8080`
- **MySQL Database**: `localhost:3306`

---

## 📚 Technical Documentation Directory

- 📐 [**docs/architecture.md**](file:///c:/Users/moksh/OneDrive/Desktop/java%20project/docs/architecture.md) — Architectural component flowcharts, sequence diagrams, and mathematical formulation.
- 🗄️ [**docs/database-design.md**](file:///c:/Users/moksh/OneDrive/Desktop/java%20project/docs/database-design.md) — Entity-relationship diagrams, schema tables, constraints, and indexes.
- 🔌 [**docs/api-documentation.md**](file:///c:/Users/moksh/OneDrive/Desktop/java%20project/docs/api-documentation.md) — REST API catalog, parameter specs, and request/response payloads.
- 📑 [**docs/project-report.md**](file:///c:/Users/moksh/OneDrive/Desktop/java%20project/docs/project-report.md) — Formal university laboratory project report with experimental results.

---

## 🎓 University Lab Evaluation Checklist

- [x] **Zero Mock Data in Algorithms**: Deterministic TF-IDF and cosine calculations run on live document text.
- [x] **No Python Dependency**: 100% pure Java document extraction and NLP implementation.
- [x] **Database Relational Integrity**: Full foreign keys, unique email constraints, and cascade relationships.
- [x] **Explainable Output**: Scores explicitly display contribution points and weight breakdown formulas.
- [x] **Role-Based Portals**: Distinct dashboards and navigation bars for Students, Recruiters, and Admins.
- [x] **Zero Dead Links**: Every route, sidebar link, and button connects to a functioning page or modal.
- [x] **Production Aesthetics**: Modern SaaS UI with dark navy theme, blue/purple accents, Lucide icons, and Recharts.
