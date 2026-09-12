import { useState, useEffect, useRef } from 'react';
import axios from 'axios';

export default function AddressAutocomplete({ label, name, value, onChange, placeholder, required }) {
    const [query, setQuery] = useState(value || '');
    const [suggestions, setSuggestions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [loading, setLoading] = useState(false);
    const wrapperRef = useRef(null);

    const isSelecting = useRef(false);

    useEffect(() => {
        if (value !== query) {
            setQuery(value || '');
        }
    }, [value]);

    useEffect(() => {
        function handleClickOutside(event) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    useEffect(() => {
        if (!query || query.length < 3) {
            setSuggestions([]);
            return;
        }

        if (isSelecting.current) {
            // Bypass fetch immediately following active list selection
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            setLoading(true);
            try {
                const res = await axios.get(`https://photon.komoot.io/api/?q=${encodeURIComponent(query)}&limit=5`);
                const formatted = res.data.features.map(f => {
                    const p = f.properties;
                    let display = `${p.name || ''}`;
                    if (p.city && p.city !== p.name) display += `, ${p.city}`;
                    if (p.state && p.state !== p.city) display += `, ${p.state}`;
                    if (p.country && p.country !== p.state) display += `, ${p.country}`;

                    return {
                        display_name: display.replace(/^,\s*/, '').trim(),
                        city: p.city || p.name || 'Unknown',
                        lat: f.geometry.coordinates[1],
                        lon: f.geometry.coordinates[0]
                    };
                }).filter(s => s.display_name.length > 0);

                setSuggestions(formatted);
                setShowDropdown(true);
            } catch (err) {
                console.error("Geocoding API failed", err);
            } finally {
                setLoading(false);
            }
        }, 600);

        return () => clearTimeout(delayDebounceFn);
    }, [query]);

    const handleSelect = (text) => {
        isSelecting.current = true;
        setQuery(text);
        setShowDropdown(false);
        if (onChange) onChange({ target: { name, value: text } });
    };

    const handleChange = (e) => {
        isSelecting.current = false;
        setQuery(e.target.value);
        if (onChange) onChange({ target: { name, value: e.target.value } });
    };

    return (
        <div className="form-group" ref={wrapperRef} style={{ position: 'relative' }}>
            <label className="form-label">{label}{required && ' *'}</label>
            <input
                className="form-input"
                name={name}
                value={query}
                onChange={handleChange}
                required={required}
                placeholder={placeholder}
                autoComplete="off"
                onFocus={() => { if (suggestions.length > 0) setShowDropdown(true); }}
            />
            {loading && <div style={{ position: 'absolute', right: 10, top: 35, fontSize: 11, color: 'var(--text-secondary)' }}>Searching API...</div>}

            {showDropdown && suggestions.length > 0 && (
                <ul style={{
                    position: 'absolute', zIndex: 10, top: '95%', left: 0, right: 0,
                    background: 'var(--glass-bg)', backdropFilter: 'blur(10px)',
                    border: '1px solid var(--border-color)', borderRadius: 6,
                    maxHeight: 200, overflowY: 'auto', listStyle: 'none', padding: 0, margin: '4px 0 0 0',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.3)'
                }}>
                    {suggestions.map((s, idx) => (
                        <li key={idx}
                            style={{
                                padding: '10px 12px', fontSize: 13, cursor: 'pointer',
                                borderBottom: idx === suggestions.length - 1 ? 'none' : '1px solid var(--border-color)'
                            }}
                            onClick={() => handleSelect(s.display_name)}
                            onMouseEnter={e => e.currentTarget.style.background = 'var(--row-hover)'}
                            onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                        >
                            {s.display_name}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
