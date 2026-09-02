import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiX, HiSearch, HiUpload, HiDocumentText, HiEye, HiExclamation, HiTrash } from 'react-icons/hi';

const MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/jpg'];

const emptyDriver = {
    employeeCode: '', name: '', phone: '', alternatePhone: '', dateOfBirth: '', joiningDate: '',
    address: '', city: '', state: '', pinCode: '', aadharNumber: '', panNumber: '', bloodGroup: '',
    branchId: '',
    // License (flat)
    licenseNumber: '', licenseType: 'HMV', licenseIssuingAuthority: '', licenseIssueDate: '', licenseExpiryDate: '',
    // Emergency contact (flat)
    ecName: '', ecRelationship: '', ecPhone: '', ecAlternatePhone: '', ecAddress: '',
};

export default function Drivers() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('DRIVER_EDIT');
    const canViewLicense = hasPermission('DRIVER_MEDICAL_VIEW');
    const [drivers, setDrivers] = useState([]);
    const [branches, setBranches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyDriver });
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');
    const [submitting, setSubmitting] = useState(false);

    // License file state
    const [licenseFile, setLicenseFile] = useState(null);
    const [fileError, setFileError] = useState('');
    const fileInputRef = useRef(null);

    useEffect(() => { loadDrivers(); loadBranches(); }, [page]);

    const loadDrivers = async () => {
        try {
            const res = await api.get(`/drivers?page=${page}&size=15`);
            const d = res.data.data;
            setDrivers(d?.content || []);
            setTotalPages(d?.totalPages || 0);
        } catch { /* handled */ } finally { setLoading(false); }
    };

    const loadBranches = async () => {
        try {
            const res = await api.get('/branches');
            setBranches(res.data.data || []);
        } catch { /* silent */ }
    };

    const handleSearch = async () => {
        if (!search.trim()) { loadDrivers(); return; }
        try {
            const res = await api.get(`/drivers/search?name=${search}&page=0&size=15`);
            const d = res.data.data;
            setDrivers(d?.content || []);
            setTotalPages(d?.totalPages || 0);
        } catch { /* handled */ }
    };

    const openCreate = () => {
        setForm({ ...emptyDriver });
        setEditId(null);
        setLicenseFile(null);
        setFileError('');
        setShowModal(true);
    };

    const openEdit = (d) => {
        setForm({
            employeeCode: d.employeeCode || '', name: d.name || '', phone: d.phone || '',
            alternatePhone: d.alternatePhone || '', dateOfBirth: d.dateOfBirth || '',
            joiningDate: d.joiningDate || '', address: d.address || '', city: d.city || '',
            state: d.state || '', pinCode: d.pinCode || '', aadharNumber: d.aadharNumber || '',
            panNumber: d.panNumber || '', bloodGroup: d.bloodGroup || '', branchId: d.branchId || '',
            // License
            licenseNumber: d.licenseNumber || '', licenseType: d.licenseType || 'HMV',
            licenseIssuingAuthority: d.licenseIssuingAuthority || '',
            licenseIssueDate: d.licenseIssueDate || '', licenseExpiryDate: d.licenseExpiryDate || '',
            // Emergency contact
            ecName: d.ecName || '', ecRelationship: d.ecRelationship || '',
            ecPhone: d.ecPhone || '', ecAlternatePhone: d.ecAlternatePhone || '', ecAddress: d.ecAddress || '',
        });
        setEditId(d.id);
        setLicenseFile(null);
        setFileError('');
        setShowModal(true);
    };

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        setFileError('');
        if (!selectedFile) { setLicenseFile(null); return; }
        if (selectedFile.size > MAX_FILE_SIZE) {
            setFileError(`File size (${(selectedFile.size / 1024 / 1024).toFixed(2)}MB) exceeds the 1MB limit`);
            setLicenseFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }
        if (!ALLOWED_FILE_TYPES.includes(selectedFile.type)) {
            setFileError('Only PDF and JPEG files are allowed');
            setLicenseFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }
        setLicenseFile(selectedFile);
    };

    const removeFile = () => {
        setLicenseFile(null);
        setFileError('');
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const formatFileSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    };

    const handleDelete = async (id) => {
        if (!confirm('Are you sure you want to delete this driver?')) return;
        try {
            await api.delete(`/drivers/${id}`);
            toast.success('Driver deleted');
            loadDrivers();
        } catch { /* handled */ }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (fileError) return;
        if (!editId && !licenseFile) {
            setFileError('Driving license file is required');
            return;
        }
        setSubmitting(true);
        const formData = new FormData();
        // Send form fields as JSON blob
        const payload = { ...form };
        formData.append('driver', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        if (licenseFile) formData.append('licenseFile', licenseFile);

        try {
            if (editId) {
                await api.put(`/drivers/${editId}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                toast.success('Driver updated');
            } else {
                await api.post('/drivers', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                toast.success('Driver created');
            }
            setShowModal(false);
            loadDrivers();
        } catch { /* handled */ } finally {
            setSubmitting(false);
        }
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Drivers</h2>
                    <p>Manage your driving personnel</p>
                </div>
                <div className="flex items-center gap-12">
                    <div className="search-bar">
                        <HiSearch className="search-icon" />
                        <input
                            placeholder="Search by name..."
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            onKeyDown={e => e.key === 'Enter' && handleSearch()}
                        />
                    </div>
                    {canEdit && (
                        <button id="create-driver-btn" className="btn btn-primary" onClick={openCreate}>
                            <HiPlus size={16} /> Add Driver
                        </button>
                    )}
                </div>
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {drivers.length > 0 ? (
                        <table>
                            <thead><tr>
                                <th>Code</th>
                                <th>Name</th>
                                <th>Phone</th>
                                <th>City</th>
                                <th>Blood Group</th>
                                <th>Branch</th>
                                <th>License No.</th>
                                <th>License Expiry</th>
                                {canViewLicense && <th>License File</th>}
                                <th>Status</th>
                                {canEdit && <th className="text-right">Actions</th>}
                            </tr></thead>
                            <tbody>
                                {drivers.map(d => (
                                    <tr key={d.id}>
                                        <td><span className="badge badge-blue">{d.employeeCode}</span></td>
                                        <td style={{ fontWeight: 600 }}>{d.name}</td>
                                        <td>{d.phone}</td>
                                        <td>{d.city}</td>
                                        <td>{d.bloodGroup}</td>
                                        <td>{d.branchName || '—'}</td>
                                        <td>{d.licenseNumber || '—'}</td>
                                        <td>
                                            {d.licenseExpiryDate ? (
                                                <span style={{
                                                    color: new Date(d.licenseExpiryDate) < new Date()
                                                        ? 'var(--red-500)' : 'var(--gray-700)',
                                                    fontWeight: new Date(d.licenseExpiryDate) < new Date() ? 600 : 400
                                                }}>
                                                    {d.licenseExpiryDate}
                                                </span>
                                            ) : '—'}
                                        </td>
                                        {canViewLicense && (
                                            <td>
                                                {d.licenseFileUrl ? (
                                                    <button
                                                        className="btn btn-ghost btn-sm"
                                                        onClick={() => window.open(d.licenseFileUrl, '_blank')}
                                                        title="View driving license"
                                                        style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}
                                                    >
                                                        <HiEye size={15} style={{ color: 'var(--blue-500)' }} />
                                                        <span style={{ fontSize: 12, color: 'var(--blue-600)', fontWeight: 600 }}>View</span>
                                                    </button>
                                                ) : (
                                                    <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>No file</span>
                                                )}
                                            </td>
                                        )}
                                        <td><span className={`badge ${d.status === 'ACTIVE' ? 'badge-green' : d.status === 'ON_LEAVE' ? 'badge-amber' : 'badge-red'}`}>{d.status}</span></td>
                                        {canEdit && (
                                            <td className="text-right">
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 4, justifyContent: 'flex-end' }}>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => openEdit(d)} title="Edit"><HiPencil size={16} /></button>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => handleDelete(d.id)} style={{ color: 'var(--red-500)' }} title="Delete driver"><HiTrash size={16} /></button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon">👤</div>
                            <h4>No drivers yet</h4>
                            <p>Add your first driver to get started</p>
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
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 760 }}>
                        <div className="modal-header">
                            <h3>{editId ? 'Edit Driver' : 'Add Driver'}</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleSubmit}>

                                {/* ── Personal Info ─────────────────────── */}
                                <p className="form-section-label">Personal Info</p>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Employee Code *</label>
                                        <input className="form-input" name="employeeCode" value={form.employeeCode} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Full Name *</label>
                                        <input className="form-input" name="name" value={form.name} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Phone *</label>
                                        <input className="form-input" name="phone" value={form.phone} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Alternate Phone</label>
                                        <input className="form-input" name="alternatePhone" value={form.alternatePhone} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Date of Birth</label>
                                        <input className="form-input" name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Joining Date</label>
                                        <input className="form-input" name="joiningDate" type="date" value={form.joiningDate} onChange={onChange} />
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Address</label>
                                        <input className="form-input" name="address" value={form.address} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">City</label>
                                        <input className="form-input" name="city" value={form.city} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">State</label>
                                        <input className="form-input" name="state" value={form.state} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Pin Code</label>
                                        <input className="form-input" name="pinCode" value={form.pinCode} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Aadhar Number</label>
                                        <input className="form-input" name="aadharNumber" value={form.aadharNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">PAN Number</label>
                                        <input className="form-input" name="panNumber" value={form.panNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Blood Group</label>
                                        <input className="form-input" name="bloodGroup" value={form.bloodGroup} onChange={onChange} placeholder="e.g. B+" />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Branch</label>
                                        <select className="form-select" name="branchId" value={form.branchId} onChange={onChange}>
                                            <option value="">Select Branch</option>
                                            {branches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                                        </select>
                                    </div>
                                </div>

                                {/* ── License Info ───────────────────────── */}
                                <p className="form-section-label" style={{ marginTop: 24 }}>License Info</p>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">License Number</label>
                                        <input className="form-input" name="licenseNumber" value={form.licenseNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">License Type</label>
                                        <input className="form-input" name="licenseType" value={form.licenseType} onChange={onChange} placeholder="e.g. HMV" />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Issuing Authority</label>
                                        <input className="form-input" name="licenseIssuingAuthority" value={form.licenseIssuingAuthority} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Issue Date</label>
                                        <input className="form-input" name="licenseIssueDate" type="date" value={form.licenseIssueDate} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Expiry Date</label>
                                        <input className="form-input" name="licenseExpiryDate" type="date" value={form.licenseExpiryDate} onChange={onChange} />
                                    </div>

                                    {/* License File Upload */}
                                    <div className="form-group full-width">
                                        <label className="form-label">
                                            Driving License Document {!editId && <span style={{ color: 'var(--red-500)' }}>*</span>}
                                            <span style={{ fontWeight: 400, color: 'var(--gray-400)', marginLeft: 6 }}>(PDF / JPEG, max 1MB)</span>
                                        </label>
                                        <div className={`file-upload-area ${fileError ? 'file-upload-error' : ''} ${licenseFile ? 'file-upload-filled' : ''}`}>
                                            {!licenseFile ? (
                                                <label className="file-upload-label" htmlFor="license-file-input">
                                                    <HiUpload size={24} className="file-upload-icon" />
                                                    <span className="file-upload-text">Click to upload driving license</span>
                                                    <span className="file-upload-hint">PDF or JPEG up to 1MB</span>
                                                    <input
                                                        ref={fileInputRef}
                                                        id="license-file-input"
                                                        type="file"
                                                        accept=".pdf,.jpg,.jpeg"
                                                        onChange={handleFileChange}
                                                        className="file-upload-input"
                                                    />
                                                </label>
                                            ) : (
                                                <div className="file-upload-preview">
                                                    <div className="file-upload-file-info">
                                                        <HiDocumentText size={20} style={{ color: 'var(--blue-500)', flexShrink: 0 }} />
                                                        <div className="file-upload-details">
                                                            <span className="file-upload-name">{licenseFile.name}</span>
                                                            <span className="file-upload-size">{formatFileSize(licenseFile.size)}</span>
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
                                        {editId && !licenseFile && (
                                            <span style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 4, display: 'block' }}>
                                                Leave empty to keep the existing license file
                                            </span>
                                        )}
                                    </div>
                                </div>

                                {/* ── Emergency Contact ──────────────────── */}
                                <p className="form-section-label" style={{ marginTop: 24 }}>Emergency Contact</p>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Contact Name</label>
                                        <input className="form-input" name="ecName" value={form.ecName} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Relationship</label>
                                        <input className="form-input" name="ecRelationship" value={form.ecRelationship} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Contact Phone</label>
                                        <input className="form-input" name="ecPhone" value={form.ecPhone} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Alternate Phone</label>
                                        <input className="form-input" name="ecAlternatePhone" value={form.ecAlternatePhone} onChange={onChange} />
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Address</label>
                                        <input className="form-input" name="ecAddress" value={form.ecAddress} onChange={onChange} />
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
