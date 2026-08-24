package com.project.service.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiLlmProvider implements LlmProvider {
    private static final Logger log = LoggerFactory.getLogger(GeminiLlmProvider.class);
    
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public GeminiLlmProvider(RestTemplate restTemplate, String apiUrl, String apiKey, String model) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String analyzeSymptoms(String symptoms) {
        String prompt = "Analyze these symptoms and return: " +
                "- urgency level (LOW, MEDIUM, or HIGH) " +
                "- chief complaint " +
                "- three suggested questions for the doctor. " +
                "Symptoms: " + symptoms + ". " +
                "Return the result as a strict JSON object with keys: 'urgencyLevel', 'chiefComplaint', 'suggestedQuestions' (which is a list of strings). Do NOT wrap it in markdown code blocks. Just return the JSON.";
        
        return callGeminiApi(prompt);
    }

    @Override
    public String generatePostVisitSummary(String clinicalNotes, String prescription) {
        String prompt = "Convert these clinical notes and prescription into a patient-friendly summary with medication schedule and follow-up steps. " +
                "Notes: " + clinicalNotes + "\nPrescription: " + prescription + "\n" +
                "Return the result as a strict JSON object with keys: 'summary', 'medications' (list of objects with 'name' and 'dosage'), and 'followUpInstructions'. Do NOT wrap it in markdown code blocks. Just return the JSON.";
                
        return callGeminiApi(prompt);
    }

    private String callGeminiApi(String prompt) {
        try {
            String url = String.format("%s/models/%s:generateContent?key=%s", apiUrl, model, apiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "responseMimeType", "application/json"
                )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map response = restTemplate.postForObject(url, request, Map.class);
            
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new RuntimeException("LLM API Call failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(Map response) {
        if (response == null || !response.containsKey("candidates")) {
            throw new RuntimeException("Invalid response format from Gemini API");
        }
        
        List candidates = (List) response.get("candidates");
        if (candidates.isEmpty()) {
            throw new RuntimeException("No candidates returned from Gemini API");
        }
        
        Map candidate = (Map) candidates.get(0);
        Map content = (Map) candidate.get("content");
        List parts = (List) content.get("parts");
        Map part = (Map) parts.get(0);
        
        String text = (String) part.get("text");
        return cleanJsonResponse(text);
    }

    private String cleanJsonResponse(String text) {
        if (text == null) return "{}";
        text = text.trim();
        // Remove markdown formatting if the model ignored the instruction
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }
}
