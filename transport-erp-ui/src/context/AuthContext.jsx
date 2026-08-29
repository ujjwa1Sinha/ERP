import { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const stored = localStorage.getItem('user');
        if (stored) {
            setUser(JSON.parse(stored));
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        const res = await api.post('/auth/login', { username, password });
        const data = res.data.data;
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        const userData = {
            id: data.userId,
            username: data.username,
            fullName: data.fullName,
            email: data.email,
            roles: data.roles,
            permissions: data.permissions || [],
        };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return userData;
    };

    const logout = () => {
        localStorage.clear();
        setUser(null);
    };

    const hasPermission = (permission) => {
        if (!user?.permissions) return false;
        // SUPER_ADMIN and OWNER have all permissions via backend,
        // but we still check the permissions set for proper flexibility
        return Array.isArray(user.permissions)
            ? user.permissions.includes(permission)
            : user.permissions.has?.(permission) || Object.values(user.permissions).includes(permission);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading, isAuthenticated: !!user, hasPermission }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
