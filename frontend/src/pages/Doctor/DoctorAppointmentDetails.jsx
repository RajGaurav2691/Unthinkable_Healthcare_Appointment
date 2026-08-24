import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function DoctorAppointmentDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState('');
  const [prescription, setPrescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        const response = await api.get(`/doctor/appointments/${id}`);
        setAppointment(response.data);
        if (response.data.clinicalNotes) setNotes(response.data.clinicalNotes);
        if (response.data.prescription) setPrescription(response.data.prescription);
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
      window.location.reload();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
    }
  };

  const handleComplete = async (e) => {
    e.preventDefault();
    if (!notes.trim() || !prescription.trim()) {
      alert("Both clinical notes and prescription are required to complete the appointment.");
      return;
    }
    setSubmitting(true);
    try {
      await api.patch(`/doctor/appointments/${id}/complete`, {
        clinicalNotes: notes,
        prescription: prescription
      });
      window.location.reload();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to complete appointment');
    } finally {
      setSubmitting(false);
    }
  };

  const handleNoShow = async () => {
    if (!window.confirm('Are you sure you want to mark this patient as a No-Show?')) return;
    try {
      await api.patch(`/doctor/appointments/${id}/no-show`);
      window.location.reload();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to mark no-show');
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

        {appointment.status === 'CONFIRMED' && (
          <form onSubmit={handleComplete} className="mb-8 p-6 bg-blue-50 rounded-xl border border-blue-100">
            <h3 className="text-xl font-bold text-blue-900 mb-4">Complete Appointment</h3>
            
            <div className="mb-4">
              <label className="block text-blue-800 font-semibold mb-2">Clinical Notes</label>
              <textarea 
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500" 
                rows="4"
                placeholder="Enter clinical notes..."
                value={notes}
                onChange={e => setNotes(e.target.value)}
                required
              />
            </div>
            
            <div className="mb-4">
              <label className="block text-blue-800 font-semibold mb-2">Prescription</label>
              <textarea 
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500" 
                rows="3"
                placeholder="Enter prescription instructions..."
                value={prescription}
                onChange={e => setPrescription(e.target.value)}
                required
              />
            </div>

            <button 
              type="submit" 
              disabled={submitting}
              className="px-6 py-2 bg-blue-600 text-white font-bold rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
            >
              {submitting ? 'Saving...' : 'Mark as Completed'}
            </button>
          </form>
        )}

        {appointment.status === 'COMPLETED' && (
          <div className="mb-8 p-6 bg-gray-50 rounded-xl border border-gray-200">
            <h3 className="text-xl font-bold text-gray-800 mb-4 border-b pb-2">Post-Visit Summary</h3>
            <div className="mb-4">
              <h4 className="font-semibold text-gray-700">Clinical Notes</h4>
              <p className="text-gray-600 whitespace-pre-wrap">{appointment.clinicalNotes}</p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-700">Prescription</h4>
              <p className="text-gray-600 whitespace-pre-wrap italic">{appointment.prescription}</p>
            </div>
          </div>
        )}

        <div className="flex justify-end pt-6 border-t gap-4">
          {appointment.status === 'CONFIRMED' && (
            <button 
              onClick={handleNoShow}
              className="px-6 py-2 border border-purple-300 text-purple-600 font-bold rounded-lg hover:bg-purple-50 transition"
            >
              Mark No-Show
            </button>
          )}
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
