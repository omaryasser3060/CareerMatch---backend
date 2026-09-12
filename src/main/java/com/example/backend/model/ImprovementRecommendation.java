package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "improvement_recommendations",
        indexes = {
                @Index(name = "idx_recommendations_match_id", columnList = "match_result_id"),
                @Index(name = "idx_recommendations_priority", columnList = "priority_score"),
                @Index(name = "idx_recommendations_status", columnList = "status"),
                @Index(name = "idx_recommendations_deleted", columnList = "deleted")
        }
)
@SQLDelete(sql = "UPDATE improvement_recommendations SET deleted = true, deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"matchResult"})
public class ImprovementRecommendation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    // ============================================================
    // Relationships
    // ============================================================

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_result_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recommendations_match_result"))
    private MatchResult matchResult;

    // ============================================================
    // Gap Information
    // ============================================================

    @NotBlank(message = "Gap name is required")
    @Size(max = 255, message = "Gap name must not exceed 255 characters")
    @Column(name = "gap_name", nullable = false, length = 255)
    private String gapName;

    @NotBlank(message = "Gap category is required")
    @Size(max = 100, message = "Gap category must not exceed 100 characters")
    @Column(name = "gap_category", nullable = false, length = 100)
    private String gapCategory;

    @NotNull(message = "Required or preferred is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "required_or_preferred", nullable = false, length = 20)
    private RequiredOrPreferred requiredOrPreferred;

    @NotNull(message = "Importance weight is required")
    @Min(value = 1, message = "Importance weight must be at least 1")
    @Max(value = 10, message = "Importance weight must be at most 10")
    @Column(name = "importance_weight", nullable = false)
    private Integer importanceWeight;

    // ============================================================
    // Evidence
    // ============================================================

    @Column(name = "job_evidence", columnDefinition = "TEXT")
    private String jobEvidence;

    @Column(name = "cv_evidence", columnDefinition = "TEXT")
    private String cvEvidence;

    @Column(name = "related_existing_strengths", columnDefinition = "TEXT")
    private String relatedExistingStrengths;

    // ============================================================
    // Recommendation
    // ============================================================

    @NotBlank(message = "Recommended action is required")
    @Column(name = "recommended_action", nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "deliverable", columnDefinition = "TEXT")
    private String deliverable;

    @NotBlank(message = "Estimated effort is required")
    @Size(max = 100, message = "Estimated effort must not exceed 100 characters")
    @Column(name = "estimated_effort", nullable = false, length = 100)
    private String estimatedEffort;

    @NotNull(message = "Expected score gain is required")
    @Min(value = 0, message = "Expected score gain must be at least 0")
    @Max(value = 100, message = "Expected score gain must be at most 100")
    @Column(name = "expected_score_gain", nullable = false)
    private Integer expectedScoreGain;

    // ============================================================
    // Priority
    // ============================================================

    @NotNull(message = "Priority score is required")
    @Min(value = 0, message = "Priority score must be at least 0")
    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore;

    @NotNull(message = "Priority label is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_label", nullable = false, length = 50)
    private PriorityLabel priorityLabel;

    @NotBlank(message = "Confidence is required")
    @Size(max = 50, message = "Confidence must not exceed 50 characters")
    @Column(name = "confidence", nullable = false, length = 50)
    private String confidence;

    // ============================================================
    // Resources
    // ============================================================

    @Column(name = "resources_json", columnDefinition = "TEXT")
    private String resourcesJson;

    // ============================================================
    // Status
    // ============================================================

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    // ============================================================
    // Audit Fields
    // ============================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============================================================
    // Optimistic Locking
    // ============================================================

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ============================================================
    // Soft Delete
    // ============================================================

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ============================================================
    // equals / hashCode
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImprovementRecommendation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ============================================================
    // Enums
    // ============================================================

    public enum RequiredOrPreferred {
        REQUIRED,
        PREFERRED
    }

    public enum PriorityLabel {
        VERY_HIGH,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum RecommendationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED
    }
}