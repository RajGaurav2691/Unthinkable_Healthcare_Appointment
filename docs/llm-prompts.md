# LLM Integration Prompts

The system relies on LLM orchestration to parse clinical symptoms and generate patient-friendly summaries. We strictly require the LLM to output valid structured JSON to guarantee type safety in our backend models.

---

## 1. Pre-Visit Symptom Summary Prompt

Triggered when the patient confirms an appointment and submits symptoms.

### Prompt Template:
```
You are a medical assistant AI. Analyze these symptoms and return a structured JSON response containing:
- "urgencyLevel": Must be exactly one of "LOW", "MEDIUM", or "HIGH".
- "chiefComplaint": A clear, concise summary of the primary complaint.
- "suggestedQuestions": An array of exactly three relevant questions for the doctor to ask.

Do not include any Markdown wrapper, formatting, or extra explanation outside the valid JSON string.

Symptoms: {symptoms}
```

### Expected Output Schema:
```json
{
  "urgencyLevel": "LOW | MEDIUM | HIGH",
  "chiefComplaint": "...",
  "suggestedQuestions": [
    "Question 1?",
    "Question 2?",
    "Question 3?"
  ]
}
```

---

## 2. Post-Visit Patient-Friendly Summary & Medication Prompt

Triggered when a doctor completes a consultation, providing clinical notes and a prescription.

### Prompt Template:
```
You are a helpful medical assistant. Translate the following clinical notes and prescriptions into a patient-friendly summary.
Also, extract any prescribed medications into a structured schedule.

Your response must be a JSON object containing:
- "summary": A patient-friendly, easy-to-understand explanation of the visit and treatment plan.
- "medications": An array of objects representing medication schedules. Each medication object must contain:
  - "medicationName": Name of the medicine.
  - "dosage": Amount to take (e.g., "10mg", "1 tablet").
  - "frequency": Must be exactly one of "ONCE_DAILY", "TWICE_DAILY", "THREE_TIMES_DAILY", "FOUR_TIMES_DAILY", "AS_NEEDED".
  - "durationDays": An integer representing number of days to take the medication.

Do not include any Markdown wrapper, formatting, or extra explanation outside the valid JSON string.

Clinical Notes: {clinicalNotes}
Prescription: {prescription}
```

### Expected Output Schema:
```json
{
  "summary": "Patient-friendly summary...",
  "medications": [
    {
      "medicationName": "Lisinopril",
      "dosage": "10mg",
      "frequency": "ONCE_DAILY",
      "durationDays": 30
    }
  ]
}
```

---

## 3. Reliability & Validation Logic

1. **Json Parsing Shield**: The JSON structure returned by the LLM is programmatically parsed in `LlmIntegrationService`.
2. **Robust Fallbacks**: If the API times out, rate limits occur, or the response fails JSON parsing, the system:
   - Does **not** crash the transaction or fail the appointment.
   - Saves the original raw clinical notes and symptom input.
   - Populates the summaries with a standardized fallback response (e.g., `UrgencyLevel.MEDIUM` and raw description text).
