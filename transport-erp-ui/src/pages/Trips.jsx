import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import {
    HiPlus, HiX, HiEye, HiPlay, HiCheck, HiBan,
    HiOutlineClipboardList, HiTruck, HiOutlineUserGroup,
    HiArrowRight, HiOutlineClock
} from 'react-icons/hi';
import AddressAutocomplete from '../components/AddressAutocomplete';

const STATUS_BADGE = {
    PLANNED: 'badge-blue',
    ASSIGNED: 'badge-purple',
    STARTED: 'badge-yellow',
    IN_TRANSIT: 'badge-yellow',
    HALTED: 'badge-orange',
    COMPLETED: 'badge-green',
    CANCELLED: 'badge-red'
};

const extractCity = (addressStr) => {
    if (!addressStr) return 'Unknown';
    const parts = addressStr.split(',').map(s => s.trim());
    if (parts.length === 4) return parts[1]; // Common Format: Name, City, State, Country
    if (parts.length === 3) return parts[0];
    if (parts.length > 4) {
        // Legacy verbose strings where City typically sits 4 indices from the end (before State/PIN/Country)
        return parts[parts.length - 4];
    }
    return addressStr.length > 20 ? addressStr.substring(0, 20) + '...' : addressStr;
};

const formatDuration = (seconds) => {
    if (!seconds) return '—';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    if (h > 0) return `${h}h ${m}m`;
    return `${m}m`;
};

const STATUS_OPTIONS = ['', 'PLANNED', 'ASSIGNED', 'STARTED', 'IN_TRANSIT', 'HALTED', 'COMPLETED', 'CANCELLED'];

const INITIAL_FORM = {
    source: '', destination: '', vehicleId: '', primaryDriverId: '',
    plannedDeparture: '', plannedArrival: '', tripType: '', remarks: ''
};

