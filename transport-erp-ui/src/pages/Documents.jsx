import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiX, HiExclamation, HiUpload, HiDocumentText, HiEye, HiSearch, HiFilter } from 'react-icons/hi';

const entityTypes = ['VEHICLE', 'DRIVER', 'COMPANY', 'TRIP'];
const docTypes = [
    'REGISTRATION_CERTIFICATE', 'INSURANCE', 'FITNESS_CERTIFICATE', 'PERMIT', 'PUC', 'ROAD_TAX', 'FASTAG',
    'DRIVING_LICENSE', 'AADHAR_CARD', 'PAN_CARD', 'MEDICAL_CERTIFICATE', 'TRAINING_CERTIFICATE',
    'POLICE_VERIFICATION', 'CONTRACT', 'AGREEMENT', 'OTHER'
];

const MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB in bytes

// Map entity types to their API endpoints and display label fields
const entityApiMap = {
    VEHICLE: { endpoint: '/vehicles', labelFn: (e) => `${e.registrationNumber} — ${e.make || ''} ${e.model || ''}`.trim() },
    DRIVER: { endpoint: '/drivers', labelFn: (e) => `${e.name} (${e.employeeCode || 'N/A'})` },
    COMPANY: { endpoint: '/branches', labelFn: (e) => e.name || e.branchName || e.id },
    TRIP: { endpoint: '/assignments', labelFn: (e) => `Trip #${e.id?.substring(0, 8)}` },
};

const emptyDoc = {
    entityType: 'VEHICLE', entityId: '', documentType: 'INSURANCE', documentNumber: '',
    issueDate: '', expiryDate: '', remarks: '', alertDaysBefore: 30
};

