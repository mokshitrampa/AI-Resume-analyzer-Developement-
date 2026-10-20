package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * SystemHealthView: Admin diagnostics showing JDBC connection details,
 * database metadata, table metrics, and JVM runtime diagnostics.
 */
public class SystemHealthView {

    private final DashboardShell shell;

    public SystemHealthView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("viewport");
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #0f172a;");

        // Header
        VBox headerBox = new VBox(6);
        Label title = new Label("System Health & JDBC Diagnostics");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Real-time telemetry, database connection parameters, and runtime memory diagnostics.");
        subtitle.getStyleClass().add("subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Grid for info cards
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Card 1: JDBC & Database Metadata
        VBox dbCard = createDatabaseInfoCard();
        grid.add(dbCard, 0, 0);

        // Card 2: JVM & Runtime Diagnostics
        VBox jvmCard = createJvmInfoCard();
        grid.add(jvmCard, 1, 0);

        // Card 3: Database Table Statistics (Record counts via JDBC)
        VBox statsCard = createTableStatsCard();
        grid.add(statsCard, 0, 1, 2, 1);

        content.getChildren().addAll(headerBox, grid);
        scrollPane.setContent(content);
        return scrollPane;
    }

    private VBox createDatabaseInfoCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label cardTitle = new Label("JDBC Connection Telemetry");
        cardTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        String dbProduct = "Unknown";
        String dbVersion = "Unknown";
        String driverName = "Unknown";
        String driverVersion = "Unknown";
        String dbUrl = "Unknown";
        String userName = "Unknown";
        boolean autoCommit = false;
        boolean isReadOnly = false;
        String status = "OFFLINE";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                status = "ACTIVE (Connected)";
                DatabaseMetaData meta = conn.getMetaData();
                dbProduct = meta.getDatabaseProductName();
                dbVersion = meta.getDatabaseProductVersion();
                driverName = meta.getDriverName();
                driverVersion = meta.getDriverVersion();
                dbUrl = meta.getURL();
                userName = meta.getUserName();
                autoCommit = conn.getAutoCommit();
                isReadOnly = conn.isReadOnly();
            }
        } catch (Exception e) {
            status = "ERROR: " + e.getMessage();
        }

        card.getChildren().addAll(
                cardTitle,
                createMetricRow("Connection Status", status, status.startsWith("ACTIVE") ? "#10b981" : "#ef4444"),
                createMetricRow("Database Engine", dbProduct, "#38bdf8"),
                createMetricRow("Engine Version", dbVersion, "#94a3b8"),
                createMetricRow("JDBC Driver", driverName, "#cbd5e1"),
                createMetricRow("Driver Version", driverVersion, "#94a3b8"),
                createMetricRow("Connection URL", dbUrl, "#a78bfa"),
                createMetricRow("Connected User", userName, "#94a3b8"),
                createMetricRow("Auto-Commit", String.valueOf(autoCommit), "#cbd5e1"),
                createMetricRow("Read-Only Mode", String.valueOf(isReadOnly), "#cbd5e1")
        );

        return card;
    }

    private VBox createJvmInfoCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label cardTitle = new Label("JVM & Process Telemetry");
        cardTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        long usedHeapMb = heapUsage.getUsed() / (1024 * 1024);
        long maxHeapMb = heapUsage.getMax() / (1024 * 1024);
        long committedHeapMb = heapUsage.getCommitted() / (1024 * 1024);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int activeThreads = Thread.activeCount();
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        String osName = System.getProperty("os.name") + " " + System.getProperty("os.arch");

        card.getChildren().addAll(
                cardTitle,
                createMetricRow("Java Runtime Version", javaVersion, "#38bdf8"),
                createMetricRow("Java VM Vendor", javaVendor, "#94a3b8"),
                createMetricRow("Operating System", osName, "#cbd5e1"),
                createMetricRow("Allocated Processors", availableProcessors + " Cores", "#10b981"),
                createMetricRow("Active Threads", String.valueOf(activeThreads), "#f59e0b"),
                createMetricRow("Heap Memory Used", usedHeapMb + " MB", "#38bdf8"),
                createMetricRow("Heap Memory Committed", committedHeapMb + " MB", "#94a3b8"),
                createMetricRow("Heap Memory Max", maxHeapMb + " MB", "#94a3b8")
        );

        return card;
    }

    private VBox createTableStatsCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label cardTitle = new Label("Relational Database Entities & Record Metrics");
        cardTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        UserDao userDao = new UserDao();
        JobDao jobDao = new JobDao();
        SkillDao skillDao = new SkillDao();
        AnalysisDao analysisDao = new AnalysisDao();

        int usersCount = 0;
        int jobsCount = 0;
        int skillsCount = 0;
        int analysesCount = 0;

        try {
            usersCount = userDao.findAll().size();
            jobsCount = jobDao.countTotal();
            skillsCount = skillDao.countTotal();
            analysesCount = analysisDao.countTotalAnalyses();
        } catch (Exception e) {
            System.err.println("Error fetching table counts: " + e.getMessage());
        }

        HBox statsRow = new HBox(30);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                createMiniStat("Registered Users", String.valueOf(usersCount), "#38bdf8"),
                createMiniStat("Active Job Postings", String.valueOf(jobsCount), "#10b981"),
                createMiniStat("Taxonomy Skills", String.valueOf(skillsCount), "#a78bfa"),
                createMiniStat("Evaluated Analyses", String.valueOf(analysesCount), "#f59e0b")
        );

        Label notice = new Label("Direct JDBC queries executed via java.sql.PreparedStatement and java.sql.ResultSet without ORM overhead.");
        notice.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-style: italic;");

        card.getChildren().addAll(cardTitle, statsRow, notice);
        return card;
    }

    private HBox createMetricRow(String label, String value, String valueColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-min-width: 170px;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(lbl, val);
        return row;
    }

    private VBox createMiniStat(String label, String value, String color) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color: #0f172a; -fx-padding: 12 20; -fx-background-radius: 8; -fx-border-color: #334155; -fx-border-radius: 8;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        box.getChildren().addAll(val, lbl);
        return box;
    }
}
