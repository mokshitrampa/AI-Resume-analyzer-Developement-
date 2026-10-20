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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JobRecommendationsView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();
    private final ApplicationDao appDao = new ApplicationDao();
    private final JobMatchingEngine matchingEngine = new JobMatchingEngine();

    public JobRecommendationsView(DashboardShell shell) {
        this.shell = shell;
    }

    private static class RankedJob {
        final Job job;
        final int matchScore;
        final List<String> matchingSkills;
        RankedJob(Job job, int matchScore, List<String> matchingSkills) {
            this.job = job;
            this.matchScore = matchScore;
            this.matchingSkills = matchingSkills;
        }
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Algorithmic Job Recommendations");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Vacancies ranked automatically by TF-IDF vector similarity and required skill overlap with your active resume.");
        subtitle.getStyleClass().add("subtitle");

        Resume activeResume = SessionManager.getActiveResume();
        User user = SessionManager.getCurrentUser();

        ExtractedResumeData extractedData = null;
        if (activeResume != null) {
            try {
                extractedData = extractedDataDao.findByResumeId(activeResume.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        VBox jobsList = new VBox(14);

        try {
            List<Job> allJobs = jobDao.findAllActive();
            List<RankedJob> rankedList = new ArrayList<>();

            for (Job j : allJobs) {
                int score = 70;
                List<String> matchingSkills = List.of();
                if (extractedData != null) {
                    var match = matchingEngine.match(extractedData, j);
                    score = match.getOverallScore();
                    matchingSkills = match.getMatchingSkills();
                }
                rankedList.add(new RankedJob(j, score, matchingSkills));
            }

            rankedList.sort(Comparator.comparingInt((RankedJob r) -> r.matchScore).reversed());

            for (RankedJob rj : rankedList) {
                Job j = rj.job;
                HBox card = new HBox(20);
                card.getStyleClass().add("card");
                card.setAlignment(Pos.CENTER_LEFT);

                // Score Chip
                VBox scoreBox = new VBox(2);
                scoreBox.setAlignment(Pos.CENTER);
                scoreBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 10px; -fx-background-radius: 12px; -fx-min-width: 75px; -fx-border-color: #3b82f6; -fx-border-width: 1.5px; -fx-border-radius: 12px;");
                Label sVal = new Label(rj.matchScore + "%");
                sVal.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #38bdf8;");
                Label sSub = new Label("Match");
                sSub.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                scoreBox.getChildren().addAll(sVal, sSub);

                // Job Details
                VBox infoBox = new VBox(4);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                Label jTitle = new Label(j.getTitle());
                jTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                Label modeBadge = new Label(j.getWorkMode());
                modeBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #60a5fa; -fx-background-color: rgba(37,99,235,0.2); -fx-padding: 2px 8px; -fx-background-radius: 4px;");
                titleRow.getChildren().addAll(jTitle, modeBadge);

                Label meta = new Label(j.getCompanyName() + " • " + j.getLocation() + " • " + j.getEmploymentType() + " • " + (j.getSalaryRange() != null ? j.getSalaryRange() : ""));
                meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

                FlowPane skillsRow = new FlowPane(4, 4);
                for (String sk : j.getRequiredSkills()) {
                    Label chip = new Label(sk);
                    chip.getStyleClass().add("badge-skill");
                    skillsRow.getChildren().add(chip);
                }

                infoBox.getChildren().addAll(titleRow, meta, skillsRow);

                // Apply Button
                Button applyBtn = new Button("Apply ➔");
                applyBtn.getStyleClass().add("btn-primary");
                applyBtn.setOnAction(e -> {
                    if (activeResume == null) {
                        AlertHelper.showError("Resume Required", "Please upload a resume first to submit applications.");
                        return;
                    }
                    try {
                        if (appDao.hasApplied(j.getId(), user.getId())) {
                            AlertHelper.showInfo("Already Applied", "You have already submitted an application for this position.");
                            return;
                        }

                        TextInputDialog td = new TextInputDialog("Excited to apply for the " + j.getTitle() + " role!");
                        td.setTitle("Submit Application");
                        td.setHeaderText("Apply to " + j.getCompanyName());
                        td.setContentText("Cover Note to Hiring Team:");
                        var res = td.showAndWait();
                        if (res.isPresent()) {
                            JobApplication app = new JobApplication();
                            app.setJobId(j.getId());
                            app.setStudentId(user.getId());
                            app.setResumeId(activeResume.getId());
                            app.setMatchScore(rj.matchScore);
                            app.setStatus("APPLIED");
                            app.setCoverLetter(res.get());
                            appDao.create(app);
                            AlertHelper.showSuccess("Success", "Application submitted successfully to " + j.getCompanyName() + "!");
                        }
                    } catch (Exception ex) {
                        AlertHelper.showError("Error", "Could not apply: " + ex.getMessage());
                    }
                });

                card.getChildren().addAll(scoreBox, infoBox, applyBtn);
                jobsList.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        root.getChildren().addAll(title, subtitle, jobsList);
        return root;
    }
}
