import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { FiPlus, FiTrash2, FiSearch, FiEdit2, FiMapPin } from 'react-icons/fi';
import api from '../services/api';
import FuelModal from '../components/FuelModal';

export default function Fuel() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedFuel, setSelectedFuel] = useState(null);

    const fetchTransactions = async () => {
        try {
            setLoading(true);
            const res = await api.get(`/fuel?page=${page}&size=10`);
            setTransactions(res.data.data.content);
            setTotalPages(res.data.data.totalPages);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTransactions();
    }, [page]);

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this fuel record?')) return;
        try {
            await api.delete(`/fuel/${id}`);
            toast.success('Fuel record deleted successfully');
            fetchTransactions();
        } catch (error) {
            console.error(error);
        }
    };

    const handleSuccess = () => {
        setIsModalOpen(false);
        fetchTransactions();
    };

    if (loading) return <div className="page-loader"><div className="spinner"></div></div>;

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Fuel Management</h2>
                    <p>Track fleet fuel expenses and vehicle efficiency</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <button
                        onClick={() => { setSelectedFuel(null); setIsModalOpen(true); }}
                        className="btn btn-primary"
                    >
                        <FiPlus size={16} /> Record Fuel
                    </button>
                </div>
            </div>

            <div className="card">
                <div className="table-wrapper">
                    {transactions.length > 0 ? (
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Vehicle</th>
                                    <th>Driver</th>
                                    <th>Trip</th>
                                    <th>Quantity</th>
                                    <th>Total Amount</th>
                                    <th>Odometer</th>
                                    <th>Location</th>
                                    <th className="text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {transactions.map((t) => (
                                    <tr key={t.id}>
                                        <td>
                                            {new Date(t.date).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                                        </td>
                                        <td style={{ fontWeight: 600 }}>
                                            {t.vehicleRegistrationNumber}
                                        </td>
                                        <td>
                                            {t.driverName || '—'}
                                        </td>
                                        <td>
                                            {t.tripRoute ? (
                                                <div style={{ fontSize: 13 }}>
                                                    <span style={{ fontWeight: 500, color: 'var(--blue-600)' }}>{t.tripRoute.split(' (')[0]}</span>
                                                    <br />
                                                    <span style={{ color: 'var(--gray-500)' }}>{t.tripRoute.substring(t.tripRoute.indexOf('('))}</span>
                                                </div>
                                            ) : '—'}
                                        </td>
                                        <td>
                                            <div style={{ fontWeight: 600 }}>{t.litres} L</div>
                                            <div style={{ fontSize: 12, color: 'var(--gray-500)' }}>@ ₹{t.pricePerLitre}/L</div>
                                        </td>
                                        <td style={{ fontWeight: 600 }}>
                                            ₹{t.totalAmount}
                                        </td>
                                        <td>
                                            {t.odometerReading ? `${t.odometerReading} km` : '—'}
                                        </td>
                                        <td>
                                            {t.location || t.fuelStation || '—'}
                                        </td>
                                        <td className="text-right">
                                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
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
                            <div className="empty-state-icon">⛽</div>
                            <h4>No fuel records found</h4>
                            <p>Click "Record Fuel" to add your first entry.</p>
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

            <FuelModal
                isOpen={isModalOpen}
                initialData={selectedFuel}
                onClose={() => setIsModalOpen(false)}
                onSuccess={handleSuccess}
            />
        </div>
    );
}
