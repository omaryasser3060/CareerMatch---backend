package com.example.backend.service;

import com.example.backend.model.Job;
import com.example.backend.service.adzuna.AdzunaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSyncService {

    private final AdzunaService adzunaService;
    private final JobService jobService;

    private static final String[] SEARCH_QUERIES = {
            "software developer",
            "backend developer",
            "frontend developer",
            "full stack developer",
            "data scientist"
    };


    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Application started. Triggering initial Adzuna job sync in background...");
        CompletableFuture.runAsync(this::syncJobs);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void syncJobs() {
        log.info("Starting scheduled Adzuna job sync");

        try {
            int totalSynced = 0;

            for (String query : SEARCH_QUERIES) {
                try {
                    List<Job> jobs = adzunaService.fetchJobs(query, "", 1);
                    int synced = syncJobList(jobs);
                    totalSynced += synced;
                    log.debug("Synced {} jobs for query: {}", synced, query);
                } catch (Exception e) {
                    log.error("Failed to sync jobs for query: {}", query, e);
                }
            }

            log.info("Adzuna job sync completed. Total synced: {}", totalSynced);

        } catch (Exception e) {
            log.error("Adzuna job sync failed", e);
        }
    }

    private int syncJobList(List<Job> jobs) {
        int synced = 0;
        for (Job job : jobs) {
            if (job.getExternalId() != null && !jobService.existsByExternalId(job.getExternalId())) {
                jobService.saveJob(job);
                synced++;
            }
        }
        return synced;
    }

    @Transactional
    public int syncNow(String query) {
        log.info("Manual sync triggered for query: {}", query);
        List<Job> jobs = adzunaService.fetchJobs(query, "", 1);
        return syncJobList(jobs);
    }
}