export default function Documents() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('DOCUMENT_EDIT');

    const [allDocuments, setAllDocuments] = useState([]);
    const [expiring, setExpiring] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyDoc });
    const [file, setFile] = useState(null);
    const [fileError, setFileError] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const fileInputRef = useRef(null);

    // Filters
    const [searchQuery, setSearchQuery] = useState('');
    const [filterEntityType, setFilterEntityType] = useState('ALL');

    // Entity options loaded dynamically based on selected entityType
    const [entityOptions, setEntityOptions] = useState([]);
    const [loadingEntities, setLoadingEntities] = useState(false);

    useEffect(() => { loadData(); }, []);

    // Load entity options when entityType changes or modal opens
    useEffect(() => {
        if (showModal && form.entityType) {
            loadEntityOptions(form.entityType);
        }
    }, [showModal, form.entityType]);

    const loadEntityOptions = async (entityType) => {
        const mapping = entityApiMap[entityType];
        if (!mapping) {
            setEntityOptions([]);
            return;
        }
        setLoadingEntities(true);
        try {
            const res = await api.get(mapping.endpoint);
            const data = res.data.data || res.data || [];
            const items = Array.isArray(data) ? data : (data.content || []);
            setEntityOptions(items.map(e => ({ id: e.id, label: mapping.labelFn(e) })));
        } catch {
            setEntityOptions([]);
        } finally {
            setLoadingEntities(false);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const [allRes, expiringRes] = await Promise.all([
                api.get('/documents'),
                api.get('/documents/expiring?daysAhead=90')
            ]);
            // Filter out DRIVING_LICENSE — those are managed on the Drivers page
            const filterDL = (docs) => (docs || []).filter(d => d.documentType !== 'DRIVING_LICENSE');
            setAllDocuments(filterDL(allRes.data.data));
            setExpiring(filterDL(expiringRes.data.data));
        } catch { /* handled by interceptor */ } finally {
            setLoading(false);
        }
    };

    const openCreate = () => {
        setForm({ ...emptyDoc });
        setEditId(null);
        setFile(null);
        setFileError('');
        setShowModal(true);
    };

    const openEdit = (d) => {
        setForm({
            entityType: d.entityType || 'VEHICLE', entityId: d.entityId || '',
            documentType: d.documentType || 'INSURANCE', documentNumber: d.documentNumber || '',
            issueDate: d.issueDate || '', expiryDate: d.expiryDate || '',
            remarks: d.remarks || '', alertDaysBefore: d.alertDaysBefore || 30
        });
        setEditId(d.id);
        setFile(null);
        setFileError('');
        setShowModal(true);
    };

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        setFileError('');

        if (!selectedFile) {
            setFile(null);
            return;
        }

        if (selectedFile.size > MAX_FILE_SIZE) {
            setFileError(`File size (${(selectedFile.size / 1024 / 1024).toFixed(2)}MB) exceeds the 1MB limit`);
            setFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        setFile(selectedFile);
    };

    const removeFile = () => {
        setFile(null);
        setFileError('');
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (fileError) return;

        setSubmitting(true);

        const formData = new FormData();
        formData.append('entityType', form.entityType);
        formData.append('entityId', form.entityId);
        formData.append('documentType', form.documentType);
        if (form.documentNumber) formData.append('documentNumber', form.documentNumber);
        if (form.issueDate) formData.append('issueDate', form.issueDate);
        if (form.expiryDate) formData.append('expiryDate', form.expiryDate);
        if (form.remarks) formData.append('remarks', form.remarks);
        formData.append('alertDaysBefore', form.alertDaysBefore || 30);
        if (file) formData.append('file', file);

        try {
            if (editId) {
                await api.put(`/documents/${editId}`, formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
                toast.success('Document updated');
            } else {
                await api.post('/documents', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
                toast.success('Document created');
            }
            setShowModal(false);
            loadData();
        } catch { /* handled */ } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm('Cancel this document?')) return;
        try {
            await api.delete(`/documents/${id}`);
            toast.success('Document cancelled');
            loadData();
        } catch { /* handled */ }
    };

    const handleViewFile = (fileUrl) => {
        if (!fileUrl) {
            toast.error('No file attached to this document');
            return;
        }
        // fileUrl is like "/api/documents/files/xxx.pdf" — open via the proxy
        window.open(fileUrl, '_blank');
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const daysUntilExpiry = (date) => {
        if (!date) return null;
        const diff = Math.ceil((new Date(date) - new Date()) / (1000 * 60 * 60 * 24));
        return diff;
    };

    const formatFileSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    };

    // Filter all documents
    const filteredDocuments = allDocuments.filter(d => {
        const matchesType = filterEntityType === 'ALL' || d.entityType === filterEntityType;
        const matchesSearch = !searchQuery ||
            d.documentType?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            d.documentNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            d.entityType?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            d.remarks?.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesType && matchesSearch;
    });

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Documents</h2>
                    <p>Track and manage vehicle & driver documents</p>
                </div>
                {canEdit && (
                    <button id="create-document-btn" className="btn btn-primary" onClick={openCreate}>
                        <HiPlus size={16} /> Add Document
                    </button>
                )}
            </div>

            {/* ── Stats Cards ───────────────────── */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon blue"><HiDocumentText size={22} /></div>
                    <div className="stat-info">
                        <h4>Total Documents</h4>
                        <div className="stat-value">{allDocuments.length}</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon amber"><HiExclamation size={22} /></div>
                    <div className="stat-info">
                        <h4>Expiring Soon</h4>
                        <div className="stat-value">{expiring.length}</div>
                        <div className="stat-sub">Within 90 days</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon red"><HiExclamation size={22} /></div>
                    <div className="stat-info">
                        <h4>Expired</h4>
                        <div className="stat-value">{allDocuments.filter(d => daysUntilExpiry(d.expiryDate) !== null && daysUntilExpiry(d.expiryDate) <= 0).length}</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon green"><HiUpload size={22} /></div>
                    <div className="stat-info">
                        <h4>With Files</h4>
                        <div className="stat-value">{allDocuments.filter(d => d.fileUrl).length}</div>
                    </div>
                </div>
            </div>

            {/* ── Section 1: Expiring Soon ───────────────────── */}
            {expiring.length > 0 && (
                <div style={{ marginBottom: 28 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
                        <div style={{
                            width: 32, height: 32, borderRadius: 'var(--radius-sm)',
                            background: 'var(--amber-100)', display: 'flex', alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            <HiExclamation size={18} style={{ color: 'var(--amber-500)' }} />
                        </div>
                        <div>
                            <h3 style={{ fontSize: 15, fontWeight: 700, color: 'var(--gray-900)' }}>Expiring Soon</h3>
                            <p style={{ fontSize: 12, color: 'var(--gray-400)', marginTop: 1 }}>{expiring.length} document(s) expiring within 90 days</p>
                        </div>
                    </div>
                    <div className="card">
                        <div className="table-wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Document</th>
                                        <th>Entity Type</th>
                                        <th>Doc Number</th>
                                        <th>Expiry Date</th>
                                        <th>Days Left</th>
                                        <th>File</th>
                                        {canEdit && <th className="text-right">Actions</th>}
                                    </tr>
                                </thead>
                                <tbody>
                                    {expiring.map(d => {
                                        const days = daysUntilExpiry(d.expiryDate);
                                        return (
                                            <tr key={d.id}>
                                                <td style={{ fontWeight: 600 }}>{d.documentType?.replace(/_/g, ' ')}</td>
                                                <td><span className="badge badge-blue">{d.entityType}</span></td>
                                                <td>{d.documentNumber || '—'}</td>
                                                <td>{d.expiryDate}</td>
                                                <td>
                                                    <span className={`badge ${days <= 0 ? 'badge-red' : days <= 30 ? 'badge-amber' : 'badge-green'}`}>
                                                        {days <= 0 ? 'Expired' : `${days} days`}
                                                    </span>
                                                </td>
                                                <td>
                                                    {d.fileUrl ? (
                                                        <button className="btn btn-ghost btn-sm" onClick={() => handleViewFile(d.fileUrl)} title="View file">
                                                            <HiEye size={15} style={{ color: 'var(--blue-500)' }} /> View
                                                        </button>
                                                    ) : (
                                                        <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>No file</span>
                                                    )}
                                                </td>
                                                {canEdit && (
                                                    <td className="text-right">
                                                        <div className="flex items-center gap-8" style={{ justifyContent: 'flex-end' }}>
                                                            <button className="btn btn-ghost btn-icon" onClick={() => openEdit(d)} title="Edit"><HiPencil size={16} /></button>
                                                            <button className="btn btn-ghost btn-icon" onClick={() => handleDelete(d.id)} style={{ color: 'var(--red-500)' }} title="Delete"><HiTrash size={16} /></button>
                                                        </div>
                                                    </td>
                                                )}
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Section 2: All Documents ───────────────────── */}
            <div style={{ marginBottom: 28 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
                    <div style={{
                        width: 32, height: 32, borderRadius: 'var(--radius-sm)',
                        background: 'var(--blue-100)', display: 'flex', alignItems: 'center',
                        justifyContent: 'center'
                    }}>
                        <HiDocumentText size={18} style={{ color: 'var(--blue-600)' }} />
                    </div>
                    <h3 style={{ fontSize: 15, fontWeight: 700, color: 'var(--gray-900)' }}>All Documents</h3>
                </div>

                <div className="card">
                    {/* Filters bar */}
                    <div style={{
                        padding: '16px 20px', borderBottom: '1px solid var(--gray-100)',
                        display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap'
                    }}>
                        {/* Search */}
                        <div style={{ position: 'relative', flex: '1 1 220px', maxWidth: 320 }}>
                            <HiSearch size={16} style={{
                                position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)',
                                color: 'var(--gray-400)'
                            }} />
                            <input
                                className="form-input" placeholder="Search documents..."
                                value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)}
                                style={{ paddingLeft: 36, width: '100%' }}
                            />
                        </div>

                        {/* Entity type filter tabs */}
                        <div style={{ display: 'flex', gap: 4, background: 'var(--gray-50)', borderRadius: 'var(--radius-sm)', padding: 3 }}>
                            {['ALL', ...entityTypes].map(t => (
                                <button key={t}
                                    onClick={() => setFilterEntityType(t)}
                                    style={{
                                        padding: '6px 14px', borderRadius: 6, border: 'none',
                                        fontSize: 12, fontWeight: 600, cursor: 'pointer',
                                        background: filterEntityType === t ? 'var(--white)' : 'transparent',
                                        color: filterEntityType === t ? 'var(--blue-600)' : 'var(--gray-500)',
                                        boxShadow: filterEntityType === t ? 'var(--shadow-sm)' : 'none',
                                        transition: 'all var(--transition-fast)'
                                    }}
                                >
                                    {t === 'ALL' ? 'All' : t.charAt(0) + t.slice(1).toLowerCase()}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="table-wrapper">
                        {filteredDocuments.length > 0 ? (
                            <table>
                                <thead>
                                    <tr>
                                        <th>Document Type</th>
                                        <th>Entity</th>
                                        <th>Doc Number</th>
                                        <th>Issue Date</th>
                                        <th>Expiry Date</th>
                                        <th>Status</th>
                                        <th>File</th>
                                        {canEdit && <th className="text-right">Actions</th>}
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredDocuments.map(d => {
                                        const days = daysUntilExpiry(d.expiryDate);
                                        return (
                                            <tr key={d.id}>
                                                <td style={{ fontWeight: 600 }}>{d.documentType?.replace(/_/g, ' ')}</td>
                                                <td>
                                                    <span className="badge badge-blue" style={{ marginRight: 6 }}>{d.entityType}</span>
                                                    <span style={{ fontSize: 11, color: 'var(--gray-400)' }}>{d.entityId?.substring(0, 8)}…</span>
                                                </td>
                                                <td>{d.documentNumber || '—'}</td>
                                                <td>{d.issueDate || '—'}</td>
                                                <td>
                                                    {d.expiryDate ? (
                                                        <span>{d.expiryDate}</span>
                                                    ) : '—'}
                                                </td>
                                                <td>
                                                    {days !== null ? (
                                                        <span className={`badge ${days <= 0 ? 'badge-red' : days <= 30 ? 'badge-amber' : 'badge-green'}`}>
                                                            {days <= 0 ? 'Expired' : days <= 90 ? `${days}d left` : 'Valid'}
                                                        </span>
                                                    ) : (
                                                        <span className="badge badge-gray">No Expiry</span>
                                                    )}
                                                </td>
                                                <td>
                                                    {d.fileUrl ? (
                                                        <button className="btn btn-ghost btn-sm" onClick={() => handleViewFile(d.fileUrl)} title="View file"
                                                            style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                                                            <HiEye size={15} style={{ color: 'var(--blue-500)' }} />
                                                            <span style={{ fontSize: 12, color: 'var(--blue-600)', fontWeight: 600 }}>View</span>
                                                        </button>
                                                    ) : (
                                                        <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>No file</span>
                                                    )}
                                                </td>
                                                {canEdit && (
                                                    <td className="text-right">
                                                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, justifyContent: 'flex-end' }}>
                                                            <button className="btn btn-ghost btn-icon" onClick={() => openEdit(d)} title="Edit">
                                                                <HiPencil size={16} />
                                                            </button>
                                                            <button className="btn btn-ghost btn-icon" onClick={() => handleDelete(d.id)}
                                                                style={{ color: 'var(--red-500)' }} title="Cancel document">
                                                                <HiTrash size={16} />
                                                            </button>
                                                        </div>
                                                    </td>
                                                )}
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        ) : (
                            <div className="empty-state">
                                <div className="empty-state-icon">📄</div>
                                <h4>{allDocuments.length === 0 ? 'No documents yet' : 'No matching documents'}</h4>
                                <p>{allDocuments.length === 0
                                    ? 'Upload your first document to get started'
                                    : 'Try adjusting your search or filter'
                                }</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* ── Create / Edit Modal ───────────────────── */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>{editId ? 'Edit Document' : 'Add Document'}</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleSubmit}>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Entity Type *</label>
                                        <select className="form-select" name="entityType" value={form.entityType}
                                            onChange={(e) => {
                                                setForm({ ...form, entityType: e.target.value, entityId: '' });
                                            }}>
                                            {entityTypes.map(t => <option key={t} value={t}>{t}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Entity *</label>
                                        <select className="form-select" name="entityId" value={form.entityId} onChange={onChange} required
                                            disabled={loadingEntities}>
                                            <option value="">{loadingEntities ? 'Loading...' : '— Select —'}</option>
                                            {entityOptions.map(opt => (
                                                <option key={opt.id} value={opt.id}>{opt.label}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Document Type *</label>
                                        <select className="form-select" name="documentType" value={form.documentType} onChange={onChange}>
                                            {docTypes.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Document Number</label>
                                        <input className="form-input" name="documentNumber" value={form.documentNumber} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Issue Date</label>
                                        <input className="form-input" name="issueDate" type="date" value={form.issueDate} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Expiry Date</label>
                                        <input className="form-input" name="expiryDate" type="date" value={form.expiryDate} onChange={onChange} />
                                    </div>

                                    {/* File Upload Area */}
                                    <div className="form-group full-width">
                                        <label className="form-label">Upload Document <span style={{ fontWeight: 400, color: 'var(--gray-400)' }}>(max 1MB)</span></label>
                                        <div className={`file-upload-area ${fileError ? 'file-upload-error' : ''} ${file ? 'file-upload-filled' : ''}`}>
                                            {!file ? (
                                                <label className="file-upload-label" htmlFor="file-input">
                                                    <HiUpload size={24} className="file-upload-icon" />
                                                    <span className="file-upload-text">Click to upload or drag and drop</span>
                                                    <span className="file-upload-hint">PDF, JPG, PNG up to 1MB</span>
                                                    <input
                                                        ref={fileInputRef}
                                                        id="file-input"
                                                        type="file"
                                                        accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                                                        onChange={handleFileChange}
                                                        className="file-upload-input"
                                                    />
                                                </label>
                                            ) : (
                                                <div className="file-upload-preview">
                                                    <div className="file-upload-file-info">
                                                        <HiDocumentText size={20} style={{ color: 'var(--blue-500)', flexShrink: 0 }} />
                                                        <div className="file-upload-details">
                                                            <span className="file-upload-name">{file.name}</span>
                                                            <span className="file-upload-size">{formatFileSize(file.size)}</span>
                                                        </div>
                                                    </div>
                                                    <button type="button" className="file-upload-remove" onClick={removeFile} title="Remove file">
                                                        <HiX size={16} />
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                        {fileError && (
                                            <span className="file-upload-error-text">
                                                <HiExclamation size={14} /> {fileError}
                                            </span>
                                        )}
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Alert Days Before</label>
                                        <input className="form-input" name="alertDaysBefore" type="number" value={form.alertDaysBefore} onChange={onChange} />
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Remarks</label>
                                        <input className="form-input" name="remarks" value={form.remarks} onChange={onChange} />
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
