import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiX } from 'react-icons/hi';

const fuels = ['DIESEL', 'PETROL', 'CNG', 'ELECTRIC', 'HYBRID'];

const emptyVehicle = {
    registrationNumber: '', vehicleType: 'TRUCK', make: '', model: '', manufactureYear: 2024,
    fuelType: 'DIESEL', capacity: '', chassisNumber: '', engineNumber: '', gpsDeviceId: '',
    insuranceExpiry: '', fitnessExpiry: '', permitExpiry: '', pollutionExpiry: '', taxExpiry: '', branchId: ''
};

export default function Vehicles() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('VEHICLE_EDIT');
    const [vehicles, setVehicles] = useState([]);
    const [branches, setBranches] = useState([]);
    const [vehicleTypes, setVehicleTypes] = useState(["TRUCK"]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyVehicle });
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => { loadVehicles(); loadBranches(); loadVehicleTypes(); }, [page]);

    const loadVehicles = async () => {
        try {
            const res = await api.get(`/vehicles?page=${page}&size=15`);
            const d = res.data.data;
            setVehicles(d?.content || []);
            setTotalPages(d?.totalPages || 0);
        } catch { /* handled */ } finally { setLoading(false); }
    };

    const loadBranches = async () => {
        try {
            const res = await api.get('/branches');
            setBranches(res.data.data || []);
        } catch { /* silent */ }
    };

    const loadVehicleTypes = async () => {
        try {
            const res = await api.get('/vehicles/types');
            setVehicleTypes(res.data.data || []);
        } catch { /* silent */ }
    };

    const openCreate = () => { setForm({ ...emptyVehicle }); setEditId(null); setShowModal(true); };
    const openEdit = (v) => {
        setForm({
            registrationNumber: v.registrationNumber || '', vehicleType: v.vehicleType || 'TRUCK',
            make: v.make || '', model: v.model || '', manufactureYear: v.manufactureYear || 2024,
            fuelType: v.fuelType || 'DIESEL', capacity: v.capacity || '', chassisNumber: v.chassisNumber || '',
            engineNumber: v.engineNumber || '', gpsDeviceId: v.gpsDeviceId || '',
            insuranceExpiry: v.insuranceExpiry || '', fitnessExpiry: v.fitnessExpiry || '',
            permitExpiry: v.permitExpiry || '', pollutionExpiry: v.pollutionExpiry || '',
            taxExpiry: v.taxExpiry || '', branchId: v.branchId || ''
        });
        setEditId(v.id);
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const payload = { ...form, capacity: Number(form.capacity), manufactureYear: Number(form.manufactureYear) };
        try {
            if (editId) {
                await api.put(`/vehicles/${editId}`, payload);
                toast.success('Vehicle updated');
            } else {
                await api.post('/vehicles', payload);
                toast.success('Vehicle created');
            }
            setShowModal(false);
            loadVehicles();
        } catch { /* handled */ }
    };

    const handleDelete = async (id) => {
        if (!confirm('Decommission this vehicle?')) return;
        try {
            await api.delete(`/vehicles/${id}`);
            toast.success('Vehicle decommissioned');
            loadVehicles();
        } catch { /* handled */ }
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Vehicles</h2>
                    <p>Manage your fleet inventory</p>
                </div>
                {canEdit && (
                    <button id="create-vehicle-btn" className="btn btn-primary" onClick={openCreate}>
                        <HiPlus size={16} /> Add Vehicle
                    </button>
                )}
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {vehicles.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Registration</th>
                                    <th>Type</th>
                                    <th>Make / Model</th>
                                    <th>Fuel</th>
                                    <th>Capacity</th>
                                    <th>Branch</th>
                                    <th>Status</th>
                                    {canEdit && <th className="text-right">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {vehicles.map(v => (
                                    <tr key={v.id}>
                                        <td style={{ fontWeight: 600 }}>{v.registrationNumber}</td>
                                        <td><span className="badge badge-blue">{v.vehicleType}</span></td>
                                        <td>{v.make} {v.model}</td>
                                        <td>{v.fuelType}</td>
                                        <td>{v.capacity}T</td>
                                        <td>{v.branchName || '—'}</td>
                                        <td><span className={`badge ${v.status === 'ACTIVE' ? 'badge-green' : v.status === 'IN_MAINTENANCE' ? 'badge-amber' : 'badge-gray'}`}>{v.status}</span></td>
                                        {canEdit && (
                                            <td className="text-right">
                                                <div className="flex items-center gap-8" style={{ justifyContent: 'flex-end' }}>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => openEdit(v)}><HiPencil size={16} /></button>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => handleDelete(v.id)} style={{ color: 'var(--red-500)' }}><HiTrash size={16} /></button>
                                                </div>
                                            </td>
                                        )}
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
                {totalPages > 1 && (
                    <div style={{ padding: 16, display: 'flex', justifyContent: 'center', gap: 8 }}>
                        <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
                        <span style={{ padding: '6px 12px', fontSize: 13, color: 'var(--gray-500)' }}>Page {page + 1} of {totalPages}</span>
                        <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
                    </div>
                )}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 720 }}>
                        <div className="modal-header">
                            <h3>{editId ? 'Edit Vehicle' : 'Add Vehicle'}</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleSubmit}>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Registration Number *</label>
                                        <input className="form-input" name="registrationNumber" value={form.registrationNumber} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Vehicle Type</label>
                                        <select className="form-select" name="vehicleType" value={form.vehicleType} onChange={onChange}>
                                            {vehicleTypes.map(t => <option key={t} value={t}>{t}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Make</label>
                                        <input className="form-input" name="make" value={form.make} onChange={onChange} placeholder="e.g. Tata" />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Model</label>
                                        <input className="form-input" name="model" value={form.model} onChange={onChange} placeholder="e.g. Prima 4928.S" />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Year</label>
                                        <input className="form-input" name="manufactureYear" type="number" value={form.manufactureYear} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Fuel Type</label>
                                        <select className="form-select" name="fuelType" value={form.fuelType} onChange={onChange}>
                                            {fuels.map(f => <option key={f} value={f}>{f}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Capacity (Tons)</label>
                                        <input className="form-input" name="capacity" type="number" value={form.capacity} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Branch</label>
                                        <select className="form-select" name="branchId" value={form.branchId} onChange={onChange}>
                                            <option value="">Select Branch</option>
                                            {branches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Chassis Number</label>
                                        <input className="form-input" name="chassisNumber" value={form.chassisNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Engine Number</label>
                                        <input className="form-input" name="engineNumber" value={form.engineNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">GPS Device ID</label>
                                        <input className="form-input" name="gpsDeviceId" value={form.gpsDeviceId} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Insurance Expiry</label>
                                        <input className="form-input" name="insuranceExpiry" type="date" value={form.insuranceExpiry} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Fitness Expiry</label>
                                        <input className="form-input" name="fitnessExpiry" type="date" value={form.fitnessExpiry} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Permit Expiry</label>
                                        <input className="form-input" name="permitExpiry" type="date" value={form.permitExpiry} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Pollution Expiry</label>
                                        <input className="form-input" name="pollutionExpiry" type="date" value={form.pollutionExpiry} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Tax Expiry</label>
                                        <input className="form-input" name="taxExpiry" type="date" value={form.taxExpiry} onChange={onChange} />
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary">{editId ? 'Update' : 'Create'}</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
