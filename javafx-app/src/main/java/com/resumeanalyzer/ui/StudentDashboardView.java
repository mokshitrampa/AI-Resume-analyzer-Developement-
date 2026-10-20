package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ApplicationDao;
import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.dao.ResumeDao;
import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.service.JobMatchingEngine;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class StudentDashboardView {

    private final DashboardShell shell;
    private final ResumeDao resumeDao = new ResumeDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();
    private final JobDao jobDao = new JobDao();
    private final ApplicationDao appDao = new ApplicationDao();
    private final JobMatchingEngine matchingEngine = new JobMatchingEngine();

    public StudentDashboardView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        User user = SessionManager.getCurrentUser();

        // Welcome Header
        VBox headerBox = new VBox(6);
        Label welcome = new Label("Welcome back, " + (user != null ? user.getFullName() : "Student") + "!");
        welcome.getStyleClass().add("title-large");
        Label subtitle = new Label("Here is your real-time recruitment matching overview and resume diagnostics.");
        subtitle.getStyleClass().add("subtitle");
        headerBox.getChildren().addAll(welcome, subtitle);

        // Fetch Data via JDBC
        List<Resume> userResumes = List.of();
        int totalApps = 0;
        try {
            if (user != null) {
                userResumes = resumeDao.findByUserId(user.getId());
                totalApps = appDao.findByStudentId(user.getId()).size();
                if (!userResumes.isEmpty() && SessionManager.getActiveResume() == null) {
                    SessionManager.setActiveResume(userResumes.get(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Resume activeResume = SessionManager.getActiveResume();
        ExtractedResumeData extractedData = null;
        if (activeResume != null) {
            try {
                extractedData = extractedDataDao.findByResumeId(activeResume.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Metrics Grid (4 Stat Cards)
        HBox statsGrid = new HBox(16);
        statsGrid.setAlignment(Pos.CENTER);

        statsGrid.getChildren().addAll(
                createStatCard("Uploaded Resumes", String.valueOf(userResumes.size()), "Document versions in storage", "#38bdf8"),
                createStatCard("Detected Skills", extractedData != null ? String.valueOf(extractedData.getTechnicalSkills().size()) : "0", "Verified competencies", "#34d399"),
                createStatCard("Applications Submitted", String.valueOf(totalApps), "Corporate position inquiries", "#c084fc"),
                createStatCard("Active Profile", activeResume != null ? "Ready" : "No Resume", activeResume != null ? activeResume.getOriginalFilename() : "Upload a PDF/DOCX", "#fbbf24")
        );

        // Main Center Split: Active Resume Status & Quick Actions
        HBox mainSplit = new HBox(20);

        // Left Panel: Active Resume Card
        VBox activeResumeCard = new VBox(14);
        activeResumeCard.getStyleClass().add("card");
        activeResumeCard.setPrefWidth(550);
        HBox.setHgrow(activeResumeCard, Priority.ALWAYS);

        Label activeCardTitle = new Label("Active Resume Profile");
        activeCardTitle.getStyleClass().add("title-medium");

        if (activeResume != null && extractedData != null) {
            Label resName = new Label("📄 " + activeResume.getOriginalFilename());
            resName.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0; -fx-font-size: 14px;");

            Label candDetails = new Label("Candidate: " + extractedData.getCandidateName() + " | Email: " + extractedData.getEmail());
            candDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            Label skillsHeader = new Label("Verified Skills (" + extractedData.getTechnicalSkills().size() + "):");
            skillsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #cbd5e1;");

            FlowPane skillsFlow = new FlowPane(6, 6);
            for (String skill : extractedData.getTechnicalSkills()) {
                Label chip = new Label(skill);
                chip.getStyleClass().add("badge-skill");
                skillsFlow.getChildren().add(chip);
            }

            HBox actionsRow = new HBox(10);
            Button analyzeBtn = new Button("Run Quality Scoring →");
            analyzeBtn.getStyleClass().add("btn-primary");
            analyzeBtn.setOnAction(e -> shell.showResumeAnalyzerView());

            Button matchBtn = new Button("Match Against Jobs →");
            matchBtn.getStyleClass().add("btn-secondary");
            matchBtn.setOnAction(e -> shell.showJobMatcherView());

            actionsRow.getChildren().addAll(analyzeBtn, matchBtn);

            activeResumeCard.getChildren().addAll(activeCardTitle, resName, candDetails, skillsHeader, skillsFlow, actionsRow);
        } else {
            Label noResumeLabel = new Label("You have not uploaded a resume yet.");
            noResumeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            Button uploadFirstBtn = new Button("Upload Your Resume Now →");
            uploadFirstBtn.getStyleClass().add("btn-primary");
            uploadFirstBtn.setOnAction(e -> shell.showResumeUploadView());
            activeResumeCard.getChildren().addAll(activeCardTitle, noResumeLabel, uploadFirstBtn);
        }

        // Right Panel: Quick Shortcuts & Seeded Positions Preview
        VBox quickJobsCard = new VBox(14);
        quickJobsCard.getStyleClass().add("card");
        quickJobsCard.setPrefWidth(450);

        Label quickTitle = new Label("Top Engineering Vacancies");
        quickTitle.getStyleClass().add("title-medium");

        VBox jobsList = new VBox(10);
        try {
            List<Job> jobs = jobDao.findAllActive();
            for (int i = 0; i < Math.min(4, jobs.size()); i++) {
                Job j = jobs.get(i);
                VBox jBox = new VBox(2);
                jBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 8px;");
                Label jTitle = new Label(j.getTitle() + " — " + j.getCompanyName());
                jTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #f1f5f9;");
                Label jMeta = new Label(j.getLocation() + " • " + j.getWorkMode() + " • " + j.getSalaryRange());
                jMeta.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                jBox.getChildren().addAll(jTitle, jMeta);
                jobsList.getChildren().add(jBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button exploreAllBtn = new Button("Explore All Job Recommendations →");
        exploreAllBtn.getStyleClass().add("btn-secondary");
        exploreAllBtn.setMaxWidth(Double.MAX_VALUE);
        exploreAllBtn.setOnAction(e -> shell.showJobRecommendationsView());

        quickJobsCard.getChildren().addAll(quickTitle, jobsList, exploreAllBtn);

        mainSplit.getChildren().addAll(activeResumeCard, quickJobsCard);

        root.getChildren().addAll(headerBox, statsGrid, mainSplit);
        return root;
    }

    private VBox createStatCard(String title, String value, String desc, String accentColor) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        valLbl.setStyle("-fx-text-fill: " + accentColor + ";");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-title");

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(valLbl, titleLbl, descLbl);
        return card;
    }
}
