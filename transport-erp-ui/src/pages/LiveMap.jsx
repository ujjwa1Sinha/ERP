import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import api from '../services/api';
import L from 'leaflet';

// Fix leaflet default icon issue in React wrapper
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
});

export default function LiveMap() {
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchLocations = () => {
            api.get('/tracking/active')
                .then(res => {
                    setLocations(res.data.data);
                    setLoading(false);
                })
                .catch(err => {
                    console.error("Failed fetching live locations", err);
                    setLoading(false);
                });
        };
        fetchLocations();
        const interval = setInterval(fetchLocations, 10000); // 10 seconds poll interval
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return (
            <div className="page-loader">
                <div className="spinner"></div>
                <p>Loading Map Assets...</p>
            </div>
        );
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            <div style={{ marginBottom: 16 }}>
                <h3 style={{ color: 'var(--gray-900)' }}>Live Dispatch Map</h3>
                <p style={{ color: 'var(--gray-400)', fontSize: 13 }}>Tracking {locations.length} active vehicles via Mobile Sensors</p>
            </div>

            <div className="card" style={{ flex: 1, width: '100%', minHeight: 600, padding: 0 }}>
                {/* zIndex 1 to ensure it stays strictly underneath the sidebars/topbars */}
                <MapContainer center={[20.5937, 78.9629]} zoom={5} style={{ height: "100%", width: "100%", zIndex: 1 }}>
                    <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution='&copy; OpenStreetMap contributors'
                    />
                    {locations?.map(loc => {
                        // Avoid plotting null locations (could happen due to bugs)
                        if (!loc.latitude || !loc.longitude) return null;

                        return (
                            <Marker key={loc.vehicleId} position={[loc.latitude, loc.longitude]}>
                                <Popup>
                                    <strong style={{ fontSize: 14 }}>{loc.vehicleRegistrationNumber}</strong><br />
                                    Speed: {loc.speed ? loc.speed + ' m/s' : '0'}<br />
                                    Sync time: {new Date(loc.recordedAt).toLocaleTimeString()}
                                </Popup>
                            </Marker>
                        );
                    })}
                </MapContainer>
            </div>
        </div>
    );
}
