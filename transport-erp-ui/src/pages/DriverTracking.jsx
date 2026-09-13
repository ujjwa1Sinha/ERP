import { useState, useEffect } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { HiOutlineLocationMarker, HiOutlineStop, HiOutlinePlay } from 'react-icons/hi';

export default function DriverTracking() {
    const [trackingId, setTrackingId] = useState(null);
    const [lastLocation, setLastLocation] = useState(null);
    const [error, setError] = useState(null);
    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState('');

    useEffect(() => {
        api.get('/vehicles?page=0&size=50')
            .then(res => setVehicles(res.data.data.content))
            .catch(err => console.error("Failed fetching vehicles", err));
    }, []);

    const startTracking = () => {
        if (!selectedVehicle) {
            toast.error("Please select a vehicle before tracking.");
            return;
        }

        if (!navigator.geolocation) {
            toast.error("Geolocation is not supported by your browser");
            return;
        }

        toast.success("Initializing GPS Tracking...");
        setError(null);

        const id = navigator.geolocation.watchPosition(
            (position) => {
                const locData = {
                    vehicleId: selectedVehicle,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude,
                    speed: position.coords.speed || 0,
                    heading: position.coords.heading || 0,
                    accuracy: position.coords.accuracy,
                    recordedAt: new Date(position.timestamp).toISOString()
                };

                setLastLocation(locData);

                api.post('/tracking/location', locData)
                    .catch(e => console.error("GPS Ping Failed", e));
            },
            (err) => {
                setError(err.message);
                toast.error("GPS Error: " + err.message);
            },
            {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
            }
        );
        setTrackingId(id);
    };

    const stopTracking = () => {
        if (trackingId !== null) {
            navigator.geolocation.clearWatch(trackingId);
            setTrackingId(null);
            toast.success("GPS Tracking Stopped.");
        }
    };

    return (
        <div style={{ maxWidth: 600, margin: '0 auto', paddingTop: 40 }}>
            <div className="card">
                <div className="card-header">
                    <h3><HiOutlineLocationMarker style={{ verticalAlign: 'middle', marginRight: 8 }} /> Driver GPS Portal</h3>
                </div>
                <div className="card-body">
                    <p style={{ marginBottom: 20 }}>
                        Select your assigned vehicle and broadcast your live location back to HQ using your mobile device.
                    </p>

                    <div className="form-group" style={{ marginBottom: 20 }}>
                        <label className="form-label">Active Vehicle</label>
                        <select
                            className="form-input"
                            value={selectedVehicle}
                            onChange={e => setSelectedVehicle(e.target.value)}
                            disabled={trackingId !== null}
                        >
                            <option value="">-- Select Vehicle --</option>
                            {vehicles.map(v => (
                                <option key={v.id} value={v.id}>{v.registrationNumber} - {v.make}</option>
                            ))}
                        </select>
                    </div>

                    {error && (
                        <div style={{ padding: 12, background: 'var(--red-100)', color: 'var(--red-500)', borderRadius: 8, marginBottom: 20 }}>
                            <strong>Error:</strong> {error}
                        </div>
                    )}

                    {lastLocation && (
                        <div className="stat-card" style={{ marginBottom: 20, padding: 16 }}>
                            <div className="stat-info">
                                <h4>Latest Ping</h4>
                                <div style={{ fontSize: 13, marginTop: 8 }}>
                                    <div><strong>Lat:</strong> {lastLocation.latitude.toFixed(6)}</div>
                                    <div><strong>Lng:</strong> {lastLocation.longitude.toFixed(6)}</div>
                                    <div><strong>Speed:</strong> {lastLocation.speed} m/s</div>
                                    <div><strong>Accuracy:</strong> &plusmn;{lastLocation.accuracy.toFixed(1)} meters</div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: 12 }}>
                        {!trackingId ? (
                            <button className="btn btn-primary" onClick={startTracking} style={{ flex: 1, padding: 14 }}>
                                <HiOutlinePlay size={20} /> Start Transmitting Location
                            </button>
                        ) : (
                            <button className="btn btn-danger" onClick={stopTracking} style={{ flex: 1, padding: 14 }}>
                                <HiOutlineStop size={20} /> Stop Transmitting Location
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
