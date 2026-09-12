package com.example.backend.service.adzuna;

import com.example.backend.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdzunaService {

    private final AdzunaClient adzunaClient;

    private static final DateTimeFormatter ADZUNA_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'"
            );

    /**
     * Fetch jobs from Adzuna.
     *
     * IMPORTANT:
     * Adzuna ingestion is intentionally lightweight.
     *
     * We do NOT:
     * - call Groq
     * - extract job skills
     * - normalize skills
     * - calculate embeddings
     * - calculate match scores
     *
     * The complete job title and description are stored as raw job data.
     * Groq analyzes the selected job later during Match Analysis.
     */
    public List<Job> fetchJobs(
            String query,
            String location,
            int page
    ) {

        AdzunaResponseDto response =
                adzunaClient.searchJobs(
                        query,
                        location,
                        page
                );

        if (response == null
                || response.getResults() == null
                || response.getResults().isEmpty()) {

            log.debug(
                    "No jobs returned from Adzuna for query='{}', location='{}', page={}",
                    query,
                    location,
                    page
            );

            return List.of();
        }

        log.info(
                "Received {} jobs from Adzuna for query='{}', location='{}', page={}",
                response.getResults().size(),
                query,
                location,
                page
        );

        return response.getResults()
                .stream()
                .map(this::mapToJob)
                .toList();
    }

    /**
     * Convert an Adzuna job into our internal Job entity.
     *
     * This method stores raw Adzuna information only.
     * Job understanding/extraction happens on-demand during matching.
     */
    private Job mapToJob(
            AdzunaJobDto dto
    ) {

        String title = safe(dto.getTitle());
        String description = safe(dto.getDescription());

        return Job.builder()
                .externalId(dto.getId())
                .title(title)
                .company(
                        dto.getCompany() != null
                                ? safe(dto.getCompany().getDisplayName())
                                : "Unknown"
                )
                .location(
                        dto.getLocation() != null
                                ? safe(dto.getLocation().getDisplayName())
                                : "Unknown"
                )
                .description(description)
                .source("adzuna")

                // Legacy fields are intentionally not populated during sync.
                .requiredSkillsJson(null)
                .preferredSkillsJson(null)
                .requirementsJson(null)
                .expectedExperienceMonths(null)

                .url(dto.getRedirectUrl())
                .salaryMin(
                        dto.getSalaryMin() != null
                                ? BigDecimal.valueOf(dto.getSalaryMin())
                                : null
                )
                .salaryMax(
                        dto.getSalaryMax() != null
                                ? BigDecimal.valueOf(dto.getSalaryMax())
                                : null
                )
                .currency("GBP")
                .employmentType(dto.getContractTime())
                .postedDate(parseDate(dto.getCreated()))
                .lastSyncAt(LocalDateTime.now())
                .build();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDateTime parseDate(String dateStr) {

        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(
                    dateStr,
                    ADZUNA_DATE_FORMAT
            );

        } catch (Exception e) {

            log.debug(
                    "Unable to parse Adzuna date '{}'",
                    dateStr
            );

            return null;
        }
    }
}
