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
    const [releasedAssignments, setReleasedAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [activeTab, setActiveTab] = useState('active'); // active | released
    const [form, setForm] = useState({ driverId: '', vehicleId: '', role: 'PRIMARY_DRIVER', remarks: '' });

    // new states for dropdown options
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [optionsLoading, setOptionsLoading] = useState(false);
    const [conflictError, setConflictError] = useState('');

    useEffect(() => { loadAssignments(); }, []);

    const loadAssignments = async () => {
        try {
            const [activeRes, releasedRes] = await Promise.all([
                api.get('/assignments/active'),
                api.get('/assignments/released').catch(() => ({ data: { data: [] } }))
            ]);
            setAssignments(activeRes.data.data || []);
            setReleasedAssignments(releasedRes.data.data || []);
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
        setConflictError('');
        setShowModal(true);
        loadOptions();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setConflictError('');
        try {
            await api.post('/assignments', form);
            toast.success('Driver assigned to vehicle');
            setShowModal(false);
            loadAssignments();
        } catch (err) {
            const msg = err?.response?.data?.message || err?.response?.data?.error || 'Failed to create assignment';
            setConflictError(msg);
        }
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

            <div className="tabs" style={{ display: 'flex', gap: '10px', marginBottom: '1rem' }}>
                <button className={`btn ${activeTab === 'active' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('active')}>Active</button>
                <button className={`btn ${activeTab === 'released' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('released')}>History</button>
            </div>

            <div className="card">
                <div className="card-header">
                    <h3>{activeTab === 'active' ? 'Active Assignments' : 'Assignment History'}</h3>
                    <span className="badge badge-blue">
                        {activeTab === 'active' ? assignments.length : releasedAssignments.length} {activeTab === 'active' ? 'active' : 'released'}
                    </span>
                </div>
                <div className="table-wrapper">
                    {(activeTab === 'active' ? assignments : releasedAssignments).length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Driver</th>
                                    <th>Vehicle</th>
                                    <th>Role</th>
                                    <th>Assigned On</th>
                                    {activeTab === 'released' && <th>Released On</th>}
                                    <th>Remarks</th>
                                    {activeTab === 'active' && canEdit && <th className="text-right">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {(activeTab === 'active' ? assignments : releasedAssignments).map(a => (
                                    <tr key={a.id}>
                                        <td style={{ fontWeight: 600 }}>{a.driverName || a.driverId?.substring(0, 8) + '...'}</td>
                                        <td>{a.vehicleRegistrationNumber || a.vehicleId?.substring(0, 8) + '...'}</td>
                                        <td><span className="badge badge-blue">{a.role?.replace(/_/g, ' ')}</span></td>
                                        <td>{a.assignedAt?.substring(0, 10) || '—'}</td>
                                        {activeTab === 'released' && <td>{a.releasedAt?.substring(0, 10)}</td>}
                                        <td style={{ maxWidth: 200 }} className="truncate">{a.remarks || '—'}</td>
                                        {activeTab === 'active' && canEdit && (
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
                            <h4>No {activeTab === 'active' ? 'active' : 'released'} assignments</h4>
                            <p>{activeTab === 'active' ? 'Assign drivers to vehicles to get started' : 'No history found'}</p>
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
                                        <select className="form-select" name="driverId" value={form.driverId} onChange={e => { onChange(e); setConflictError(''); }} required disabled={optionsLoading}>
                                            <option value="">Select Driver</option>
                                            {drivers.map(d => {
                                                const isAssigned = assignments.some(a => a.driverId === d.id);
                                                return (
                                                    <option key={d.id} value={d.id} style={isAssigned ? { color: '#ef4444', fontStyle: 'italic' } : {}}>
                                                        {d.name} ({d.employeeCode || d.id.substring(0, 6)}){isAssigned ? ' — Already Assigned' : ''}
                                                    </option>
                                                );
                                            })}
                                        </select>
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Vehicle *</label>
                                        <select className="form-select" name="vehicleId" value={form.vehicleId} onChange={e => { onChange(e); setConflictError(''); }} required disabled={optionsLoading}>
                                            <option value="">Select Vehicle</option>
                                            {vehicles.map(v => {
                                                const isAssigned = assignments.some(a => a.vehicleId === v.id);
                                                return (
                                                    <option key={v.id} value={v.id} style={isAssigned ? { color: '#ef4444', fontStyle: 'italic' } : {}}>
                                                        {v.registrationNumber} {v.make ? `(${v.make} ${v.model})` : ''}{isAssigned ? ' — Already Assigned' : ''}
                                                    </option>
                                                );
                                            })}
                                        </select>
                                    </div>

                                    {conflictError && (
                                        <div className="form-group full-width" style={{
                                            padding: '10px 14px',
                                            background: '#fef2f2',
                                            border: '1px solid #fca5a5',
                                            borderRadius: 8,
                                            color: '#dc2626',
                                            fontSize: 13,
                                            display: 'flex',
                                            alignItems: 'flex-start',
                                            gap: 8
                                        }}>
                                            <span style={{ fontWeight: 700, fontSize: 16, lineHeight: 1 }}>⚠</span>
                                            <span>{conflictError}</span>
                                        </div>
                                    )}
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
