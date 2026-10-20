package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class JobModerationView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private TableView<Job> table = new TableView<>();

    public JobModerationView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Job Moderation & Quality Control");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Audit all employer vacancy postings across the platform and prune outdated or spam listings.");
        subtitle.getStyleClass().add("subtitle");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);

        TableColumn<Job, String> titleCol = new TableColumn<>("Title & Role");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Job, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompanyName()));

        TableColumn<Job, String> locCol = new TableColumn<>("Location & Mode");
        locCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation() + " (" + data.getValue().getWorkMode() + ")"));

        TableColumn<Job, String> skillsCol = new TableColumn<>("Required Competencies");
        skillsCol.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue().getRequiredSkills())));

        TableColumn<Job, Void> actCol = new TableColumn<>("Moderation Action");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Remove Listing");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setOnAction(e -> {
                    Job j = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirm("Confirm Removal", "Remove job listing '" + j.getTitle() + "' at " + j.getCompanyName() + "?")) {
                        try {
                            jobDao.delete(j.getId());
                            loadJobs();
                            AlertHelper.showSuccess("Removed", "Job vacancy purged from platform.");
                        } catch (Exception ex) {
                            AlertHelper.showError("Error", "Could not remove job: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delBtn);
            }
        });

        table.getColumns().addAll(titleCol, compCol, locCol, skillsCol, actCol);
        loadJobs();

        root.getChildren().addAll(title, subtitle, table);
        return root;
    }

    private void loadJobs() {
        try {
            List<Job> jobs = jobDao.findAllActive();
            table.setItems(FXCollections.observableArrayList(jobs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
