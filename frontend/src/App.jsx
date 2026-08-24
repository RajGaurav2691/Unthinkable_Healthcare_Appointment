import { BrowserRouter as Router, Routes, Route, Link, NavLink } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import ProtectedRoute from './components/ProtectedRoute';
import RoleRoute from './components/RoleRoute';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminDoctors from './pages/admin/AdminDoctors';
import AdminDoctorForm from './pages/admin/AdminDoctorForm';
import AdminAppointments from './pages/admin/AdminAppointments';
import AdminUsers from './pages/admin/AdminUsers';
import AdminNotifications from './pages/admin/AdminNotifications';
import PatientDoctorSearch from './pages/patient/PatientDoctorSearch';
import DoctorProfile from './pages/patient/DoctorProfile';
import BookingFlow from './pages/patient/BookingFlow';
import PatientAppointments from './pages/patient/PatientAppointments';
import PatientDashboard from './pages/patient/PatientDashboard';
import DoctorDashboard from './pages/Doctor/DoctorDashboard';
import DoctorAppointmentDetails from './pages/Doctor/DoctorAppointmentDetails';
import DoctorProfileManager from './pages/Doctor/DoctorProfileManager';

const navLinkClass = ({ isActive }) =>
  `nav-link ${isActive ? 'nav-link--active' : ''}`;

const Navigation = () => {
  const { user, logout } = useAuth();

  return (
    <nav className="top-nav">
      <div className="top-nav__brand">
        <Link to="/">🏥 HealthCare.io</Link>
      </div>
      <div className="top-nav__links">
        {!user ? (
          <>
            <NavLink to="/login" className={navLinkClass}>Login</NavLink>
            <NavLink to="/register" className={navLinkClass}>Register</NavLink>
          </>
        ) : (
          <div className="top-nav__user-links">
            {user.role === 'PATIENT' && (
              <>
                <NavLink to="/patient/dashboard" className={navLinkClass}>Dashboard</NavLink>
                <NavLink to="/patient/doctors" className={navLinkClass}>Find Doctors</NavLink>
                <NavLink to="/patient/appointments" className={navLinkClass}>My Appointments</NavLink>
              </>
            )}
            {user.role === 'DOCTOR' && (
              <>
                <NavLink to="/doctor/dashboard" className={navLinkClass}>Dashboard</NavLink>
                <NavLink to="/doctor/profile" className={navLinkClass}>Profile & Schedule</NavLink>
              </>
            )}
            {user.role === 'ADMIN' && (
              <>
                <NavLink to="/admin/dashboard" className={navLinkClass}>Dashboard</NavLink>
                <NavLink to="/admin/doctors" className={navLinkClass}>Doctors</NavLink>
                <NavLink to="/admin/appointments" className={navLinkClass}>Appointments</NavLink>
                <NavLink to="/admin/users" className={navLinkClass}>Patients</NavLink>
                <NavLink to="/admin/notifications" className={navLinkClass}>Notifications</NavLink>
              </>
            )}
            <span className="top-nav__username">👤 {user.name}</span>
            <button id="logout-btn" onClick={logout} className="btn btn--danger btn--sm">Logout</button>
          </div>
        )}
      </div>
    </nav>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app-shell">
          <Navigation />
          <main className="app-main">
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Patient Routes */}
              <Route path="/patient/dashboard" element={<RoleRoute allowedRoles={['PATIENT']}><PatientDashboard /></RoleRoute>} />
              <Route path="/patient/doctors" element={<RoleRoute allowedRoles={['PATIENT']}><PatientDoctorSearch /></RoleRoute>} />
              <Route path="/patient/doctors/:id" element={<RoleRoute allowedRoles={['PATIENT']}><DoctorProfile /></RoleRoute>} />
              <Route path="/patient/book/:id" element={<RoleRoute allowedRoles={['PATIENT']}><BookingFlow /></RoleRoute>} />
              <Route path="/patient/appointments" element={<RoleRoute allowedRoles={['PATIENT']}><PatientAppointments /></RoleRoute>} />

              {/* Doctor Routes */}
              <Route path="/doctor/dashboard" element={<RoleRoute allowedRoles={['DOCTOR']}><DoctorDashboard /></RoleRoute>} />
              <Route path="/doctor/appointments/:id" element={<RoleRoute allowedRoles={['DOCTOR']}><DoctorAppointmentDetails /></RoleRoute>} />
              <Route path="/doctor/profile" element={<RoleRoute allowedRoles={['DOCTOR']}><DoctorProfileManager /></RoleRoute>} />

              {/* Admin Routes */}
              <Route path="/admin/dashboard" element={<RoleRoute allowedRoles={['ADMIN']}><AdminDashboard /></RoleRoute>} />
              <Route path="/admin/doctors" element={<RoleRoute allowedRoles={['ADMIN']}><AdminDoctors /></RoleRoute>} />
              <Route path="/admin/doctors/new" element={<RoleRoute allowedRoles={['ADMIN']}><AdminDoctorForm /></RoleRoute>} />
              <Route path="/admin/doctors/:id/edit" element={<RoleRoute allowedRoles={['ADMIN']}><AdminDoctorForm /></RoleRoute>} />
              <Route path="/admin/appointments" element={<RoleRoute allowedRoles={['ADMIN']}><AdminAppointments /></RoleRoute>} />
              <Route path="/admin/users" element={<RoleRoute allowedRoles={['ADMIN']}><AdminUsers /></RoleRoute>} />
              <Route path="/admin/notifications" element={<RoleRoute allowedRoles={['ADMIN']}><AdminNotifications /></RoleRoute>} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
