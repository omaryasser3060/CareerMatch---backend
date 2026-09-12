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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "match_results",
        indexes = {
                @Index(name = "idx_match_results_user_id", columnList = "user_id"),
                @Index(name = "idx_match_results_cv_id", columnList = "cv_id"),
                @Index(name = "idx_match_results_job_id", columnList = "job_id"),
                @Index(name = "idx_match_results_created_at", columnList = "created_at"),
                @Index(name = "idx_match_results_score", columnList = "overall_match_score"),
                @Index(name = "idx_match_results_deleted", columnList = "deleted")
        }
)
@SQLDelete(sql = "UPDATE match_results SET deleted = true, deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "cv", "job", "recommendations"})
public class MatchResult implements Serializable {

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
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_results_user"))
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_results_cv"))
    private CV cv;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_results_job"))
    private Job job;

    // ============================================================
    // Match Score
    // ============================================================

    @NotNull(message = "Match score is required")
    @Min(value = 0, message = "Match score must be at least 0")
    @Max(value = 100, message = "Match score must be at most 100")
    @Column(name = "overall_match_score", nullable = false)
    private Integer overallMatchScore;

    @Column(name = "score_breakdown_json", columnDefinition = "TEXT")
    private String scoreBreakdownJson;

    // ============================================================
    // Skills Analysis
    // ============================================================

    @Column(name = "matched_skills_json", columnDefinition = "TEXT")
    private String matchedSkillsJson;

    @Column(name = "missing_skills_json", columnDefinition = "TEXT")
    private String missingSkillsJson;

    @Column(name = "strengths_json", columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(name = "improvement_plan_json", columnDefinition = "TEXT")
    private String improvementPlanJson;

    @Column(name = "evidence_json", columnDefinition = "TEXT")
    private String evidenceJson;

    // ============================================================
    // Confidence & Review
    // ============================================================

    @NotBlank(message = "Extraction confidence is required")
    @Size(max = 50, message = "Extraction confidence must not exceed 50 characters")
    @Column(name = "extraction_confidence", nullable = false, length = 50)
    private String extractionConfidence;

    @Column(name = "human_review_flag", nullable = false)
    @Builder.Default
    private boolean humanReviewFlag = false;

    // ============================================================
    // Denormalized Job Info (for history)
    // ============================================================

    @Size(max = 500, message = "Job title must not exceed 500 characters")
    @Column(name = "job_title", length = 500)
    private String jobTitle;

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Column(name = "company_name", length = 255)
    private String companyName;

    // ============================================================
    // Relationships
    // ============================================================

    @JsonIgnore
    @OneToMany(mappedBy = "matchResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ImprovementRecommendation> recommendations = new ArrayList<>();

    // ============================================================
    // Audit Fields
    // ============================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 36)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 36)
    private String lastModifiedBy;

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
    // Helper Methods
    // ============================================================

    public void addRecommendation(ImprovementRecommendation recommendation) {
        recommendations.add(recommendation);
        recommendation.setMatchResult(this);
    }

    public void removeRecommendation(ImprovementRecommendation recommendation) {
        recommendations.remove(recommendation);
        recommendation.setMatchResult(null);
    }

    // ============================================================
    // equals / hashCode
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}