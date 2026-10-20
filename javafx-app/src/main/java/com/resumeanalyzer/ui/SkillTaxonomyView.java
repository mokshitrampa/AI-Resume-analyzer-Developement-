package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.SkillDao;
import com.resumeanalyzer.model.Skill;
import com.resumeanalyzer.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillTaxonomyView {

    private final DashboardShell shell;
    private final SkillDao skillDao = new SkillDao();
    private TableView<Skill> table = new TableView<>();

    public SkillTaxonomyView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(6);
        Label title = new Label("NLP Skill Taxonomy & Alias Dictionary");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Manage canonical technical competencies and synonym normalization rules (e.g. 'JS' → 'JavaScript').");
        subtitle.getStyleClass().add("subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Technical Skill");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddDialog());

        topRow.getChildren().addAll(headerBox, spacer, addBtn);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);

        TableColumn<Skill, String> nameCol = new TableColumn<>("Canonical Skill Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Skill, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Skill, String> aliasCol = new TableColumn<>("Aliases & Synonyms");
        aliasCol.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue().getAliases())));

        TableColumn<Skill, Void> actCol = new TableColumn<>("Action");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setOnAction(e -> {
                    Skill s = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirm("Confirm Delete", "Remove skill '" + s.getName() + "' from taxonomy?")) {
                        try {
                            skillDao.delete(s.getId());
                            loadSkills();
                        } catch (Exception ex) {
                            AlertHelper.showError("Error", "Could not delete skill: " + ex.getMessage());
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

        table.getColumns().addAll(nameCol, catCol, aliasCol, actCol);
        loadSkills();

        root.getChildren().addAll(topRow, table);
        return root;
    }

    private void loadSkills() {
        try {
            List<Skill> skills = skillDao.findAll();
            table.setItems(FXCollections.observableArrayList(skills));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddDialog() {
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Add New Technical Skill");
        dialog.setHeaderText("Register canonical skill and recognition aliases");

        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setPrefWidth(450);

        TextField nameField = new TextField();
        nameField.setPromptText("Skill Name (e.g. Kotlin, Kafka, GraphQL)");

        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("PROGRAMMING_LANGUAGE", "FRAMEWORK", "DATABASE", "CLOUD", "TOOL", "METHODOLOGY", "OTHER");
        catCombo.setValue("PROGRAMMING_LANGUAGE");
        catCombo.setMaxWidth(Double.MAX_VALUE);

        TextField aliasField = new TextField();
        aliasField.setPromptText("Aliases comma-separated (e.g. kt, kotlin-lang)");

        box.getChildren().addAll(new Label("Skill Name:"), nameField, new Label("Category:"), catCombo, new Label("Aliases:"), aliasField);

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String n = nameField.getText().trim();
                if (n.isEmpty()) return null;
                List<String> aliases = Arrays.stream(aliasField.getText().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                return new Skill(null, n, catCombo.getValue(), aliases);
            }
            return null;
        });

        var res = dialog.showAndWait();
        if (res.isPresent() && res.get() != null) {
            try {
                skillDao.create(res.get());
                loadSkills();
                AlertHelper.showSuccess("Skill Added", "Skill added to NLP taxonomy database!");
            } catch (Exception ex) {
                AlertHelper.showError("Error", "Could not add skill: " + ex.getMessage());
            }
        }
    }
}
