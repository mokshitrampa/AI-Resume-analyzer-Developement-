package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ApplicationDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class RecruiterDashboardView {

    private final DashboardShell shell;
    private final JobDao jobDao = new JobDao();
    private final ApplicationDao appDao = new ApplicationDao();

    public RecruiterDashboardView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        User user = SessionManager.getCurrentUser();

        // Header
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(6);
        Label title = new Label("Recruiter Talent Acquisition Hub");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Publish engineering positions, evaluate candidate match scores, and shortlist candidates.");
        subtitle.getStyleClass().add("subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button postBtn = new Button("+ Post New Opening");
        postBtn.getStyleClass().add("btn-primary");
        postBtn.setOnAction(e -> shell.showPostJobView());

        topRow.getChildren().addAll(headerBox, spacer, postBtn);

        // Fetch Metrics via JDBC
        List<Job> myJobs = List.of();
        int totalApps = 0;
        int shortlisted = 0;
        try {
            if (user != null) {
                myJobs = jobDao.findByRecruiterId(user.getId());
                for (Job j : myJobs) {
                    var apps = appDao.findByJobId(j.getId());
                    totalApps += apps.size();
                    for (var a : apps) {
                        if ("SHORTLISTED".equalsIgnoreCase(a.getStatus())) shortlisted++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Metrics Grid (4 Stat Cards)
        HBox statsGrid = new HBox(16);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.getChildren().addAll(
                createStatCard("Total Published Jobs", String.valueOf(myJobs.size()), "Vacancies authored by you", "#38bdf8"),
                createStatCard("Active Listings", String.valueOf(myJobs.stream().filter(Job::isActive).count()), "Open for applications", "#34d399"),
                createStatCard("Candidate Applicants", String.valueOf(totalApps), "Total candidate submissions", "#c084fc"),
                createStatCard("Shortlisted Talents", String.valueOf(shortlisted), "Selected for interviews", "#fbbf24")
        );

        // Openings List Card
        VBox jobsCard = new VBox(14);
        jobsCard.getStyleClass().add("card");

        HBox jHeader = new HBox();
        jHeader.setAlignment(Pos.CENTER_LEFT);
        Label jTitle = new Label("Your Managed Job Vacancies");
        jTitle.getStyleClass().add("title-medium");
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Button manageAllBtn = new Button("Manage All Jobs →");
        manageAllBtn.getStyleClass().add("btn-secondary");
        manageAllBtn.setOnAction(e -> shell.showManageJobsView());
        jHeader.getChildren().addAll(jTitle, sp2, manageAllBtn);

        VBox listContainer = new VBox(10);
        if (myJobs.isEmpty()) {
            Label empty = new Label("You have not published any jobs yet. Click '+ Post New Opening' to get started.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 20px 0;");
            listContainer.getChildren().add(empty);
        } else {
            for (Job j : myJobs) {
                HBox row = new HBox(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px 16px; -fx-background-radius: 10px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 10px;");

                VBox details = new VBox(4);
                HBox.setHgrow(details, Priority.ALWAYS);
                Label t = new Label(j.getTitle() + " — " + j.getCompanyName());
                t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f1f5f9;");
                Label m = new Label(j.getLocation() + " • " + j.getWorkMode() + " • " + j.getSalaryRange());
                m.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
                details.getChildren().addAll(t, m);

                Button revBtn = new Button("Review Applicants →");
                revBtn.getStyleClass().add("btn-secondary");
                revBtn.setOnAction(e -> shell.showCandidateReviewView());

                row.getChildren().addAll(details, revBtn);
                listContainer.getChildren().add(row);
            }
        }

        jobsCard.getChildren().addAll(jHeader, listContainer);

        root.getChildren().addAll(topRow, statsGrid, jobsCard);
        return root;
    }

    private VBox createStatCard(String title, String value, String desc, String accentColor) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        valLbl.setStyle("-fx-text-fill: " + accentColor + ";");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-title");

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(valLbl, titleLbl, descLbl);
        return card;
    }
}
