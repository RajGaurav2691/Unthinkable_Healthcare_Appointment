import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';

const StatCard = ({ label, value, icon, color, linkTo }) => (
  <Link to={linkTo || '#'} className={`stat-card stat-card--${color}`}>
    <div className="stat-card__icon">{icon}</div>
    <div className="stat-card__content">
      <p className="stat-card__value">{value ?? '...'}</p>
      <p className="stat-card__label">{label}</p>
    </div>
  </Link>
);

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/admin/stats')
      .then(res => setStats(res.data))
      .catch(err => console.error('Failed to load stats', err))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="admin-dashboard">
      <div className="admin-dashboard__header">
        <h1>Admin Dashboard</h1>
        <p className="admin-dashboard__subtitle">System overview and management</p>
      </div>

      {loading ? (
        <div className="loading-spinner">Loading stats...</div>
      ) : (
        <div className="admin-stats-grid">
          <StatCard label="Total Patients" value={stats?.totalPatients} icon="👤" color="blue" linkTo="/admin/users" />
          <StatCard label="Total Doctors" value={stats?.totalDoctors} icon="🩺" color="green" linkTo="/admin/doctors" />
          <StatCard label="Active Doctors" value={stats?.activeDoctors} icon="✅" color="teal" linkTo="/admin/doctors" />
          <StatCard label="Appointments Today" value={stats?.appointmentsToday} icon="📅" color="purple" linkTo="/admin/appointments" />
          <StatCard label="Confirmed Upcoming" value={stats?.confirmedUpcoming} icon="🔜" color="indigo" linkTo="/admin/appointments?status=CONFIRMED" />
          <StatCard label="Cancelled Total" value={stats?.cancelledTotal} icon="❌" color="red" linkTo="/admin/appointments?status=CANCELLED" />
          <StatCard label="Failed Notifications" value={stats?.failedNotifications} icon="⚠️" color="orange" linkTo="/admin/notifications?status=failed" />
        </div>
      )}

      <div className="admin-quick-links">
        <h2>Quick Actions</h2>
        <div className="admin-quick-links__grid">
          <Link to="/admin/doctors/new" className="btn btn--primary">➕ Add New Doctor</Link>
          <Link to="/admin/doctors" className="btn btn--secondary">👨‍⚕️ Manage Doctors</Link>
          <Link to="/admin/appointments" className="btn btn--secondary">📋 View All Appointments</Link>
          <Link to="/admin/notifications" className="btn btn--secondary">🔔 Notification Status</Link>
          <Link to="/admin/users" className="btn btn--secondary">👥 View Patients</Link>
        </div>
      </div>
    </div>
  );
}
