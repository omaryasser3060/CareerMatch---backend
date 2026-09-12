package com.example.backend.service.ai;

import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.dto.ai.JobRequirementsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ScoringService {

    /*
     * Final score = 100%
     *
     * Required Skills       = 40%
     * Experience            = 20%
     * Projects / Evidence   = 15%
     * Education             = 10%
     * Preferred Skills      = 5%
     * Semantic Similarity   = 10%
     */
    private static final double REQUIRED_WEIGHT = 0.40;
    private static final double EXPERIENCE_WEIGHT = 0.20;
    private static final double PROJECTS_WEIGHT = 0.15;
    private static final double EDUCATION_WEIGHT = 0.10;
    private static final double PREFERRED_WEIGHT = 0.05;
    private static final double SEMANTIC_WEIGHT = 0.10;

    public int calculateMatchScore(
            CandidateProfileDto candidate,
            JobRequirementsDto job,
            List<String> candidateSkills,
            List<String> requiredSkills,
            List<String> preferredSkills,
            double semanticSimilarity
    ) {

        double requiredCoverage =
                calculateCoverage(
                        candidateSkills,
                        requiredSkills
                );

        double preferredCoverage =
                calculateCoverage(
                        candidateSkills,
                        preferredSkills
                );

        double experienceScore =
                calculateExperienceScore(
                        candidate,
                        job
                );

        double projectsScore =
                calculateProjectsScore(
                        candidate,
                        requiredSkills
                );

        double educationScore =
                calculateEducationScore(
                        candidate,
                        job
                );

        double semanticScore =
                clamp(semanticSimilarity);

        /*
         * Only score dimensions that are actually supported by the job profile.
         * This prevents an Adzuna job with no explicit skill/education/experience
         * requirements from receiving free points for requirements that do not exist.
         * The available weights are normalized back to 100%.
         */
        double weightedScore = 0.0;
        double availableWeight = 0.0;

        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            weightedScore += requiredCoverage * REQUIRED_WEIGHT;
            availableWeight += REQUIRED_WEIGHT;

            weightedScore += projectsScore * PROJECTS_WEIGHT;
            availableWeight += PROJECTS_WEIGHT;
        }

        if (job != null
                && job.getExperienceMonths() != null
                && job.getExperienceMonths() > 0) {
            weightedScore += experienceScore * EXPERIENCE_WEIGHT;
            availableWeight += EXPERIENCE_WEIGHT;
        }

        if (job != null
                && job.getEducationRequirements() != null
                && !job.getEducationRequirements().isEmpty()) {
            weightedScore += educationScore * EDUCATION_WEIGHT;
            availableWeight += EDUCATION_WEIGHT;
        }

        if (preferredSkills != null && !preferredSkills.isEmpty()) {
            weightedScore += preferredCoverage * PREFERRED_WEIGHT;
            availableWeight += PREFERRED_WEIGHT;
        }

        if (Double.isFinite(semanticSimilarity)) {
            weightedScore += semanticScore * SEMANTIC_WEIGHT;
            availableWeight += SEMANTIC_WEIGHT;
        }

        double score =
                availableWeight > 0.0
                        ? weightedScore / availableWeight
                        : 0.0;

        int finalScore =
                (int) Math.round(score * 100);

        finalScore = Math.max(
                0,
                Math.min(100, finalScore)
        );

        log.debug(
                "Match score calculated: required={}, experience={}, " +
                        "projects={}, education={}, preferred={}, " +
                        "semantic={}, final={}",
                requiredCoverage,
                experienceScore,
                projectsScore,
                educationScore,
                preferredCoverage,
                semanticScore,
                finalScore
        );

        return finalScore;
    }

    /**
     * Calculates how much of the required experience
     * the candidate actually has.
     */
    private double calculateExperienceScore(
            CandidateProfileDto candidate,
            JobRequirementsDto job
    ) {

        if (candidate == null || job == null) {
            return 0.0;
        }

        Integer candidateMonths =
                candidate.getExperienceMonths();

        Integer requiredMonths =
                job.getExperienceMonths();

        /*
         * If the job does not explicitly require experience,
         * experience should not hurt the candidate.
         */
        if (requiredMonths == null || requiredMonths <= 0) {
            return 1.0;
        }

        if (candidateMonths == null || candidateMonths <= 0) {
            return 0.0;
        }

        return clamp(
                (double) candidateMonths / requiredMonths
        );
    }

    /**
     * Measures whether the candidate has project evidence
     * demonstrating the required skills.
     *
     * This is intentionally separate from skill matching.
     */
    private double calculateProjectsScore(
            CandidateProfileDto candidate,
            List<String> requiredSkills
    ) {

        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 1.0;
        }

        if (candidate == null
                || candidate.getProjects() == null
                || candidate.getProjects().isEmpty()) {
            return 0.0;
        }

        long demonstratedRequirements =
                requiredSkills.stream()
                        .filter(required ->
                                required != null
                                        && !required.isBlank()
                                        && candidate.getProjects()
                                        .stream()
                                        .anyMatch(project ->
                                                project != null
                                                        && project.getSkillsEvidence() != null
                                                        && project.getSkillsEvidence()
                                                        .stream()
                                                        .anyMatch(skill ->
                                                                skillsMatch(
                                                                        skill,
                                                                        required
                                                                )
                                                        )
                                        )
                        )
                        .count();

        return clamp(
                (double) demonstratedRequirements
                        / requiredSkills.size()
        );
    }

    /**
     * Calculates education compatibility.
     *
     * V1 intentionally uses text matching because the LLM
     * already converts education requirements into structured text.
     */
    private double calculateEducationScore(
            CandidateProfileDto candidate,
            JobRequirementsDto job
    ) {

        if (job == null) {
            return 0.0;
        }

        List<String> requirements =
                job.getEducationRequirements();

        if (requirements == null || requirements.isEmpty()) {
            return 1.0;
        }

        if (candidate == null
                || candidate.getEducation() == null) {
            return 0.0;
        }

        String candidateEducation =
                (
                        safe(candidate.getEducation().getDegree())
                                + " "
                                + safe(candidate.getEducation().getLevel())
                                + " "
                                + safe(candidate.getEducation().getField())
                                + " "
                                + safe(candidate.getEducation().getInstitution())
                ).toLowerCase(Locale.ROOT);

        long matched =
                requirements.stream()
                        .filter(req ->
                                req != null
                                        && !req.isBlank()
                                        && educationMatches(
                                        candidateEducation,
                                        req
                                )
                        )
                        .count();

        return clamp(
                (double) matched / requirements.size()
        );
    }

    /**
     * Calculates skill coverage.
     *
     * candidateSkills and targetSkills should already be normalized
     * by SkillNormalizationService.
     *
     * We still perform safe case-insensitive matching here so
     * "Java" and "java" do not accidentally become different skills.
     */
    private double calculateCoverage(
            List<String> candidateSkills,
            List<String> targetSkills
    ) {

        if (targetSkills == null || targetSkills.isEmpty()) {
            return 1.0;
        }

        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return 0.0;
        }

        long matched =
                targetSkills.stream()
                        .filter(target ->
                                candidateSkills.stream()
                                        .anyMatch(candidate ->
                                                skillsMatch(
                                                        candidate,
                                                        target
                                                )
                                        )
                        )
                        .count();

        return clamp(
                (double) matched / targetSkills.size()
        );
    }

    /**
     * Returns the skills/requirements that exist in both lists.
     */
    public List<String> findMatched(
            List<String> candidateSkills,
            List<String> targetSkills
    ) {

        if (candidateSkills == null
                || targetSkills == null
                || candidateSkills.isEmpty()
                || targetSkills.isEmpty()) {
            return List.of();
        }

        List<String> matched = new ArrayList<>();

        for (String target : targetSkills) {

            if (target == null || target.isBlank()) {
                continue;
            }

            boolean exists =
                    candidateSkills.stream()
                            .anyMatch(candidate ->
                                    skillsMatch(
                                            candidate,
                                            target
                                    )
                            );

            if (exists) {
                matched.add(target);
            }
        }

        return matched;
    }

    /**
     * Returns the requirements missing from the candidate profile.
     */
    public List<String> findMissing(
            List<String> candidateSkills,
            List<String> targetSkills
    ) {

        if (targetSkills == null
                || targetSkills.isEmpty()) {
            return List.of();
        }

        if (candidateSkills == null
                || candidateSkills.isEmpty()) {
            return List.copyOf(targetSkills);
        }

        List<String> missing = new ArrayList<>();

        for (String target : targetSkills) {

            if (target == null || target.isBlank()) {
                continue;
            }

            boolean exists =
                    candidateSkills.stream()
                            .anyMatch(candidate ->
                                    skillsMatch(
                                            candidate,
                                            target
                                    )
                            );

            if (!exists) {
                missing.add(target);
            }
        }

        return missing;
    }

    /**
     * Safe skill comparison.
     */
    private boolean skillsMatch(
            String first,
            String second
    ) {

        if (first == null || second == null) {
            return false;
        }

        String a =
                first.trim()
                        .toLowerCase(Locale.ROOT);

        String b =
                second.trim()
                        .toLowerCase(Locale.ROOT);

        if (a.isBlank() || b.isBlank()) {
            return false;
        }

        return a.equals(b);
    }

    /**
     * Simple V1 education matching.
     */
    private boolean educationMatches(
            String candidateEducation,
            String requirement
    ) {

        if (candidateEducation == null
                || requirement == null) {
            return false;
        }

        String normalizedRequirement =
                requirement
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (normalizedRequirement.isBlank()) {
            return false;
        }

        /*
         * Direct match.
         */
        if (candidateEducation.contains(normalizedRequirement)) {
            return true;
        }

        /*
         * Handle common wording differences.
         */
        if (normalizedRequirement.contains("computer science")
                && candidateEducation.contains("computer science")) {
            return true;
        }

        if (normalizedRequirement.contains("software engineering")
                && candidateEducation.contains("software engineering")) {
            return true;
        }

        if (normalizedRequirement.contains("information technology")
                && candidateEducation.contains("information technology")) {
            return true;
        }

        if (normalizedRequirement.contains("related field")) {

            return candidateEducation.contains("computer science")
                    || candidateEducation.contains("software engineering")
                    || candidateEducation.contains("information technology")
                    || candidateEducation.contains("computer engineering");
        }

        return false;
    }

    private double clamp(double value) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(1.0, value)
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}