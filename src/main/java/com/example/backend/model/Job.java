package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String source;

    @Column(columnDefinition = "TEXT")
    private String requiredSkillsJson;

    @Column(columnDefinition = "TEXT")
    private String preferredSkillsJson;

    private Integer expectedExperienceMonths;

    private LocalDateTime postedDate;

    private String url;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMax;

    private String currency;

    private String employmentType;

    private String remoteType;

    private LocalDateTime lastSyncAt;
}