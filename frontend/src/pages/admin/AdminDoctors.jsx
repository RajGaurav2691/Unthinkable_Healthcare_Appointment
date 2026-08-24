import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function AdminDoctors() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
      const response = await api.get('/admin/doctors');
      setDoctors(response.data);
    } catch (error) {
      console.error('Failed to fetch doctors', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleStatus = async (id, currentStatus) => {
    try {
      await api.patch(`/admin/doctors/${id}/status?status=${!currentStatus}`);
      fetchDoctors();
    } catch (error) {
      console.error('Failed to update status', error);
    }
  };

  if (loading) return <div className="p-10 text-center">Loading...</div>;

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold">Manage Doctors</h2>
        <Link to="/admin/doctors/new" className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">
          + Add Doctor
        </Link>
      </div>

      <div className="bg-white rounded-lg shadow border border-gray-100 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b">
              <th className="p-4 font-semibold text-gray-700">Name</th>
              <th className="p-4 font-semibold text-gray-700">Specialization</th>
              <th className="p-4 font-semibold text-gray-700">Experience</th>
              <th className="p-4 font-semibold text-gray-700">Status</th>
              <th className="p-4 font-semibold text-gray-700">Actions</th>
            </tr>
          </thead>
          <tbody>
            {doctors.map(doctor => (
              <tr key={doctor.id} className="border-b hover:bg-gray-50 transition">
                <td className="p-4">{doctor.name}</td>
                <td className="p-4">{doctor.specialization}</td>
                <td className="p-4">{doctor.experience} yrs</td>
                <td className="p-4">
                  <button
                    onClick={() => toggleStatus(doctor.id, doctor.activeStatus)}
                    className={`px-3 py-1 rounded-full text-sm font-medium ${doctor.activeStatus ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}
                  >
                    {doctor.activeStatus ? 'Active' : 'Inactive'}
                  </button>
                </td>
                <td className="p-4">
                  <Link to={`/admin/doctors/${doctor.id}/edit`} className="text-blue-600 hover:underline mr-4">Edit</Link>
                </td>
              </tr>
            ))}
            {doctors.length === 0 && (
              <tr>
                <td colSpan="5" className="p-6 text-center text-gray-500">No doctors found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
