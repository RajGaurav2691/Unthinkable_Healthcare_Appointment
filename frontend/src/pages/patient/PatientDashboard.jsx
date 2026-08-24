import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';
import { useAuth } from '../../context/AuthContext';

const PatientDashboard = () => {
    const { user } = useAuth();
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const res = await api.get('/appointments/patient');
                setAppointments(res.data);
            } catch (err) {
                setError(err.response?.data?.error || 'Failed to load dashboard data');
            } finally {
                setLoading(false);
            }
        };
        fetchDashboardData();
    }, []);

    if (loading) {
        return <div className="flex justify-center items-center h-64"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div></div>;
    }

    if (error) {
        return <div className="p-4 bg-red-100 text-red-700 rounded-md text-center">{error}</div>;
    }

    const upcomingAppointments = appointments.filter(a => ['HELD', 'CONFIRMED'].includes(a.status));
    const nextAppointment = upcomingAppointments.length > 0 ? upcomingAppointments[0] : null;
    const completedAppointments = appointments.filter(a => a.status === 'COMPLETED');
    const recentVisit = completedAppointments.length > 0 ? completedAppointments[0] : null;

    return (
        <div className="max-w-6xl mx-auto space-y-8 animate-fade-in">
            <header className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-gray-800">Welcome, {user.name}</h1>
                <Link to="/patient/doctors" className="bg-blue-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-blue-700 transition shadow-lg hover:shadow-xl transform hover:-translate-y-0.5">
                    Find a Doctor
                </Link>
            </header>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Next Appointment Widget */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <h2 className="text-xl font-bold text-gray-800 mb-4 border-b pb-2">Next Appointment</h2>
                    {nextAppointment ? (
                        <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                            <div className="flex justify-between items-start mb-2">
                                <div>
                                    <p className="font-semibold text-blue-900 text-lg">Dr. {nextAppointment.doctorName}</p>
                                    <p className="text-blue-700 text-sm">{nextAppointment.doctorSpecialization}</p>
                                </div>
                                <span className="bg-blue-200 text-blue-800 text-xs px-2 py-1 rounded-full font-bold uppercase">
                                    {nextAppointment.status}
                                </span>
                            </div>
                            <div className="mt-4 pt-4 border-t border-blue-200/50 flex justify-between items-center">
                                <div>
                                    <p className="text-blue-900 font-medium">Date & Time</p>
                                    <p className="text-blue-800 text-sm">{nextAppointment.appointmentDate} at {nextAppointment.startTime}</p>
                                </div>
                                <Link to="/patient/appointments" className="text-sm font-semibold text-blue-700 hover:text-blue-900 underline">Manage</Link>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-xl border border-dashed border-gray-200">
                            <p>You have no upcoming appointments.</p>
                            <Link to="/patient/doctors" className="text-blue-600 hover:underline mt-2 inline-block">Book one now</Link>
                        </div>
                    )}
                </div>

                {/* Recent Visit / Medication Widget */}
                <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col">
                    <h2 className="text-xl font-bold text-gray-800 mb-4 border-b pb-2">Recent Visit & Medication</h2>
                    {recentVisit ? (
                        <div className="flex-grow flex flex-col justify-between">
                            <div>
                                <p className="text-sm text-gray-500 mb-1">Last visit with Dr. {recentVisit.doctorName} on {recentVisit.appointmentDate}</p>
                                <div className="bg-green-50 rounded-xl p-4 border border-green-100 mt-3">
                                    <h3 className="font-semibold text-green-900 mb-2">Clinical Notes:</h3>
                                    <p className="text-green-800 text-sm line-clamp-2">{recentVisit.clinicalNotes || "No notes provided."}</p>
                                </div>
                            </div>
                            
                            <div className="bg-purple-50 rounded-xl p-4 border border-purple-100 mt-4">
                                <h3 className="font-semibold text-purple-900 mb-2 flex items-center">
                                    <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
                                    Prescription
                                </h3>
                                <p className="text-purple-800 text-sm italic">{recentVisit.prescription || "No prescription issued."}</p>
                            </div>
                        </div>
                    ) : (
                         <div className="text-center py-8 flex-grow flex flex-col justify-center items-center text-gray-500 bg-gray-50 rounded-xl border border-dashed border-gray-200">
                             <svg className="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
                             <p>No past visits found.</p>
                         </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PatientDashboard;
