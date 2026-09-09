package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cvs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    @Column(columnDefinition = "TEXT")
    private String skillsJson;

    @Column(columnDefinition = "TEXT")
    private String profileJson;

    @Column(nullable = false)
    @Builder.Default
    private boolean parsed = false;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    private LocalDateTime parsedAt;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String fileType;
}