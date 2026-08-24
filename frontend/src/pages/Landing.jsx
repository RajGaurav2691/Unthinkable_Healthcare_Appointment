import { Link } from 'react-router-dom';

function Landing() {
    return (
        <div className="max-w-4xl mx-auto mt-12 text-center">
            <h1 className="text-4xl font-extrabold text-gray-900 mb-6">
                Welcome to Healthcare Appointment & Follow-up Manager
            </h1>
            <p className="text-lg text-gray-600 mb-8">
                A complete production-style system for managing patient appointments, doctor schedules, and follow-ups.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12">
                <div className="p-6 bg-white rounded-lg shadow border border-gray-100 hover:shadow-md transition">
                    <h2 className="text-2xl font-semibold mb-2">Patients</h2>
                    <p className="text-gray-600 mb-4">Book appointments and receive AI summaries.</p>
                    <Link to="/patient/dashboard" className="text-blue-600 font-medium hover:underline">Patient Dashboard &rarr;</Link>
                </div>
                <div className="p-6 bg-white rounded-lg shadow border border-gray-100 hover:shadow-md transition">
                    <h2 className="text-2xl font-semibold mb-2">Doctors</h2>
                    <p className="text-gray-600 mb-4">Manage schedule, appointments, and prescriptions.</p>
                    <Link to="/doctor/dashboard" className="text-blue-600 font-medium hover:underline">Doctor Dashboard &rarr;</Link>
                </div>
                <div className="p-6 bg-white rounded-lg shadow border border-gray-100 hover:shadow-md transition">
                    <h2 className="text-2xl font-semibold mb-2">Admins</h2>
                    <p className="text-gray-600 mb-4">Manage users and global platform settings.</p>
                    <Link to="/admin/dashboard" className="text-blue-600 font-medium hover:underline">Admin Dashboard &rarr;</Link>
                </div>
            </div>
        </div>
    );
}

export default Landing;
