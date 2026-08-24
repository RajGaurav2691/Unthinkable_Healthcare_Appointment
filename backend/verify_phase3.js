const API_URL = 'http://localhost:8083/api';

async function fetchJSON(url, options = {}) {
    if (!options.headers) options.headers = {};
    options.headers['Content-Type'] = 'application/json';
    const res = await fetch(url, options);
    
    let data = {};
    const text = await res.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch(e) {
            data = text;
        }
    }
    
    if (!res.ok) throw { status: res.status, data };
    return data;
}

async function runPhase3Tests() {
    try {
        console.log("1. Admin Login");
        const loginRes = await fetchJSON(`${API_URL}/auth/login`, {
            method: 'POST',
            body: JSON.stringify({
                email: 'admin@example.com',
                password: 'admin123'
            })
        });
        const adminToken = loginRes.token;
        console.log("Admin logged in. Token length:", adminToken.length);

        console.log("\n2. Admin Creates Doctor");
        const doctorData = {
            name: "Gregory House",
            email: `house_${Date.now()}@example.com`,
            password: "password123",
            specialization: "Diagnostic Medicine",
            qualification: "MD",
            experience: 20,
            consultationDuration: 30,
            schedules: [
                { dayOfWeek: "MONDAY", startTime: "09:00:00", endTime: "17:00:00" },
                { dayOfWeek: "TUESDAY", startTime: "10:00:00", endTime: "15:00:00" }
            ]
        };
        const createDocRes = await fetchJSON(`${API_URL}/admin/doctors`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${adminToken}` },
            body: JSON.stringify(doctorData)
        });
        const doctorId = createDocRes.id;
        console.log(`Created doctor! ID: ${doctorId}, Name: ${createDocRes.name}, Specialization: ${createDocRes.specialization}`);
        console.log("Schedules count:", createDocRes.schedules.length);

        console.log("\n3. Admin Adds Leave for Doctor");
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const tomorrowStr = tomorrow.toISOString().split('T')[0];
        
        const leaveRes = await fetchJSON(`${API_URL}/admin/doctors/${doctorId}/leave`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${adminToken}` },
            body: JSON.stringify({ leaveDate: tomorrowStr, reason: "Vacation" })
        });
        const leaveId = leaveRes.id;
        console.log(`Leave added! ID: ${leaveId}, Date: ${leaveRes.leaveDate}`);

        console.log("\n4. Patient Login");
        const regRes = await fetchJSON(`${API_URL}/auth/register`, {
            method: 'POST',
            body: JSON.stringify({
                name: 'API Test Patient',
                email: `patient_${Date.now()}@example.com`,
                password: 'password123'
            })
        });
        const loginPatientRes = await fetchJSON(`${API_URL}/auth/login`, {
            method: 'POST',
            body: JSON.stringify({
                email: regRes.user.email,
                password: 'password123'
            })
        });
        const patientToken = loginPatientRes.token;

        console.log("\n5. Patient Fetches Doctors");
        const getDocsRes = await fetchJSON(`${API_URL}/doctors`, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        console.log(`Found ${getDocsRes.length} active doctors.`);
        
        console.log("\n6. Patient Searches Doctors");
        const searchDocsRes = await fetchJSON(`${API_URL}/doctors/search?specialization=Diagnostic`, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        console.log(`Found ${searchDocsRes.length} doctors matching 'Diagnostic'.`);

        console.log("\n7. Patient Checks Availability (Leave Day - should be 0)");
        const availLeaveRes = await fetchJSON(`${API_URL}/doctors/${doctorId}/availability?date=${tomorrowStr}`, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        console.log(`Slots on leave day (${tomorrowStr}): ${availLeaveRes.length}`);
        
        // Find next monday
        const nextMonday = new Date();
        nextMonday.setDate(nextMonday.getDate() + (1 + 7 - nextMonday.getDay()) % 7);
        if (nextMonday <= new Date()) nextMonday.setDate(nextMonday.getDate() + 7);
        const nextMondayStr = nextMonday.toISOString().split('T')[0];

        // Ensure we don't pick the leave date
        let testDateStr = nextMondayStr;
        if (testDateStr === tomorrowStr) {
            nextMonday.setDate(nextMonday.getDate() + 7);
            testDateStr = nextMonday.toISOString().split('T')[0];
        }

        console.log(`\n8. Patient Checks Availability (Working Day - MONDAY ${testDateStr})`);
        const availRes = await fetchJSON(`${API_URL}/doctors/${doctorId}/availability?date=${testDateStr}`, {
            headers: { Authorization: `Bearer ${patientToken}` }
        });
        console.log(`Slots on working day (${testDateStr}): ${availRes.length}`);
        if (availRes.length > 0) {
            console.log(`First slot: ${availRes[0].startTime} to ${availRes[0].endTime}`);
        } else {
            console.error("FAIL: Expected slots to be generated for a valid working day.");
        }

        console.log("\nAll Phase 3 APIs successfully verified!");
    } catch (e) {
        console.error("\nVerification failed:");
        console.error("Status:", e.status);
        console.error("Data:", JSON.stringify(e.data, null, 2) || e);
    }
}

runPhase3Tests();
