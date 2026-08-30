import { useEffect, useRef } from 'react';

const TIMEOUT_MS = 15 * 60 * 1000;  // total idle time before logout
const WARNING_MS = 1 * 60 * 1000;  // warn this many ms before logout
const CHECK_EVERY_MS = 5 * 1000;       // how often to check (every 5s)
const STORAGE_KEY = 'erp_last_activity';

const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'click'];

/**
 * Inactivity logout via localStorage timestamp + polling.
 * Writing/reading a timestamp is immune to React re-render cycles.
 */
export default function useInactivityLogout({ onLogout, onWarn, onResume, enabled }) {
    const warnedRef = useRef(false);
    const intervalRef = useRef(null);
    const onLogoutRef = useRef(onLogout);
    const onWarnRef = useRef(onWarn);
    const onResumeRef = useRef(onResume);

    // Keep callback refs fresh without re-running the main effect
    useEffect(() => { onLogoutRef.current = onLogout; });
    useEffect(() => { onWarnRef.current = onWarn; });
    useEffect(() => { onResumeRef.current = onResume; });

    useEffect(() => {
        if (!enabled) return;

        // ── Record activity timestamp ────────────────────────────────────────
        const stamp = () => localStorage.setItem(STORAGE_KEY, Date.now().toString());
        stamp(); // initialise on mount

        ACTIVITY_EVENTS.forEach(evt => window.addEventListener(evt, stamp, { passive: true }));

        // ── Polling check ────────────────────────────────────────────────────
        intervalRef.current = setInterval(() => {
            const last = parseInt(localStorage.getItem(STORAGE_KEY) || '0', 10);
            const idle = Date.now() - last;
            const timeLeft = TIMEOUT_MS - idle;

            if (idle >= TIMEOUT_MS) {
                // Time's up — logout
                warnedRef.current = false;
                localStorage.removeItem(STORAGE_KEY);
                onLogoutRef.current?.();

            } else if (timeLeft <= WARNING_MS && !warnedRef.current) {
                // Entering warning zone
                warnedRef.current = true;
                onWarnRef.current?.(Math.round(timeLeft / 1000)); // pass seconds left

            } else if (timeLeft > WARNING_MS && warnedRef.current) {
                // User was active — cancel warning
                warnedRef.current = false;
                onResumeRef.current?.();
            }
        }, CHECK_EVERY_MS);

        return () => {
            clearInterval(intervalRef.current);
            ACTIVITY_EVENTS.forEach(evt => window.removeEventListener(evt, stamp));
        };
    }, [enabled]); // eslint-disable-line react-hooks/exhaustive-deps
}
