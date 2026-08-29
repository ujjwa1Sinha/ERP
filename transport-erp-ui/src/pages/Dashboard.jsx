import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import {
    HiOutlineOfficeBuilding, HiOutlineTruck,
    HiOutlineUserGroup, HiOutlineDocumentText
} from 'react-icons/hi';

export default function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({ branches: 0, vehicles: 0, drivers: 0, documents: 0 });
    const [recentVehicles, setRecentVehicles] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDashboard();
    }, []);

    const loadDashboard = async () => {
        try {
            const [branchRes, vehicleRes, driverRes] = await Promise.allSettled([
                api.get('/branches'),
                api.get('/vehicles?page=0&size=5'),
                api.get('/drivers?page=0&size=5'),
            ]);

            setStats({
                branches: branchRes.status === 'fulfilled' ? (branchRes.value.data.data?.length || 0) : 0,
                vehicles: vehicleRes.status === 'fulfilled' ? (vehicleRes.value.data.data?.totalElements || 0) : 0,
                drivers: driverRes.status === 'fulfilled' ? (driverRes.value.data.data?.totalElements || 0) : 0,
                documents: 0,
            });

            if (vehicleRes.status === 'fulfilled') {
                setRecentVehicles(vehicleRes.value.data.data?.content || []);
            }
        } catch {
            // silent
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Welcome back, {user?.fullName?.split(' ')[0] || 'Admin'} 👋</h2>
                    <p>Here's what's happening with your fleet today</p>
                </div>
            </div>

            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon blue"><HiOutlineOfficeBuilding size={24} /></div>
                    <div className="stat-info">
                        <h4>Branches</h4>
                        <div className="stat-value">{stats.branches}</div>
                        <div className="stat-sub">Total depots</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon green"><HiOutlineTruck size={24} /></div>
                    <div className="stat-info">
                        <h4>Vehicles</h4>
                        <div className="stat-value">{stats.vehicles}</div>
                        <div className="stat-sub">In fleet</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon amber"><HiOutlineUserGroup size={24} /></div>
                    <div className="stat-info">
                        <h4>Drivers</h4>
                        <div className="stat-value">{stats.drivers}</div>
                        <div className="stat-sub">Registered</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon red"><HiOutlineDocumentText size={24} /></div>
                    <div className="stat-info">
                        <h4>Documents</h4>
                        <div className="stat-value">{stats.documents}</div>
                        <div className="stat-sub">Tracked</div>
                    </div>
                </div>
            </div>

            {/* Recent Vehicles */}
            <div className="card">
                <div className="card-header">
                    <h3>Recent Vehicles</h3>
                </div>
                <div className="table-wrapper">
                    {recentVehicles.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Registration</th>
                                    <th>Type</th>
                                    <th>Make / Model</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentVehicles.map(v => (
                                    <tr key={v.id}>
                                        <td style={{ fontWeight: 600 }}>{v.registrationNumber}</td>
                                        <td>{v.vehicleType}</td>
                                        <td>{v.make} {v.model}</td>
                                        <td>
                                            <span className={`badge ${v.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}`}>
                                                {v.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon">🚛</div>
                            <h4>No vehicles yet</h4>
                            <p>Add your first vehicle to get started</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
