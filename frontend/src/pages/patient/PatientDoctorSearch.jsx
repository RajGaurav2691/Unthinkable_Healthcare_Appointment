import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

export default function PatientDoctorSearch() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchSpec, setSearchSpec] = useState('');

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async (spec = '') => {
    setLoading(true);
    try {
      const url = spec ? `/doctors/search?specialization=${encodeURIComponent(spec)}` : `/doctors`;
      const response = await api.get(url);
      setDoctors(response.data);
    } catch (error) {
      console.error('Failed to fetch doctors', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchDoctors(searchSpec);
  };

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <h2 className="text-3xl font-bold mb-6 text-gray-800">Find a Doctor</h2>
      
      <form onSubmit={handleSearch} className="mb-8 flex gap-4">
        <input 
          type="text" 
          placeholder="Search by Specialization (e.g. Cardiologist)" 
          value={searchSpec}
          onChange={(e) => setSearchSpec(e.target.value)}
          className="border rounded p-3 flex-1 text-lg shadow-sm focus:ring-2 focus:ring-blue-500"
        />
        <button type="submit" className="bg-blue-600 text-white px-8 py-3 rounded font-bold shadow hover:bg-blue-700 transition">
          Search
        </button>
      </form>

      {loading ? (
        <div className="text-center text-xl text-gray-500 py-10">Loading doctors...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {doctors.map(doctor => (
            <div key={doctor.id} className="bg-white p-6 rounded-xl shadow-md border border-gray-100 hover:shadow-lg transition">
              <h3 className="text-2xl font-bold text-gray-800 mb-1">Dr. {doctor.name}</h3>
              <p className="text-blue-600 font-semibold mb-2">{doctor.specialization}</p>
              <p className="text-gray-600 text-sm mb-4">{doctor.qualification} • {doctor.experience} yrs exp.</p>
              
              <Link to={`/patient/doctors/${doctor.id}`} className="block w-full text-center bg-gray-50 text-blue-600 border border-blue-200 font-semibold py-2 rounded hover:bg-blue-50 transition">
                View Profile & Book
              </Link>
            </div>
          ))}
          {doctors.length === 0 && (
            <div className="col-span-full text-center text-gray-500 py-10 text-xl">
              No doctors found matching your search.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
