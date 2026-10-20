package com.resumeanalyzer.service;

import com.resumeanalyzer.model.ExtractedResumeData;
import com.resumeanalyzer.model.Job;
import com.resumeanalyzer.model.JobMatchResult;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class JobMatchingEngine {

    private final TfIdfVectorizer vectorizer = new TfIdfVectorizer();
    private final CosineSimilarity cosineSimilarity = new CosineSimilarity();
    private final SkillExtractionService skillExtractionService = new SkillExtractionService();

    public JobMatchResult match(ExtractedResumeData resume, Job job) {
        JobMatchResult result = new JobMatchResult();
        result.setResumeId(resume.getResumeId());
        result.setJobId(job.getId());
        result.setJobTitle(job.getTitle());
        result.setCompanyName(job.getCompanyName());

        // 1. Candidate skills set (lowercase for case-insensitive matching)
        Set<String> candidateSkills = resume.getTechnicalSkills().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());

        // 2. Required skills match (Base weight: 40%)
        List<String> reqSkills = job.getRequiredSkills() != null ? job.getRequiredSkills() : Collections.emptyList();
        int reqScore = 100;
        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (!reqSkills.isEmpty()) {
            int matchCount = 0;
            for (String req : reqSkills) {
                if (candidateSkills.contains(req.toLowerCase())) {
                    matchCount++;
                    matching.add(req);
                } else {
                    missing.add(req);
                }
            }
            reqScore = (int) Math.round(((double) matchCount / reqSkills.size()) * 100.0);
        }
        result.setRequiredSkillsScore(reqScore);
        result.setMatchingSkills(matching);
        result.setMissingSkills(missing);

        // 3. Preferred skills match (Base weight: 20%)
        List<String> prefSkills = job.getPreferredSkills() != null ? job.getPreferredSkills() : Collections.emptyList();
        boolean hasPref = !prefSkills.isEmpty();
        int prefScore = 100;
        if (hasPref) {
            int prefMatch = 0;
            for (String pref : prefSkills) {
                if (candidateSkills.contains(pref.toLowerCase())) {
                    prefMatch++;
                    if (!matching.contains(pref)) matching.add(pref);
                }
            }
            prefScore = (int) Math.round(((double) prefMatch / prefSkills.size()) * 100.0);
        }
        result.setPreferredSkillsScore(prefScore);

        // 4. TF-IDF & Cosine Similarity (Base weight: 20%)
        String resumeFullText = (resume.getRawText() != null ? resume.getRawText() : "") + " " + String.join(" ", resume.getTechnicalSkills());
        String jobFullText = (job.getTitle() != null ? job.getTitle() : "") + " " +
                (job.getDescription() != null ? job.getDescription() : "") + " " +
                String.join(" ", reqSkills) + " " + String.join(" ", prefSkills);

        Map<String, Double> vecResume = vectorizer.computeTfIdf(resumeFullText, jobFullText);
        Map<String, Double> vecJob = vectorizer.computeTfIdf(jobFullText, resumeFullText);
        double cosine = cosineSimilarity.calculate(vecResume, vecJob);
        int semanticScore = (int) Math.round(cosine * 100.0);
        result.setSemanticScore(semanticScore);

        // 5. Experience Alignment (Base weight: 10%)
        boolean hasExpReq = job.getMinExperienceYears() > 0;
        int expScore = 80;
        if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
            expScore = Math.min(100, 70 + (resume.getExperience().size() * 10));
        }
        result.setExperienceScore(expScore);

        // 6. Education Alignment (Base weight: 10%)
        int eduScore = 75;
        if (resume.getDegree() != null && !resume.getDegree().trim().isEmpty()) {
            eduScore = 90;
            if (resume.getCgpa() != null && !resume.getCgpa().trim().isEmpty()) {
                eduScore = 95;
            }
        }
        result.setEducationScore(eduScore);

        // 7. Dynamic Weight Redistribution
        double baseReqW = 0.40;
        double basePrefW = hasPref ? 0.20 : 0.0;
        double baseSemanticW = 0.20;
        double baseExpW = hasExpReq ? 0.10 : 0.05;
        double baseEduW = 0.10;

        double totalActiveWeight = baseReqW + basePrefW + baseSemanticW + baseExpW + baseEduW;

        double effReqW = baseReqW / totalActiveWeight;
        double effPrefW = basePrefW / totalActiveWeight;
        double effSemanticW = baseSemanticW / totalActiveWeight;
        double effExpW = baseExpW / totalActiveWeight;
        double effEduW = baseEduW / totalActiveWeight;

        double ptsReq = reqScore * effReqW;
        double ptsPref = prefScore * effPrefW;
        double ptsSemantic = semanticScore * effSemanticW;
        double ptsExp = expScore * effExpW;
        double ptsEdu = eduScore * effEduW;

        double finalScore = ptsReq + ptsPref + ptsSemantic + ptsExp + ptsEdu;
        int overall = (int) Math.round(Math.min(100.0, Math.max(0.0, finalScore)));
        result.setOverallScore(overall);

        // Component breakdown
        List<JobMatchResult.ScoreComponent> comps = new ArrayList<>();
        comps.add(new JobMatchResult.ScoreComponent("Required Skills Match", reqScore, Math.round(effReqW * 1000.0) / 10.0, Math.round(ptsReq * 10.0) / 10.0));
        if (hasPref) {
            comps.add(new JobMatchResult.ScoreComponent("Preferred Skills Match", prefScore, Math.round(effPrefW * 1000.0) / 10.0, Math.round(ptsPref * 10.0) / 10.0));
        }
        comps.add(new JobMatchResult.ScoreComponent("TF-IDF Semantic Similarity", semanticScore, Math.round(effSemanticW * 1000.0) / 10.0, Math.round(ptsSemantic * 10.0) / 10.0));
        comps.add(new JobMatchResult.ScoreComponent("Experience Match", expScore, Math.round(effExpW * 1000.0) / 10.0, Math.round(ptsExp * 10.0) / 10.0));
        comps.add(new JobMatchResult.ScoreComponent("Education & Degree", eduScore, Math.round(effEduW * 1000.0) / 10.0, Math.round(ptsEdu * 10.0) / 10.0));
        result.setComponents(comps);

        // Human-readable explanation
        StringBuilder sb = new StringBuilder();
        sb.append("Explainable Job Match Breakdown:\n");
        for (JobMatchResult.ScoreComponent c : comps) {
            sb.append(String.format("• %s: Score %d%% × Weight %.1f%% = +%.1f pts\n",
                    c.getName(), c.getRawScore(), c.getEffectiveWeightPercentage(), c.getContributedPoints()));
        }
        sb.append(String.format("Final Composite Calculated Score: %d / 100", overall));
        result.setExplanation(sb.toString());
        result.setMatchedAt(LocalDateTime.now());

        return result;
    }
}
