import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiX } from 'react-icons/hi';

const emptyBranch = { name: '', code: '', address: '', city: '', state: '', pinCode: '', phone: '', email: '', contactPerson: '' };

export default function Branches() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('BRANCH_EDIT');
    const [branches, setBranches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyBranch });

    useEffect(() => { loadBranches(); }, []);

    const loadBranches = async () => {
        try {
            const res = await api.get('/branches');
            setBranches(res.data.data || []);
        } catch { /* handled */ } finally { setLoading(false); }
    };

    const openCreate = () => { setForm({ ...emptyBranch }); setEditId(null); setShowModal(true); };
    const openEdit = (b) => { setForm({ ...b }); setEditId(b.id); setShowModal(true); };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editId) {
                await api.put(`/branches/${editId}`, form);
                toast.success('Branch updated');
            } else {
                await api.post('/branches', form);
                toast.success('Branch created');
            }
            setShowModal(false);
            loadBranches();
        } catch { /* handled */ }
    };

    const handleDelete = async (id) => {
        if (!confirm('Deactivate this branch?')) return;
        try {
            await api.delete(`/branches/${id}`);
            toast.success('Branch deactivated');
            loadBranches();
        } catch { /* handled */ }
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Branches</h2>
                    <p>Manage your depot locations</p>
                </div>
                {canEdit && (
                    <button id="create-branch-btn" className="btn btn-primary" onClick={openCreate}>
                        <HiPlus size={16} /> Add Branch
                    </button>
                )}
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {branches.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Code</th>
                                    <th>City</th>
                                    <th>Contact</th>
                                    <th>Phone</th>
                                    <th>Status</th>
                                    {canEdit && <th className="text-right">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {branches.map(b => (
                                    <tr key={b.id}>
                                        <td style={{ fontWeight: 600 }}>{b.name}</td>
                                        <td><span className="badge badge-blue">{b.code}</span></td>
                                        <td>{b.city}, {b.state}</td>
                                        <td>{b.contactPerson}</td>
                                        <td>{b.phone}</td>
                                        <td><span className={`badge ${b.active !== false ? 'badge-green' : 'badge-red'}`}>{b.active !== false ? 'Active' : 'Inactive'}</span></td>
                                        {canEdit && (
                                            <td className="text-right">
                                                <div className="flex items-center gap-8" style={{ justifyContent: 'flex-end' }}>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => openEdit(b)} title="Edit"><HiPencil size={16} /></button>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => handleDelete(b.id)} title="Deactivate" style={{ color: 'var(--red-500)' }}><HiTrash size={16} /></button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon">🏢</div>
                            <h4>No branches yet</h4>
                            <p>Create your first branch to get started</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>{editId ? 'Edit Branch' : 'Create Branch'}</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleSubmit}>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Branch Name *</label>
                                        <input className="form-input" name="name" value={form.name} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Code *</label>
                                        <input className="form-input" name="code" value={form.code} onChange={onChange} required />
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
                                        <label className="form-label">Phone</label>
                                        <input className="form-input" name="phone" value={form.phone} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Email</label>
                                        <input className="form-input" name="email" type="email" value={form.email} onChange={onChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Contact Person</label>
                                        <input className="form-input" name="contactPerson" value={form.contactPerson} onChange={onChange} />
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
