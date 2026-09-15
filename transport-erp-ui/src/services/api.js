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

// ── In-Memory Caching (Stale until mutation) ─────────────────────
const apiCache = new Map();
const clearCache = () => apiCache.clear();

const originalGet = api.get;
api.get = async (url, config = {}) => {
    if (config.noCache) return originalGet.apply(api, [url, config]);

    // Quick cache hit
    if (apiCache.has(url)) {
        return Promise.resolve(apiCache.get(url));
    }

    const response = await originalGet.apply(api, [url, config]);
    // Cache the successful response
    if (response?.data) apiCache.set(url, response);
    return response;
};

// Global cache invalidation on any mutations
const originalPost = api.post;
api.post = async (...args) => { clearCache(); return originalPost.apply(api, args); };

const originalPut = api.put;
api.put = async (...args) => { clearCache(); return originalPut.apply(api, args); };

const originalDelete = api.delete;
api.delete = async (...args) => { clearCache(); return originalDelete.apply(api, args); };

export default api;
