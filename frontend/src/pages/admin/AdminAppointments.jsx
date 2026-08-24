import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../../api/axiosConfig';

const STATUS_OPTIONS = ['ALL', 'HELD', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'];

const statusBadge = (status) => {
  const colors = {
    CONFIRMED: '#2563eb', COMPLETED: '#16a34a',
    CANCELLED: '#dc2626', HELD: '#d97706',
    NO_SHOW: '#9333ea'
  };
  return (
    <span style={{
      background: colors[status] || '#6b7280',
      color: '#fff', padding: '2px 10px',
      borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600
    }}>{status}</span>
  );
};

export default function AdminAppointments() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState(searchParams.get('status') || 'ALL');

  useEffect(() => {
    setLoading(true);
    const params = filter !== 'ALL' ? { status: filter } : {};
    api.get('/api/admin/appointments', { params })
      .then(res => setAppointments(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [filter]);

  const handleFilter = (status) => {
    setFilter(status);
    if (status !== 'ALL') setSearchParams({ status });
    else setSearchParams({});
  };

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <h1>All Appointments</h1>
        <p>{appointments.length} record(s)</p>
      </div>

      <div className="filter-tabs">
        {STATUS_OPTIONS.map(s => (
          <button
            key={s}
            onClick={() => handleFilter(s)}
            className={`filter-tab ${filter === s ? 'filter-tab--active' : ''}`}
          >
            {s}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="loading-spinner">Loading...</div>
      ) : appointments.length === 0 ? (
        <div className="empty-state">No appointments found for the selected filter.</div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Patient</th>
                <th>Doctor</th>
                <th>Date</th>
                <th>Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map(a => (
                <tr key={a.id}>
                  <td>#{a.id}</td>
                  <td>{a.patient?.name || '—'}</td>
                  <td>Dr. {a.doctor?.user?.name || '—'}</td>
                  <td>{a.appointmentDate}</td>
                  <td>{a.startTime}</td>
                  <td>{statusBadge(a.status)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
