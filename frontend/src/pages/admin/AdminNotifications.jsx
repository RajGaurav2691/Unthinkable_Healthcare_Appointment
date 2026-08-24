import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../../api/axiosConfig';

const statusBadge = (status) => {
  const config = {
    SENT: { bg: '#dcfce7', color: '#15803d' },
    PENDING: { bg: '#fef9c3', color: '#a16207' },
    FAILED: { bg: '#fee2e2', color: '#dc2626' },
  };
  const s = config[status] || { bg: '#f3f4f6', color: '#374151' };
  return (
    <span style={{
      background: s.bg, color: s.color,
      padding: '2px 10px', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600
    }}>{status}</span>
  );
};

export default function AdminNotifications() {
  const [searchParams] = useSearchParams();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState(searchParams.get('status') || 'ALL');

  useEffect(() => {
    setLoading(true);
    const params = filter !== 'ALL' ? { status: filter } : {};
    api.get('/api/admin/notifications', { params })
      .then(res => setNotifications(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [filter]);

  const failedCount = notifications.filter(n => n.status === 'FAILED').length;

  return (
    <div className="admin-page">
      <div className="admin-page__header">
        <h1>Notification Status</h1>
        <p>{notifications.length} notification(s) {failedCount > 0 && <span style={{ color: '#dc2626', fontWeight: 700 }}>— {failedCount} failed</span>}</p>
      </div>

      <div className="filter-tabs">
        {['ALL', 'SENT', 'PENDING', 'FAILED'].map(s => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`filter-tab ${filter === s ? 'filter-tab--active' : ''}`}
          >
            {s}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="loading-spinner">Loading...</div>
      ) : notifications.length === 0 ? (
        <div className="empty-state">No notifications found.</div>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Recipient</th>
                <th>Type</th>
                <th>Subject</th>
                <th>Status</th>
                <th>Retries</th>
                <th>Error</th>
              </tr>
            </thead>
            <tbody>
              {notifications
                .filter(n => filter === 'ALL' || n.status === filter)
                .map(n => (
                  <tr key={n.id} style={{ background: n.status === 'FAILED' ? '#fff1f2' : 'inherit' }}>
                    <td>#{n.id}</td>
                    <td>{n.recipient}</td>
                    <td style={{ fontSize: '0.75rem' }}>{n.type}</td>
                    <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{n.subject}</td>
                    <td>{statusBadge(n.status)}</td>
                    <td>{n.retryCount ?? 0}</td>
                    <td style={{ color: '#dc2626', fontSize: '0.75rem', maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {n.errorMessage || '—'}
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
