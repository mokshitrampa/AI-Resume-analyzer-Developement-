package com.resumeanalyzer.ui;

import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class DashboardShell {

    private final Stage stage;
    private BorderPane mainLayout;
    private VBox navLinksBox;
    private final Map<String, Button> navButtons = new HashMap<>();

    public DashboardShell(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #0f172a;");

        // Build Sidebar
        VBox sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        // Load Default View based on role
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            if ("RECRUITER".equals(user.getRole())) {
                showRecruiterDashboard();
            } else if ("ADMIN".equals(user.getRole())) {
                showAdminDashboard();
            } else {
                showStudentDashboard();
            }
        }

        Scene scene = new Scene(mainLayout, 1280, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        return scene;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(260);
        sidebar.getStyleClass().add("sidebar");

        User user = SessionManager.getCurrentUser();

        // Brand
        HBox brandBox = new HBox(8);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 10, 10, 10));
        Label logo = new Label("⚡ ResumeAI");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");
        brandBox.getChildren().add(logo);

        // User Profile Summary Card
        VBox profileCard = new VBox(4);
        profileCard.setStyle("-fx-background-color: #1e293b; -fx-padding: 12px; -fx-background-radius: 10px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 10px;");
        Label roleBadge = new Label(user != null ? user.getRole() + " PORTAL" : "PORTAL");
        roleBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        Label userName = new Label(user != null ? user.getFullName() : "User");
        userName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label userEmail = new Label(user != null ? user.getEmail() : "");
        userEmail.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        profileCard.getChildren().addAll(roleBadge, userName, userEmail);

        // Navigation Links
        navLinksBox = new VBox(6);
        navLinksBox.setPadding(new Insets(10, 0, 10, 0));

        if (user != null && "RECRUITER".equals(user.getRole())) {
            addNavLink("Dashboard", "recruiter_dashboard", this::showRecruiterDashboard);
            addNavLink("Post New Vacancy", "recruiter_post_job", this::showPostJobView);
            addNavLink("Manage Jobs", "recruiter_manage_jobs", this::showManageJobsView);
            addNavLink("Candidate Applications", "recruiter_candidates", this::showCandidateReviewView);
        } else if (user != null && "ADMIN".equals(user.getRole())) {
            addNavLink("System Analytics", "admin_dashboard", this::showAdminDashboard);
            addNavLink("User Directory", "admin_users", this::showUserManagementView);
            addNavLink("Job Moderation", "admin_jobs", this::showJobModerationView);
            addNavLink("Skill Taxonomy", "admin_skills", this::showSkillTaxonomyView);
            addNavLink("Diagnostics & Health", "admin_health", this::showSystemHealthView);
        } else {
            // Student
            addNavLink("Dashboard", "student_dashboard", this::showStudentDashboard);
            addNavLink("Upload Resume", "student_upload", this::showResumeUploadView);
            addNavLink("Resume Quality Analyzer", "student_analyzer", this::showResumeAnalyzerView);
            addNavLink("Job Matcher Engine", "student_matcher", this::showJobMatcherView);
            addNavLink("Skill Gap Roadmap", "student_roadmap", this::showSkillGapRoadmapView);
            addNavLink("Job Recommendations", "student_recommendations", this::showJobRecommendationsView);
            addNavLink("My Applications", "student_applications", this::showMyApplicationsView);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Sign out button
        Button logoutBtn = new Button("Sign Out ➔");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            SessionManager.clear();
            LoginView loginView = new LoginView(stage);
            stage.setScene(loginView.createScene());
        });

        sidebar.getChildren().addAll(brandBox, profileCard, navLinksBox, spacer, logoutBtn);
        return sidebar;
    }

    private void addNavLink(String label, String key, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            setActiveButton(key);
            action.run();
        });
        navButtons.put(key, btn);
        navLinksBox.getChildren().add(btn);
    }

    private void setActiveButton(String activeKey) {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            Button b = entry.getValue();
            b.getStyleClass().removeAll("nav-button", "nav-button-active");
            if (entry.getKey().equals(activeKey)) {
                b.getStyleClass().add("nav-button-active");
            } else {
                b.getStyleClass().add("nav-button");
            }
        }
    }

    public void setContent(Node content, String key) {
        setActiveButton(key);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f172a; -fx-background-color: transparent; -fx-padding: 0;");
        mainLayout.setCenter(scroll);
    }

    // Navigation View Callbacks
    public void showStudentDashboard() {
        setContent(new StudentDashboardView(this).getView(), "student_dashboard");
    }

    public void showResumeUploadView() {
        setContent(new ResumeUploadView(this, stage).getView(), "student_upload");
    }

    public void showResumeAnalyzerView() {
        setContent(new ResumeAnalyzerView(this).getView(), "student_analyzer");
    }

    public void showJobMatcherView() {
        setContent(new JobMatcherView(this).getView(), "student_matcher");
    }

    public void showSkillGapRoadmapView() {
        setContent(new SkillGapRoadmapView(this).getView(), "student_roadmap");
    }

    public void showJobRecommendationsView() {
        setContent(new JobRecommendationsView(this).getView(), "student_recommendations");
    }

    public void showMyApplicationsView() {
        setContent(new MyApplicationsView(this).getView(), "student_applications");
    }

    public void showRecruiterDashboard() {
        setContent(new RecruiterDashboardView(this).getView(), "recruiter_dashboard");
    }

    public void showPostJobView() {
        setContent(new PostJobView(this).getView(), "recruiter_post_job");
    }

    public void showManageJobsView() {
        setContent(new ManageJobsView(this).getView(), "recruiter_manage_jobs");
    }

    public void showCandidateReviewView() {
        setContent(new CandidateReviewView(this).getView(), "recruiter_candidates");
    }

    public void showAdminDashboard() {
        setContent(new AdminDashboardView(this).getView(), "admin_dashboard");
    }

    public void showUserManagementView() {
        setContent(new UserManagementView(this).getView(), "admin_users");
    }

    public void showJobModerationView() {
        setContent(new JobModerationView(this).getView(), "admin_jobs");
    }

    public void showSkillTaxonomyView() {
        setContent(new SkillTaxonomyView(this).getView(), "admin_skills");
    }

    public void showSystemHealthView() {
        setContent(new SystemHealthView(this).getView(), "admin_health");
    }
}
