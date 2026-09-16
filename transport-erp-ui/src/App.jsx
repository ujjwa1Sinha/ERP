import { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Toaster } from 'react-hot-toast';
import Layout from './components/Layout';

const Login = lazy(() => import('./pages/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Branches = lazy(() => import('./pages/Branches'));
const Vehicles = lazy(() => import('./pages/Vehicles'));
const Drivers = lazy(() => import('./pages/Drivers'));
const Fuel = lazy(() => import('./pages/Fuel'));
const Documents = lazy(() => import('./pages/Documents'));
const Assignments = lazy(() => import('./pages/Assignments'));
const Trips = lazy(() => import('./pages/Trips'));
const Users = lazy(() => import('./pages/Users'));
const DriverTracking = lazy(() => import('./pages/DriverTracking'));
const LiveMap = lazy(() => import('./pages/LiveMap'));

function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <div className="page-loader"><div className="spinner"></div></div>;
  return isAuthenticated ? children : <Navigate to="/login" />;
}

export default function App() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

  return (
    <>
      <Toaster position="top-right" />
      <Suspense fallback={<div className="page-loader"><div className="spinner"></div></div>}>
        <Routes>
          <Route path="/login" element={isAuthenticated ? <Navigate to="/" /> : <Login />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Dashboard />} />
            <Route path="branches" element={<Branches />} />
            <Route path="vehicles" element={<Vehicles />} />
            <Route path="drivers" element={<Drivers />} />
            <Route path="fuel" element={<Fuel />} />
            <Route path="documents" element={<Documents />} />
            <Route path="assignments" element={<Assignments />} />
            <Route path="trips" element={<Trips />} />
            <Route path="users" element={<Users />} />
            <Route path="driver-tracking" element={<DriverTracking />} />
            <Route path="live-map" element={<LiveMap />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Suspense>
    </>
  );
}
