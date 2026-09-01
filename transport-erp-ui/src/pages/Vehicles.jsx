import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiX, HiUpload, HiDocumentText, HiEye, HiExclamation } from 'react-icons/hi';

const MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];

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
    const [submitting, setSubmitting] = useState(false);

    // Insurance file state
    const [insuranceFile, setInsuranceFile] = useState(null);
    const [fileError, setFileError] = useState('');
    const fileInputRef = useRef(null);

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

    const openCreate = () => {
        setForm({ ...emptyVehicle });
        setEditId(null);
        setInsuranceFile(null);
        setFileError('');
        setShowModal(true);
    };
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
        setInsuranceFile(null);
        setFileError('');
        setShowModal(true);
    };

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        setFileError('');
        if (!selectedFile) { setInsuranceFile(null); return; }
        if (selectedFile.size > MAX_FILE_SIZE) {
            setFileError(`File size (${(selectedFile.size / 1024 / 1024).toFixed(2)}MB) exceeds the 1MB limit`);
            setInsuranceFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }
        if (!ALLOWED_FILE_TYPES.includes(selectedFile.type)) {
            setFileError('Only PDF and image files are allowed');
            setInsuranceFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }
        setInsuranceFile(selectedFile);
    };

    const removeFile = () => {
        setInsuranceFile(null);
        setFileError('');
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const formatFileSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (fileError) return;
        if (!editId && !insuranceFile) {
            setFileError('Insurance document is required');
            return;
        }
        setSubmitting(true);

        const payload = { ...form, capacity: Number(form.capacity), manufactureYear: Number(form.manufactureYear) };
        const formData = new FormData();
        formData.append('vehicle', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        if (insuranceFile) formData.append('insuranceFile', insuranceFile);

        try {
            if (editId) {
                await api.put(`/vehicles/${editId}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                toast.success('Vehicle updated');
            } else {
                await api.post('/vehicles', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                toast.success('Vehicle created');
            }
            setShowModal(false);
            loadVehicles();
        } catch { /* handled */ } finally {
            setSubmitting(false);
        }
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
                                    <th>Insurance Doc</th>
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
                                        <td>
                                            {v.insuranceFileUrl ? (
                                                <button
                                                    className="btn btn-ghost btn-sm"
                                                    onClick={() => window.open(v.insuranceFileUrl, '_blank')}
                                                    title="View Document"
                                                    style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}
                                                >
                                                    <HiEye size={15} style={{ color: 'var(--blue-500)' }} />
                                                    <span style={{ fontSize: 12, color: 'var(--blue-600)', fontWeight: 600 }}>View</span>
                                                </button>
                                            ) : (
                                                <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>—</span>
                                            )}
                                        </td>
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
                                    <div className="form-group full-width" style={{ marginTop: 12 }}>
                                        <label className="form-label">
                                            Insurance Document {!editId && <span style={{ color: 'var(--red-500)' }}>*</span>}
                                            <span style={{ fontWeight: 400, color: 'var(--gray-400)', marginLeft: 6 }}>(PDF / Image, max 1MB)</span>
                                        </label>
                                        <div className={`file-upload-area ${fileError ? 'file-upload-error' : ''} ${insuranceFile ? 'file-upload-filled' : ''}`}>
                                            {!insuranceFile ? (
                                                <label className="file-upload-label" htmlFor="insurance-file-input">
                                                    <HiUpload size={24} className="file-upload-icon" />
                                                    <span className="file-upload-text">Click to upload insurance</span>
                                                    <span className="file-upload-hint">PDF or Image up to 1MB</span>
                                                    <input
                                                        ref={fileInputRef}
                                                        id="insurance-file-input"
                                                        type="file"
                                                        accept=".pdf,.jpg,.jpeg,.png"
                                                        onChange={handleFileChange}
                                                        className="file-upload-input"
                                                    />
                                                </label>
                                            ) : (
                                                <div className="file-upload-preview">
                                                    <div className="file-upload-file-info">
                                                        <HiDocumentText size={20} style={{ color: 'var(--blue-500)', flexShrink: 0 }} />
                                                        <div className="file-upload-details">
                                                            <span className="file-upload-name">{insuranceFile.name}</span>
                                                            <span className="file-upload-size">{formatFileSize(insuranceFile.size)}</span>
                                                        </div>
                                                    </div>
                                                    <button type="button" className="file-upload-remove" onClick={removeFile} title="Remove file">
                                                        <HiX size={16} />
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                        {fileError && (
                                            <span className="file-upload-error-text"><HiExclamation size={14} /> {fileError}</span>
                                        )}
                                        {editId && !insuranceFile && (
                                            <span style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 4, display: 'block' }}>
                                                Leave empty to keep the existing insurance document
                                            </span>
                                        )}
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary" disabled={submitting || !!fileError}>
                                        {submitting ? 'Saving...' : editId ? 'Update' : 'Create'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
