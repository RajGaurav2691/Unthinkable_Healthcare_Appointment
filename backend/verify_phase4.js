const axios = require('axios');
const assert = require('assert');

const API_URL = 'http://localhost:8083/api';

async function run() {
    console.log("=== Phase 4 Verification ===");

    let adminToken, patientToken, doctorToken;
    
    // 1. Login Admin
    let res = await axios.post(`${API_URL}/auth/login`, { email: 'admin@example.com', password: 'admin123' });
    adminToken = res.data.token;
    console.log("Admin logged in");

    // 2. Create Doctor
    const doctorData = {
        name: 'Jane Smith',
        email: 'janesmith3@hospital.com',
        password: 'password123',
        specialization: 'Dermatologist',
        qualification: 'MBBS',
        experience: 5,
        consultationDuration: 20,
        schedules: [
            { dayOfWeek: 'MONDAY', startTime: '09:00:00', endTime: '13:00:00' },
            { dayOfWeek: 'TUESDAY', startTime: '09:00:00', endTime: '13:00:00' }
        ]
    };
    try {
        res = await axios.post(`${API_URL}/admin/doctors`, doctorData, {
            headers: { Authorization: `Bearer ${adminToken}` }
        });
        console.log("Doctor created");
    } catch(e) {
        if(e.response?.status === 400) console.log("Doctor might already exist, proceeding...");
        else throw e;
    }

    // Login Doctor
    res = await axios.post(`${API_URL}/auth/login`, { email: doctorData.email, password: doctorData.password });
    doctorToken = res.data.token;
    console.log("Doctor logged in");
    
    // Get Doctor Profile ID
    res = await axios.get(`${API_URL}/doctors?specialization=Dermatologist`, {
        headers: { Authorization: `Bearer ${adminToken}` }
    });
    const doctor = res.data.find(d => d.email === doctorData.email);
    assert(doctor, "Doctor should exist");

    // Schedule already set during creation
    console.log("Schedule set");

    // Register Patient
    const patientData = {
        name: 'Phase 4 Patient',
        email: 'phase4@patient.com',
        password: 'password123'
    };
    try {
        await axios.post(`${API_URL}/auth/register`, patientData);
    } catch(e) {}
    
    // Login Patient
    res = await axios.post(`${API_URL}/auth/login`, { email: patientData.email, password: patientData.password });
    patientToken = res.data.token;
    console.log("Patient logged in");

    // Hold Appointment
    const appointmentDate = '2026-08-31'; // Monday
    const startTime = '09:00:00';

    try {
        res = await axios.post(`${API_URL}/appointments/hold`, {
            doctorId: doctor.id,
            appointmentDate,
            startTime
        }, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        const holdId = res.data.id;
        console.log(`Slot held with ID: ${holdId}`);
        assert(res.data.status === 'HELD', "Status should be HELD");

        // Confirm Appointment
        res = await axios.post(`${API_URL}/appointments/${holdId}/confirm`, {
            symptoms: 'I have a severe rash on my arm.'
        }, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        console.log("Appointment confirmed!");
        assert(res.data.status === 'CONFIRMED', "Status should be CONFIRMED");
        assert(res.data.aiSummary, "AI Summary should be populated");
        
        // Get Doctor Appointments
        res = await axios.get(`${API_URL}/doctor/appointments`, {
            headers: { Authorization: `Bearer ${doctorToken}` }
        });
        assert(res.data.length > 0, "Doctor should have appointments");
        console.log("Doctor appointment retrieved");
        
    } catch(e) {
        console.error(e.response?.data || e.message);
        throw e;
    }

    console.log("=== Phase 4 Verification Passed ===");
}

run().catch(console.error);
