package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_title", columnList = "title"),
                @Index(name = "idx_jobs_company", columnList = "company"),
                @Index(name = "idx_jobs_location", columnList = "location"),
                @Index(name = "idx_jobs_source", columnList = "source"),
                @Index(name = "idx_jobs_external_id", columnList = "external_id"),
                @Index(name = "idx_jobs_posted_date", columnList = "posted_date"),
                @Index(name = "idx_jobs_deleted", columnList = "deleted")
        }
)
@SQLDelete(sql = "UPDATE jobs SET deleted = true, deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    // ============================================================
    // External Reference (Adzuna)
    // ============================================================

    @Column(name = "external_id", length = 255)
    private String externalId;

    // ============================================================
    // Job Information
    // ============================================================

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @NotBlank(message = "Company is required")
    @Size(max = 255, message = "Company must not exceed 255 characters")
    @Column(name = "company", nullable = false, length = 255)
    private String company;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "location", length = 255)
    private String location;

    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Size(max = 100, message = "Source must not exceed 100 characters")
    @Column(name = "source", length = 100)
    private String source;

    // ============================================================
    // Skills
    // ============================================================

    /*
     * Legacy job-analysis fields kept for database/backward compatibility.
     * They are NOT populated during Adzuna synchronization.
     * Job requirements are extracted on-demand during Match Analysis.
     */
    @Column(name = "required_skills_json", columnDefinition = "TEXT")
    private String requiredSkillsJson;

    @Column(name = "preferred_skills_json", columnDefinition = "TEXT")
    private String preferredSkillsJson;

    @Column(name = "requirements_json", columnDefinition = "TEXT")
    private String requirementsJson;
    // ============================================================
    // Experience & Salary
    // ============================================================

    /*
     * Legacy field kept for database/backward compatibility.
     * It is no longer calculated during Adzuna synchronization.
     */
    @Column(name = "expected_experience_months")
    private Integer expectedExperienceMonths;

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    @Column(name = "currency", length = 10)
    private String currency;

    // ============================================================
    // Job Type
    // ============================================================

    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Size(max = 50, message = "Remote type must not exceed 50 characters")
    @Column(name = "remote_type", length = 50)
    private String remoteType;

    // ============================================================
    // URLs & Dates
    // ============================================================

    @Size(max = 500, message = "URL must not exceed 500 characters")
    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    // ============================================================
    // Audit Fields
    // ============================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
    // equals / hashCode
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job job)) return false;
        return id != null && Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }





}