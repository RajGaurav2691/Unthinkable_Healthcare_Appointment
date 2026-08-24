import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function DoctorProfile() {
  const { id } = useParams();
  const [doctor, setDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  
  useEffect(() => {
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    setSelectedDate(today);

    const fetchDoctor = async () => {
      try {
        const response = await api.get(`/doctors/${id}`);
        setDoctor(response.data);
      } catch (err) {
        console.error('Failed to fetch doctor', err);
      }
    };
    fetchDoctor();
  }, [id]);

  useEffect(() => {
    if (selectedDate && doctor) {
      fetchAvailability(selectedDate);
    }
  }, [selectedDate, doctor]);

  const fetchAvailability = async (date) => {
    setLoadingSlots(true);
    try {
      const response = await api.get(`/doctors/${id}/availability?date=${date}`);
      setSlots(response.data);
    } catch (err) {
      console.error('Failed to fetch availability', err);
      setSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  if (!doctor) return <div className="p-10 text-center text-xl">Loading profile...</div>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-6">
        <Link to="/patient/dashboard" className="text-blue-600 hover:underline">← Back to Search</Link>
      </div>

      <div className="bg-white p-8 rounded-xl shadow-md border border-gray-100 mb-8">
        <h2 className="text-4xl font-bold text-gray-800 mb-2">Dr. {doctor.name}</h2>
        <p className="text-xl text-blue-600 font-semibold mb-4">{doctor.specialization}</p>
        <div className="flex gap-8 text-gray-600">
          <div>
            <span className="font-bold text-gray-800">Qualification:</span> {doctor.qualification}
          </div>
          <div>
            <span className="font-bold text-gray-800">Experience:</span> {doctor.experience} years
          </div>
          <div>
            <span className="font-bold text-gray-800">Consultation:</span> {doctor.consultationDuration} mins
          </div>
        </div>
      </div>

      <div className="bg-white p-8 rounded-xl shadow-md border border-gray-100">
        <h3 className="text-2xl font-bold text-gray-800 mb-6">Book an Appointment</h3>
        
        <div className="mb-6 max-w-sm">
          <label className="block text-gray-700 font-bold mb-2">Select Date</label>
          <input 
            type="date" 
            value={selectedDate} 
            onChange={(e) => setSelectedDate(e.target.value)}
            min={new Date().toISOString().split('T')[0]} // prevent past dates
            className="w-full border rounded-lg p-3 text-lg shadow-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <h4 className="text-lg font-bold text-gray-700 mb-4">Available Slots</h4>
          {loadingSlots ? (
            <div className="text-gray-500">Checking availability...</div>
          ) : slots.length > 0 ? (
            <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-4">
              {slots.map((slot, index) => (
                <button 
                  key={index} 
                  className="py-2 px-3 border border-blue-200 text-blue-700 rounded-lg hover:bg-blue-600 hover:text-white transition font-semibold"
                  title={slot.status}
                >
                  {slot.startTime.substring(0, 5)}
                </button>
              ))}
            </div>
          ) : (
            <div className="p-4 bg-gray-50 border rounded-lg text-center text-gray-500">
              No slots available for this date.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
