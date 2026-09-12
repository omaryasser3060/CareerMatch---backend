package com.example.backend.service.adzuna;

import com.example.backend.config.AdzunaConfig;
import com.example.backend.exception.AdzunaApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdzunaClient {

    private final WebClient.Builder webClientBuilder;
    private final AdzunaConfig adzunaConfig;
    private final ObjectMapper objectMapper;

    public AdzunaResponseDto searchJobs(
            String query,
            String location,
            int page
    ) {

        if (adzunaConfig.getAppId() == null
                || adzunaConfig.getAppId().isBlank()
                || adzunaConfig.getAppKey() == null
                || adzunaConfig.getAppKey().isBlank()) {

            throw new AdzunaApiException(
                    "Adzuna API credentials not configured"
            );
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(
                        adzunaConfig.getBaseUrl()
                                + "/jobs/"
                                + adzunaConfig.getCountry()
                                + "/search/"
                                + page
                )
                .queryParam("app_id", adzunaConfig.getAppId())
                .queryParam("app_key", adzunaConfig.getAppKey())
                .queryParam(
                        "what",
                        query == null ? "" : query
                )
                .queryParam(
                        "where",
                        location == null ? "" : location
                )
                .queryParam(
                        "results_per_page",
                        adzunaConfig.getResultsPerPage()
                )
                .queryParam(
                        "content-type",
                        "application/json"
                )
                .build()
                .encode()
                .toUri();

        log.debug(
                "Calling Adzuna API: page={}, query={}, location={}",
                page,
                query,
                location
        );

        try {

            String response = webClientBuilder
                    .build()
                    .get()
                    .uri(uri)
                    .header(
                            "Accept",
                            "application/json"
                    )
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            clientResponse ->
                                    clientResponse
                                            .bodyToMono(String.class)
                                            .defaultIfEmpty("")
                                            .flatMap(body -> {

                                                        int status =
                                                                clientResponse
                                                                        .statusCode()
                                                                        .value();

                                                        log.error(
                                                                "Adzuna API returned HTTP {} for query='{}': {}",
                                                                status,
                                                                query,
                                                                truncate(body, 1000)
                                                        );

                                                        return Mono.error(
                                                                new AdzunaApiException(
                                                                        "Adzuna API returned HTTP "
                                                                                + status
                                                                                + ": "
                                                                                + truncate(body, 1000)
                                                                )
                                                        );
                                                    }
                                            )
                    )
                    .bodyToMono(String.class)
                    .timeout(
                            Duration.ofMillis(
                                    adzunaConfig.getTimeout()
                            )
                    )
                    .block();

            if (response == null || response.isBlank()) {

                throw new AdzunaApiException(
                        "Adzuna API returned an empty response"
                );
            }

            log.info(
                    "Adzuna response received for query='{}': {}",
                    query,
                    truncate(response, 1500)
            );

            AdzunaResponseDto parsed =
                    objectMapper.readValue(
                            response,
                            AdzunaResponseDto.class
                    );

            int resultCount =
                    parsed.getResults() == null
                            ? 0
                            : parsed.getResults().size();

            log.info(
                    "Adzuna parsed response: query='{}', page={}, count={}, results={}",
                    query,
                    page,
                    parsed.getCount(),
                    resultCount
            );

            return parsed;

        } catch (AdzunaApiException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Adzuna API call failed. Query={}",
                    query,
                    e
            );

            throw new AdzunaApiException(
                    "Adzuna API call failed: " + e.getMessage(),
                    e
            );
        }
    }

    private String truncate(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }
}