package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CV cv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Integer overallMatchScore;

    @Column(columnDefinition = "TEXT")
    private String scoreBreakdownJson;

    @Column(columnDefinition = "TEXT")
    private String matchedSkillsJson;

    @Column(columnDefinition = "TEXT")
    private String missingSkillsJson;

    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(columnDefinition = "TEXT")
    private String improvementPlanJson;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(nullable = false)
    private String extractionConfidence;

    @Column(nullable = false)
    @Builder.Default
    private boolean humanReviewFlag = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private String jobTitle;

    @Column
    private String companyName;
}