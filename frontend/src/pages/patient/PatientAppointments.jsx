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
              <div className="flex-grow">
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
                {appt.status === 'COMPLETED' && (
                  <div className="mt-4 flex flex-col gap-4">
                    <div className="p-4 bg-gray-50 rounded-lg border border-gray-100 w-full">
                      <div className="mb-3">
                        <h4 className="font-semibold text-gray-700 text-sm uppercase tracking-wide">Clinical Notes</h4>
                        <p className="text-gray-600 mt-1 whitespace-pre-wrap">{appt.clinicalNotes || "No notes provided."}</p>
                      </div>
                      <div>
                        <h4 className="font-semibold text-gray-700 text-sm uppercase tracking-wide">Prescription</h4>
                        <p className="text-gray-600 mt-1 whitespace-pre-wrap italic">{appt.prescription || "No prescription issued."}</p>
                      </div>
                    </div>

                    {(() => {
                      try {
                        if (!appt.postVisitAiSummary) return null;
                        const parsed = JSON.parse(appt.postVisitAiSummary);
                        return (
                          <div className="p-5 bg-blue-50 rounded-lg border border-blue-100 w-full">
                            <h4 className="font-bold text-blue-900 flex items-center gap-2 mb-3 border-b border-blue-200 pb-2">
                              ✨ AI Post-Visit Summary
                            </h4>
                            <div className="mb-4">
                              <p className="text-blue-800">{parsed.summary}</p>
                            </div>
                            
                            {parsed.medications && parsed.medications.length > 0 && (
                              <div className="mb-4">
                                <h5 className="font-semibold text-blue-900 mb-2">Medication Schedule:</h5>
                                <div className="overflow-hidden rounded border border-blue-200">
                                  <table className="min-w-full text-sm text-left">
                                    <thead className="bg-blue-100 text-blue-900">
                                      <tr>
                                        <th className="px-4 py-2 border-b border-blue-200">Medication</th>
                                        <th className="px-4 py-2 border-b border-blue-200">Dosage / Frequency</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      {parsed.medications.map((med, idx) => (
                                        <tr key={idx} className="border-b border-blue-100 last:border-0 bg-white">
                                          <td className="px-4 py-2 text-gray-800 font-medium">{med.name}</td>
                                          <td className="px-4 py-2 text-gray-600">{med.dosage}</td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              </div>
                            )}

                            {parsed.followUpInstructions && (
                              <div>
                                <h5 className="font-semibold text-blue-900 mb-1">Follow-up:</h5>
                                <p className="text-blue-800">{parsed.followUpInstructions}</p>
                              </div>
                            )}
                            
                            <div className="mt-4 pt-3 border-t border-blue-200">
                              <p className="text-xs text-blue-600 italic">
                                <strong>Disclaimer:</strong> This summary is automatically generated by AI to help you understand your doctor's notes. It is not a medical diagnosis. Always follow your doctor's exact prescription and clinical instructions.
                              </p>
                            </div>
                          </div>
                        );
                      } catch (e) {
                        return null;
                      }
                    })()}
                  </div>
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
