package com.resumeanalyzer.dao;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Manages JDBC connections, DDL schema creation, and database seeding.
 * Supports Dual-Mode:
 *  1. Default Embedded Persistent H2 in MySQL compatibility mode (zero-setup, runs out of the box).
 *  2. Live MySQL 8.0 if configured in db.properties or system properties.
 */
public class DatabaseManager {

    private static String jdbcUrl;
    private static String jdbcUser = "sa";
    private static String jdbcPassword = "";
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        // Default: Persistent H2 with MySQL compatibility
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        jdbcUrl = "jdbc:h2:./data/resumedb_fx;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        jdbcUser = "sa";
        jdbcPassword = "";

        // Check if db.properties exists in current dir
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(in);
                if (props.getProperty("db.url") != null) {
                    jdbcUrl = props.getProperty("db.url");
                    jdbcUser = props.getProperty("db.user", "root");
                    jdbcPassword = props.getProperty("db.password", "");
                }
            } catch (Exception e) {
                System.err.println("Could not load db.properties, using default H2-MySQL: " + e.getMessage());
            }
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDatabase();
            initialized = true;
        }
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    public static synchronized void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    phone VARCHAR(30),
                    location VARCHAR(100),
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // 2. Resumes Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resumes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    original_filename VARCHAR(255) NOT NULL,
                    stored_filename VARCHAR(255) NOT NULL,
                    file_path VARCHAR(500) NOT NULL,
                    file_size BIGINT NOT NULL,
                    active BOOLEAN DEFAULT TRUE,
                    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // 3. Extracted Resume Data Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS extracted_resume_data (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    resume_id BIGINT NOT NULL UNIQUE,
                    candidate_name VARCHAR(150),
                    email VARCHAR(150),
                    phone VARCHAR(50),
                    location VARCHAR(100),
                    degree VARCHAR(150),
                    institution VARCHAR(150),
                    graduation_year INT,
                    cgpa VARCHAR(20),
                    raw_text LONGTEXT,
                    technical_skills TEXT,
                    experience_data TEXT,
                    education_data TEXT,
                    projects_data TEXT,
                    certifications_data TEXT,
                    extracted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                )
            """);

            // 4. Jobs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jobs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    recruiter_id BIGINT,
                    title VARCHAR(200) NOT NULL,
                    company_name VARCHAR(150) NOT NULL,
                    location VARCHAR(100) NOT NULL,
                    work_mode VARCHAR(20) NOT NULL,
                    employment_type VARCHAR(30) NOT NULL,
                    salary_range VARCHAR(100),
                    experience_level VARCHAR(50),
                    min_experience_years INT DEFAULT 0,
                    description TEXT NOT NULL,
                    required_skills TEXT NOT NULL,
                    preferred_skills TEXT,
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (recruiter_id) REFERENCES users(id) ON DELETE SET NULL
                )
            """);

            // 5. Job Applications Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS job_applications (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    job_id BIGINT NOT NULL,
                    student_id BIGINT NOT NULL,
                    resume_id BIGINT NOT NULL,
                    match_score INT DEFAULT 0,
                    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
                    cover_letter TEXT,
                    recruiter_notes TEXT,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                )
            """);

            // 6. Resume Analyses Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resume_analyses (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    resume_id BIGINT NOT NULL,
                    overall_score INT NOT NULL,
                    completeness_score INT,
                    skill_coverage_score INT,
                    education_score INT,
                    experience_score INT,
                    project_score INT,
                    formatting_score INT,
                    keyword_relevance_score INT,
                    strengths TEXT,
                    weaknesses TEXT,
                    missing_info TEXT,
                    improvement_suggestions TEXT,
                    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                )
            """);

            // 7. Skills Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS skills (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    category VARCHAR(50) NOT NULL,
                    aliases TEXT
                )
            """);

            // Seed initial data if empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Seed default users if users table is empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertUser(conn, "admin@resumeanalyzer.com", "Admin@123456", "Platform Administrator", "ADMIN", "+91 9900011223", "Headquarters");
                insertUser(conn, "recruiter@tech.com", "Password@123", "Sarah Recruiter", "RECRUITER", "+91 9988776655", "Bengaluru");
                insertUser(conn, "student@test.com", "Password@123", "John Doe", "STUDENT", "+91 9876543210", "Bengaluru");
                System.out.println("Seeded default users via JDBC.");
            }
        }

        // Seed default jobs if jobs table is empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM jobs")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertJob(conn, "Java Backend Engineer", "Apex Global Tech", "Bengaluru, India (or Remote)", "HYBRID", "FULL_TIME",
                        "₹12,00,000 - ₹18,00,000", "0-2 Years", 0,
                        "Looking for a passionate Java Developer to build high-performance microservices and RESTful APIs using Spring Boot and MySQL.",
                        "Java, Spring Boot, REST API, MySQL, Git",
                        "Docker, Kubernetes, Redis, Microservices");

                insertJob(conn, "Full Stack Developer", "CloudScale Networks", "Hyderabad, India (Hybrid)", "HYBRID", "FULL_TIME",
                        "₹14,00,000 - ₹22,00,000", "1-3 Years", 1,
                        "Build end-to-end cloud platforms with React, Spring Boot, and PostgreSQL.",
                        "Java, Spring Boot, React, JavaScript, SQL",
                        "TypeScript, Tailwind CSS, Docker, AWS");

                insertJob(conn, "Python Backend Developer", "DataSphere Analytics", "Pune, India (Remote)", "REMOTE", "FULL_TIME",
                        "₹10,00,000 - ₹16,00,000", "0-2 Years", 0,
                        "Develop data processing pipelines, REST APIs, and backend algorithms.",
                        "Python, Django, FastAPI, SQL, Git",
                        "PostgreSQL, Redis, Docker, Pandas");

                insertJob(conn, "Frontend React Specialist", "NextGen UI Labs", "Mumbai, India", "ON_SITE", "FULL_TIME",
                        "₹9,00,000 - ₹15,00,000", "1-2 Years", 1,
                        "Craft high-performance user interfaces with modern React, Tailwind CSS, and state management.",
                        "React, JavaScript, TypeScript, HTML, CSS",
                        "Tailwind CSS, Redux, REST API, Vite");

                insertJob(conn, "Cloud & DevOps Engineer", "InfraCore Cloud Solutions", "Gurugram, India", "HYBRID", "FULL_TIME",
                        "₹15,00,000 - ₹24,00,000", "2-4 Years", 2,
                        "Automate CI/CD pipelines, container orchestration, and cloud infrastructure.",
                        "Docker, Kubernetes, Linux, Git, CI/CD",
                        "AWS, Terraform, Jenkins, Python");

                insertJob(conn, "Junior Software Engineer", "InnoTech Systems", "Noida, India", "HYBRID", "FULL_TIME",
                        "₹6,00,000 - ₹9,50,000", "Fresher / 0-1 Year", 0,
                        "Entry level engineering role for recent graduates with strong problem-solving skills in Java.",
                        "Java, SQL, Git, OOP, Data Structures",
                        "Spring Boot, HTML, CSS, JavaScript");

                insertJob(conn, "Database Developer & Analyst", "SecureData Matrix", "Chennai, India", "REMOTE", "FULL_TIME",
                        "₹11,00,000 - ₹17,00,000", "1-3 Years", 1,
                        "Design relational database schemas, write complex queries, optimize stored procedures.",
                        "SQL, MySQL, PostgreSQL, Database Design",
                        "Python, Performance Tuning, MongoDB, Redis");
                System.out.println("Seeded 7 default engineering jobs via JDBC.");
            }
        }

        // Seed default skills if skills table is empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM skills")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertSkill(conn, "Java", "PROGRAMMING_LANGUAGE", "java 21, java 17, core java, jse");
                insertSkill(conn, "Python", "PROGRAMMING_LANGUAGE", "py, python3");
                insertSkill(conn, "JavaScript", "PROGRAMMING_LANGUAGE", "js, ecmascript, es6");
                insertSkill(conn, "TypeScript", "PROGRAMMING_LANGUAGE", "ts");
                insertSkill(conn, "Spring Boot", "FRAMEWORK", "springboot, spring-boot, spring framework");
                insertSkill(conn, "React", "FRAMEWORK", "react.js, reactjs");
                insertSkill(conn, "Docker", "CLOUD", "containerization, containers");
                insertSkill(conn, "Kubernetes", "CLOUD", "k8s");
                insertSkill(conn, "MySQL", "DATABASE", "my-sql, rdbms");
                insertSkill(conn, "PostgreSQL", "DATABASE", "postgres, pgsql");
                insertSkill(conn, "Redis", "DATABASE", "redis cache");
                insertSkill(conn, "Git", "TOOL", "github, gitlab, version control");
                insertSkill(conn, "REST API", "METHODOLOGY", "restful api, rest web services, restful");
                insertSkill(conn, "Microservices", "METHODOLOGY", "microservice architecture, distributed systems");
                insertSkill(conn, "AWS", "CLOUD", "amazon web services");
                System.out.println("Seeded canonical skills via JDBC.");
            }
        }
    }

    private static void insertUser(Connection conn, String email, String password, String name, String role, String phone, String location) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (email, password, full_name, role, phone, location) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, name);
            ps.setString(4, role);
            ps.setString(5, phone);
            ps.setString(6, location);
            ps.executeUpdate();
        }
    }

    private static void insertJob(Connection conn, String title, String company, String loc, String mode, String type,
                                  String salary, String expLevel, int minExp, String desc, String reqSkills, String prefSkills) throws SQLException {
        String sql = "INSERT INTO jobs (title, company_name, location, work_mode, employment_type, salary_range, experience_level, min_experience_years, description, required_skills, preferred_skills) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, company);
            ps.setString(3, loc);
            ps.setString(4, mode);
            ps.setString(5, type);
            ps.setString(6, salary);
            ps.setString(7, expLevel);
            ps.setInt(8, minExp);
            ps.setString(9, desc);
            ps.setString(10, reqSkills);
            ps.setString(11, prefSkills);
            ps.executeUpdate();
        }
    }

    private static void insertSkill(Connection conn, String name, String category, String aliases) throws SQLException {
        String sql = "INSERT INTO skills (name, category, aliases) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, aliases);
            ps.executeUpdate();
        }
    }
}
