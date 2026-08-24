package com.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.entity.UrgencyLevel;
import com.project.service.llm.LlmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmIntegrationServiceTest {

    private LlmProvider llmProvider;
    private LlmIntegrationService llmIntegrationService;

    @BeforeEach
    void setUp() {
        llmProvider = mock(LlmProvider.class);
        ObjectMapper objectMapper = new ObjectMapper();
        llmIntegrationService = new LlmIntegrationService(llmProvider, objectMapper);
    }

    @Test
    void processSymptoms_success_validJson() {
        String mockResponse = """
            {
              "urgencyLevel": "HIGH",
              "chiefComplaint": "Severe headache",
              "suggestedQuestions": ["How long?", "Any fever?"]
            }
            """;
        when(llmProvider.analyzeSymptoms(anyString())).thenReturn(mockResponse);

        LlmIntegrationService.PreVisitLlmResult result = llmIntegrationService.processSymptoms("I have a very bad headache");

        assertEquals(UrgencyLevel.HIGH, result.urgencyLevel());
        assertEquals("Severe headache", result.summary());
        assertTrue(result.rawJson().contains("How long?"));
    }

    @Test
    void processSymptoms_fallback_malformedJson() {
        when(llmProvider.analyzeSymptoms(anyString())).thenReturn("This is not valid JSON.");

        LlmIntegrationService.PreVisitLlmResult result = llmIntegrationService.processSymptoms("headache");

        // Should fallback to LOW urgency
        assertEquals(UrgencyLevel.LOW, result.urgencyLevel());
        assertEquals("Patient reported symptoms.", result.summary());
    }

    @Test
    void processSymptoms_fallback_exception() {
        when(llmProvider.analyzeSymptoms(anyString())).thenThrow(new RuntimeException("API Timeout"));

        LlmIntegrationService.PreVisitLlmResult result = llmIntegrationService.processSymptoms("headache");

        assertEquals(UrgencyLevel.LOW, result.urgencyLevel());
        assertEquals("Patient reported symptoms.", result.summary());
    }

    @Test
    void processSymptoms_invalidUrgencyLevel() {
        String mockResponse = """
            {
              "urgencyLevel": "EXTREME",
              "chiefComplaint": "Headache",
              "suggestedQuestions": []
            }
            """;
        when(llmProvider.analyzeSymptoms(anyString())).thenReturn(mockResponse);

        LlmIntegrationService.PreVisitLlmResult result = llmIntegrationService.processSymptoms("headache");

        // Should default to LOW because EXTREME is not valid enum
        assertEquals(UrgencyLevel.LOW, result.urgencyLevel());
        assertEquals("Headache", result.summary());
    }

    @Test
    void generatePostVisitSummary_success() {
        String mockResponse = "{\"summary\": \"All good\"}";
        when(llmProvider.generatePostVisitSummary(anyString(), anyString())).thenReturn(mockResponse);

        String result = llmIntegrationService.generatePostVisitSummary("notes", "meds");
        assertEquals(mockResponse, result);
    }

    @Test
    void generatePostVisitSummary_exceptionFallback() {
        when(llmProvider.generatePostVisitSummary(anyString(), anyString())).thenThrow(new RuntimeException("Error"));

        String result = llmIntegrationService.generatePostVisitSummary("notes", "meds");
        assertTrue(result.contains("Clinical notes were recorded"));
    }
}
