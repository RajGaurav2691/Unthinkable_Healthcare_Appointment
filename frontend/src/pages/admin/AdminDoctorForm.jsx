import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function AdminDoctorForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    specialization: '',
    qualification: '',
    experience: 0,
    consultationDuration: 30,
    activeStatus: true,
    schedules: []
  });

  const [leaveDate, setLeaveDate] = useState('');
  const [leaveReason, setLeaveReason] = useState('');
  const [leaves, setLeaves] = useState([]);
  
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEdit) {
      fetchDoctor();
    }
  }, [id]);

  const fetchDoctor = async () => {
    try {
      // Actually we don't have a specific get by id for admin, but we can use the patient endpoint which returns active status or we can fetch all and filter.
      // Let's fetch all and filter, or we can use the patient endpoint if it returns inactive doctors? The patient endpoint only returns active doctors by ID.
      // Wait, let's just fetch all admin doctors and find the one.
      const response = await api.get('/admin/doctors');
      const doctor = response.data.find(d => d.id === parseInt(id));
      if (doctor) {
        setFormData({
          name: doctor.name,
          email: doctor.email, // not used in update
          password: '',
          specialization: doctor.specialization,
          qualification: doctor.qualification,
          experience: doctor.experience,
          consultationDuration: doctor.consultationDuration,
          activeStatus: doctor.activeStatus,
          schedules: doctor.schedules || []
        });
        setLeaves(doctor.leaves || []);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleScheduleChange = (index, field, value) => {
    const newSchedules = [...formData.schedules];
    newSchedules[index][field] = value;
    setFormData(prev => ({ ...prev, schedules: newSchedules }));
  };

  const addSchedule = () => {
    setFormData(prev => ({
      ...prev,
      schedules: [...prev.schedules, { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00' }]
    }));
  };

  const removeSchedule = (index) => {
    const newSchedules = formData.schedules.filter((_, i) => i !== index);
    setFormData(prev => ({ ...prev, schedules: newSchedules }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      if (isEdit) {
        // Exclude email and password for update
        const { email, password, ...updateData } = formData;
        await api.put(`/admin/doctors/${id}`, updateData);
      } else {
        await api.post('/admin/doctors', formData);
      }
      navigate('/admin/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to save doctor');
    }
  };

  const handleAddLeave = async (e) => {
    e.preventDefault();
    if (!isEdit) return;
    try {
      const res = await api.post(`/admin/doctors/${id}/leave`, { leaveDate, reason: leaveReason });
      setLeaves(prev => [...prev, res.data]);
      setLeaveDate('');
      setLeaveReason('');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to add leave');
    }
  };

  const handleDeleteLeave = async (leaveId) => {
    try {
      await api.delete(`/admin/doctors/${id}/leave/${leaveId}`);
      setLeaves(prev => prev.filter(l => l.id !== leaveId));
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h2 className="text-3xl font-bold mb-6">{isEdit ? 'Edit Doctor' : 'Add New Doctor'}</h2>
      {error && <div className="mb-4 p-3 bg-red-100 text-red-700 rounded">{error}</div>}
      
      <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow border border-gray-100 mb-8">
        <div className="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label className="block text-gray-700 mb-1">Name</label>
            <input name="name" value={formData.name} onChange={handleChange} required className="w-full border rounded p-2" />
          </div>
          <div>
            <label className="block text-gray-700 mb-1">Email</label>
            <input name="email" value={formData.email} onChange={handleChange} required={!isEdit} disabled={isEdit} className="w-full border rounded p-2 disabled:bg-gray-100" />
          </div>
          {!isEdit && (
            <div>
              <label className="block text-gray-700 mb-1">Password</label>
              <input name="password" type="password" value={formData.password} onChange={handleChange} required className="w-full border rounded p-2" />
            </div>
          )}
          <div>
            <label className="block text-gray-700 mb-1">Specialization</label>
            <input name="specialization" value={formData.specialization} onChange={handleChange} required className="w-full border rounded p-2" />
          </div>
          <div>
            <label className="block text-gray-700 mb-1">Qualification</label>
            <input name="qualification" value={formData.qualification} onChange={handleChange} required className="w-full border rounded p-2" />
          </div>
          <div>
            <label className="block text-gray-700 mb-1">Experience (Years)</label>
            <input name="experience" type="number" value={formData.experience} onChange={handleChange} required className="w-full border rounded p-2" />
          </div>
          <div>
            <label className="block text-gray-700 mb-1">Consultation Duration (mins)</label>
            <input name="consultationDuration" type="number" value={formData.consultationDuration} onChange={handleChange} required className="w-full border rounded p-2" />
          </div>
          {isEdit && (
            <div className="flex items-center mt-6">
              <input name="activeStatus" type="checkbox" checked={formData.activeStatus} onChange={handleChange} className="mr-2" />
              <label className="text-gray-700">Active Status</label>
            </div>
          )}
        </div>

        <h3 className="text-xl font-bold mt-6 mb-2">Working Schedule</h3>
        {formData.schedules.map((schedule, index) => (
          <div key={index} className="flex gap-4 items-center mb-2">
            <select value={schedule.dayOfWeek} onChange={(e) => handleScheduleChange(index, 'dayOfWeek', e.target.value)} className="border rounded p-2">
              {['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'].map(d => <option key={d} value={d}>{d}</option>)}
            </select>
            <input type="time" value={schedule.startTime} onChange={(e) => handleScheduleChange(index, 'startTime', e.target.value)} required className="border rounded p-2" />
            <span>to</span>
            <input type="time" value={schedule.endTime} onChange={(e) => handleScheduleChange(index, 'endTime', e.target.value)} required className="border rounded p-2" />
            <button type="button" onClick={() => removeSchedule(index)} className="text-red-500 hover:text-red-700">Remove</button>
          </div>
        ))}
        <button type="button" onClick={addSchedule} className="text-blue-600 mt-2 hover:underline">+ Add Schedule</button>

        <div className="mt-8 flex gap-4">
          <button type="submit" className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">Save Doctor</button>
          <button type="button" onClick={() => navigate('/admin/dashboard')} className="bg-gray-300 text-gray-800 px-6 py-2 rounded hover:bg-gray-400">Cancel</button>
        </div>
      </form>

      {isEdit && (
        <div className="bg-white p-6 rounded-lg shadow border border-gray-100">
          <h3 className="text-xl font-bold mb-4">Leave Management</h3>
          <form onSubmit={handleAddLeave} className="flex gap-4 items-end mb-6">
            <div>
              <label className="block text-gray-700 mb-1">Leave Date</label>
              <input type="date" value={leaveDate} onChange={e => setLeaveDate(e.target.value)} required className="border rounded p-2" />
            </div>
            <div>
              <label className="block text-gray-700 mb-1">Reason</label>
              <input type="text" value={leaveReason} onChange={e => setLeaveReason(e.target.value)} className="border rounded p-2 w-64" />
            </div>
            <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">Add Leave</button>
          </form>

          <ul>
            {leaves.map(leave => (
              <li key={leave.id} className="flex justify-between items-center bg-gray-50 p-3 rounded mb-2 border">
                <span><strong>{leave.leaveDate}</strong> - {leave.reason || 'No reason provided'}</span>
                <button onClick={() => handleDeleteLeave(leave.id)} className="text-red-500 hover:text-red-700 text-sm">Delete</button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
