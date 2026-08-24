package com.project.service;

import com.project.entity.UrgencyLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LlmIntegrationService {

    @Value("${application.llm.api-key:}")
    private String llmApiKey;

    public LlmResult processSymptoms(String symptoms) {
        if (llmApiKey == null || llmApiKey.trim().isEmpty()) {
            return generateMockResponse(symptoms);
        }
        
        // TODO: In a real implementation with a valid API key, we would use RestTemplate 
        // or WebClient to call the Gemini / OpenAI API here.
        // For now, since we may not have an active network dependency setup, fallback to mock.
        return generateMockResponse(symptoms);
    }

    private LlmResult generateMockResponse(String symptoms) {
        String lower = symptoms.toLowerCase();
        UrgencyLevel level = UrgencyLevel.LOW;
        
        if (lower.contains("pain") || lower.contains("blood") || lower.contains("severe") || lower.contains("emergency")) {
            level = UrgencyLevel.HIGH;
        } else if (lower.contains("fever") || lower.contains("cough") || lower.contains("headache")) {
            level = UrgencyLevel.MEDIUM;
        }

        String summary = "Patient reports the following symptoms: " + symptoms + ". " +
                "Based on keywords, this appears to be a " + level + " urgency situation.";

        return new LlmResult(summary, level);
    }

    public record LlmResult(String summary, UrgencyLevel urgencyLevel) {}
}
