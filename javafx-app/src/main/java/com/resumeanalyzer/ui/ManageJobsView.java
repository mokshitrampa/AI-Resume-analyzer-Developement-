package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ManageJobsView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private TableView<Job> table = new TableView<>();

    public ManageJobsView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Manage Published Job Vacancies");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Inspect, edit, or archive your listed positions and access applicant review queues.");
        subtitle.getStyleClass().add("subtitle");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);

        TableColumn<Job, String> titleCol = new TableColumn<>("Job Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Job, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompanyName()));

        TableColumn<Job, String> locCol = new TableColumn<>("Location & Mode");
        locCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation() + " (" + data.getValue().getWorkMode() + ")"));

        TableColumn<Job, String> skillsCol = new TableColumn<>("Required Skills");
        skillsCol.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue().getRequiredSkills())));

        TableColumn<Job, String> salaryCol = new TableColumn<>("Salary Range");
        salaryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSalaryRange() != null ? data.getValue().getSalaryRange() : "N/A"));

        TableColumn<Job, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button reviewBtn = new Button("Candidates");
            private final Button delBtn = new Button("Delete");
            private final HBox pane = new HBox(6, reviewBtn, delBtn);

            {
                reviewBtn.getStyleClass().add("btn-secondary");
                delBtn.getStyleClass().add("btn-danger");

                reviewBtn.setOnAction(e -> {
                    Job j = getTableView().getItems().get(getIndex());
                    shell.showCandidateReviewView();
                });

                delBtn.setOnAction(e -> {
                    Job j = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirm("Confirm Deletion", "Are you sure you want to delete job '" + j.getTitle() + "'?")) {
                        try {
                            jobDao.delete(j.getId());
                            loadJobs();
                            AlertHelper.showSuccess("Deleted", "Job opening removed.");
                        } catch (Exception ex) {
                            AlertHelper.showError("Error", "Could not delete job: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(titleCol, compCol, locCol, skillsCol, salaryCol, actCol);
        loadJobs();

        root.getChildren().addAll(title, subtitle, table);
        return root;
    }

    private void loadJobs() {
        try {
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                List<Job> list = jobDao.findByRecruiterId(user.getId());
                table.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
