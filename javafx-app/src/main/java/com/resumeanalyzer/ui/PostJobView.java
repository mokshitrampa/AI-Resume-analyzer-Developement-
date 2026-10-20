package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.User;
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

public class PostJobView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();

    public PostJobView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Publish New Engineering Position");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Fill in job specifications, technical requirements, and target candidate qualifications.");
        subtitle.getStyleClass().add("subtitle");

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card");

        TextField titleField = new TextField();
        titleField.setPromptText("Job Title (e.g. Senior Spring Boot Microservices Architect)");

        TextField compField = new TextField();
        compField.setPromptText("Company / Organization Name");

        TextField locField = new TextField();
        locField.setPromptText("Location (e.g. Bengaluru, India or Remote)");

        HBox modeRow = new HBox(12);
        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("REMOTE", "HYBRID", "ON_SITE");
        modeCombo.setValue("HYBRID");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("FULL_TIME", "INTERNSHIP", "CONTRACT", "PART_TIME");
        typeCombo.setValue("FULL_TIME");

        TextField minExpField = new TextField("2");
        minExpField.setPromptText("Min Experience (Years)");

        modeRow.getChildren().addAll(new Label("Work Mode:"), modeCombo, new Label("Type:"), typeCombo, new Label("Min Exp:"), minExpField);

        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary Range (e.g. ₹15,00,000 - ₹25,00,000)");

        TextField reqSkillsField = new TextField();
        reqSkillsField.setPromptText("Required Skills comma-separated (e.g. Java, Spring Boot, MySQL, REST API, Git)");

        TextField prefSkillsField = new TextField();
        prefSkillsField.setPromptText("Preferred Skills comma-separated (e.g. Docker, Kubernetes, AWS, Redis)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Detailed Job Description & Responsibilities...");
        descArea.setPrefRowCount(5);

        Button submitBtn = new Button("Publish Job Vacancy ➔");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> {
            String jTitle = titleField.getText().trim();
            String comp = compField.getText().trim();
            String loc = locField.getText().trim();
            String req = reqSkillsField.getText().trim();

            if (jTitle.isEmpty() || comp.isEmpty() || loc.isEmpty() || req.isEmpty()) {
                AlertHelper.showError("Validation Error", "Title, company, location, and required skills are mandatory.");
                return;
            }

            try {
                User user = SessionManager.getCurrentUser();
                Job job = new Job();
                if (user != null) job.setRecruiterId(user.getId());
                job.setTitle(jTitle);
                job.setCompanyName(comp);
                job.setLocation(loc);
                job.setWorkMode(modeCombo.getValue());
                job.setEmploymentType(typeCombo.getValue());
                try {
                    job.setMinExperienceYears(Integer.parseInt(minExpField.getText().trim()));
                } catch (Exception ignored) { job.setMinExperienceYears(0); }
                job.setSalaryRange(salaryField.getText().trim());
                job.setDescription(descArea.getText().trim().isEmpty() ? "Engineering position at " + comp : descArea.getText().trim());

                List<String> reqSkills = Arrays.stream(req.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                job.setRequiredSkills(reqSkills);

                List<String> prefSkills = Arrays.stream(prefSkillsField.getText().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                job.setPreferredSkills(prefSkills);

                jobDao.create(job);
                AlertHelper.showSuccess("Job Published", "New engineering opening published successfully!");
                shell.showManageJobsView();
            } catch (Exception ex) {
                AlertHelper.showError("Database Error", "Failed to publish job: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        formCard.getChildren().addAll(
                new Label("Position Title:"), titleField,
                new Label("Company Name:"), compField,
                new Label("Location:"), locField,
                modeRow,
                new Label("Compensation / Salary Range:"), salaryField,
                new Label("Required Technical Skills:"), reqSkillsField,
                new Label("Preferred / Nice-to-Have Skills:"), prefSkillsField,
                new Label("Role Overview & Description:"), descArea,
                submitBtn
        );

        root.getChildren().addAll(title, subtitle, formCard);
        return root;
    }
}
