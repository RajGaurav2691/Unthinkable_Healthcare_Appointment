require('dotenv').config({ path: '../.env' });
const { Client } = require('pg');

async function setupDatabase() {
    console.log("Connecting to postgres to ensure 'healthcare' exists...");
    const client = new Client({
        user: process.env.POSTGRES_USER || 'postgres',
        password: process.env.POSTGRES_PASSWORD || 'postgres',
        host: process.env.POSTGRES_HOST || 'localhost',
        port: parseInt(process.env.POSTGRES_PORT || '5432'),
        database: 'postgres' // Connect to default DB first
    });

    try {
        await client.connect();
        console.log("Connected to PostgreSQL successfully.");

        const res = await client.query("SELECT datname FROM pg_database WHERE datname = 'healthcare'");
        if (res.rowCount === 0) {
            console.log("Database 'healthcare' does not exist. Creating...");
            await client.query('CREATE DATABASE healthcare');
            console.log("Database 'healthcare' created successfully.");
        } else {
            console.log("Database 'healthcare' already exists. Leaving it alone.");
        }
    } catch (err) {
        console.error("Error setting up database:", err.message);
    } finally {
        await client.end();
    }
}

setupDatabase();
