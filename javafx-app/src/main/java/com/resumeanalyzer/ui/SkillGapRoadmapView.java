package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.JobMatchingEngine;
import com.resumeanalyzer.service.SkillGapService;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class SkillGapRoadmapView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();
    private final JobMatchingEngine matchingEngine = new JobMatchingEngine();
    private final SkillGapService gapService = new SkillGapService();

    public SkillGapRoadmapView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Skill Gap Analysis & 5-Phase Career Roadmap");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Milestone-based progression pathway and portfolio project suggestions synthesized from detected missing skills.");
        subtitle.getStyleClass().add("subtitle");

        Resume activeResume = SessionManager.getActiveResume();
        if (activeResume == null) {
            VBox emptyCard = new VBox(12);
            emptyCard.setAlignment(Pos.CENTER);
            emptyCard.getStyleClass().add("card");
            emptyCard.setPadding(new Insets(40));

            Label noRes = new Label("Please upload or select an active resume first to generate a gap roadmap.");
            noRes.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            Button uploadBtn = new Button("Go to Resume Upload →");
            uploadBtn.getStyleClass().add("btn-primary");
            uploadBtn.setOnAction(e -> shell.showResumeUploadView());
            emptyCard.getChildren().addAll(noRes, uploadBtn);

            root.getChildren().addAll(title, subtitle, emptyCard);
            return root;
        }

        ExtractedResumeData extractedData = null;
        List<Job> activeJobs = List.of();
        try {
            extractedData = extractedDataDao.findByResumeId(activeResume.getId());
            activeJobs = jobDao.findAllActive();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Job Selector for Gap Roadmap
        VBox selectorCard = new VBox(10);
        selectorCard.getStyleClass().add("card");
        Label selectLbl = new Label("Generate Roadmap for Target Position:");
        selectLbl.getStyleClass().add("title-medium");

        ComboBox<Job> jobCombo = new ComboBox<>();
        jobCombo.setMaxWidth(Double.MAX_VALUE);
        for (Job j : activeJobs) {
            jobCombo.getItems().add(j);
        }
        jobCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " — " + item.getCompanyName());
            }
        });
        jobCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " — " + item.getCompanyName());
            }
        });

        VBox roadmapContentBox = new VBox(16);

        final ExtractedResumeData finalData = extractedData;
        jobCombo.setOnAction(e -> {
            Job selectedJob = jobCombo.getValue();
            if (selectedJob != null && finalData != null) {
                var match = matchingEngine.match(finalData, selectedJob);
                displayRoadmap(roadmapContentBox, selectedJob.getTitle(), match.getMissingSkills());
            }
        });

        if (!activeJobs.isEmpty()) {
            jobCombo.setValue(activeJobs.get(0));
            var match = matchingEngine.match(finalData, activeJobs.get(0));
            displayRoadmap(roadmapContentBox, activeJobs.get(0).getTitle(), match.getMissingSkills());
        }

        selectorCard.getChildren().addAll(selectLbl, jobCombo);
        root.getChildren().addAll(title, subtitle, selectorCard, roadmapContentBox);
        return root;
    }

    private void displayRoadmap(VBox container, String targetRole, List<String> missingSkills) {
        container.getChildren().clear();

        SkillGapService.SkillGapReport report = gapService.generateGapReport(targetRole, missingSkills);

        // Header Summary Card
        VBox summaryCard = new VBox(8);
        summaryCard.getStyleClass().add("card");
        Label roleTitle = new Label("Career Learning Milestone: " + report.getTargetRole());
        roleTitle.getStyleClass().add("title-medium");

        HBox missingBox = new HBox(8);
        missingBox.setAlignment(Pos.CENTER_LEFT);
        Label missLbl = new Label("Targeted Skill Gaps:");
        missLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #fca5a5;");
        missingBox.getChildren().add(missLbl);

        if (report.getMissingSkills().isEmpty()) {
            Label chip = new Label("All core skills verified! Focusing on system design & production hardening.");
            chip.setStyle("-fx-font-size: 12px; -fx-text-fill: #34d399;");
            missingBox.getChildren().add(chip);
        } else {
            FlowPane fp = new FlowPane(6, 6);
            for (String s : report.getMissingSkills()) {
                Label chip = new Label(s);
                chip.getStyleClass().add("badge-missing");
                fp.getChildren().add(chip);
            }
            missingBox.getChildren().add(fp);
        }
        summaryCard.getChildren().addAll(roleTitle, missingBox);
        container.getChildren().add(summaryCard);

        // 5-Phase Timeline Grid
        VBox timelineBox = new VBox(12);
        Label phasesHeader = new Label("5-Phase Sequential Learning Pathway");
        phasesHeader.getStyleClass().add("title-medium");
        timelineBox.getChildren().add(phasesHeader);

        for (SkillGapService.RoadmapPhase phase : report.getRoadmap()) {
            VBox phaseCard = new VBox(8);
            phaseCard.getStyleClass().add("card");
            phaseCard.setStyle("-fx-background-color: #1e293b; -fx-border-color: #3b82f6; -fx-border-width: 0 0 0 4px; -fx-padding: 16px;");

            HBox topRow = new HBox(12);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label("PHASE " + phase.getPhaseNumber());
            badge.setStyle("-fx-background-color: rgba(37, 99, 235, 0.2); -fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px;");

            Label pTitle = new Label(phase.getTitle());
            pTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label dur = new Label("⏱ " + phase.getEstimatedDuration());
            dur.setStyle("-fx-font-size: 11px; -fx-text-fill: #fbbf24; -fx-font-weight: bold;");

            topRow.getChildren().addAll(badge, pTitle, sp, dur);

            Label desc = new Label(phase.getDescription());
            desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            desc.setWrapText(true);

            HBox skillsRow = new HBox(6);
            skillsRow.setAlignment(Pos.CENTER_LEFT);
            Label skHead = new Label("Focus Competencies:");
            skHead.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1;");
            skillsRow.getChildren().add(skHead);
            for (String s : phase.getSkills()) {
                Label chip = new Label(s);
                chip.getStyleClass().add("badge-skill");
                skillsRow.getChildren().add(chip);
            }

            Label resLbl = new Label("Recommended Learning Sources: " + phase.getResource());
            resLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

            phaseCard.getChildren().addAll(topRow, desc, skillsRow, resLbl);
            timelineBox.getChildren().add(phaseCard);
        }
        container.getChildren().add(timelineBox);

        // Portfolio Projects Section
        VBox projBox = new VBox(10);
        projBox.getStyleClass().add("card");

        Label projTitle = new Label("Targeted Portfolio Project Ideas");
        projTitle.getStyleClass().add("title-medium");
        Label projSub = new Label("Build and publish these applied software projects on GitHub to demonstrate competence to recruiters.");
        projSub.getStyleClass().add("subtitle");
        projBox.getChildren().addAll(projTitle, projSub);

        for (String p : report.getSuggestedProjects()) {
            HBox item = new HBox(8);
            item.setStyle("-fx-background-color: #0f172a; -fx-padding: 10px 14px; -fx-background-radius: 8px;");
            Label bullet = new Label("💡");
            Label text = new Label(p);
            text.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");
            text.setWrapText(true);
            item.getChildren().addAll(bullet, text);
            projBox.getChildren().add(item);
        }

        container.getChildren().add(projBox);
    }
}
