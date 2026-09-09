package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "improvement_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_result_id", nullable = false)
    private MatchResult matchResult;

    @Column(nullable = false)
    private String gapName;

    @Column(nullable = false)
    private String gapCategory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequiredOrPreferred requiredOrPreferred;

    @Column(nullable = false)
    private Integer importanceWeight;

    @Column(columnDefinition = "TEXT")
    private String jobEvidence;

    @Column(columnDefinition = "TEXT")
    private String cvEvidence;

    @Column(columnDefinition = "TEXT")
    private String relatedExistingStrengths;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String recommendedAction;

    @Column(columnDefinition = "TEXT")
    private String deliverable;

    @Column(nullable = false)
    private String estimatedEffort;

    @Column(nullable = false)
    private Integer expectedScoreGain;

    @Column(nullable = false)
    private Integer priorityScore;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PriorityLabel priorityLabel;

    @Column(nullable = false)
    private String confidence;

    @Column(columnDefinition = "TEXT")
    private String resourcesJson;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public enum RequiredOrPreferred {
        REQUIRED, PREFERRED
    }

    public enum PriorityLabel {
        VERY_HIGH, HIGH, MEDIUM, LOW
    }

    public enum RecommendationStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    }
}