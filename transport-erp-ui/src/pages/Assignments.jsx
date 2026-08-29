import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiX, HiLink } from 'react-icons/hi';

const roles = ['PRIMARY_DRIVER', 'SECONDARY_DRIVER', 'RELIEF_DRIVER'];

export default function Assignments() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('ASSIGNMENT_EDIT');
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({ driverId: '', vehicleId: '', role: 'PRIMARY_DRIVER', remarks: '' });

    // new states for dropdown options
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [optionsLoading, setOptionsLoading] = useState(false);

    useEffect(() => { loadAssignments(); }, []);

    const loadAssignments = async () => {
        try {
            const res = await api.get('/assignments/active');
            setAssignments(res.data.data || []);
        } catch { /* handled */ } finally { setLoading(false); }
    };

    const loadOptions = async () => {
        if (drivers.length > 0 && vehicles.length > 0) return;
        setOptionsLoading(true);
        try {
            const [dRes, vRes] = await Promise.all([
                api.get('/drivers?size=1000'),
                api.get('/vehicles?size=1000')
            ]);
            setDrivers(dRes.data.data?.content || dRes.data.data || []);
            setVehicles(vRes.data.data?.content || vRes.data.data || []);
        } catch (error) {
            toast.error('Failed to load drivers and vehicles. Try again.');
        } finally {
            setOptionsLoading(false);
        }
    };

    const handleOpenModal = () => {
        setForm({ driverId: '', vehicleId: '', role: 'PRIMARY_DRIVER', remarks: '' });
        setShowModal(true);
        loadOptions();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/assignments', form);
            toast.success('Driver assigned to vehicle');
            setShowModal(false);
            loadAssignments();
        } catch { /* handled */ }
    };

    const handleRelease = async (id) => {
        if (!confirm('Release this assignment?')) return;
        try {
            await api.post(`/assignments/${id}/release`);
            toast.success('Assignment released');
            loadAssignments();
        } catch { /* handled */ }
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Assignments</h2>
                    <p>Map drivers to vehicles</p>
                </div>
                {canEdit && (
                    <button id="create-assignment-btn" className="btn btn-primary" onClick={handleOpenModal}>
                        <HiPlus size={16} /> New Assignment
                    </button>
                )}
            </div>

            <div className="card">
                <div className="card-header">
                    <h3>Active Assignments</h3>
                    <span className="badge badge-blue">{assignments.length} active</span>
                </div>
                <div className="table-wrapper">
                    {assignments.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Driver</th>
                                    <th>Vehicle</th>
                                    <th>Role</th>
                                    <th>Assigned On</th>
                                    <th>Remarks</th>
                                    {canEdit && <th className="text-right">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {assignments.map(a => (
                                    <tr key={a.id}>
                                        <td style={{ fontWeight: 600 }}>{a.driverName || a.driverId?.substring(0, 8) + '...'}</td>
                                        <td>{a.vehicleRegistration || a.vehicleId?.substring(0, 8) + '...'}</td>
                                        <td><span className="badge badge-blue">{a.role?.replace(/_/g, ' ')}</span></td>
                                        <td>{a.assignedAt?.substring(0, 10) || '—'}</td>
                                        <td style={{ maxWidth: 200 }} className="truncate">{a.remarks || '—'}</td>
                                        {canEdit && (
                                            <td className="text-right">
                                                <button className="btn btn-secondary btn-sm" onClick={() => handleRelease(a.id)}>
                                                    Release
                                                </button>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon"><HiLink size={48} /></div>
                            <h4>No active assignments</h4>
                            <p>Assign drivers to vehicles to get started</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 520 }}>
                        <div className="modal-header">
                            <h3>New Assignment</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleSubmit}>
                                <div className="form-grid">
                                    <div className="form-group full-width">
                                        <label className="form-label">Driver *</label>
                                        <select className="form-select" name="driverId" value={form.driverId} onChange={onChange} required disabled={optionsLoading}>
                                            <option value="">Select Driver</option>
                                            {drivers.map(d => (
                                                <option key={d.id} value={d.id}>
                                                    {d.name} ({d.employeeCode || d.id.substring(0, 6)})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Vehicle *</label>
                                        <select className="form-select" name="vehicleId" value={form.vehicleId} onChange={onChange} required disabled={optionsLoading}>
                                            <option value="">Select Vehicle</option>
                                            {vehicles.map(v => (
                                                <option key={v.id} value={v.id}>
                                                    {v.registrationNumber} {v.make ? `(${v.make} ${v.model})` : ''}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Role</label>
                                        <select className="form-select" name="role" value={form.role} onChange={onChange}>
                                            {roles.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Remarks</label>
                                        <input className="form-input" name="remarks" value={form.remarks} onChange={onChange} />
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary">Assign</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
