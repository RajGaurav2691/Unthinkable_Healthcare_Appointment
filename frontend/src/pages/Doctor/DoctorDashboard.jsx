import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function DoctorDashboard() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const response = await api.get('/doctor/appointments');
      setAppointments(response.data);
    } catch (err) {
      console.error('Failed to fetch appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'bg-green-100 text-green-800 border-green-200';
      case 'HELD': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'CANCELLED': return 'bg-red-100 text-red-800 border-red-200';
      case 'COMPLETED': return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'NO_SHOW': return 'bg-purple-100 text-purple-800 border-purple-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getUrgencyColor = (level) => {
    switch (level) {
      case 'HIGH': return 'text-red-600 bg-red-50';
      case 'MEDIUM': return 'text-orange-600 bg-orange-50';
      case 'LOW': return 'text-green-600 bg-green-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  if (loading) return <div className="p-10 text-center">Loading appointments...</div>;

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Doctor Dashboard</h2>
      </div>

      <div className="mb-6">
        <h3 className="text-2xl font-bold text-gray-700 mb-4">Your Appointments</h3>
      </div>

      {appointments.length === 0 ? (
        <div className="bg-white p-10 text-center rounded-xl shadow-sm border border-gray-100 text-gray-500">
          You don't have any appointments booked yet.
        </div>
      ) : (
        <div className="grid gap-6">
          {appointments.map(appt => (
            <div key={appt.id} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <h3 className="text-xl font-bold text-gray-800">{appt.patientName}</h3>
                  <span className={`px-2 py-1 text-xs font-bold rounded border uppercase ${getStatusColor(appt.status)}`}>
                    {appt.status}
                  </span>
                  {appt.urgencyLevel && (
                    <span className={`px-2 py-1 text-xs font-bold rounded border uppercase ${getUrgencyColor(appt.urgencyLevel)}`}>
                      {appt.urgencyLevel} URGENCY
                    </span>
                  )}
                </div>
                
                <div className="text-gray-800 font-semibold flex items-center gap-2 mb-3">
                  <span>📅 {appt.appointmentDate}</span>
                  <span>⏰ {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}</span>
                </div>

                {appt.aiSummary && (
                  <div className="bg-blue-50 p-3 rounded-lg text-sm text-blue-900 border border-blue-100">
                    <strong>AI Summary:</strong> {appt.aiSummary}
                  </div>
                )}
              </div>
              
              <div className="flex flex-col gap-2 w-full md:w-auto">
                <Link 
                  to={`/doctor/appointments/${appt.id}`}
                  className="w-full md:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold text-center shadow-sm"
                >
                  View Details
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
