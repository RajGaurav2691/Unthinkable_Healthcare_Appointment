import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import ProtectedRoute from './components/ProtectedRoute';
import RoleRoute from './components/RoleRoute';
import AdminDoctors from './pages/admin/AdminDoctors';
import AdminDoctorForm from './pages/admin/AdminDoctorForm';
import PatientDoctorSearch from './pages/patient/PatientDoctorSearch';
import DoctorProfile from './pages/patient/DoctorProfile';
import BookingFlow from './pages/patient/BookingFlow';
import PatientAppointments from './pages/patient/PatientAppointments';
import PatientDashboard from './pages/patient/PatientDashboard';
import DoctorDashboard from './pages/Doctor/DoctorDashboard';
import DoctorAppointmentDetails from './pages/Doctor/DoctorAppointmentDetails';
import DoctorProfileManager from './pages/Doctor/DoctorProfileManager';

const Navigation = () => {
  const { user, logout } = useAuth();

  return (
    <nav className="flex justify-between items-center p-4 bg-white shadow-sm border-b border-gray-100">
      <div className="text-xl font-bold text-blue-600">
        <Link to="/">HealthCare.io</Link>
      </div>
      <div className="space-x-4">
        {!user ? (
          <>
            <Link to="/login" className="text-gray-600 hover:text-blue-600">Login</Link>
            <Link to="/register" className="text-gray-600 hover:text-blue-600">Register</Link>
          </>
        ) : (
          <div className="flex items-center space-x-4">
            {user.role === 'PATIENT' && (
              <>
                <Link to="/patient/dashboard" className="text-gray-600 hover:text-blue-600 font-semibold">Dashboard</Link>
                <Link to="/patient/doctors" className="text-gray-600 hover:text-blue-600 font-semibold">Find Doctors</Link>
                <Link to="/patient/appointments" className="text-gray-600 hover:text-blue-600 font-semibold">My Appointments</Link>
              </>
            )}
            {user.role === 'DOCTOR' && (
              <>
                <Link to="/doctor/dashboard" className="text-gray-600 hover:text-blue-600 font-semibold">Dashboard</Link>
                <Link to="/doctor/profile" className="text-gray-600 hover:text-blue-600 font-semibold">Profile & Schedule</Link>
              </>
            )}
            {user.role === 'ADMIN' && (
              <>
                <Link to="/admin/dashboard" className="text-gray-600 hover:text-blue-600 font-semibold">Admin Dashboard</Link>
              </>
            )}
            <span className="text-gray-500 border-l pl-4">Hello, {user.name}</span>
            <button onClick={logout} className="text-red-500 hover:text-red-700 font-semibold">Logout</button>
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
        <div className="min-h-screen bg-gray-50 text-gray-800 font-sans">
          <Navigation />
          <main className="p-4">
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              
              <Route path="/patient/dashboard" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <PatientDashboard />
                </RoleRoute>
              } />

              <Route path="/patient/doctors" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <PatientDoctorSearch />
                </RoleRoute>
              } />
              
              <Route path="/patient/doctors/:id" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <DoctorProfile />
                </RoleRoute>
              } />

              <Route path="/patient/book/:id" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <BookingFlow />
                </RoleRoute>
              } />

              <Route path="/patient/appointments" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <PatientAppointments />
                </RoleRoute>
              } />
              
              <Route path="/doctor/dashboard" element={
                <RoleRoute allowedRoles={['DOCTOR']}>
                  <DoctorDashboard />
                </RoleRoute>
              } />

              <Route path="/doctor/appointments/:id" element={
                <RoleRoute allowedRoles={['DOCTOR']}>
                  <DoctorAppointmentDetails />
                </RoleRoute>
              } />

              <Route path="/doctor/profile" element={
                <RoleRoute allowedRoles={['DOCTOR']}>
                  <DoctorProfileManager />
                </RoleRoute>
              } />

              <Route path="/admin/dashboard" element={
                <RoleRoute allowedRoles={['ADMIN']}>
                  <AdminDoctors />
                </RoleRoute>
              } />
              
              <Route path="/admin/doctors/new" element={
                <RoleRoute allowedRoles={['ADMIN']}>
                  <AdminDoctorForm />
                </RoleRoute>
              } />
              
              <Route path="/admin/doctors/:id/edit" element={
                <RoleRoute allowedRoles={['ADMIN']}>
                  <AdminDoctorForm />
                </RoleRoute>
              } />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
