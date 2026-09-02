import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import {
    HiOutlineOfficeBuilding, HiOutlineTruck,
    HiOutlineUserGroup, HiOutlineDocumentText,
    HiOutlineClipboardList, HiOutlineCheckCircle,
    HiOutlineUsers, HiArrowRight
} from 'react-icons/hi';

const STATUS_BADGE = {
    ACTIVE: 'badge-green',
    INACTIVE: 'badge-gray',
    default: 'badge-gray',
};

export default function Dashboard() {
    const { user, hasPermission } = useAuth();

    const [stats, setStats] = useState({
        branches: 0, vehicles: 0, drivers: 0, documents: 0,
        activeAssignments: 0, totalAssignments: 0, users: 0,
    });
    const [recentVehicles, setRecentVehicles] = useState([]);
    const [recentDrivers, setRecentDrivers] = useState([]);
    const [recentUsers, setRecentUsers] = useState([]);
    const [recentAssignments, setRecentAssignments] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadDashboard(); }, []);

    const loadDashboard = async () => {
        try {
            const [
                branchRes, vehicleRes, driverRes,
                assignActiveRes, assignReleasedRes, usersRes,
            ] = await Promise.allSettled([
                api.get('/branches'),
                api.get('/vehicles?page=0&size=5'),
                api.get('/drivers?page=0&size=5'),
                api.get('/assignments/active'),
                api.get('/assignments/released'),
                api.get('/auth/users'),
            ]);

            const ok = r => r.status === 'fulfilled';
            const data = r => r.value?.data?.data;

            const activeList = ok(assignActiveRes) ? (data(assignActiveRes) || []) : [];
            const releasedList = ok(assignReleasedRes) ? (data(assignReleasedRes) || []) : [];
            const usersList = ok(usersRes) ? (data(usersRes) || []) : [];
            // const docList = ok(docRes) ? (data(docRes) || []) : [];

            setStats({
                branches: ok(branchRes) ? (data(branchRes)?.length || 0) : 0,
                vehicles: ok(vehicleRes) ? (data(vehicleRes)?.totalElements || 0) : 0,
                drivers: ok(driverRes) ? (data(driverRes)?.totalElements || 0) : 0,
                // documents: docList.length,
                activeAssignments: activeList.length,
                totalAssignments: activeList.length + releasedList.length,
                users: usersList.length,
            });

            setRecentVehicles(ok(vehicleRes) ? (data(vehicleRes)?.content?.slice(0, 5) || []) : []);
            setRecentDrivers(ok(driverRes) ? (data(driverRes)?.content?.slice(0, 5) || []) : []);
            setRecentUsers(usersList.slice(0, 5));
            setRecentAssignments(activeList.slice(0, 5));
        } catch {
            // silent
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            {/* Page header */}
            <div className="page-header">
                <div>
                    <h2>Welcome back, {user?.fullName?.split(' ')[0] || 'Admin'} 👋</h2>
                    <p>Here's what's happening with your fleet today</p>
                </div>
            </div>

            {/* ── Stat cards ─────────────────────────────────────────── */}
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
                {/* <div className="stat-card">
                    <div className="stat-icon red"><HiOutlineDocumentText size={24} /></div>
                    <div className="stat-info">
                        <h4>Documents</h4>
                        <div className="stat-value">{stats.documents}</div>
                        <div className="stat-sub">Tracked</div>
                    </div>
                </div> */}
                <div className="stat-card">
                    <div className="stat-icon green"><HiOutlineCheckCircle size={24} /></div>
                    <div className="stat-info">
                        <h4>Active Assignments</h4>
                        <div className="stat-value">{stats.activeAssignments}</div>
                        <div className="stat-sub">Currently on duty</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon blue"><HiOutlineClipboardList size={24} /></div>
                    <div className="stat-info">
                        <h4>Total Assignments</h4>
                        <div className="stat-value">{stats.totalAssignments}</div>
                        <div className="stat-sub">All time</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon amber"><HiOutlineUsers size={24} /></div>
                    <div className="stat-info">
                        <h4>Users</h4>
                        <div className="stat-value">{stats.users}</div>
                        <div className="stat-sub">System accounts</div>
                    </div>
                </div>
            </div>

            {/* ── Recent tables — 2-column grid ───────────────────────── */}
            <div className="dashboard-grid">

                {/* Recent Vehicles */}
                <div className="card">
                    <div className="card-header">
                        <h3>Recent Vehicles</h3>
                        <Link to="/vehicles" className="dashboard-view-all">View all <HiArrowRight /></Link>
                    </div>
                    <div className="table-wrapper">
                        {recentVehicles.length > 0 ? (
                            <table>
                                <thead><tr>
                                    <th>Registration</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                </tr></thead>
                                <tbody>
                                    {recentVehicles.map(v => (
                                        <tr key={v.id}>
                                            <td style={{ fontWeight: 600 }}>{v.registrationNumber}</td>
                                            <td>{v.vehicleTypeName || v.vehicleType}</td>
                                            <td><span className={`badge ${STATUS_BADGE[v.status] || STATUS_BADGE.default}`}>{v.status}</span></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : <EmptyState icon="🚛" title="No vehicles yet" />}
                    </div>
                </div>

                {/* Recent Drivers */}
                <div className="card">
                    <div className="card-header">
                        <h3>Recent Drivers</h3>
                        <Link to="/drivers" className="dashboard-view-all">View all <HiArrowRight /></Link>
                    </div>
                    <div className="table-wrapper">
                        {recentDrivers.length > 0 ? (
                            <table>
                                <thead><tr>
                                    <th>Name</th>
                                    <th>Employee Code</th>
                                    <th>Status</th>
                                </tr></thead>
                                <tbody>
                                    {recentDrivers.map(d => (
                                        <tr key={d.id}>
                                            <td style={{ fontWeight: 600 }}>{d.fullName || d.name}</td>
                                            <td>{d.employeeCode}</td>
                                            <td><span className={`badge ${STATUS_BADGE[d.status] || STATUS_BADGE.default}`}>{d.status || '—'}</span></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : <EmptyState icon="👤" title="No drivers yet" />}
                    </div>
                </div>

                {/* Active Assignments */}
                <div className="card">
                    <div className="card-header">
                        <h3>Active Assignments</h3>
                        <Link to="/assignments" className="dashboard-view-all">View all <HiArrowRight /></Link>
                    </div>
                    <div className="table-wrapper">
                        {recentAssignments.length > 0 ? (
                            <table>
                                <thead><tr>
                                    <th>Driver</th>
                                    <th>Vehicle</th>
                                    <th>Role</th>
                                </tr></thead>
                                <tbody>
                                    {recentAssignments.map(a => (
                                        <tr key={a.id}>
                                            <td style={{ fontWeight: 600 }}>{a.driverName}</td>
                                            <td>{a.vehicleRegistrationNumber}</td>
                                            <td><span className="badge badge-green">{a.role || 'PRIMARY_DRIVER'}</span></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : <EmptyState icon="📋" title="No active assignments" />}
                    </div>
                </div>

                {/* Recent Users */}
                {hasPermission('USER_VIEW') && (
                    <div className="card">
                        <div className="card-header">
                            <h3>Recent Users</h3>
                            <Link to="/users" className="dashboard-view-all">View all <HiArrowRight /></Link>
                        </div>
                        <div className="table-wrapper">
                            {recentUsers.length > 0 ? (
                                <table>
                                    <thead><tr>
                                        <th>Name</th>
                                        <th>Username</th>
                                        <th>Role</th>
                                    </tr></thead>
                                    <tbody>
                                        {recentUsers.map(u => (
                                            <tr key={u.id}>
                                                <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                                                <td style={{ color: 'var(--gray-500)' }}>@{u.username}</td>
                                                <td>
                                                    <span className="badge badge-blue">
                                                        {Array.isArray(u.roles) ? u.roles[0] : [...(u.roles || [])][0] || '—'}
                                                    </span>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            ) : <EmptyState icon="👥" title="No users yet" />}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

function EmptyState({ icon, title }) {
    return (
        <div className="empty-state">
            <div className="empty-state-icon">{icon}</div>
            <h4>{title}</h4>
        </div>
    );
}
