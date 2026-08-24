package com.project.service.llm;

public class MockLlmProvider implements LlmProvider {

    @Override
    public String analyzeSymptoms(String symptoms) {
        String lower = symptoms != null ? symptoms.toLowerCase() : "";
        String level = "LOW";
        
        if (lower.contains("pain") || lower.contains("blood") || lower.contains("severe") || lower.contains("emergency")) {
            level = "HIGH";
        } else if (lower.contains("fever") || lower.contains("cough") || lower.contains("headache")) {
            level = "MEDIUM";
        }

        // Return structured JSON
        return String.format("""
            {
              "urgencyLevel": "%s",
              "chiefComplaint": "Patient reported: %s",
              "suggestedQuestions": [
                "When did the symptoms start?",
                "Are you currently taking any medication?",
                "Have you experienced this before?"
              ]
            }
            """, level, escapeJson(symptoms != null ? symptoms : ""));
    }

    @Override
    public String generatePostVisitSummary(String clinicalNotes, String prescription) {
        return String.format("""
            {
              "summary": "Based on the clinical notes: %s",
              "medications": [
                {
                  "name": "Mock Medication",
                  "dosage": "As prescribed: %s"
                }
              ],
              "followUpInstructions": "Please follow up if symptoms persist."
            }
            """, escapeJson(clinicalNotes != null ? clinicalNotes : ""), escapeJson(prescription != null ? prescription : ""));
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