export default function Trips() {
    const { hasPermission } = useAuth();
    const canCreate = hasPermission('TRIP_CREATE');
    const canAssign = hasPermission('TRIP_ASSIGN');

    const [trips, setTrips] = useState([]);
    const [stats, setStats] = useState({});
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // Modals
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedTrip, setSelectedTrip] = useState(null);
    const [timeline, setTimeline] = useState([]);

    // Form & dropdowns
    const [form, setForm] = useState(INITIAL_FORM);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [vehicles, setVehicles] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [optionsLoaded, setOptionsLoaded] = useState(false);

    // ────────────────── DATA LOADING ──────────────────

    const loadTrips = useCallback(async () => {
        try {
            const params = { page, size: 15, sortBy: 'createdAt' };
            if (statusFilter) params.status = statusFilter;
            const res = await api.get('/trips', { params });
            const data = res.data.data;
            setTrips(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch { /* handled */ } finally { setLoading(false); }
    }, [page, statusFilter]);

    const loadStats = useCallback(async () => {
        try {
            const res = await api.get('/trips/stats');
            setStats(res.data.data || {});
        } catch { /* ignore */ }
    }, []);

    useEffect(() => { loadTrips(); loadStats(); }, [loadTrips, loadStats]);

    const loadOptions = async () => {
        if (optionsLoaded) return;
        try {
            const [vRes, dRes] = await Promise.all([
                api.get('/vehicles?size=1000'),
                api.get('/drivers?size=1000')
            ]);
            setVehicles(vRes.data.data?.content || vRes.data.data || []);
            setDrivers(dRes.data.data?.content || dRes.data.data || []);
            setOptionsLoaded(true);
        } catch { toast.error('Failed to load options'); }
    };

    // ────────────────── ACTIONS ──────────────────

    const handleCreate = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;
        setIsSubmitting(true);

        const payload = { ...form };
        if (payload.plannedDeparture) payload.plannedDeparture = new Date(payload.plannedDeparture).toISOString();
        if (payload.plannedArrival) payload.plannedArrival = new Date(payload.plannedArrival).toISOString();
        Object.keys(payload).forEach(k => { if (payload[k] === '') delete payload[k]; });

        const optimisticTrip = {
            ...payload,
            id: `temp-${Date.now()}`,
            tripNumber: 'TRX-PENDING',
            status: 'PLANNED'
        };

        const originalTrips = [...trips];
        setTrips([optimisticTrip, ...trips]);
        setShowCreateModal(false);
        setForm(INITIAL_FORM);

        try {
            await api.post('/trips', payload);
            toast.success('Trip created');
            loadTrips();
            loadStats();
        } catch (err) {
            toast.error(err?.response?.data?.message || 'Failed to create trip');
            setTrips(originalTrips);
            setShowCreateModal(true);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleLifecycleAction = async (tripId, action, body = {}) => {
        // Optimistic update for status changes
        const isStatusChange = ['start', 'complete', 'cancel'].includes(action);
        let originalTrips = [...trips];

        if (isStatusChange) {
            const statusMap = { 'start': 'STARTED', 'complete': 'COMPLETED', 'cancel': 'CANCELLED' };
            setTrips(trips.map(t => t.id === tripId ? { ...t, status: statusMap[action] } : t));
            if (selectedTrip && selectedTrip.id === tripId) {
                setSelectedTrip({ ...selectedTrip, status: statusMap[action] });
            }
        }

        try {
            await api.patch(`/trips/${tripId}/${action}`, body);
            toast.success(`Trip updated`);
            loadTrips();
            loadStats();
            if (selectedTrip?.id === tripId) openDetail(tripId);
        } catch (err) {
            toast.error(err?.response?.data?.message || `Failed to update trip`);
            if (isStatusChange) {
                setTrips(originalTrips);
                if (selectedTrip?.id === tripId) openDetail(tripId);
            }
        }
    };

    const handleUpdateTimes = async (e) => {
        e.preventDefault();
        const payload = {
            plannedDeparture: new Date(selectedTrip.plannedDeparture).toISOString(),
            plannedArrival: new Date(selectedTrip.plannedArrival).toISOString()
        };
        try {
            // Assuming endpoint exists for updating trips or patching
            await api.put(`/trips/${selectedTrip.id}`, payload);
            toast.success('Trip times updated');
            loadTrips();
        } catch (err) {
            toast.error('Failed to update trip times');
        }
    };

    const openDetail = async (tripId) => {
        try {
            const [tripRes, timelineRes] = await Promise.all([
                api.get(`/trips/${tripId}`),
                api.get(`/trips/${tripId}/timeline`)
            ]);
            setSelectedTrip(tripRes.data.data);
            setTimeline(timelineRes.data.data || []);
            setShowDetailModal(true);
        } catch { toast.error('Failed to load trip details'); }
    };

    const openCreateModal = () => {
        setForm({ ...INITIAL_FORM, idempotencyKey: crypto.randomUUID() });
        setShowCreateModal(true);
        loadOptions();
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    // ────────────────── RENDER ──────────────────

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            {/* Header */}
            <div className="page-header">
                <div>
                    <h2>Trips</h2>
                    <p>Manage dispatch and trip lifecycle</p>
                </div>
                {canCreate && (
                    <button id="create-trip-btn" className="btn btn-primary" onClick={openCreateModal}>
                        <HiPlus size={16} /> New Trip
                    </button>
                )}
            </div>

            {/* Stats cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: 12, marginBottom: '1.5rem' }}>
                {['PLANNED', 'ASSIGNED', 'STARTED', 'COMPLETED', 'CANCELLED'].map(s => (
                    <div key={s} className="card" style={{ padding: '14px 16px', cursor: 'pointer', border: statusFilter === s ? '2px solid var(--primary)' : undefined }}
                        onClick={() => { setStatusFilter(statusFilter === s ? '' : s); setPage(0); }}>
                        <div style={{ fontSize: 22, fontWeight: 700 }}>{stats[s] || 0}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-secondary)', textTransform: 'capitalize' }}>{s.toLowerCase().replace('_', ' ')}</div>
                    </div>
                ))}
            </div>

            {/* Filter */}
            <div style={{ display: 'flex', gap: 10, marginBottom: '1rem', alignItems: 'center' }}>
                <select className="form-select" value={statusFilter}
                    onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
                    style={{ maxWidth: 200 }}>
                    <option value="">All Statuses</option>
                    {STATUS_OPTIONS.filter(Boolean).map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                </select>
                {statusFilter && <button className="btn btn-secondary btn-sm" onClick={() => { setStatusFilter(''); setPage(0); }}>Clear</button>}
                <span style={{ marginLeft: 'auto', fontSize: 13, color: 'var(--text-secondary)' }}>
                    Total: {stats.TOTAL || 0}
                </span>
            </div>

            {/* Table */}
            <div className="card">
                <div className="card-header">
                    <h3>Trip List</h3>
                    <span className="badge badge-blue">{trips.length} shown</span>
                </div>
                <div className="table-wrapper">
                    {trips.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Trip #</th>
                                    <th>Route</th>
                                    <th>Vehicle</th>
                                    <th>Driver</th>
                                    <th>Planned Departure</th>
                                    <th>Status</th>
                                    <th className="text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {trips.map(t => (
                                    <tr key={t.id}>
                                        <td style={{ fontWeight: 600, fontFamily: 'monospace' }}>{t.tripNumber}</td>
                                        <td title={`${t.source} → ${t.destination}`}>
                                            <span style={{ fontWeight: 600 }}>{extractCity(t.source)}</span>
                                            <HiArrowRight size={12} style={{ margin: '0 6px', color: 'var(--text-secondary)' }} />
                                            <span style={{ fontWeight: 600 }}>{extractCity(t.destination)}</span>
                                        </td>
                                        <td>{t.vehicleRegistrationNumber || <span style={{ color: 'var(--text-secondary)' }}>Unassigned</span>}</td>
                                        <td>{t.primaryDriverName || <span style={{ color: 'var(--text-secondary)' }}>Unassigned</span>}</td>
                                        <td>{t.plannedDeparture ? new Date(t.plannedDeparture).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—'}</td>
                                        <td><span className={`badge ${STATUS_BADGE[t.status] || 'badge-gray'}`}>{t.status?.replace('_', ' ')}</span></td>
                                        <td className="text-right" style={{ display: 'flex', gap: 4, justifyContent: 'flex-end' }}>
                                            <button className="btn btn-secondary btn-sm" onClick={() => openDetail(t.id)} title="View"><HiEye size={14} /></button>
                                            {canCreate && t.status === 'ASSIGNED' && (
                                                <button className="btn btn-primary btn-sm" onClick={() => handleLifecycleAction(t.id, 'start')} title="Start"><HiPlay size={14} /></button>
                                            )}
                                            {canCreate && ['STARTED', 'IN_TRANSIT', 'HALTED'].includes(t.status) && (
                                                <button className="btn btn-sm" style={{ background: '#16a34a', color: '#fff', border: 'none' }} onClick={() => handleLifecycleAction(t.id, 'complete')} title="Complete"><HiCheck size={14} /></button>
                                            )}
                                            {canCreate && !['COMPLETED', 'CANCELLED'].includes(t.status) && (
                                                <button className="btn btn-sm" style={{ background: '#dc2626', color: '#fff', border: 'none' }} onClick={() => { if (confirm('Cancel this trip?')) handleLifecycleAction(t.id, 'cancel'); }} title="Cancel"><HiBan size={14} /></button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon"><HiOutlineClipboardList size={48} /></div>
                            <h4>No trips found</h4>
                            <p>{statusFilter ? `No ${statusFilter.toLowerCase()} trips` : 'Create your first trip to get started'}</p>
                        </div>
                    )}
                </div>
                {/* Pagination */}
                {totalPages > 1 && (
                    <div style={{ display: 'flex', justifyContent: 'center', gap: 8, padding: '12px 0' }}>
                        <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</button>
                        <span style={{ fontSize: 13, lineHeight: '32px' }}>Page {page + 1} of {totalPages}</span>
                        <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
                    </div>
                )}
            </div>

            {/* ─────────── CREATE MODAL ─────────── */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 640 }}>
                        <div className="modal-header">
                            <h3>Create New Trip</h3>
                            <button className="modal-close" onClick={() => setShowCreateModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleCreate}>
                                <div className="form-grid">
                                    <AddressAutocomplete label="Source" name="source" value={form.source} onChange={onChange} required={true} placeholder="e.g. Connaught Place, New Delhi" />
                                    <AddressAutocomplete label="Destination" name="destination" value={form.destination} onChange={onChange} required={true} placeholder="e.g. Cyber City, Gurgaon" />

                                    <div className="form-group">
                                        <label className="form-label">Vehicle (Optional for Future Planning)</label>
                                        <select className="form-select" name="vehicleId" value={form.vehicleId} onChange={onChange}>
                                            <option value="">Unassigned (Plan for later)</option>
                                            {vehicles.map(v => <option key={v.id} value={v.id}>{v.registrationNumber} {v.make ? `(${v.make})` : ''}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Primary Driver (Optional)</label>
                                        <select className="form-select" name="primaryDriverId" value={form.primaryDriverId} onChange={onChange}>
                                            <option value="">Unassigned (Plan for later)</option>
                                            {drivers.map(d => <option key={d.id} value={d.id}>{d.name} ({d.employeeCode || '—'})</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Planned Departure *</label>
                                        <input className="form-input" type="datetime-local" name="plannedDeparture" value={form.plannedDeparture} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Planned Arrival *</label>
                                        <input className="form-input" type="datetime-local" name="plannedArrival" value={form.plannedArrival} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Trip Type *</label>
                                        <input className="form-input" name="tripType" value={form.tripType} onChange={onChange} placeholder="e.g. REGULAR, EXPRESS" required />
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Remarks</label>
                                        <input className="form-input" name="remarks" value={form.remarks} onChange={onChange} />
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)} disabled={isSubmitting}>Cancel</button>
                                    <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                                        {isSubmitting ? 'Creating your trip...' : 'Create Trip'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {/* ─────────── DETAIL MODAL ─────────── */}
            {showDetailModal && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowDetailModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 680 }}>
                        <div className="modal-header">
                            <h3 style={{ fontFamily: 'monospace' }}>{selectedTrip.tripNumber}</h3>
                            <button className="modal-close" onClick={() => setShowDetailModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                            {/* Trip info cards */}
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 20 }}>
                                <InfoCard label="Route" value={`${selectedTrip.source} → ${selectedTrip.destination}`} />
                                <InfoCard label="Status" value={<span className={`badge ${STATUS_BADGE[selectedTrip.status]}`}>{selectedTrip.status?.replace('_', ' ')}</span>} />
                                <InfoCard label="Vehicle" icon={<HiTruck size={14} />} value={selectedTrip.vehicleRegistrationNumber || 'Unassigned'} />
                                <InfoCard label="Driver" icon={<HiOutlineUserGroup size={14} />} value={selectedTrip.primaryDriverName || 'Unassigned'} />
                                <InfoCard label="Planned Departure" value={selectedTrip.plannedDeparture ? new Date(selectedTrip.plannedDeparture).toLocaleString('en-IN') : '—'} />
                                <InfoCard label="Planned Arrival" value={selectedTrip.plannedArrival ? new Date(selectedTrip.plannedArrival).toLocaleString('en-IN') : '—'} />
                                {selectedTrip.actualDeparture && <InfoCard label="Actual Departure" value={new Date(selectedTrip.actualDeparture).toLocaleString('en-IN')} />}
                                {selectedTrip.actualArrival && <InfoCard label="Actual Arrival" value={new Date(selectedTrip.actualArrival).toLocaleString('en-IN')} />}
                                {selectedTrip.distancePlanned && <InfoCard label="Distance Planned" value={`${selectedTrip.distancePlanned} km`} />}
                                {selectedTrip.distanceActual && <InfoCard label="Distance Actual" value={`${selectedTrip.distanceActual} km`} />}
                                {selectedTrip.durationPlannedSeconds && <InfoCard label="Standard Duration (OSRM)" value={formatDuration(selectedTrip.durationPlannedSeconds)} />}
                                {selectedTrip.durationActualSeconds && <InfoCard label="Actual Transit Duration" value={formatDuration(selectedTrip.durationActualSeconds)} />}
                            </div>

                            {selectedTrip.remarks && (
                                <div style={{ padding: '10px 14px', background: 'var(--bg-secondary)', borderRadius: 8, marginBottom: 20, fontSize: 13 }}>
                                    <strong>Remarks:</strong> {selectedTrip.remarks}
                                </div>
                            )}

                            {/* Edit Times (Replacing Assign buttons) */}
                            {canCreate && !['COMPLETED', 'CANCELLED'].includes(selectedTrip.status) && (
                                <form onSubmit={handleUpdateTimes} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: 12, marginBottom: 20, alignItems: 'end' }}>
                                    <div className="form-group" style={{ marginBottom: 0 }}>
                                        <label className="form-label">New Planned Departure</label>
                                        <input className="form-input" type="datetime-local" value={selectedTrip.plannedDeparture?.slice(0, 16) || ''} onChange={e => setSelectedTrip({ ...selectedTrip, plannedDeparture: e.target.value })} />
                                    </div>
                                    <div className="form-group" style={{ marginBottom: 0 }}>
                                        <label className="form-label">New Planned Arrival</label>
                                        <input className="form-input" type="datetime-local" value={selectedTrip.plannedArrival?.slice(0, 16) || ''} onChange={e => setSelectedTrip({ ...selectedTrip, plannedArrival: e.target.value })} />
                                    </div>
                                    <button type="submit" className="btn btn-primary" style={{ height: 42 }}>Save Times</button>
                                </form>
                            )}

                            {/* Lifecycle actions */}
                            <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
                                {canCreate && selectedTrip.status === 'ASSIGNED' && (
                                    <button className="btn btn-primary btn-sm" onClick={() => handleLifecycleAction(selectedTrip.id, 'start')}>
                                        <HiPlay size={14} /> Start Trip
                                    </button>
                                )}
                                {canCreate && ['STARTED', 'IN_TRANSIT', 'HALTED'].includes(selectedTrip.status) && (
                                    <button className="btn btn-sm" style={{ background: '#16a34a', color: '#fff', border: 'none' }}
                                        onClick={() => handleLifecycleAction(selectedTrip.id, 'complete')}>
                                        <HiCheck size={14} /> Complete
                                    </button>
                                )}
                                {canCreate && !['COMPLETED', 'CANCELLED'].includes(selectedTrip.status) && (
                                    <button className="btn btn-sm" style={{ background: '#dc2626', color: '#fff', border: 'none' }}
                                        onClick={() => { if (confirm('Cancel this trip?')) handleLifecycleAction(selectedTrip.id, 'cancel'); }}>
                                        <HiBan size={14} /> Cancel
                                    </button>
                                )}
                            </div>

                            {/* Timeline */}
                            <h4 style={{ marginBottom: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
                                <HiOutlineClock size={16} /> Trip Timeline
                            </h4>
                            {timeline.length > 0 ? (
                                <div style={{ position: 'relative', paddingLeft: 24 }}>
                                    <div style={{ position: 'absolute', left: 7, top: 4, bottom: 4, width: 2, background: 'var(--border-color)' }} />
                                    {timeline.map((ev, i) => (
                                        <div key={ev.id} style={{ position: 'relative', paddingBottom: i < timeline.length - 1 ? 16 : 0, paddingLeft: 12 }}>
                                            <div style={{
                                                position: 'absolute', left: -20, top: 4,
                                                width: 12, height: 12, borderRadius: '50%',
                                                background: i === timeline.length - 1 ? 'var(--primary)' : 'var(--text-secondary)',
                                                border: '2px solid var(--bg-primary)'
                                            }} />
                                            <div style={{ fontSize: 13, fontWeight: 600 }}>{ev.eventType?.replace(/_/g, ' ')}</div>
                                            <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                                                {new Date(ev.eventTimestamp).toLocaleString('en-IN')}
                                                {ev.createdBy && ` · by ${ev.createdBy}`}
                                            </div>
                                            {ev.remarks && <div style={{ fontSize: 12, marginTop: 2 }}>{ev.remarks}</div>}
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p style={{ color: 'var(--text-secondary)', fontSize: 13 }}>No events yet.</p>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );

    // ────────────────── PROMPT HELPERS ──────────────────

    function promptAssign(type) {
        // Obsoleted
    }
}

function InfoCard({ label, value, icon }) {
    return (
        <div style={{ padding: '10px 14px', background: 'var(--bg-secondary)', borderRadius: 8 }}>
            <div style={{ fontSize: 11, color: 'var(--text-secondary)', marginBottom: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                {icon} {label}
            </div>
            <div style={{ fontSize: 14, fontWeight: 500 }}>{value}</div>
        </div>
    );
}
