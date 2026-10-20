package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.UserDao;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class UserManagementView {

    private final DashboardShell shell;
    private final UserDao userDao = new UserDao();
    private TableView<User> table = new TableView<>();
    private List<User> allUsers = new ArrayList<>();
    private TextField searchField = new TextField();

    public UserManagementView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("User Directory & Moderation");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Manage registered student candidates, corporate recruiters, and administrators.");
        subtitle.getStyleClass().add("subtitle");

        // Search Bar
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField.setPromptText("Search by name, email, or role...");
        searchField.setPrefWidth(350);
        searchField.textProperty().addListener((obs, old, val) -> filterUsers(val));
        searchRow.getChildren().addAll(new Label("Filter Accounts:"), searchField);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setMaxWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("ADMIN".equalsIgnoreCase(item)) badge.getStyleClass().add("badge-admin");
                    else if ("RECRUITER".equalsIgnoreCase(item)) badge.getStyleClass().add("badge-recruiter");
                    else badge.getStyleClass().add("badge-student");
                    setGraphic(badge);
                }
            }
        });

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Suspended"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label l = new Label(item);
                    l.setStyle("Active".equals(item) ? "-fx-text-fill: #34d399; -fx-font-weight: bold;" : "-fx-text-fill: #f87171; -fx-font-weight: bold;");
                    setGraphic(l);
                }
            }
        });

        TableColumn<User, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button delBtn = new Button("Delete");
            private final HBox pane = new HBox(6, toggleBtn, delBtn);

            {
                delBtn.getStyleClass().add("btn-danger");

                toggleBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    boolean newStatus = !u.isActive();
                    try {
                        userDao.updateStatus(u.getId(), newStatus);
                        u.setActive(newStatus);
                        loadUsers();
                    } catch (Exception ex) {
                        AlertHelper.showError("Error", "Could not update user: " + ex.getMessage());
                    }
                });

                delBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirm("Confirm Delete", "Permanently delete user '" + u.getEmail() + "'?")) {
                        try {
                            userDao.delete(u.getId());
                            loadUsers();
                            AlertHelper.showSuccess("Deleted", "User deleted successfully.");
                        } catch (Exception ex) {
                            AlertHelper.showError("Error", "Could not delete user: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User u = getTableView().getItems().get(getIndex());
                    toggleBtn.setText(u.isActive() ? "Suspend" : "Activate");
                    toggleBtn.getStyleClass().removeAll("btn-secondary", "btn-success");
                    toggleBtn.getStyleClass().add(u.isActive() ? "btn-secondary" : "btn-success");
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, emailCol, roleCol, statusCol, actCol);
        loadUsers();

        root.getChildren().addAll(title, subtitle, searchRow, table);
        return root;
    }

    private void loadUsers() {
        try {
            allUsers = userDao.findAll();
            filterUsers(searchField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            table.setItems(FXCollections.observableArrayList(allUsers));
            return;
        }
        String q = query.toLowerCase();
        List<User> filtered = allUsers.stream()
                .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(q)) ||
                             (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) ||
                             (u.getRole() != null && u.getRole().toLowerCase().contains(q)))
                .toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }
}
