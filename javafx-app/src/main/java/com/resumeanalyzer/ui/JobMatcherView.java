package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ApplicationDao;
import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.*;
import com.resumeanalyzer.service.JobMatchingEngine;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobMatcherView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();
    private final ApplicationDao appDao = new ApplicationDao();
    private final JobMatchingEngine matchingEngine = new JobMatchingEngine();

    private ComboBox<JobOption> jobCombo = new ComboBox<>();
    private TextArea customJobArea = new TextArea();
    private TextField customTitleField = new TextField();
    private TextField customSkillsField = new TextField();
    private CheckBox useCustomCheck = new CheckBox("Paste Custom Job Description instead of selecting from database");
    private VBox customBox = new VBox(8);
    private VBox resultContainer = new VBox(16);

    private static class JobOption {
        final Job job;
        JobOption(Job job) { this.job = job; }
        @Override
        public String toString() {
            return job.getTitle() + " — " + job.getCompanyName() + " (" + job.getLocation() + ")";
        }
    }

    public JobMatcherView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Explainable Job Matching Engine");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Compare your resume against openings using TF-IDF cosine similarity, skill taxonomy, and dynamic weighting.");
        subtitle.getStyleClass().add("subtitle");

        Resume activeResume = SessionManager.getActiveResume();
        if (activeResume == null) {
            VBox emptyCard = new VBox(12);
            emptyCard.setAlignment(Pos.CENTER);
            emptyCard.getStyleClass().add("card");
            emptyCard.setPadding(new Insets(40));

            Label noRes = new Label("Please upload or select an active resume first.");
            noRes.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            Button uploadBtn = new Button("Go to Resume Upload →");
            uploadBtn.getStyleClass().add("btn-primary");
            uploadBtn.setOnAction(e -> shell.showResumeUploadView());
            emptyCard.getChildren().addAll(noRes, uploadBtn);

            root.getChildren().addAll(title, subtitle, emptyCard);
            return root;
        }

        // Job Selector Card
        VBox selectorCard = new VBox(14);
        selectorCard.getStyleClass().add("card");

        Label selectLbl = new Label("Select Target Job Position");
        selectLbl.getStyleClass().add("title-medium");

        jobCombo.setMaxWidth(Double.MAX_VALUE);
        try {
            List<Job> jobs = jobDao.findAllActive();
            for (Job j : jobs) {
                jobCombo.getItems().add(new JobOption(j));
            }
            if (!jobCombo.getItems().isEmpty()) {
                jobCombo.setValue(jobCombo.getItems().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        useCustomCheck.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        useCustomCheck.setOnAction(e -> {
            boolean use = useCustomCheck.isSelected();
            jobCombo.setDisable(use);
            customBox.setVisible(use);
            customBox.setManaged(use);
        });

        // Custom job inputs
        customBox.setVisible(false);
        customBox.setManaged(false);
        customTitleField.setPromptText("Job Title (e.g. Senior Microservices Developer)");
        customSkillsField.setPromptText("Required Skills comma-separated (e.g. Java, Spring Boot, MySQL, Docker)");
        customJobArea.setPromptText("Paste raw job description text here...");
        customJobArea.setPrefRowCount(4);
        customBox.getChildren().addAll(new Label("Job Title:"), customTitleField, new Label("Required Skills:"), customSkillsField, new Label("Description:"), customJobArea);

        Button calculateBtn = new Button("Compute Explainable Match Score ➔");
        calculateBtn.getStyleClass().add("btn-primary");
        calculateBtn.setOnAction(e -> handleCalculate(activeResume));

        selectorCard.getChildren().addAll(selectLbl, jobCombo, useCustomCheck, customBox, calculateBtn);

        // Result Card Container
        resultContainer.setVisible(false);
        resultContainer.setManaged(false);

        root.getChildren().addAll(title, subtitle, selectorCard, resultContainer);
        return root;
    }

    private void handleCalculate(Resume activeResume) {
        try {
            ExtractedResumeData extractedData = extractedDataDao.findByResumeId(activeResume.getId());
            if (extractedData == null) {
                AlertHelper.showError("Data Error", "Could not load extracted resume text.");
                return;
            }

            Job targetJob;
            if (useCustomCheck.isSelected()) {
                String cTitle = customTitleField.getText().trim();
                if (cTitle.isEmpty()) cTitle = "Custom Position";
                targetJob = new Job();
                targetJob.setId(0L);
                targetJob.setTitle(cTitle);
                targetJob.setCompanyName("Custom Employer");
                targetJob.setLocation("Remote");
                targetJob.setDescription(customJobArea.getText());
                targetJob.setRequiredSkills(Arrays.stream(customSkillsField.getText().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
            } else {
                JobOption opt = jobCombo.getValue();
                if (opt == null) {
                    AlertHelper.showError("Selection Required", "Please select a job opening.");
                    return;
                }
                targetJob = opt.job;
            }

            JobMatchResult result = matchingEngine.match(extractedData, targetJob);
            displayResult(result, targetJob, activeResume);

        } catch (Exception ex) {
            AlertHelper.showError("Calculation Error", "Failed to calculate job match: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void displayResult(JobMatchResult result, Job job, Resume activeResume) {
        resultContainer.getChildren().clear();
        resultContainer.getStyleClass().add("card");
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);

        // Header
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER_LEFT);

        VBox scoreMeter = new VBox(2);
        scoreMeter.setAlignment(Pos.CENTER);
        scoreMeter.setStyle("-fx-background-color: #0f172a; -fx-padding: 14px; -fx-background-radius: 9999px; -fx-min-width: 90px; -fx-min-height: 90px; -fx-border-color: #2563eb; -fx-border-width: 3px; -fx-border-radius: 9999px;");
        Label val = new Label(result.getOverallScore() + "%");
        val.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #38bdf8;");
        scoreMeter.getChildren().add(val);

        VBox info = new VBox(4);
        Label jTitle = new Label(result.getJobTitle() + " @ " + result.getCompanyName());
        jTitle.getStyleClass().add("title-medium");
        Label mathSub = new Label("Explainable Weighted Linear Combination Algorithm: Final Score = ∑ (w_i × s_i)");
        mathSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        info.getChildren().addAll(jTitle, mathSub);

        topBox.getChildren().addAll(scoreMeter, info);

        // Components Breakdown Table
        VBox compBox = new VBox(8);
        Label compHeader = new Label("Auditable Component Weights & Contributed Points:");
        compHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e2e8f0;");
        compBox.getChildren().add(compHeader);

        for (JobMatchResult.ScoreComponent c : result.getComponents()) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #0f172a; -fx-padding: 8px 12px; -fx-background-radius: 8px;");

            Label name = new Label(c.getName());
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: #cbd5e1; -fx-min-width: 220px;");

            ProgressBar pb = new ProgressBar(c.getRawScore() / 100.0);
            pb.setPrefWidth(200);

            Label raw = new Label(c.getRawScore() + "%");
            raw.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-min-width: 45px;");

            Label weight = new Label("Weight: " + c.getEffectiveWeightPercentage() + "%");
            weight.setStyle("-fx-font-size: 11px; -fx-text-fill: #60a5fa; -fx-min-width: 90px;");

            Label contrib = new Label("+" + c.getContributedPoints() + " pts");
            contrib.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

            row.getChildren().addAll(name, pb, raw, weight, contrib);
            compBox.getChildren().add(row);
        }

        // Matching and Missing Skills
        VBox skillsBreakdown = new VBox(10);

        Label matchSkillsLbl = new Label("Matching Skills Possessed (" + result.getMatchingSkills().size() + "):");
        matchSkillsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #34d399;");
        FlowPane matchFlow = new FlowPane(6, 6);
        for (String s : result.getMatchingSkills()) {
            Label chip = new Label("✔ " + s);
            chip.getStyleClass().add("badge-skill");
            matchFlow.getChildren().add(chip);
        }

        Label missSkillsLbl = new Label("Missing Required Job Competencies (" + result.getMissingSkills().size() + "):");
        missSkillsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #f87171;");
        FlowPane missFlow = new FlowPane(6, 6);
        if (result.getMissingSkills().isEmpty()) {
            Label chip = new Label("Great news! You possess all required skills for this position.");
            chip.setStyle("-fx-font-size: 12px; -fx-text-fill: #34d399;");
            missFlow.getChildren().add(chip);
        } else {
            for (String s : result.getMissingSkills()) {
                Label chip = new Label("✕ " + s);
                chip.getStyleClass().add("badge-missing");
                missFlow.getChildren().add(chip);
            }
        }
        skillsBreakdown.getChildren().addAll(matchSkillsLbl, matchFlow, missSkillsLbl, missFlow);

        // Action Buttons Row
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button roadmapBtn = new Button("View 5-Phase Skill Gap Roadmap ➔");
        roadmapBtn.getStyleClass().add("btn-secondary");
        roadmapBtn.setOnAction(e -> shell.showSkillGapRoadmapView());

        Button applyBtn = new Button("Apply for this Opening →");
        applyBtn.getStyleClass().add("btn-success");
        applyBtn.setOnAction(e -> handleApply(job, activeResume, result.getOverallScore()));

        btnRow.getChildren().addAll(roadmapBtn, applyBtn);

        resultContainer.getChildren().addAll(topBox, compBox, skillsBreakdown, btnRow);
    }

    private void handleApply(Job job, Resume activeResume, int score) {
        User user = SessionManager.getCurrentUser();
        if (user == null || job == null || job.getId() == 0L) {
            AlertHelper.showError("Application Error", "Cannot apply to a custom job. Select an active platform job listing.");
            return;
        }

        try {
            if (appDao.hasApplied(job.getId(), user.getId())) {
                AlertHelper.showInfo("Already Applied", "You have already submitted an application for this position.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog("Excited about this engineering opening!");
            dialog.setTitle("Submit Application");
            dialog.setHeaderText("Apply for " + job.getTitle() + " at " + job.getCompanyName());
            dialog.setContentText("Optional Cover Letter / Note to Recruiter:");
            var res = dialog.showAndWait();
            if (res.isPresent()) {
                JobApplication app = new JobApplication();
                app.setJobId(job.getId());
                app.setStudentId(user.getId());
                app.setResumeId(activeResume.getId());
                app.setMatchScore(score);
                app.setStatus("APPLIED");
                app.setCoverLetter(res.get());
                appDao.create(app);

                AlertHelper.showSuccess("Application Submitted", "Your resume and match score have been sent to the recruiter!");
            }
        } catch (Exception ex) {
            AlertHelper.showError("Database Error", "Failed to submit application: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
