package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.dao.ResumeDao;
import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.service.ResumeParserService;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResumeUploadView {

    private final DashboardShell shell;
    private final Stage stage;
    private final ResumeParserService parserService = new ResumeParserService();
    private final ResumeDao resumeDao = new ResumeDao();
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();

    private File selectedFile;
    private ExtractedResumeData parsedData;

    // Editable form controls for Human-in-the-loop review
    private TextField nameField = new TextField();
    private TextField emailField = new TextField();
    private TextField phoneField = new TextField();
    private TextField degreeField = new TextField();
    private TextField instField = new TextField();
    private TextField gradYearField = new TextField();
    private TextField cgpaField = new TextField();
    private TextArea skillsArea = new TextArea();
    private Label fileStatusLabel = new Label("No file selected yet");
    private VBox reviewContainer = new VBox(14);

    public ResumeUploadView(DashboardShell shell, Stage stage) {
        this.shell = shell;
        this.stage = stage;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        // Title
        Label title = new Label("Upload & Extract Resume");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Select or drop your PDF / DOCX resume. The engine will extract fields for your review.");
        subtitle.getStyleClass().add("subtitle");

        // Drop Area Card
        VBox dropCard = new VBox(12);
        dropCard.setAlignment(Pos.CENTER);
        dropCard.getStyleClass().add("card");
        dropCard.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 16px; -fx-border-color: #3b82f6; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 16px; -fx-padding: 30px;");

        Label iconLbl = new Label("📂");
        iconLbl.setStyle("-fx-font-size: 36px;");

        Label dropText = new Label("Drag & Drop your Resume here, or Browse your computer");
        dropText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        Label formatHint = new Label("Supports PDF, DOCX (Max 5 MB) • Instant text extraction via Apache PDFBox & POI");
        formatHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Button browseBtn = new Button("Browse Document File...");
        browseBtn.getStyleClass().add("btn-primary");
        browseBtn.setOnAction(e -> handleBrowse());

        fileStatusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

        dropCard.getChildren().addAll(iconLbl, dropText, formatHint, browseBtn, fileStatusLabel);

        // Setup Drag & Drop
        dropCard.setOnDragOver(event -> {
            if (event.getGestureSource() != dropCard && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropCard.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && !db.getFiles().isEmpty()) {
                File file = db.getFiles().get(0);
                processFile(file);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Review Form Container
        reviewContainer.getStyleClass().add("card");
        reviewContainer.setVisible(false);
        reviewContainer.setManaged(false);

        buildReviewForm();

        root.getChildren().addAll(title, subtitle, dropCard, reviewContainer);
        return root;
    }

    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Resume Document");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resume Files (*.pdf, *.docx)", "*.pdf", "*.docx"),
                new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents (*.docx)", "*.docx")
        );

        // Default to samples directory if exists
        File samplesDir = new File("samples");
        if (samplesDir.exists()) {
            chooser.setInitialDirectory(samplesDir);
        }

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            processFile(file);
        }
    }

    private void processFile(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".pdf") && !name.endsWith(".docx")) {
            AlertHelper.showError("Invalid File Format", "Only PDF and DOCX files are supported.");
            return;
        }

        if (file.length() > 5 * 1024 * 1024) {
            AlertHelper.showError("File Too Large", "File size exceeds the 5 MB limit.");
            return;
        }

        this.selectedFile = file;
        fileStatusLabel.setText("Selected: " + file.getName() + " (" + (file.length() / 1024) + " KB) — Parsing content...");

        try {
            parsedData = parserService.parse(file, 0L);

            // Populate form
            nameField.setText(parsedData.getCandidateName() != null ? parsedData.getCandidateName() : "");
            emailField.setText(parsedData.getEmail() != null ? parsedData.getEmail() : "");
            phoneField.setText(parsedData.getPhone() != null ? parsedData.getPhone() : "");
            degreeField.setText(parsedData.getDegree() != null ? parsedData.getDegree() : "");
            instField.setText(parsedData.getInstitution() != null ? parsedData.getInstitution() : "");
            gradYearField.setText(parsedData.getGraduationYear() != null ? String.valueOf(parsedData.getGraduationYear()) : "");
            cgpaField.setText(parsedData.getCgpa() != null ? parsedData.getCgpa() : "");
            skillsArea.setText(String.join(", ", parsedData.getTechnicalSkills()));

            fileStatusLabel.setText("✅ Parsed successfully! Please review and verify the extracted data below.");
            reviewContainer.setVisible(true);
            reviewContainer.setManaged(true);
        } catch (Exception e) {
            AlertHelper.showError("Parsing Error", "Failed to parse document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildReviewForm() {
        Label reviewTitle = new Label("Review & Verify Extracted Resume Data");
        reviewTitle.getStyleClass().add("title-medium");

        Label reviewSubtitle = new Label("Human-in-the-loop validation: Edit any fields before saving to your profile database.");
        reviewSubtitle.getStyleClass().add("subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        grid.add(new Label("Candidate Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Email Address:"), 2, 0);
        grid.add(emailField, 3, 0);

        grid.add(new Label("Phone Number:"), 0, 1);
        grid.add(phoneField, 1, 1);

        grid.add(new Label("Degree / Program:"), 2, 1);
        grid.add(degreeField, 3, 1);

        grid.add(new Label("Institution Name:"), 0, 2);
        grid.add(instField, 1, 2);

        grid.add(new Label("Graduation Year:"), 2, 2);
        grid.add(gradYearField, 3, 2);

        grid.add(new Label("CGPA / Percentage:"), 0, 3);
        grid.add(cgpaField, 1, 3);

        VBox skillsBox = new VBox(6);
        Label skillsLbl = new Label("Detected Technical Skills (comma separated):");
        skillsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        skillsArea.setPrefRowCount(3);
        skillsArea.setWrapText(true);
        skillsBox.getChildren().addAll(skillsLbl, skillsArea);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save & Set as Active Profile →");
        saveBtn.getStyleClass().add("btn-success");
        saveBtn.setOnAction(e -> handleSave());

        btnRow.getChildren().add(saveBtn);

        reviewContainer.getChildren().addAll(reviewTitle, reviewSubtitle, grid, skillsBox, btnRow);
    }

    private void handleSave() {
        User user = SessionManager.getCurrentUser();
        if (user == null || selectedFile == null || parsedData == null) {
            AlertHelper.showError("Save Error", "Please select a valid resume first.");
            return;
        }

        try {
            // 1. Copy file to local uploads directory
            File uploadsDir = new File("./uploads/resumes");
            if (!uploadsDir.exists()) uploadsDir.mkdirs();

            String storedName = UUID.randomUUID() + "_" + selectedFile.getName();
            File destFile = new File(uploadsDir, storedName);

            try (FileInputStream in = new FileInputStream(selectedFile);
                 FileOutputStream out = new FileOutputStream(destFile)) {
                in.transferTo(out);
            }

            // 2. Persist Resume in JDBC
            Resume res = new Resume();
            res.setUserId(user.getId());
            res.setOriginalFilename(selectedFile.getName());
            res.setStoredFilename(storedName);
            res.setFilePath(destFile.getAbsolutePath());
            res.setFileSize(selectedFile.length());
            res.setUploadedAt(LocalDateTime.now());
            Resume savedResume = resumeDao.create(res);

            // 3. Persist ExtractedResumeData in JDBC
            parsedData.setResumeId(savedResume.getId());
            parsedData.setCandidateName(nameField.getText().trim());
            parsedData.setEmail(emailField.getText().trim());
            parsedData.setPhone(phoneField.getText().trim());
            parsedData.setDegree(degreeField.getText().trim());
            parsedData.setInstitution(instField.getText().trim());
            try {
                parsedData.setGraduationYear(Integer.parseInt(gradYearField.getText().trim()));
            } catch (Exception ignored) {}
            parsedData.setCgpa(cgpaField.getText().trim());

            List<String> updatedSkills = Arrays.stream(skillsArea.getText().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            parsedData.setTechnicalSkills(updatedSkills);

            extractedDataDao.save(parsedData);

            // Set as active resume
            SessionManager.setActiveResume(savedResume);

            AlertHelper.showSuccess("Resume Saved", "Resume and verified skills stored in database via JDBC!");
            shell.showStudentDashboard();

        } catch (Exception ex) {
            AlertHelper.showError("Database Error", "Failed to save resume: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
