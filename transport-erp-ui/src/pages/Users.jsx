import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiX, HiUserAdd } from 'react-icons/hi';

const availableRoles = [
    'SUPER_ADMIN', 'OWNER', 'BRANCH_ADMIN', 'FLEET_MANAGER', 'DISPATCHER',
    'ACCOUNTANT', 'HR', 'MAINTENANCE_MANAGER', 'DRIVER', 'VIEWER'
];

const emptyUser = {
    username: '', password: '', email: '', fullName: '', phone: '', roles: ['VIEWER']
};

const emptyUpdate = {
    fullName: '', email: '', phone: '', password: '', roles: [], active: true, branchId: ''
};

export default function Users() {
    const { hasPermission } = useAuth();
    const canEdit = hasPermission('USER_EDIT');
    const [users, setUsers] = useState([]);
    const [branches, setBranches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [editUser, setEditUser] = useState(null);
    const [form, setForm] = useState({ ...emptyUser });
    const [updateForm, setUpdateForm] = useState({ ...emptyUpdate });

    useEffect(() => { loadUsers(); loadBranches(); }, []);

    const loadUsers = async () => {
        try {
            const res = await api.get('/auth/users');
            setUsers(res.data.data || []);
        } catch { /* handled */ } finally { setLoading(false); }
    };

    const loadBranches = async () => {
        try {
            const res = await api.get('/branches');
            setBranches(res.data.data || []);
        } catch { /* silent */ }
    };

    const openCreate = () => { setForm({ ...emptyUser }); setShowModal(true); };
    const openEdit = (u) => {
        setEditUser(u);
        setUpdateForm({
            fullName: u.fullName || '',
            email: u.email || '',
            phone: u.phone || '',
            password: '',
            roles: u.roles || [],
            active: u.active !== false,
            branchId: u.branchId || ''
        });
        setShowEditModal(true);
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await api.post('/auth/register', form);
            toast.success('User created');
            setShowModal(false);
            loadUsers();
        } catch { /* handled */ }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        const payload = {};
        if (updateForm.fullName) payload.fullName = updateForm.fullName;
        if (updateForm.email) payload.email = updateForm.email;
        if (updateForm.phone) payload.phone = updateForm.phone;
        if (updateForm.password) payload.password = updateForm.password;
        if (updateForm.roles.length) payload.roles = updateForm.roles;
        if (updateForm.branchId) payload.branchId = updateForm.branchId;
        payload.active = updateForm.active;
        try {
            await api.put(`/auth/users/${editUser.id}`, payload);
            toast.success('User updated');
            setShowEditModal(false);
            loadUsers();
        } catch { /* handled */ }
    };

    const handleDeactivate = async (id) => {
        if (!confirm('Deactivate this user?')) return;
        try {
            await api.delete(`/auth/users/${id}`);
            toast.success('User deactivated');
            loadUsers();
        } catch { /* handled */ }
    };

    const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
    const onUpdateChange = (e) => setUpdateForm({ ...updateForm, [e.target.name]: e.target.value });

    const toggleRole = (role, isUpdate = false) => {
        if (isUpdate) {
            const roles = updateForm.roles.includes(role)
                ? updateForm.roles.filter(r => r !== role)
                : [...updateForm.roles, role];
            setUpdateForm({ ...updateForm, roles });
        } else {
            const roles = form.roles.includes(role)
                ? form.roles.filter(r => r !== role)
                : [...form.roles, role];
            setForm({ ...form, roles });
        }
    };

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>User Management</h2>
                    <p>Manage system users and their roles</p>
                </div>
                {canEdit && (
                    <button id="create-user-btn" className="btn btn-primary" onClick={openCreate}>
                        <HiUserAdd size={16} /> Add User
                    </button>
                )}
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {users.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Email</th>
                                    <th>Phone</th>
                                    <th>Roles</th>
                                    <th>Branch</th>
                                    <th>Status</th>
                                    {canEdit && <th className="text-right">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {users.map(u => (
                                    <tr key={u.id}>
                                        <td>
                                            <div>
                                                <div style={{ fontWeight: 600 }}>{u.fullName}</div>
                                                <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>@{u.username}</div>
                                            </div>
                                        </td>
                                        <td>{u.email}</td>
                                        <td>{u.phone || '—'}</td>
                                        <td>
                                            <div className="flex items-center gap-8" style={{ flexWrap: 'wrap' }}>
                                                {(u.roles || []).map(r => (
                                                    <span key={r} className="badge badge-blue">{r}</span>
                                                ))}
                                            </div>
                                        </td>
                                        <td>{u.branchName || '—'}</td>
                                        <td>
                                            <span className={`badge ${u.active !== false ? 'badge-green' : 'badge-red'}`}>
                                                {u.active !== false ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        {canEdit && (
                                            <td className="text-right">
                                                <div className="flex items-center gap-8" style={{ justifyContent: 'flex-end' }}>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => openEdit(u)}><HiPencil size={16} /></button>
                                                    <button className="btn btn-ghost btn-icon" onClick={() => handleDeactivate(u.id)} style={{ color: 'var(--red-500)' }}><HiTrash size={16} /></button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon">👥</div>
                            <h4>No users found</h4>
                            <p>Create your first user</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Create Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Register New User</h3>
                            <button className="modal-close" onClick={() => setShowModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleCreate}>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Username *</label>
                                        <input className="form-input" name="username" value={form.username} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Password *</label>
                                        <input className="form-input" name="password" type="password" value={form.password} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Full Name *</label>
                                        <input className="form-input" name="fullName" value={form.fullName} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Email *</label>
                                        <input className="form-input" name="email" type="email" value={form.email} onChange={onChange} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Phone</label>
                                        <input className="form-input" name="phone" value={form.phone} onChange={onChange} />
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Roles</label>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 4 }}>
                                            {availableRoles.map(role => (
                                                <button
                                                    key={role}
                                                    type="button"
                                                    className={`badge ${form.roles.includes(role) ? 'badge-blue' : 'badge-gray'}`}
                                                    style={{ cursor: 'pointer', padding: '6px 14px', border: 'none' }}
                                                    onClick={() => toggleRole(role)}
                                                >
                                                    {role}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary">Register</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {/* Edit Modal */}
            {showEditModal && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Edit User — {editUser?.username}</h3>
                            <button className="modal-close" onClick={() => setShowEditModal(false)}><HiX /></button>
                        </div>
                        <div className="modal-body">
                            <form onSubmit={handleUpdate}>
                                <div className="form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Full Name</label>
                                        <input className="form-input" name="fullName" value={updateForm.fullName} onChange={onUpdateChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Email</label>
                                        <input className="form-input" name="email" type="email" value={updateForm.email} onChange={onUpdateChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Phone</label>
                                        <input className="form-input" name="phone" value={updateForm.phone} onChange={onUpdateChange} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">New Password</label>
                                        <input className="form-input" name="password" type="password" value={updateForm.password} onChange={onUpdateChange} placeholder="Leave blank to keep" />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Branch</label>
                                        <select className="form-select" name="branchId" value={updateForm.branchId} onChange={onUpdateChange}>
                                            <option value="">No Branch</option>
                                            {branches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Status</label>
                                        <select className="form-select" name="active" value={updateForm.active} onChange={e => setUpdateForm({ ...updateForm, active: e.target.value === 'true' })}>
                                            <option value="true">Active</option>
                                            <option value="false">Inactive</option>
                                        </select>
                                    </div>
                                    <div className="form-group full-width">
                                        <label className="form-label">Roles</label>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 4 }}>
                                            {availableRoles.map(role => (
                                                <button
                                                    key={role}
                                                    type="button"
                                                    className={`badge ${updateForm.roles.includes(role) ? 'badge-blue' : 'badge-gray'}`}
                                                    style={{ cursor: 'pointer', padding: '6px 14px', border: 'none' }}
                                                    onClick={() => toggleRole(role, true)}
                                                >
                                                    {role}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary">Update User</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
