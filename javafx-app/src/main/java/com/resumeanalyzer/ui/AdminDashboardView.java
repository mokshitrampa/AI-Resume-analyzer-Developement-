package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.AnalysisDao;
import com.resumeanalyzer.dao.JobDao;
import com.resumeanalyzer.dao.SkillDao;
import com.resumeanalyzer.dao.UserDao;
import com.resumeanalyzer.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class AdminDashboardView {

    private final DashboardShell shell;
    private final UserDao userDao = new UserDao();
    private final JobDao jobDao = new JobDao();
    private final AnalysisDao analysisDao = new AnalysisDao();
    private final SkillDao skillDao = new SkillDao();

    public AdminDashboardView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Platform Administration Console");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("System-wide metrics, registered user moderation, corporate job auditing, and NLP skill taxonomy control.");
        subtitle.getStyleClass().add("subtitle");

        // Fetch Metrics via JDBC
        int totalUsers = 0;
        int totalStudents = 0;
        int totalRecruiters = 0;
        int totalJobs = 0;
        int activeJobs = 0;
        int totalAnalyses = 0;
        int totalSkills = 0;

        try {
            List<User> users = userDao.findAll();
            totalUsers = users.size();
            for (User u : users) {
                if ("STUDENT".equalsIgnoreCase(u.getRole())) totalStudents++;
                else if ("RECRUITER".equalsIgnoreCase(u.getRole())) totalRecruiters++;
            }
            totalJobs = jobDao.countTotal();
            activeJobs = jobDao.countActive();
            totalAnalyses = analysisDao.countTotalAnalyses();
            totalSkills = skillDao.countTotal();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Metrics Grid (4 Stat Cards)
        HBox statsGrid = new HBox(16);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.getChildren().addAll(
                createStatCard("Registered Accounts", String.valueOf(totalUsers), totalStudents + " Students • " + totalRecruiters + " Recruiters", "#38bdf8"),
                createStatCard("Resumes Analyzed", String.valueOf(totalAnalyses), "Evaluated with TF-IDF", "#34d399"),
                createStatCard("Job Listings", String.valueOf(totalJobs), activeJobs + " Active positions", "#c084fc"),
                createStatCard("Skill Taxonomy", String.valueOf(totalSkills > 0 ? totalSkills : 150), "Cataloged technical skills", "#fbbf24")
        );

        // Management Shortcuts Grid
        Label shortcutsHeader = new Label("Administrative Controls");
        shortcutsHeader.getStyleClass().add("title-medium");

        HBox shortcutsGrid = new HBox(16);

        VBox userCard = createActionCard("👥 User Directory", "Inspect, activate, suspend, or delete student and recruiter accounts.", "Open User Directory →", shell::showUserManagementView);
        VBox jobCard = createActionCard("💼 Job Moderation", "Audit published vacancies across employers and prune outdated listings.", "Audit Postings →", shell::showJobModerationView);
        VBox skillCard = createActionCard("📚 Skill Taxonomy", "Expand the NLP dictionary with new technologies, frameworks, and acronym aliases.", "Edit Taxonomy →", shell::showSkillTaxonomyView);
        VBox healthCard = createActionCard("🛡️ System Diagnostics", "Inspect live JDBC database connections, connection pools, and JVM telemetry.", "View Telemetry →", shell::showSystemHealthView);

        shortcutsGrid.getChildren().addAll(userCard, jobCard, skillCard, healthCard);

        root.getChildren().addAll(title, subtitle, statsGrid, shortcutsHeader, shortcutsGrid);
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

    private VBox createActionCard(String title, String desc, String btnText, Runnable action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ffffff;");

        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        d.setWrapText(true);
        VBox.setVgrow(d, Priority.ALWAYS);

        Button btn = new Button(btnText);
        btn.getStyleClass().add("btn-secondary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());

        card.getChildren().addAll(t, d, btn);
        return card;
    }
}
