import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function DoctorAppointmentDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        const response = await api.get(`/doctor/appointments/${id}`);
        setAppointment(response.data);
      } catch (err) {
        console.error('Failed to fetch appointment', err);
        navigate('/doctor/dashboard');
      } finally {
        setLoading(false);
      }
    };
    fetchAppointment();
  }, [id, navigate]);

  const handleCancel = async () => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      await api.patch(`/appointments/${id}/cancel`);
      navigate('/doctor/dashboard');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
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

  if (loading) return <div className="p-10 text-center text-xl">Loading details...</div>;
  if (!appointment) return null;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <Link to="/doctor/dashboard" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
      </div>

      <div className="bg-white p-8 rounded-xl shadow-lg border border-gray-100">
        <div className="flex justify-between items-start mb-6 border-b pb-6">
          <div>
            <h2 className="text-3xl font-bold text-gray-800 mb-2">Appointment Details</h2>
            <div className="flex gap-4 text-gray-600 font-semibold">
              <span>📅 {appointment.appointmentDate}</span>
              <span>⏰ {appointment.startTime.substring(0, 5)} - {appointment.endTime.substring(0, 5)}</span>
            </div>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500 font-bold mb-1">Status</div>
            <span className="px-3 py-1 bg-gray-100 text-gray-800 font-bold rounded uppercase border">
              {appointment.status}
            </span>
          </div>
        </div>

        <div className="mb-8">
          <h3 className="text-xl font-bold text-gray-800 mb-4">Patient Information</h3>
          <p className="text-lg text-gray-700"><strong>Name:</strong> {appointment.patientName}</p>
        </div>

        <div className="mb-8">
          <h3 className="text-xl font-bold text-gray-800 mb-4">Patient Symptoms</h3>
          <div className="bg-gray-50 p-4 rounded-lg text-gray-700 whitespace-pre-wrap border border-gray-200">
            {appointment.symptoms || 'No symptoms provided.'}
          </div>
        </div>

        {appointment.aiSummary && (
          <div className="mb-8">
            <h3 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              ✨ AI Assessment
              {appointment.urgencyLevel && (
                <span className={`px-2 py-1 text-xs uppercase border rounded ${getUrgencyColor(appointment.urgencyLevel)}`}>
                  {appointment.urgencyLevel} URGENCY
                </span>
              )}
            </h3>
            <div className="bg-blue-50 p-4 rounded-lg text-blue-900 border border-blue-100">
              {appointment.aiSummary}
            </div>
          </div>
        )}

        <div className="flex justify-end pt-6 border-t">
          {(appointment.status === 'CONFIRMED' || appointment.status === 'HELD') && (
            <button 
              onClick={handleCancel}
              className="px-6 py-2 border border-red-300 text-red-600 font-bold rounded-lg hover:bg-red-50 transition"
            >
              Cancel Appointment
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
