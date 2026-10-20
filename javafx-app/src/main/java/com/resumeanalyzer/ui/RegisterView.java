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

public class RegisterView {

    private final Stage stage;
    private final UserDao userDao = new UserDao();

    public RegisterView(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(14);
        card.setMaxWidth(500);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 20px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 30px;");

        Label title = new Label("Create Account");
        title.getStyleClass().add("title-large");

        Label subtitle = new Label("Register as a Student Candidate or Corporate Recruiter");
        subtitle.getStyleClass().add("subtitle");

        VBox headerBox = new VBox(4, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (at least 6 characters)");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("STUDENT", "RECRUITER");
        roleCombo.setValue("STUDENT");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField locationField = new TextField();
        locationField.setPromptText("City / Location");

        Button regBtn = new Button("Complete Registration");
        regBtn.getStyleClass().add("btn-primary");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String pass = passField.getText();
            String role = roleCombo.getValue();
            String phone = phoneField.getText();
            String loc = locationField.getText();

            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
                AlertHelper.showError("Validation Error", "Name, email, and password are required.");
                return;
            }

            try {
                if (userDao.emailExists(email)) {
                    AlertHelper.showError("Registration Failed", "This email is already registered. Please sign in.");
                    return;
                }

                User newUser = new User();
                newUser.setFullName(name.trim());
                newUser.setEmail(email.trim().toLowerCase());
                newUser.setPassword(pass.trim());
                newUser.setRole(role);
                newUser.setPhone(phone);
                newUser.setLocation(loc);

                User created = userDao.create(newUser);
                AlertHelper.showSuccess("Account Created", "Your account has been successfully registered! Logging you in...");
                SessionManager.setCurrentUser(created);
                DashboardShell shell = new DashboardShell(stage);
                stage.setScene(shell.createScene());
            } catch (Exception ex) {
                AlertHelper.showError("Registration Error", "Failed to register: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Hyperlink backLink = new Hyperlink("← Already have an account? Sign In");
        backLink.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px;");
        backLink.setOnAction(e -> {
            LoginView loginView = new LoginView(stage);
            stage.setScene(loginView.createScene());
        });

        card.getChildren().addAll(headerBox, nameField, emailField, passField, roleCombo, phoneField, locationField, regBtn, backLink);
        root.setCenter(card);

        Scene scene = new Scene(root, 1024, 720);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}
        return scene;
    }
}
