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
            <span className="text-gray-600">Hello, {user.name} ({user.role})</span>
            <button onClick={logout} className="text-red-500 hover:text-red-700">Logout</button>
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
                  <PatientDoctorSearch />
                </RoleRoute>
              } />
              
              <Route path="/patient/doctors/:id" element={
                <RoleRoute allowedRoles={['PATIENT']}>
                  <DoctorProfile />
                </RoleRoute>
              } />
              
              <Route path="/doctor/dashboard" element={
                <RoleRoute allowedRoles={['DOCTOR']}>
                  <div className="p-10 text-2xl text-center">Doctor Dashboard (Protected)</div>
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
