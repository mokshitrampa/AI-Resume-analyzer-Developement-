package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.UserDao;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView {

    private final Stage stage;
    private final UserDao userDao = new UserDao();

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(18);
        card.setMaxWidth(460);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 20px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 35px;");

        // Header
        Label logoBadge = new Label("⚡ RESUMEAI INTELLIGENCE");
        logoBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-background-color: rgba(56, 189, 248, 0.15); -fx-padding: 4px 10px; -fx-background-radius: 9999px;");

        Label title = new Label("Welcome Back");
        title.getStyleClass().add("title-large");

        Label subtitle = new Label("Sign in to your intelligent career matching portal");
        subtitle.getStyleClass().add("subtitle");

        VBox headerBox = new VBox(6, logoBadge, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Form Fields
        VBox emailBox = new VBox(6);
        Label emailLabel = new Label("EMAIL ADDRESS");
        emailLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. student@test.com");
        emailBox.getChildren().addAll(emailLabel, emailField);

        VBox passBox = new VBox(6);
        Label passLabel = new Label("PASSWORD");
        passLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passBox.getChildren().addAll(passLabel, passField);

        // Sign In Button
        Button loginBtn = new Button("Sign In to Portal →");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String pass = passField.getText();
            if (email == null || email.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
                AlertHelper.showError("Validation Error", "Please enter both email and password.");
                return;
            }
            try {
                User user = userDao.authenticate(email, pass);
                if (user != null) {
                    SessionManager.setCurrentUser(user);
                    DashboardShell shell = new DashboardShell(stage);
                    stage.setScene(shell.createScene());
                } else {
                    AlertHelper.showError("Authentication Failed", "Invalid email address or password. Please check your credentials.");
                }
            } catch (Exception ex) {
                AlertHelper.showError("Database Error", "Failed to connect to database: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Quick Demo Accounts Fillers
        Label demoLabel = new Label("QUICK DEMO ONE-CLICK CREDENTIALS");
        demoLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        HBox demoBox = new HBox(8);
        demoBox.setAlignment(Pos.CENTER);

        Button studentDemo = new Button("Student");
        studentDemo.setStyle("-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
        studentDemo.setOnAction(e -> {
            emailField.setText("student@test.com");
            passField.setText("Password@123");
        });

        Button recruiterDemo = new Button("Recruiter");
        recruiterDemo.setStyle("-fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #60a5fa; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
        recruiterDemo.setOnAction(e -> {
            emailField.setText("recruiter@tech.com");
            passField.setText("Password@123");
        });

        Button adminDemo = new Button("Admin");
        adminDemo.setStyle("-fx-background-color: rgba(168, 85, 247, 0.15); -fx-text-fill: #c084fc; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
        adminDemo.setOnAction(e -> {
            emailField.setText("admin@resumeanalyzer.com");
            passField.setText("Admin@123456");
        });

        demoBox.getChildren().addAll(studentDemo, recruiterDemo, adminDemo);

        // Register link
        HBox footerBox = new HBox(6);
        footerBox.setAlignment(Pos.CENTER);
        Label noAccLabel = new Label("Don't have an account?");
        noAccLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Hyperlink regLink = new Hyperlink("Create an Account");
        regLink.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;");
        regLink.setOnAction(e -> {
            RegisterView regView = new RegisterView(stage);
            stage.setScene(regView.createScene());
        });
        footerBox.getChildren().addAll(noAccLabel, regLink);

        card.getChildren().addAll(headerBox, emailBox, passBox, loginBtn, demoLabel, demoBox, footerBox);
        root.setCenter(card);

        Scene scene = new Scene(root, 1024, 720);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        return scene;
    }
}
