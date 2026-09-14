import { useState, useEffect } from 'react';
import { State, City } from 'country-state-city';

const COUNTRY_CODE = 'IN';

export default function LocationSelector({ form, onChange, required = false }) {
    const [states, setStates] = useState([]);
    const [cities, setCities] = useState([]);
    const [selectedStateCode, setSelectedStateCode] = useState('');

    useEffect(() => {
        const inStates = State.getStatesOfCountry(COUNTRY_CODE);
        setStates(inStates);

        // initialize stateCode if parent has a state name already
        if (form.state) {
            const temp = inStates.find(s => s.name === form.state);
            if (temp) {
                setSelectedStateCode(temp.isoCode);
                setCities(City.getCitiesOfState(COUNTRY_CODE, temp.isoCode));
            }
        }
    }, [form.state]);

    const handleStateChange = (e) => {
        const code = e.target.value;
        setSelectedStateCode(code);

        const stateObj = states.find(s => s.isoCode === code);
        const name = stateObj ? stateObj.name : '';

        onChange({ target: { name: 'state', value: name } });
        onChange({ target: { name: 'city', value: '' } }); // Reset city
        setCities(City.getCitiesOfState(COUNTRY_CODE, code));
    };

    return (
        <>
            <div className="form-group">
                <label className="form-label">State {required && '*'}</label>
                <select className="form-select" name="stateCode" value={selectedStateCode} onChange={handleStateChange} required={required}>
                    <option value="">Select State</option>
                    {states.map(s => <option key={s.isoCode} value={s.isoCode}>{s.name}</option>)}
                </select>
            </div>
            <div className="form-group">
                <label className="form-label">City {required && '*'}</label>
                <select className="form-select" name="city" value={form.city || ''} onChange={(e) => onChange(e)} required={required} disabled={!selectedStateCode}>
                    <option value="">Select City</option>
                    {cities.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
                </select>
            </div>
        </>
    );
}
