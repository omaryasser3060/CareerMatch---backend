package com.example.backend.service.ai;

import com.example.backend.config.GroqConfig;
import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.dto.ai.JobRequirementsDto;
import com.example.backend.dto.ai.RecommendationDto;
import com.example.backend.exception.LLMException;
import com.example.backend.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    private final GroqConfig groqConfig;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Extract structured candidate profile from CV text.
     *
     * Groq is used here only for CV analysis.
     */
    public CandidateProfileDto extractCandidateProfile(String cvText) {

        log.info(
                "Extracting candidate profile from CV text via Groq..."
        );

        String prompt =
                promptBuilder.buildCandidateProfilePrompt(cvText);

        String response =
                callGroqApi(prompt);

        CandidateProfileDto profile =
                JsonUtils.fromJson(
                        response,
                        CandidateProfileDto.class
                );

        if (profile == null) {

            log.error(
                    "Failed to parse CandidateProfileDto. Raw response: {}",
                    response
            );

            throw new LLMException(
                    "Failed to parse candidate profile"
            );
        }

        return profile;
    }

    /**
     * Extract structured requirements from a job.
     *
     * IMPORTANT:
     * This is intentionally called during Match Analysis,
     * not during Adzuna job ingestion.
     */
    public JobRequirementsDto extractJobRequirements(
            String jobTitle,
            String jobDescription
    ) {

        log.info(
                "Extracting job requirements via Groq for job: {}",
                jobTitle
        );

        String prompt =
                promptBuilder.buildJobRequirementsPrompt(
                        jobTitle,
                        jobDescription
                );

        String response =
                callGroqApi(prompt);

        JobRequirementsDto requirements =
                JsonUtils.fromJson(
                        response,
                        JobRequirementsDto.class
                );

        if (requirements == null) {

            log.error(
                    "Failed to parse JobRequirementsDto. Raw response: {}",
                    response
            );

            throw new LLMException(
                    "Failed to parse job requirements"
            );
        }

        return requirements;
    }

    /**
     * Generate human-readable recommendations after
     * deterministic scoring has already been completed.
     *
     * Groq does NOT calculate the Match Score.
     */
    public RecommendationDto generateRecommendations(
            CandidateProfileDto candidate,
            JobRequirementsDto job,
            List<String> matchedRequired,
            List<String> missingRequired,
            List<String> matchedPreferred,
            List<String> missingPreferred,
            int matchScore
    ) {

        log.info(
                "Generating recommendations for match score: {} via Groq...",
                matchScore
        );

        String prompt =
                promptBuilder.buildRecommendationsPrompt(
                        candidate,
                        job,
                        matchedRequired,
                        missingRequired,
                        matchedPreferred,
                        missingPreferred,
                        matchScore
                );

        String response =
                callGroqApi(prompt);

        RecommendationDto recommendations =
                JsonUtils.fromJson(
                        response,
                        RecommendationDto.class
                );

        if (recommendations == null) {

            log.error(
                    "Failed to parse RecommendationDto. Raw response: {}",
                    response
            );

            throw new LLMException(
                    "Failed to parse recommendations"
            );
        }

        return recommendations;
    }

    /**
     * Central Groq API call.
     *
     * GPT-OSS 20B supports JSON Object Mode.
     *
     * Important:
     * - include_reasoning=false prevents reasoning output from
     *   interfering with the structured JSON response.
     * - max_completion_tokens gives the model enough room to
     *   complete CV/job/recommendation JSON.
     * - response_format=json_object forces valid JSON.
     */
    private String callGroqApi(String prompt) {

        if (prompt == null || prompt.isBlank()) {

            throw new LLMException(
                    "Groq prompt cannot be empty"
            );
        }

        if (groqConfig.getApiKey() == null
                || groqConfig.getApiKey().isBlank()) {

            throw new LLMException(
                    "Groq API key is not configured"
            );
        }

        if (groqConfig.getChatModel() == null
                || groqConfig.getChatModel().isBlank()) {

            throw new LLMException(
                    "Groq chat model is not configured"
            );
        }

        try {

            RestTemplate restTemplate =
                    new RestTemplate();

            String url =
                    groqConfig.getBaseUrl()
                            + "/chat/completions";

            /*
             * GPT-OSS JSON requests should explicitly tell the model
             * to return JSON and disable reasoning output.
             */
            Map<String, Object> systemMessage =
                    Map.of(
                            "role",
                            "system",

                            "content",
                            """
                            You are a precise structured-data extraction assistant.

                            Return ONLY one valid JSON object.

                            Do not return:
                            - markdown
                            - code fences
                            - explanations
                            - comments
                            - reasoning
                            - text before or after the JSON

                            The JSON must follow the exact structure requested
                            by the user prompt.
                            """
                    );

            Map<String, Object> userMessage =
                    Map.of(
                            "role",
                            "user",

                            "content",
                            prompt
                                    + """

                                    
                                    
                                    FINAL OUTPUT REQUIREMENT:
                                    Return ONLY a valid JSON object.
                                    Do NOT use markdown.
                                    Do NOT include ```json.
                                    Do NOT include explanations.
                                    """
                    );

            Map<String, Object> requestBody =
                    Map.of(
                            "model",
                            groqConfig.getChatModel(),

                            "messages",
                            List.of(
                                    systemMessage,
                                    userMessage
                            ),

                            "temperature",
                            groqConfig.getTemperature(),

                            /*
                             * GPT-OSS 20B supports JSON Object Mode.
                             */
                            "response_format",
                            Map.of(
                                    "type",
                                    "json_object"
                            ),

                            /*
                             * Do not return reasoning content.
                             */
                            "include_reasoning",
                            false,

                            /*
                             * CV profiles and recommendation objects
                             * can be relatively large.
                             */
                            "max_completion_tokens",
                            4096
                    );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.setAccept(
                    List.of(
                            MediaType.APPLICATION_JSON
                    )
            );

            headers.setBearerAuth(
                    groqConfig.getApiKey()
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            log.debug(
                    "Calling Groq API. Model={}, promptLength={}",
                    groqConfig.getChatModel(),
                    prompt.length()
            );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );

            if (response.getStatusCode().isError()) {

                throw new LLMException(
                        "Groq API returned HTTP "
                                + response.getStatusCode().value()
                );
            }

            String responseBody =
                    response.getBody();

            if (responseBody == null
                    || responseBody.isBlank()) {

                throw new LLMException(
                        "Groq returned an empty response"
                );
            }

            log.debug(
                    "Groq raw response received. Length={}",
                    responseBody.length()
            );

            JsonNode root =
                    objectMapper.readTree(
                            responseBody
                    );

            JsonNode choices =
                    root.path("choices");

            if (!choices.isArray()
                    || choices.isEmpty()) {

                log.error(
                        "Groq response contains no choices: {}",
                        responseBody
                );

                throw new LLMException(
                        "Groq response contains no choices"
                );
            }

            JsonNode message =
                    choices
                            .get(0)
                            .path("message");

            JsonNode content =
                    message.path("content");

            if (content.isMissingNode()
                    || content.isNull()
                    || !content.isTextual()) {

                log.error(
                        "Groq response contains no textual message content: {}",
                        responseBody
                );

                throw new LLMException(
                        "Groq response contains no valid message content"
                );
            }

            String rawContent =
                    content.asText();

            if (rawContent == null
                    || rawContent.isBlank()) {

                log.error(
                        "Groq returned empty message content."
                );

                throw new LLMException(
                        "Groq returned empty message content"
                );
            }

            String cleaned =
                    cleanJsonResponse(
                            rawContent
                    );

            /*
             * Validate JSON before returning it.
             *
             * This gives us a much clearer error than allowing
             * JsonUtils to fail later.
             */
            try {

                objectMapper.readTree(
                        cleaned
                );

            } catch (Exception jsonException) {

                log.error(
                        "Groq returned invalid JSON. Raw content: {}",
                        rawContent
                );

                throw new LLMException(
                        "Groq returned invalid JSON: "
                                + jsonException.getMessage()
                );
            }

            return cleaned;

        } catch (HttpClientErrorException e) {

            String errorBody =
                    e.getResponseBodyAsString();

            log.error(
                    "Groq API HTTP error {}. Response: {}",
                    e.getStatusCode().value(),
                    truncate(
                            errorBody,
                            3000
                    )
            );

            throw new LLMException(
                    "Groq API Error: HTTP "
                            + e.getStatusCode().value()
                            + ": "
                            + truncate(
                            errorBody,
                            1500
                    )
            );

        } catch (LLMException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Groq API call failed. Reason: {}",
                    e.getMessage(),
                    e
            );

            throw new LLMException(
                    "Groq API Error: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Remove accidental markdown fences if the model returns them.
     *
     * JSON Object Mode should normally make this unnecessary,
     * but keeping this makes the integration more defensive.
     */
    private String cleanJsonResponse(
            String response
    ) {

        if (response == null) {
            return "{}";
        }

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned.substring(
                            7
                    );

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.substring(
                            3
                    );
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 3
                    );
        }

        return cleaned.trim();
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

        return value.substring(
                0,
                maxLength
        ) + "...";
    }
}

