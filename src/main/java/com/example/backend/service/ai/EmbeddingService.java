package com.example.backend.service.ai;

import com.example.backend.config.GeminiConfig;
import com.example.backend.exception.LLMException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    /**
     * Calculate cosine similarity between two complete text profiles.
     *
     * The text can contain:
     *
     * Candidate:
     * - skills
     * - experience
     * - projects
     * - education
     * - certifications
     * - languages
     *
     * Job:
     * - title
     * - description
     * - required skills
     * - preferred skills
     * - responsibilities
     * - experience
     * - education
     * - certifications
     * - languages
     * - other requirements
     */
    public double cosineSimilarity(
            String text1,
            String text2
    ) {

        if (text1 == null
                || text1.isBlank()
                || text2 == null
                || text2.isBlank()) {

            log.warn(
                    "Cannot calculate semantic similarity because one or both texts are empty."
            );

            return 0.0;
        }

        List<Double> vec1 =
                getEmbedding(text1);

        List<Double> vec2 =
                getEmbedding(text2);

        return cosineSimilarity(
                vec1,
                vec2
        );
    }

    /**
     * Calculate cosine similarity between two embedding vectors.
     */
    public double cosineSimilarity(
            List<Double> vec1,
            List<Double> vec2
    ) {

        if (vec1 == null
                || vec2 == null
                || vec1.isEmpty()
                || vec2.isEmpty()) {

            return 0.0;
        }

        if (vec1.size() != vec2.size()) {

            log.warn(
                    "Cannot calculate cosine similarity. " +
                            "Embedding dimensions differ: {} vs {}",
                    vec1.size(),
                    vec2.size()
            );

            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {

            Double value1 =
                    vec1.get(i);

            Double value2 =
                    vec2.get(i);

            if (value1 == null
                    || value2 == null) {
                continue;
            }

            dotProduct +=
                    value1 * value2;

            norm1 +=
                    Math.pow(
                            value1,
                            2
                    );

            norm2 +=
                    Math.pow(
                            value2,
                            2
                    );
        }

        if (norm1 == 0.0
                || norm2 == 0.0) {

            return 0.0;
        }

        double similarity =
                dotProduct
                        / (
                        Math.sqrt(norm1)
                                * Math.sqrt(norm2)
                );

        /*
         * Floating point safety.
         */
        if (Double.isNaN(similarity)
                || Double.isInfinite(similarity)) {

            return 0.0;
        }

        return Math.max(
                -1.0,
                Math.min(
                        1.0,
                        similarity
                )
        );
    }

    /**
     * Generate an embedding using the configured Gemini embedding model.
     *
     * Recommended model:
     *
     * gemini-embedding-001
     */
    public List<Double> getEmbedding(
            String text
    ) {

        if (text == null
                || text.isBlank()) {

            throw new LLMException(
                    "Cannot generate embedding for empty text"
            );
        }

        String modelName =
                geminiConfig.getEmbeddingModel();

        if (modelName == null
                || modelName.isBlank()) {

            throw new LLMException(
                    "Gemini embedding model is not configured"
            );
        }

        String apiKey =
                geminiConfig.getApiKey();

        if (apiKey == null
                || apiKey.isBlank()) {

            throw new LLMException(
                    "Gemini API key is not configured"
            );
        }

        try {

            log.debug(
                    "Generating Gemini embedding using model: {}",
                    modelName
            );

            RestTemplate restTemplate =
                    new RestTemplate();

            String url =
                    "https://generativelanguage.googleapis.com/"
                            + "v1beta/models/"
                            + modelName
                            + ":embedContent?key="
                            + apiKey;

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            Map<String, Object> requestBody =
                    Map.of(
                            "model",
                            "models/" + modelName,

                            "content",
                            Map.of(
                                    "parts",
                                    List.of(
                                            Map.of(
                                                    "text",
                                                    text
                                            )
                                    )
                            )
                    );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );

            if (response.getBody() == null
                    || response.getBody().isBlank()) {

                throw new LLMException(
                        "Gemini returned an empty embedding response"
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.getBody()
                    );

            JsonNode embeddingNode =
                    root.path("embedding")
                            .path("values");

            if (!embeddingNode.isArray()
                    || embeddingNode.isEmpty()) {

                log.error(
                        "Gemini returned invalid embedding response: {}",
                        response.getBody()
                );

                throw new LLMException(
                        "Gemini returned an empty embedding for model: "
                                + modelName
                );
            }

            List<Double> embedding =
                    objectMapper.convertValue(
                            embeddingNode,
                            new TypeReference<List<Double>>() {
                            }
                    );

            if (embedding == null
                    || embedding.isEmpty()) {

                throw new LLMException(
                        "Gemini returned an empty embedding vector"
                );
            }

            return embedding;

        } catch (LLMException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Failed to generate embedding using Gemini model: {}",
                    modelName,
                    e
            );

            throw new LLMException(
                    "Failed to generate embedding using Gemini: "
                            + e.getMessage()
            );
        }
    }
}