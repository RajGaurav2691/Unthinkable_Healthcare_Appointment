import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

export default function DoctorProfileManager() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
  const [formData, setFormData] = useState({
    name: '',
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

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await api.get('/doctor/profile');
      const p = res.data;
      setProfile(p);
      setFormData({
        name: p.name,
        specialization: p.specialization,
        qualification: p.qualification,
        experience: p.experience,
        consultationDuration: p.consultationDuration,
        activeStatus: p.activeStatus,
        schedules: p.schedules.map(s => ({ dayOfWeek: s.dayOfWeek, startTime: s.startTime, endTime: s.endTime }))
      });
      setLeaves(p.leaves || []);
    } catch (err) {
      console.error(err);
      alert('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put('/doctor/profile', formData);
      alert('Profile successfully updated!');
      fetchProfile();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleAddSchedule = () => {
    setFormData({
      ...formData,
      schedules: [...formData.schedules, { dayOfWeek: 'MONDAY', startTime: '09:00:00', endTime: '17:00:00' }]
    });
  };

  const updateSchedule = (index, field, value) => {
    const newSchedules = [...formData.schedules];
    newSchedules[index][field] = value;
    setFormData({ ...formData, schedules: newSchedules });
  };

  const removeSchedule = (index) => {
    const newSchedules = formData.schedules.filter((_, i) => i !== index);
    setFormData({ ...formData, schedules: newSchedules });
  };

  const handleAddLeave = async (e) => {
    e.preventDefault();
    try {
      await api.post('/doctor/profile/leave', { leaveDate, reason: leaveReason });
      setLeaveDate('');
      setLeaveReason('');
      fetchProfile();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add leave');
    }
  };

  const handleRemoveLeave = async (leaveId) => {
    if(!window.confirm("Remove this leave?")) return;
    try {
      await api.delete(`/doctor/profile/leave/${leaveId}`);
      fetchProfile();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to remove leave');
    }
  };

  if (loading) return <div className="p-10 text-center">Loading profile...</div>;

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-8">
      <h2 className="text-3xl font-bold text-gray-800">My Profile & Schedule</h2>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 className="text-xl font-bold text-gray-700 mb-4 border-b pb-2">Professional Details & Working Hours</h3>
        <form onSubmit={handleProfileSave}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-gray-700 font-semibold mb-1">Name</label>
              <input type="text" className="w-full border p-2 rounded" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">Specialization</label>
              <input type="text" className="w-full border p-2 rounded" required value={formData.specialization} onChange={e => setFormData({...formData, specialization: e.target.value})} />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">Qualification</label>
              <input type="text" className="w-full border p-2 rounded" required value={formData.qualification} onChange={e => setFormData({...formData, qualification: e.target.value})} />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">Experience (Years)</label>
              <input type="number" className="w-full border p-2 rounded" required value={formData.experience} onChange={e => setFormData({...formData, experience: parseInt(e.target.value)})} />
            </div>
            <div>
              <label className="block text-gray-700 font-semibold mb-1">Consultation Duration (mins)</label>
              <input type="number" className="w-full border p-2 rounded" required value={formData.consultationDuration} onChange={e => setFormData({...formData, consultationDuration: parseInt(e.target.value)})} />
            </div>
          </div>

          <div className="mb-6">
            <div className="flex justify-between items-center mb-2">
              <label className="block text-gray-700 font-semibold">Working Schedules</label>
              <button type="button" onClick={handleAddSchedule} className="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded hover:bg-blue-200">
                + Add Schedule
              </button>
            </div>
            
            {formData.schedules.map((schedule, idx) => (
              <div key={idx} className="flex gap-2 mb-2 items-center">
                <select className="border p-2 rounded w-1/3" value={schedule.dayOfWeek} onChange={e => updateSchedule(idx, 'dayOfWeek', e.target.value)}>
                  {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].map(day => (
                    <option key={day} value={day}>{day}</option>
                  ))}
                </select>
                <input type="time" className="border p-2 rounded w-1/3" step="1" required value={schedule.startTime} onChange={e => updateSchedule(idx, 'startTime', e.target.value)} />
                <input type="time" className="border p-2 rounded w-1/3" step="1" required value={schedule.endTime} onChange={e => updateSchedule(idx, 'endTime', e.target.value)} />
                <button type="button" onClick={() => removeSchedule(idx)} className="text-red-500 font-bold px-2 hover:bg-red-50 rounded">X</button>
              </div>
            ))}
          </div>

          <button type="submit" disabled={saving} className="bg-blue-600 text-white px-6 py-2 rounded font-bold hover:bg-blue-700 disabled:opacity-50">
            {saving ? 'Saving...' : 'Save Profile & Schedule'}
          </button>
        </form>
      </div>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 className="text-xl font-bold text-gray-700 mb-4 border-b pb-2">Leave Management</h3>
        <form onSubmit={handleAddLeave} className="flex gap-4 items-end mb-6">
          <div className="flex-1">
            <label className="block text-gray-700 font-semibold mb-1">Leave Date</label>
            <input type="date" className="w-full border p-2 rounded" required value={leaveDate} onChange={e => setLeaveDate(e.target.value)} min={new Date().toISOString().split('T')[0]} />
          </div>
          <div className="flex-[2]">
            <label className="block text-gray-700 font-semibold mb-1">Reason</label>
            <input type="text" className="w-full border p-2 rounded" required value={leaveReason} onChange={e => setLeaveReason(e.target.value)} placeholder="E.g. Personal vacation" />
          </div>
          <button type="submit" className="bg-purple-600 text-white px-6 py-2 rounded font-bold hover:bg-purple-700 h-10">
            Add Leave
          </button>
        </form>

        <div>
          {leaves.length === 0 ? (
            <p className="text-gray-500 italic">No upcoming leaves.</p>
          ) : (
            <ul className="space-y-2">
              {leaves.map(l => (
                <li key={l.id} className="flex justify-between items-center bg-gray-50 p-3 rounded border">
                  <div>
                    <span className="font-bold text-gray-800">{l.leaveDate}</span>
                    <span className="text-gray-500 ml-4">{l.reason}</span>
                  </div>
                  <button onClick={() => handleRemoveLeave(l.id)} className="text-red-600 hover:underline text-sm font-semibold">Remove</button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

    </div>
  );
}
