package com.project.service.llm;

import com.project.entity.UrgencyLevel;

public interface LlmProvider {

    /**
     * Analyzes patient symptoms and returns structured JSON output containing
     * urgencyLevel, chiefComplaint, and suggestedQuestions.
     * 
     * @param symptoms The raw symptoms provided by the patient.
     * @return A JSON string representing the structured analysis.
     */
    String analyzeSymptoms(String symptoms);

    /**
     * Analyzes clinical notes and prescription to generate a patient-friendly summary.
     * 
     * @param clinicalNotes The doctor's clinical notes.
     * @param prescription The doctor's prescription.
     * @return A JSON string representing the structured post-visit summary.
     */
    String generatePostVisitSummary(String clinicalNotes, String prescription);
}
