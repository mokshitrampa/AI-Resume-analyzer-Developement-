package com.resumeanalyzer.ui;

import com.resumeanalyzer.dao.AnalysisDao;
import com.resumeanalyzer.dao.ExtractedDataDao;
import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.ResumeAnalysis;
import com.resumeanalyzer.service.ResumeAnalysisService;
import com.resumeanalyzer.service.ResumeImprovementAssistant;
import com.resumeanalyzer.util.AlertHelper;
import com.resumeanalyzer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ResumeAnalyzerView {

    private final DashboardShell shell;
    private final ExtractedDataDao extractedDataDao = new ExtractedDataDao();
    private final AnalysisDao analysisDao = new AnalysisDao();
    private final ResumeAnalysisService analysisService = new ResumeAnalysisService();
    private final ResumeImprovementAssistant improvementAssistant = new ResumeImprovementAssistant();

    public ResumeAnalyzerView(DashboardShell shell) {
        this.shell = shell;
    }

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("Resume Quality Analyzer");
        title.getStyleClass().add("title-large");
        Label subtitle = new Label("Deterministic 0–100 rubric evaluation across 7 key recruitment dimensions with phrasing optimization.");
        subtitle.getStyleClass().add("subtitle");

        Resume activeResume = SessionManager.getActiveResume();
        if (activeResume == null) {
            VBox emptyCard = new VBox(12);
            emptyCard.setAlignment(Pos.CENTER);
            emptyCard.getStyleClass().add("card");
            emptyCard.setPadding(new Insets(40));

            Label noRes = new Label("Please upload or select a resume first to run quality analysis.");
            noRes.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            Button uploadBtn = new Button("Go to Resume Upload →");
            uploadBtn.getStyleClass().add("btn-primary");
            uploadBtn.setOnAction(e -> shell.showResumeUploadView());
            emptyCard.getChildren().addAll(noRes, uploadBtn);

            root.getChildren().addAll(title, subtitle, emptyCard);
            return root;
        }

        ExtractedResumeData extractedData = null;
        try {
            extractedData = extractedDataDao.findByResumeId(activeResume.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (extractedData == null) {
            AlertHelper.showError("Data Error", "Could not load extracted text for this resume.");
            root.getChildren().addAll(title, subtitle);
            return root;
        }

        // Run Analysis or Fetch Cached from JDBC
        ResumeAnalysis analysis = null;
        try {
            analysis = analysisDao.findLatestByResumeId(activeResume.getId());
            if (analysis == null) {
                analysis = analysisService.analyze(extractedData);
                analysisDao.save(analysis);
            }
        } catch (Exception e) {
            analysis = analysisService.analyze(extractedData);
        }

        // Score Hero Card
        HBox scoreHero = new HBox(24);
        scoreHero.setAlignment(Pos.CENTER_LEFT);
        scoreHero.getStyleClass().add("card");
        scoreHero.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 16px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 24px;");

        // Score Meter Circle / Gauge
        VBox scoreMeter = new VBox(4);
        scoreMeter.setAlignment(Pos.CENTER);
        scoreMeter.setStyle("-fx-background-color: #0f172a; -fx-padding: 16px; -fx-background-radius: 9999px; -fx-min-width: 110px; -fx-min-height: 110px; -fx-border-color: #3b82f6; -fx-border-width: 3px; -fx-border-radius: 9999px;");
        Label scoreVal = new Label(String.valueOf(analysis.getOverallScore()));
        scoreVal.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #38bdf8;");
        Label scoreMax = new Label("/ 100");
        scoreMax.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        scoreMeter.getChildren().addAll(scoreVal, scoreMax);

        VBox scoreInfo = new VBox(6);
        Label scoreHeader = new Label("Overall Estimated Quality Score: " + analysis.getOverallScore() + "%");
        scoreHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label scoreDesc = new Label("Document: " + activeResume.getOriginalFilename() + " • Evaluated deterministically against university hiring standards.");
        scoreDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        String ratingText = analysis.getOverallScore() >= 85 ? "Excellent Candidate Profile" :
                analysis.getOverallScore() >= 70 ? "Good Profile with Minor Action Items" : "Profile Requires Additional Technical Expansion";
        Label ratingBadge = new Label("★ " + ratingText);
        ratingBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #34d399; -fx-background-color: rgba(16, 185, 129, 0.15); -fx-padding: 4px 10px; -fx-background-radius: 6px;");

        scoreInfo.getChildren().addAll(scoreHeader, scoreDesc, ratingBadge);
        scoreHero.getChildren().addAll(scoreMeter, scoreInfo);

        // 7 Category Progress Bars
        VBox rubricCard = new VBox(12);
        rubricCard.getStyleClass().add("card");

        Label rubricTitle = new Label("Category-Wise Performance Breakdown");
        rubricTitle.getStyleClass().add("title-medium");

        GridPane catGrid = new GridPane();
        catGrid.setHgap(16);
        catGrid.setVgap(12);

        addCategoryBar(catGrid, 0, "Completeness", analysis.getCompletenessScore());
        addCategoryBar(catGrid, 1, "Technical Breadth", analysis.getSkillCoverageScore());
        addCategoryBar(catGrid, 2, "Education & Performance", analysis.getEducationScore());
        addCategoryBar(catGrid, 3, "Work Experience", analysis.getExperienceScore());
        addCategoryBar(catGrid, 4, "Applied Project Portfolio", analysis.getProjectScore());
        addCategoryBar(catGrid, 5, "Formatting & Length", analysis.getFormattingScore());
        addCategoryBar(catGrid, 6, "Keyword Relevance", analysis.getKeywordRelevanceScore());

        rubricCard.getChildren().addAll(rubricTitle, catGrid);

        // Strengths & Weaknesses Split
        HBox feedbackSplit = new HBox(16);

        VBox strengthsCard = new VBox(10);
        strengthsCard.getStyleClass().add("card");
        strengthsCard.setPrefWidth(550);
        HBox.setHgrow(strengthsCard, Priority.ALWAYS);

        Label stTitle = new Label("Detected Profile Strengths (" + analysis.getStrengths().size() + ")");
        stTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399; -fx-font-size: 14px;");
        strengthsCard.getChildren().add(stTitle);

        for (String s : analysis.getStrengths()) {
            Label item = new Label("✔ " + s);
            item.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");
            item.setWrapText(true);
            strengthsCard.getChildren().add(item);
        }

        VBox weakCard = new VBox(10);
        weakCard.getStyleClass().add("card");
        weakCard.setPrefWidth(550);
        HBox.setHgrow(weakCard, Priority.ALWAYS);

        Label wkTitle = new Label("Opportunities for Improvement");
        wkTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #fbbf24; -fx-font-size: 14px;");
        weakCard.getChildren().add(wkTitle);

        for (String w : analysis.getWeaknesses()) {
            Label item = new Label("⚠ " + w);
            item.setStyle("-fx-font-size: 12px; -fx-text-fill: #fca5a5;");
            item.setWrapText(true);
            weakCard.getChildren().add(item);
        }
        for (String m : analysis.getMissingInfo()) {
            Label item = new Label("• Missing section: " + m);
            item.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            weakCard.getChildren().add(item);
        }

        feedbackSplit.getChildren().addAll(strengthsCard, weakCard);

        // AI Phrasing Improvement Assistant Section
        VBox phrasingCard = new VBox(14);
        phrasingCard.getStyleClass().add("card");

        Label aiTitle = new Label("AI Phrasing & Action-Verb Enhancer");
        aiTitle.getStyleClass().add("title-medium");
        Label aiSubtitle = new Label("Side-by-side comparison transforming passive statements into quantifiable, action-oriented bullet points.");
        aiSubtitle.getStyleClass().add("subtitle");

        VBox suggestionsBox = new VBox(12);
        List<ResumeImprovementAssistant.PhrasingSuggestion> suggestions =
                improvementAssistant.generateSuggestions(extractedData.getExperience());

        for (ResumeImprovementAssistant.PhrasingSuggestion ps : suggestions) {
            VBox itemBox = new VBox(6);
            itemBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px; -fx-background-radius: 10px; -fx-border-color: #334155; -fx-border-width: 1px; -fx-border-radius: 10px;");

            Label origLbl = new Label("Original Bullet: \"" + ps.getOriginalBullet() + "\"");
            origLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            origLbl.setWrapText(true);

            Label sugLbl = new Label("✨ Suggested Enhancement: \"" + ps.getSuggestedBullet() + "\"");
            sugLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");
            sugLbl.setWrapText(true);

            Label ratLbl = new Label("Rationale: " + ps.getRationale());
            ratLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            ratLbl.setWrapText(true);

            itemBox.getChildren().addAll(origLbl, sugLbl, ratLbl);
            suggestionsBox.getChildren().add(itemBox);
        }

        phrasingCard.getChildren().addAll(aiTitle, aiSubtitle, suggestionsBox);

        root.getChildren().addAll(title, subtitle, scoreHero, rubricCard, feedbackSplit, phrasingCard);
        return root;
    }

    private void addCategoryBar(GridPane grid, int row, String name, int score) {
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1; -fx-min-width: 180px;");

        ProgressBar pb = new ProgressBar(score / 100.0);
        pb.setPrefWidth(400);

        Label scoreLbl = new Label(score + "%");
        scoreLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        grid.add(nameLbl, 0, row);
        grid.add(pb, 1, row);
        grid.add(scoreLbl, 2, row);
    }
}
