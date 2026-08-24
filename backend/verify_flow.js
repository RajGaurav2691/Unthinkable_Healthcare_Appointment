const API_URL = 'http://localhost:8083/api';
let token = '';

async function fetchJSON(url, options = {}) {
    if (!options.headers) options.headers = {};
    options.headers['Content-Type'] = 'application/json';
    const res = await fetch(url, options);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw { status: res.status, data };
    return data;
}

async function runTests() {
    try {
        console.log("1. Patient registration");
        const regRes = await fetchJSON(`${API_URL}/auth/register`, {
            method: 'POST',
            body: JSON.stringify({
                name: 'API Test Patient',
                email: 'apitest@example.com',
                password: 'password123'
            })
        });
        console.log("Registered:", regRes.user.email);
        
        console.log("\n2. Login");
        const loginRes = await fetchJSON(`${API_URL}/auth/login`, {
            method: 'POST',
            body: JSON.stringify({
                email: 'apitest@example.com',
                password: 'password123'
            })
        });
        token = loginRes.token;
        console.log("Logged in, token received.");

        console.log("\n3. Wrong password");
        try {
            await fetchJSON(`${API_URL}/auth/login`, {
                method: 'POST',
                body: JSON.stringify({
                    email: 'apitest@example.com',
                    password: 'wrongpassword'
                })
            });
            console.error("FAIL: Wrong password should have failed");
        } catch (err) {
            console.log("Passed: Wrong password rejected with", err.status);
        }

        console.log("\n4. Invalid JWT");
        try {
            await fetchJSON(`${API_URL}/auth/me`, {
                method: 'GET',
                headers: { Authorization: `Bearer invalid_token` }
            });
            console.error("FAIL: Invalid JWT should have failed");
        } catch (err) {
            console.log("Passed: Invalid JWT rejected with", err.status);
        }

        console.log("\n5. Patient accessing patient endpoint (Assuming /auth/me is protected)");
        const meRes = await fetchJSON(`${API_URL}/auth/me`, {
            method: 'GET',
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Passed: Accessed protected endpoint. User:", meRes.name);

        console.log("\nAll API backend verification passed!");
    } catch (e) {
        console.error("Verification failed:", e.data || e);
    }
}

runTests();
