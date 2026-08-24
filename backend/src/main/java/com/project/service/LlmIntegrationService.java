package com.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.entity.UrgencyLevel;
import com.project.service.llm.LlmProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(LlmIntegrationService.class);
    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;

    public PreVisitLlmResult processSymptoms(String symptoms) {
        try {
            String jsonResult = llmProvider.analyzeSymptoms(symptoms);
            return parsePreVisitResult(jsonResult, symptoms);
        } catch (Exception e) {
            log.error("Failed to process symptoms with LLM. Returning fallback.", e);
            return generatePreVisitFallback(symptoms);
        }
    }

    public String generatePostVisitSummary(String clinicalNotes, String prescription) {
        try {
            return llmProvider.generatePostVisitSummary(clinicalNotes, prescription);
        } catch (Exception e) {
            log.error("Failed to generate post-visit summary with LLM. Returning fallback.", e);
            return generatePostVisitFallback(clinicalNotes, prescription);
        }
    }

    private PreVisitLlmResult parsePreVisitResult(String jsonString, String originalSymptoms) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            
            String urgencyStr = rootNode.path("urgencyLevel").asText("LOW").toUpperCase();
            UrgencyLevel level;
            try {
                level = UrgencyLevel.valueOf(urgencyStr);
            } catch (IllegalArgumentException e) {
                level = UrgencyLevel.LOW;
            }
            
            String chiefComplaint = rootNode.path("chiefComplaint").asText("");
            
            // Just returning the raw JSON string or we can store it directly. 
            // We want to return the whole json string so we can save it in DB, 
            // but also extract urgency and summary for immediate use if needed.
            return new PreVisitLlmResult(chiefComplaint, level, jsonString);
        } catch (Exception e) {
            log.error("Failed to parse LLM JSON response for pre-visit: {}", jsonString, e);
            return generatePreVisitFallback(originalSymptoms);
        }
    }

    private PreVisitLlmResult generatePreVisitFallback(String symptoms) {
        String json = String.format("""
            {
              "urgencyLevel": "LOW",
              "chiefComplaint": "Patient reported symptoms.",
              "suggestedQuestions": ["Can you describe your symptoms in more detail?"]
            }
            """);
        return new PreVisitLlmResult("Patient reported symptoms.", UrgencyLevel.LOW, json);
    }

    private String generatePostVisitFallback(String clinicalNotes, String prescription) {
        return String.format("""
            {
              "summary": "Clinical notes were recorded.",
              "medications": [],
              "followUpInstructions": "Please refer to your doctor's direct instructions."
            }
            """);
    }

    public record PreVisitLlmResult(String summary, UrgencyLevel urgencyLevel, String rawJson) {}
}
