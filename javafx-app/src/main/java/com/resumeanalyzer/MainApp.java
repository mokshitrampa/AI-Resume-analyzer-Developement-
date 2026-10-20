package com.resumeanalyzer;

import com.resumeanalyzer.dao.DatabaseManager;
import com.resumeanalyzer.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp: JavaFX Application Entry point.
 * Initializes JDBC database tables and seed data, then displays the Login interface.
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize JDBC Database schemas and seed data automatically
        System.out.println("Initializing JDBC Database and seeding initial records...");
        DatabaseManager.initializeDatabase();
        System.out.println("JDBC Database schema verified and ready.");
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AI-Powered Resume Analyzer & Job Matcher (JavaFX + JDBC Edition)");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);

        LoginView loginView = new LoginView(primaryStage);
        Scene scene = loginView.createScene();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
