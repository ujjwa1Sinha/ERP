import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';
import { FiX } from 'react-icons/fi';

export default function MaintenanceModal({ isOpen, onClose, onSuccess, initialData }) {
    const [loading, setLoading] = useState(false);
    const [vehicles, setVehicles] = useState([]);
    const [formData, setFormData] = useState({
        vehicleId: '',
        maintenanceType: 'SERVICE',
        serviceDate: new Date().toISOString().slice(0, 16),
        odometerReading: '',
        vendor: '',
        cost: '',
        description: '',
        nextServiceDate: '',
        nextServiceOdometer: ''
    });

    useEffect(() => {
        if (isOpen) {
            fetchVehicles();
            if (initialData) {
                setFormData({
                    vehicleId: initialData.vehicleId,
                    maintenanceType: initialData.maintenanceType,
                    serviceDate: initialData.serviceDate ? new Date(initialData.serviceDate).toISOString().slice(0, 16) : new Date().toISOString().slice(0, 16),
                    odometerReading: initialData.odometerReading || '',
                    vendor: initialData.vendor || '',
                    cost: initialData.cost || '',
                    description: initialData.description || '',
                    nextServiceDate: initialData.nextServiceDate ? new Date(initialData.nextServiceDate).toISOString().slice(0, 16) : '',
                    nextServiceOdometer: initialData.nextServiceOdometer || ''
                });
            } else {
                setFormData({
                    vehicleId: '',
                    maintenanceType: 'SERVICE',
                    serviceDate: new Date().toISOString().slice(0, 16),
                    odometerReading: '',
                    vendor: '',
                    cost: '',
                    description: '',
                    nextServiceDate: '',
                    nextServiceOdometer: ''
                });
            }
        }
    }, [isOpen, initialData]);

    const fetchVehicles = async () => {
        try {
            const res = await api.get('/vehicles?size=100');
            setVehicles(res.data.data?.content || res.data?.content || []);
        } catch (error) {
            console.error(error);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onClose(); // Optimistically close modal immediately

        const payload = {
            ...formData,
            serviceDate: new Date(formData.serviceDate).toISOString(),
            nextServiceDate: formData.nextServiceDate ? new Date(formData.nextServiceDate).toISOString() : null,
            odometerReading: formData.odometerReading ? parseFloat(formData.odometerReading) : 0,
            cost: formData.cost ? parseFloat(formData.cost) : 0,
            nextServiceOdometer: formData.nextServiceOdometer ? parseFloat(formData.nextServiceOdometer) : null
        };

        const request = initialData
            ? api.put(`/maintenance/${initialData.id}`, payload)
            : api.post('/maintenance', payload);

        toast.promise(request, {
            loading: 'Saving maintenance record...',
            success: 'Maintenance record saved!',
            error: 'Failed to save maintenance record'
        }).then(() => {
            if (onSuccess) onSuccess();
        }).catch(error => {
            console.error(error);
        });
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 640 }}>
                <div className="modal-header">
                    <h3>{initialData ? 'Edit Record' : 'Record Maintenance'}</h3>
                    <button className="modal-close" onClick={onClose}><FiX size={20} /></button>
                </div>

                <div className="modal-body">
                    <form onSubmit={handleSubmit}>
                        <div className="form-grid">
                            <div className="form-group full-width">
                                <label className="form-label">Vehicle *</label>
                                <select
                                    name="vehicleId"
                                    value={formData.vehicleId}
                                    onChange={handleChange}
                                    required
                                    className="form-select"
                                >
                                    <option value="">Select a vehicle...</option>
                                    {vehicles.map(v => (
                                        <option key={v.id} value={v.id}>{v.registrationNumber}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Maintenance Type *</label>
                                <select
                                    name="maintenanceType"
                                    value={formData.maintenanceType}
                                    onChange={handleChange}
                                    required
                                    className="form-select"
                                >
                                    <option value="SERVICE">Service</option>
                                    <option value="REPAIR">Repair</option>
                                    <option value="TYRES">Tyres</option>
                                    <option value="BATTERY">Battery</option>
                                    <option value="ACCIDENT">Accident</option>
                                    <option value="INSPECTION">Inspection</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Date & Time *</label>
                                <input
                                    type="datetime-local"
                                    name="serviceDate"
                                    required
                                    className="form-input"
                                    value={formData.serviceDate}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Cost (₹) *</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    required
                                    className="form-input"
                                    name="cost"
                                    placeholder="e.g. 1500.50"
                                    value={formData.cost}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Current Odometer</label>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.1"
                                    className="form-input"
                                    name="odometerReading"
                                    placeholder="e.g. 154320"
                                    value={formData.odometerReading}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group full-width">
                                <label className="form-label">Vendor / Garage</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    name="vendor"
                                    placeholder="e.g. Metro Motors"
                                    value={formData.vendor}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group full-width">
                                <label className="form-label">Description / Remarks</label>
                                <textarea
                                    className="form-input"
                                    name="description"
                                    rows="2"
                                    placeholder="Any additional details..."
                                    value={formData.description}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group full-width" style={{ marginTop: 12, paddingTop: 16, borderTop: '1px solid var(--border-color)' }}>
                                <h4 style={{ marginBottom: 16, fontSize: '14px', color: 'var(--gray-600)' }}>Upcoming Service (Optional)</h4>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Next Service Date</label>
                                <input
                                    type="datetime-local"
                                    name="nextServiceDate"
                                    className="form-input"
                                    value={formData.nextServiceDate}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Next Service Odometer</label>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.1"
                                    className="form-input"
                                    name="nextServiceOdometer"
                                    placeholder="e.g. 160000"
                                    value={formData.nextServiceOdometer}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="form-actions" style={{ marginTop: 24 }}>
                            <button type="button" onClick={onClose} className="btn btn-secondary">
                                Cancel
                            </button>
                            <button type="submit" disabled={loading} className="btn btn-primary">
                                {loading ? 'Saving...' : 'Save Record'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
