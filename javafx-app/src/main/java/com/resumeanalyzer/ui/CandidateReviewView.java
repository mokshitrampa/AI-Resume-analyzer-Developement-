package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ApplicationDao;
import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.JobApplication;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class CandidateReviewView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private final ApplicationDao appDao = new ApplicationDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();

    private ComboBox<Job> jobCombo = new ComboBox<>();
    private TableView<JobApplication> table = new TableView<>();

    public CandidateReviewView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Candidate Applications & Shortlisting");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Review candidate submissions ranked by AI match score, inspect verified skills, and update hiring decisions.");
        subtitle.getStyleClass().add("subtitle");

        // Job Selector Row
        HBox selectorRow = new HBox(12);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        Label selLbl = new Label("Select Position:");
        selLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e2e8f0;");

        jobCombo.setPrefWidth(350);
        jobCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getLocation() + ")");
            }
        });
        jobCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Job item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getLocation() + ")");
            }
        });

        jobCombo.setOnAction(e -> {
            Job j = jobCombo.getValue();
            if (j != null) loadApplications(j.getId());
        });

        selectorRow.getChildren().addAll(selLbl, jobCombo);

        // Candidate Table
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);

        TableColumn<JobApplication, String> nameCol = new TableColumn<>("Candidate Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));

        TableColumn<JobApplication, String> emailCol = new TableColumn<>("Email & Phone");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStudentEmail() + " • " + (data.getValue().getStudentPhone() != null ? data.getValue().getStudentPhone() : "N/A")));

        TableColumn<JobApplication, String> scoreCol = new TableColumn<>("Match Score");
        scoreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMatchScore() + "%"));

        TableColumn<JobApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<JobApplication, String> dateCol = new TableColumn<>("Applied Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppliedAt() != null ? data.getValue().getAppliedAt().toLocalDate().toString() : "Recent"));

        TableColumn<JobApplication, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button inspectBtn = new Button("Inspect");
            private final Button statusBtn = new Button("Decision");
            private final HBox pane = new HBox(6, inspectBtn, statusBtn);

            {
                inspectBtn.getStyleClass().add("btn-secondary");
                statusBtn.getStyleClass().add("btn-primary");

                inspectBtn.setOnAction(e -> {
                    JobApplication app = getTableView().getItems().get(getIndex());
                    showCandidateDetails(app);
                });

                statusBtn.setOnAction(e -> {
                    JobApplication app = getTableView().getItems().get(getIndex());
                    showStatusDialog(app);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(nameCol, emailCol, scoreCol, statusCol, dateCol, actCol);

        loadJobs();

        root.getChildren().addAll(title, subtitle, selectorRow, table);
        return root;
    }

    private void loadJobs() {
        try {
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                List<Job> jobs = jobDao.findByRecruiterId(user.getId());
                if (jobs.isEmpty()) {
                    jobs = jobDao.findAllActive(); // Fallback so recruiter can inspect sample jobs
                }
                jobCombo.setItems(FXCollections.observableArrayList(jobs));
                if (!jobs.isEmpty()) {
                    jobCombo.setValue(jobs.get(0));
                    loadApplications(jobs.get(0).getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadApplications(Long jobId) {
        try {
            List<JobApplication> apps = appDao.findByJobId(jobId);
            table.setItems(FXCollections.observableArrayList(apps));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCandidateDetails(JobApplication app) {
        try {
            ExtractedResumeData data = extractedDataDao.findByResumeId(app.getResumeId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Candidate Profile: " + app.getStudentName());
            dialog.setHeaderText("Match Score: " + app.getMatchScore() + "% | Position: " + app.getJobTitle());

            VBox box = new VBox(10);
            box.setPadding(new Insets(16));
            box.setPrefWidth(500);

            Label cand = new Label("Candidate: " + app.getStudentName() + " (" + app.getStudentEmail() + ")");
            cand.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");

            Label cover = new Label("Cover Note: \"" + (app.getCoverLetter() != null ? app.getCoverLetter() : "None provided") + "\"");
            cover.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
            cover.setWrapText(true);

            Label skillsLbl = new Label("Verified Skills (" + (data != null ? data.getTechnicalSkills().size() : 0) + "):");
            skillsLbl.setStyle("-fx-font-weight: bold;");

            FlowPane fp = new FlowPane(4, 4);
            if (data != null) {
                for (String s : data.getTechnicalSkills()) {
                    Label c = new Label(s);
                    c.getStyleClass().add("badge-skill");
                    fp.getChildren().add(c);
                }
            }

            box.getChildren().addAll(cand, cover, skillsLbl, fp);
            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Could not load candidate details: " + e.getMessage());
        }
    }

    private void showStatusDialog(JobApplication app) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(app.getStatus(), "APPLIED", "REVIEWED", "SHORTLISTED", "REJECTED");
        dialog.setTitle("Application Decision");
        dialog.setHeaderText("Update Hiring Decision for " + app.getStudentName());
        dialog.setContentText("Select Status:");

        var res = dialog.showAndWait();
        if (res.isPresent()) {
            try {
                TextInputDialog notesDialog = new TextInputDialog(app.getRecruiterNotes() != null ? app.getRecruiterNotes() : "");
                notesDialog.setTitle("Recruiter Notes");
                notesDialog.setHeaderText("Add Internal Notes for " + res.get());
                notesDialog.setContentText("Notes:");
                var notesRes = notesDialog.showAndWait();
                String notes = notesRes.orElse("");

                appDao.updateStatus(app.getId(), res.get(), notes);
                AlertHelper.showSuccess("Updated", "Candidate status updated to " + res.get() + "!");
                if (jobCombo.getValue() != null) {
                    loadApplications(jobCombo.getValue().getId());
                }
            } catch (Exception ex) {
                AlertHelper.showError("Error", "Could not update status: " + ex.getMessage());
            }
        }
    }
}
