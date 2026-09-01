import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
    // In production, use VITE_API_URL. In development, fallback to '/api' which Vite proxies.
    baseURL: import.meta.env.VITE_API_URL || '/api',
    headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle 401 globally
api.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err.response?.status === 401) {
            localStorage.clear();
            window.location.href = '/login';
        }
        const message = err.response?.data?.message || err.message || 'Something went wrong';
        toast.error(message);
        return Promise.reject(err);
    }
);

export default api;
