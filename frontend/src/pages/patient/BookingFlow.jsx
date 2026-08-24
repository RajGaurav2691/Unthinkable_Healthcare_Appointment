import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function BookingFlow() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState(null);
  const [symptoms, setSymptoms] = useState('');
  const [timeLeft, setTimeLeft] = useState(600); // 10 mins
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        const response = await api.get(`/appointments/${id}`);
        const appt = response.data;
        if (appt.status !== 'HELD') {
          navigate('/patient/appointments');
          return;
        }
        
        // Calculate remaining time
        const holdTime = new Date(appt.createdAt).getTime();
        const now = new Date().getTime();
        const diffSeconds = Math.floor((10 * 60 * 1000 - (now - holdTime)) / 1000);
        
        if (diffSeconds <= 0) {
          alert('Slot hold expired.');
          navigate('/patient/dashboard');
          return;
        }
        
        setTimeLeft(diffSeconds);
        setAppointment(appt);
      } catch (err) {
        console.error('Failed to fetch appointment', err);
        navigate('/patient/dashboard');
      }
    };
    fetchAppointment();
  }, [id, navigate]);

  useEffect(() => {
    if (timeLeft <= 0) {
      alert('Your hold on this slot has expired.');
      navigate('/patient/dashboard');
      return;
    }

    const timerId = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timerId);
  }, [timeLeft, navigate]);

  const handleConfirm = async (e) => {
    e.preventDefault();
    if (!symptoms.trim()) {
      setError('Please describe your symptoms');
      return;
    }

    setSubmitting(true);
    try {
      await api.post(`/appointments/${id}/confirm`, { symptoms });
      navigate('/patient/appointments');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to confirm appointment');
      setSubmitting(false);
    }
  };

  const handleCancel = async () => {
    try {
      await api.patch(`/appointments/${id}/cancel`);
      navigate('/patient/dashboard');
    } catch (err) {
      console.error('Cancel failed', err);
    }
  };

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  if (!appointment) return <div className="p-10 text-center text-xl">Loading...</div>;

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="bg-white p-8 rounded-xl shadow-lg border border-gray-100">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold text-gray-800">Complete Booking</h2>
          <div className="bg-orange-100 text-orange-700 font-mono font-bold px-4 py-2 rounded-lg text-xl flex items-center gap-2">
            ⏱ {formatTime(timeLeft)}
          </div>
        </div>

        <div className="bg-blue-50 p-6 rounded-lg mb-8 text-blue-900">
          <h3 className="font-bold text-lg mb-2">Appointment Details</h3>
          <p><strong>Doctor:</strong> Dr. {appointment.doctorName} ({appointment.doctorSpecialization})</p>
          <p><strong>Date:</strong> {appointment.appointmentDate}</p>
          <p><strong>Time:</strong> {appointment.startTime.substring(0, 5)} - {appointment.endTime.substring(0, 5)}</p>
        </div>

        {error && <div className="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</div>}

        <form onSubmit={handleConfirm}>
          <div className="mb-6">
            <label className="block text-gray-700 font-bold mb-2">Describe your symptoms (Required)</label>
            <p className="text-sm text-gray-500 mb-2">This helps the doctor understand your situation before the visit. An AI summary will also be generated.</p>
            <textarea
              className="w-full border rounded-lg p-3 h-32 focus:ring-2 focus:ring-blue-500"
              placeholder="E.g., I have been having a severe headache for the past two days..."
              value={symptoms}
              onChange={(e) => setSymptoms(e.target.value)}
              disabled={submitting}
            />
          </div>

          <div className="flex justify-between items-center">
            <button 
              type="button" 
              onClick={handleCancel}
              className="px-6 py-2 border border-gray-300 text-gray-600 font-bold rounded-lg hover:bg-gray-100 transition"
              disabled={submitting}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={submitting}
              className="px-8 py-3 bg-blue-600 text-white font-bold rounded-lg hover:bg-blue-700 shadow-md transition disabled:bg-blue-400"
            >
              {submitting ? 'Confirming...' : 'Confirm Appointment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
