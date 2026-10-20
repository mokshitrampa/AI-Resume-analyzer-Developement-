package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ApplicationDao;
import com.resumeanalyzer.model.JobApplication;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class MyApplicationsView {

    private final DashboardShell shell;
    private final ApplicationDao appDao = new ApplicationDao();

    public MyApplicationsView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("My Submitted Applications");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Track recruitment status, recruiter feedback, and match ratings across your applications.");
        subtitle.getStyleClass().add("subtitle");

        User user = SessionManager.getCurrentUser();
        TableView<JobApplication> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);

        TableColumn<JobApplication, String> jobCol = new TableColumn<>("Position & Role");
        jobCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobTitle()));

        TableColumn<JobApplication, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompanyName()));

        TableColumn<JobApplication, String> scoreCol = new TableColumn<>("Match Score");
        scoreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMatchScore() + "%"));

        TableColumn<JobApplication, String> statusCol = new TableColumn<>("Decision Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("SHORTLISTED".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold;");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: rgba(59, 130, 246, 0.2); -fx-text-fill: #60a5fa; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<JobApplication, String> notesCol = new TableColumn<>("Recruiter Feedback");
        notesCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRecruiterNotes() != null ? data.getValue().getRecruiterNotes() : "Under review"));

        TableColumn<JobApplication, String> dateCol = new TableColumn<>("Applied Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppliedAt() != null ? data.getValue().getAppliedAt().toLocalDate().toString() : "Recent"));

        table.getColumns().addAll(jobCol, compCol, scoreCol, statusCol, notesCol, dateCol);

        try {
            if (user != null) {
                List<JobApplication> apps = appDao.findByStudentId(user.getId());
                table.setItems(FXCollections.observableArrayList(apps));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        root.getChildren().addAll(title, subtitle, table);
        return root;
    }
}
