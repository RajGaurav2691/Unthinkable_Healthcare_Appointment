import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function PatientAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const response = await api.get('/appointments/patient');
      setAppointments(response.data);
    } catch (err) {
      console.error('Failed to fetch appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      await api.patch(`/appointments/${id}/cancel`);
      fetchAppointments();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
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

  if (loading) return <div className="p-10 text-center">Loading appointments...</div>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">My Appointments</h2>
        <Link 
          to="/patient/dashboard"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold shadow-sm transition"
        >
          Book New Appointment
        </Link>
      </div>

      {appointments.length === 0 ? (
        <div className="bg-white p-10 text-center rounded-xl shadow-sm border border-gray-100 text-gray-500">
          You don't have any appointments yet.
        </div>
      ) : (
        <div className="grid gap-6">
          {appointments.map(appt => (
            <div key={appt.id} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h3 className="text-xl font-bold text-gray-800">Dr. {appt.doctorName}</h3>
                  <span className={`px-2 py-1 text-xs font-bold rounded border uppercase ${getStatusColor(appt.status)}`}>
                    {appt.status}
                  </span>
                </div>
                <p className="text-gray-600 mb-1">{appt.doctorSpecialization}</p>
                <div className="text-gray-800 font-semibold flex items-center gap-2">
                  <span>📅 {appt.appointmentDate}</span>
                  <span>⏰ {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}</span>
                </div>
                {appt.status === 'HELD' && (
                  <p className="text-sm text-yellow-600 mt-2 font-semibold">
                    Payment/Confirmation pending. <Link to={`/patient/book/${appt.id}`} className="underline">Complete booking</Link>.
                  </p>
                )}
              </div>
              
              <div className="flex gap-2 w-full md:w-auto">
                {(appt.status === 'CONFIRMED' || appt.status === 'HELD') && (
                  <button 
                    onClick={() => handleCancel(appt.id)}
                    className="w-full md:w-auto px-4 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition font-semibold"
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
