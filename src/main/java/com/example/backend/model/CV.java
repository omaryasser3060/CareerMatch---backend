package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
import java.util.Objects;

@Entity
@Table(
        name = "cvs",
        indexes = {
                @Index(name = "idx_cvs_user_id", columnList = "user_id"),
                @Index(name = "idx_cvs_parsed", columnList = "parsed"),
                @Index(name = "idx_cvs_uploaded_at", columnList = "uploaded_at"),
                @Index(name = "idx_cvs_deleted", columnList = "deleted")
        }
)
@SQLDelete(sql = "UPDATE cvs SET deleted = true, deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "matchResults"})
public class CV implements Serializable {

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
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cvs_user"))
    private User user;

    // ============================================================
    // File Metadata
    // ============================================================

    @NotBlank(message = "Filename is required")
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @NotBlank(message = "File URL is required")
    @Size(max = 500, message = "File URL must not exceed 500 characters")
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "File type is required")
    @Size(max = 100, message = "File type must not exceed 100 characters")
    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    // ============================================================
    // Parsed Content
    // ============================================================

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "skills_json", columnDefinition = "TEXT")
    private String skillsJson;

    @Column(name = "profile_json", columnDefinition = "TEXT")
    private String profileJson;

    @Column(name = "parsed", nullable = false)
    @Builder.Default
    private boolean parsed = false;

    @Column(name = "extraction_confidence", length = 50)
    private String extractionConfidence;

    // ============================================================
    // Audit Fields
    // ============================================================

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

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
        if (!(o instanceof CV cv)) return false;
        return id != null && Objects.equals(id, cv.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}