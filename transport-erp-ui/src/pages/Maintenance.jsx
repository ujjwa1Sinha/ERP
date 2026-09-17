import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { FiPlus, FiTrash2, FiEdit2 } from 'react-icons/fi';
import api from '../services/api';
import MaintenanceModal from '../components/MaintenanceModal';
import ExportButtons from '../components/ExportButtons';

export default function Maintenance() {
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedRecord, setSelectedRecord] = useState(null);

    const fetchRecords = async () => {
        try {
            setLoading(true);
            const res = await api.get(`/maintenance?page=${page}&size=10`);
            setRecords(res.data.data?.content || res.data?.content || []);
            setTotalPages(res.data.data?.totalPages || res.data?.totalPages || 0);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRecords();
    }, [page]);

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this maintenance record?')) return;
        try {
            await api.delete(`/maintenance/${id}`);
            toast.success('Maintenance record deleted successfully');
            fetchRecords();
        } catch (error) {
            console.error(error);
        }
    };

    const handleSuccess = () => {
        setIsModalOpen(false);
        fetchRecords();
    };

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Maintenance Management</h2>
                    <p>Track vehicle servicing, repairs, and upcoming maintenance</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <ExportButtons entityType="maintenance" />
                    <button
                        onClick={() => { setSelectedRecord(null); setIsModalOpen(true); }}
                        className="btn btn-primary"
                    >
                        <FiPlus size={16} /> Add Record
                    </button>
                </div>
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {records.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Vehicle</th>
                                    <th>Type</th>
                                    <th>Cost</th>
                                    <th>Odometer</th>
                                    <th>Vendor</th>
                                    <th>Next Service</th>
                                    <th className="text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {records.map((t) => (
                                    <tr key={t.id}>
                                        <td>
                                            {new Date(t.serviceDate).toLocaleDateString()}
                                        </td>
                                        <td style={{ fontWeight: 600 }}>
                                            {t.vehicleRegistrationNumber}
                                        </td>
                                        <td>
                                            <span style={{
                                                padding: '4px 8px', borderRadius: '4px', fontSize: '12px',
                                                background: 'var(--blue-50)', color: 'var(--blue-600)',
                                                border: '1px solid var(--blue-200)'
                                            }}>
                                                {t.maintenanceType}
                                            </span>
                                        </td>
                                        <td style={{ fontWeight: 600 }}>
                                            ₹{t.cost}
                                        </td>
                                        <td>
                                            {t.odometerReading ? `${t.odometerReading} km` : '—'}
                                        </td>
                                        <td>
                                            {t.vendor || '—'}
                                        </td>
                                        <td>
                                            {t.nextServiceDate ? new Date(t.nextServiceDate).toLocaleDateString() : '—'}
                                        </td>
                                        <td className="text-right">
                                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                                                <button
                                                    onClick={() => { setSelectedRecord(t); setIsModalOpen(true); }}
                                                    className="btn btn-ghost btn-icon"
                                                    title="Edit Record"
                                                >
                                                    <FiEdit2 size={16} />
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(t.id)}
                                                    className="btn btn-ghost btn-icon"
                                                    style={{ color: 'var(--red-500)' }}
                                                    title="Delete Record"
                                                >
                                                    <FiTrash2 size={16} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-state-icon">🔧</div>
                            <h4>No maintenance records found</h4>
                            <p>Click "Add Record" to add your first entry.</p>
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

            <MaintenanceModal
                isOpen={isModalOpen}
                initialData={selectedRecord}
                onClose={() => setIsModalOpen(false)}
                onSuccess={handleSuccess}
            />
        </div>
    );
}
