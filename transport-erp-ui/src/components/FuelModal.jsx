import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { FiX, FiInfo } from 'react-icons/fi';
import api from '../services/api';

export default function FuelModal({ isOpen, onClose, onSuccess, initialData = null }) {
    const [vehicles, setVehicles] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [trips, setTrips] = useState([]);
    const [fetchingTrips, setFetchingTrips] = useState(false);
    const [formData, setFormData] = useState({
        vehicleId: '',
        driverId: '',
        tripId: '',
        date: new Date().toISOString().slice(0, 16),
        litres: '',
        pricePerLitre: '',
        totalAmount: '',
        odometerReading: '',
        location: '',
        fuelStation: '',
        remarks: ''
    });
    const [submitting, setSubmitting] = useState(false);
    const [autoCalculating, setAutoCalculating] = useState(true);

    useEffect(() => {
        if (!isOpen) return;
        fetchDependencies();
        if (initialData) {
            setFormData({
                id: initialData.id,
                vehicleId: initialData.vehicleId,
                driverId: initialData.driverId || '',
                tripId: initialData.tripId || '',
                date: new Date(initialData.date).toISOString().slice(0, 16),
                litres: initialData.litres,
                pricePerLitre: initialData.pricePerLitre,
                totalAmount: initialData.totalAmount,
                odometerReading: initialData.odometerReading || '',
                location: initialData.location || '',
                fuelStation: initialData.fuelStation || '',
                remarks: initialData.remarks || ''
            });

            if (initialData.vehicleId) {
                fetchTripsForVehicle(initialData.vehicleId);
            }
        } else {
            setFormData({
                vehicleId: '',
                driverId: '',
                tripId: '',
                date: new Date().toISOString().slice(0, 16),
                litres: '',
                pricePerLitre: '',
                totalAmount: '',
                odometerReading: '',
                location: '',
                fuelStation: '',
                remarks: ''
            });
            setTrips([]);
        }
    }, [isOpen, initialData]);

    const fetchTripsForVehicle = async (vehicleId) => {
        if (!vehicleId) {
            setTrips([]);
            return;
        }
        setFetchingTrips(true);
        try {
            // Fetch trips associated with this vehicle, perhaps filtering to active ones
            const response = await api.get(`/trips?vehicleId=${vehicleId}&size=50`);
            setTrips(response.data.data.content || []);
        } catch (error) {
            console.error('Error fetching trips for vehicle:', error);
            toast.error('Failed to load trips for vehicle');
        } finally {
            setFetchingTrips(false);
        }
    };

    const handleVehicleChange = (e) => {
        const vehicleId = e.target.value;
        setFormData({ ...formData, vehicleId, tripId: '' });
        fetchTripsForVehicle(vehicleId);
    };

    const fetchDependencies = async () => {
        try {
            const [vRes, dRes] = await Promise.all([
                api.get('/vehicles?size=1000'),
                api.get('/drivers?size=1000')
            ]);
            setVehicles(vRes.data.data.content || []);
            setDrivers(dRes.data.data.content || []);
        } catch (error) {
            console.error(error);
        }
    };

    // Auto-calculate logic
    useEffect(() => {
        if (autoCalculating && formData.litres && formData.pricePerLitre) {
            const total = parseFloat(formData.litres) * parseFloat(formData.pricePerLitre);
            setFormData(prev => ({ ...prev, totalAmount: total.toFixed(2) }));
        }
    }, [formData.litres, formData.pricePerLitre, autoCalculating]);

    // Smart Fuel Pricing Auto-Fetch
    useEffect(() => {
        const fetchEstimatedPrice = async (source, destination, vehicleId) => {
            try {
                const res = await api.get(`/fuel/price-estimate?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&vehicleId=${vehicleId}`);
                if (res.data?.data?.estimatedPricePerLitre) {
                    setFormData(prev => ({
                        ...prev,
                        pricePerLitre: res.data.data.estimatedPricePerLitre.toString()
                    }));
                    setAutoCalculating(true);
                    toast.success(`Fetched estimated ${res.data.data.fuelType} price for route!`);
                }
            } catch (error) {
                console.error("Error fetching price estimate:", error);
            }
        };

        if (formData.tripId && formData.vehicleId) {
            const selectedTrip = trips.find(t => t.id === formData.tripId);
            if (selectedTrip) {
                if (selectedTrip.source && selectedTrip.destination) {
                    fetchEstimatedPrice(selectedTrip.source, selectedTrip.destination, formData.vehicleId);
                }
                if (selectedTrip.primaryDriverId) {
                    setFormData(prev => ({ ...prev, driverId: selectedTrip.primaryDriverId }));
                }
            }
        }
    }, [formData.tripId, formData.vehicleId, trips]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const payload = {
                ...formData,
                date: new Date(formData.date).toISOString(), // Ensure UTC Instant string
                litres: parseFloat(formData.litres),
                pricePerLitre: formData.pricePerLitre ? parseFloat(formData.pricePerLitre) : null,
                totalAmount: parseFloat(formData.totalAmount),
                odometerReading: formData.odometerReading ? parseFloat(formData.odometerReading) : null,
            };

            // Fix null driver/trip submission if unselected
            if (!payload.driverId) payload.driverId = null;
            if (!payload.tripId) payload.tripId = null;

            if (formData.id) {
                await api.put(`/fuel/${formData.id}`, payload);
                toast.success('Fuel record updated successfully');
            } else {
                await api.post('/fuel', payload);
                toast.success('Fuel record created successfully');
            }
            onSuccess();
        } catch (error) {
            console.error(error);
        } finally {
            setSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 720 }}>
                <div className="modal-header">
                    <h3>{initialData ? 'Edit Fuel Transaction' : 'Record Fuel Transaction'}</h3>
                    <button className="modal-close" onClick={onClose}><FiX size={20} /></button>
                </div>

                <div className="modal-body">
                    <form onSubmit={handleSubmit}>
                        <div className="form-grid">
                            <div className="form-group">
                                <label className="form-label">Vehicle *</label>
                                <select
                                    className="form-select"
                                    required
                                    value={formData.vehicleId}
                                    onChange={handleVehicleChange}
                                >
                                    <option value="">Select a vehicle...</option>
                                    {vehicles.map(v => (
                                        <option key={v.id} value={v.id}>{v.registrationNumber}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Driver (Optional)</label>
                                <select
                                    className="form-select"
                                    value={formData.driverId}
                                    onChange={e => setFormData({ ...formData, driverId: e.target.value })}
                                    disabled={!!formData.tripId}
                                    style={formData.tripId ? { backgroundColor: 'var(--gray-50)', color: 'var(--gray-500)' } : {}}
                                >
                                    <option value="">{formData.tripId ? 'Inherited from Trip...' : 'Select a driver...'}</option>
                                    {drivers.map(d => (
                                        <option key={d.id} value={d.id}>{d.name} ({d.employeeCode || 'No Code'})</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Associated Trip *</label>
                                <select
                                    className="form-select"
                                    required
                                    value={formData.tripId}
                                    onChange={e => setFormData({ ...formData, tripId: e.target.value })}
                                    disabled={!formData.vehicleId || fetchingTrips}
                                >
                                    <option value="">{fetchingTrips ? 'Loading trips...' : 'Select a recent trip...'}</option>
                                    {trips.map(t => {
                                        let label = `${t.tripNumber} (${t.source} to ${t.destination})`;
                                        if (label.length > 50) label = label.substring(0, 47) + '...';
                                        return (
                                            <option key={t.id} value={t.id} title={`${t.tripNumber} (${t.source} to ${t.destination})`}>
                                                {label}
                                            </option>
                                        );
                                    })}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Date & Time *</label>
                                <input
                                    type="datetime-local"
                                    className="form-input"
                                    required
                                    value={formData.date}
                                    onChange={e => setFormData({ ...formData, date: e.target.value })}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Odometer Reading (km)</label>
                                <input
                                    type="number"
                                    step="0.1"
                                    min="0"
                                    className="form-input"
                                    placeholder="e.g. 154320"
                                    value={formData.odometerReading}
                                    onChange={e => setFormData({ ...formData, odometerReading: e.target.value })}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Litres *</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0.1"
                                    required
                                    className="form-input"
                                    placeholder="e.g. 45.5"
                                    value={formData.litres}
                                    onChange={e => setFormData({ ...formData, litres: e.target.value })}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Price per Litre (₹)</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    className="form-input"
                                    placeholder="e.g. 96.50"
                                    value={formData.pricePerLitre}
                                    onChange={e => {
                                        setFormData({ ...formData, pricePerLitre: e.target.value });
                                        setAutoCalculating(true);
                                    }}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span>Total Amount (₹) *</span>
                                    {autoCalculating && formData.litres && formData.pricePerLitre && (
                                        <span style={{ fontSize: 11, color: 'var(--blue-500)' }}>Auto</span>
                                    )}
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0.1"
                                    required
                                    className="form-input"
                                    style={autoCalculating && formData.totalAmount ? { backgroundColor: 'rgba(59, 130, 246, 0.1)' } : {}}
                                    placeholder="Final amount"
                                    value={formData.totalAmount}
                                    onChange={e => {
                                        setFormData({ ...formData, totalAmount: e.target.value });
                                        setAutoCalculating(false);
                                    }}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Fuel Station / Location</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    placeholder="e.g. IndianOil Pipeline Road"
                                    value={formData.fuelStation}
                                    onChange={e => setFormData({ ...formData, fuelStation: e.target.value })}
                                />
                            </div>

                            <div className="form-group full-width">
                                <label className="form-label">Remarks</label>
                                <textarea
                                    className="form-input"
                                    rows="2"
                                    placeholder="Any additional details..."
                                    value={formData.remarks}
                                    onChange={e => setFormData({ ...formData, remarks: e.target.value })}
                                ></textarea>
                            </div>
                        </div>

                        <div className="form-actions" style={{ marginTop: 24 }}>
                            <button type="button" onClick={onClose} className="btn btn-secondary">
                                Cancel
                            </button>
                            <button type="submit" disabled={submitting} className="btn btn-primary">
                                {submitting ? 'Saving...' : 'Save Record'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
