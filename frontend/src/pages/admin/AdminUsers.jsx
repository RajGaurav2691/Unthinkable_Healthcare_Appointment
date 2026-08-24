import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

export default function AdminUsers() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/admin/users')
      .then(res => setPatients(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <h1>Registered Patients</h1>
        <p>{patients.length} patient(s) registered</p>
      </div>

      {loading ? (
        <div className="loading-spinner">Loading...</div>
      ) : patients.length === 0 ? (
        <div className="empty-state">No patients registered yet.</div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>Email</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p, i) => (
                <tr key={p.id}>
                  <td>{i + 1}</td>
                  <td>{p.name}</td>
                  <td>{p.email}</td>
                  <td>
                    <span style={{
                      background: p.enabled ? '#dcfce7' : '#fee2e2',
                      color: p.enabled ? '#15803d' : '#dc2626',
                      padding: '2px 10px', borderRadius: '9999px', fontSize: '0.75rem'
                    }}>
                      {p.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
