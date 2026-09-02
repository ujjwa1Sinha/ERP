import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import useInactivityLogout from '../hooks/useInactivityLogout';
import {
    HiOutlineViewGrid, HiOutlineOfficeBuilding, HiOutlineTruck,
    HiOutlineUserGroup, HiOutlineLink,
    HiOutlineUsers, HiOutlineLogout, HiMenu, HiX
} from 'react-icons/hi';

// Each nav item can have a `requiredPermission` — if absent, all users see it
const navItems = [
    { section: 'Overview' },
    { path: '/', label: 'Dashboard', icon: HiOutlineViewGrid },
    { section: 'Fleet Management' },
    { path: '/branches', label: 'Branches', icon: HiOutlineOfficeBuilding, requiredPermission: 'BRANCH_VIEW' },
    { path: '/vehicles', label: 'Vehicles', icon: HiOutlineTruck, requiredPermission: 'VEHICLE_VIEW' },
    { path: '/drivers', label: 'Drivers', icon: HiOutlineUserGroup, requiredPermission: 'DRIVER_VIEW' },
    { section: 'Operations' },
    { path: '/assignments', label: 'Assignments', icon: HiOutlineLink, requiredPermission: 'ASSIGNMENT_VIEW' },
    { section: 'Administration' },
    { path: '/users', label: 'Users', icon: HiOutlineUsers, requiredPermission: 'USER_VIEW' },
];

const pageTitles = {
    '/': 'Dashboard',
    '/branches': 'Branches',
    '/vehicles': 'Vehicles',
    '/drivers': 'Drivers',
    '/assignments': 'Assignments',
    '/users': 'User Management',
};

export default function Layout() {
    const { user, logout, hasPermission } = useAuth();
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [showWarning, setShowWarning] = useState(false);
    const [countdown, setCountdown] = useState(60);
    const pageTitle = pageTitles[location.pathname] || 'Central Transport';
    const initials = user?.fullName?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U';

    const handleWarn = (secondsLeft) => {
        setShowWarning(true);
        setCountdown(secondsLeft ?? 60);
    };

    const handleResume = () => {
        setShowWarning(false);
    };

    const handleLogout = () => {
        setShowWarning(false);
        logout();
    };

    useInactivityLogout({
        onLogout: handleLogout,
        onWarn: handleWarn,
        onResume: handleResume,
        enabled: !!user,
    });

    // Filter nav items based on user permissions
    const filteredNavItems = navItems.filter(item => {
        if (item.section) {
            // Show section header only if the NEXT non-section items have at least one visible
            return true; // We filter empty sections below
        }
        if (!item.requiredPermission) return true;
        return hasPermission(item.requiredPermission);
    });

    // Remove section headers that have no visible items after them
    const cleanedNavItems = filteredNavItems.filter((item, index, arr) => {
        if (!item.section) return true;
        // Check if there's at least one non-section item before the next section
        for (let i = index + 1; i < arr.length; i++) {
            if (arr[i].section) break;
            if (arr[i].path) return true;
        }
        return false;
    });

    const handleNavClick = () => {
        // Close sidebar on mobile when navigating
        setSidebarOpen(false);
    };

    return (
        <div className="app-layout">
            {/* Mobile Overlay */}
            {sidebarOpen && (
                <div
                    className="sidebar-overlay"
                    onClick={() => setSidebarOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
                <div className="sidebar-header">
                    <div className="sidebar-logo">
                        <div className="sidebar-logo-icon">🚛</div>
                        <div className="sidebar-logo-text">
                            <h1>Central Transport</h1>
                            <span>Fleet Management</span>
                        </div>
                    </div>
                    {/* Close button for mobile */}
                    <button
                        className="sidebar-close-btn"
                        onClick={() => setSidebarOpen(false)}
                        aria-label="Close menu"
                    >
                        <HiX size={20} />
                    </button>
                </div>

                <nav className="sidebar-nav">
                    {cleanedNavItems.map((item, i) =>
                        item.section ? (
                            <div key={i} className="sidebar-section-title">{item.section}</div>
                        ) : (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                end={item.path === '/'}
                                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                                onClick={handleNavClick}
                            >
                                <item.icon className="sidebar-icon" />
                                {item.label}
                            </NavLink>
                        )
                    )}
                </nav>

                <div className="sidebar-footer">
                    <div className="sidebar-user">
                        <div className="sidebar-avatar">{initials}</div>
                        <div className="sidebar-user-info">
                            <div className="sidebar-user-name">{user?.fullName || 'User'}</div>
                            <div className="sidebar-user-role">{user?.roles?.[0] || 'Admin'}</div>
                        </div>
                    </div>
                </div>
            </aside>

            {/* Main Content */}
            <div className="main-content">
                <header className="topbar">
                    <div className="topbar-left">
                        <button
                            className="hamburger-btn"
                            onClick={() => setSidebarOpen(true)}
                            aria-label="Open menu"
                        >
                            <HiMenu size={22} />
                        </button>
                        <h2>{pageTitle}</h2>
                    </div>
                    <div className="topbar-right">
                        <button className="btn btn-ghost" onClick={logout} title="Logout">
                            <HiOutlineLogout size={18} />
                            Logout
                        </button>
                    </div>
                </header>

                <div className="page-content fade-in">
                    <Outlet />
                </div>
            </div>

            {/* Inactivity Warning Banner */}
            {showWarning && (
                <div style={{
                    position: 'fixed', bottom: 24, left: '50%', transform: 'translateX(-50%)',
                    background: '#78350f', color: '#fef3c7', borderRadius: 12,
                    padding: '14px 24px', display: 'flex', alignItems: 'center', gap: 16,
                    boxShadow: '0 8px 32px rgba(0,0,0,0.35)', zIndex: 9999,
                    border: '1px solid #f59e0b', minWidth: 340, maxWidth: '90vw',
                    animation: 'slideUp 0.3s ease'
                }}>
                    <span style={{ fontSize: 22 }}>⏱️</span>
                    <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 700, fontSize: 14 }}>Session expiring soon</div>
                        <div style={{ fontSize: 13, opacity: 0.85 }}>
                            You'll be logged out in <strong>{Math.floor(countdown / 60)}:{String(countdown % 60).padStart(2, '0')}</strong> due to inactivity.
                        </div>
                    </div>
                    <button
                        onClick={handleResume}
                        style={{
                            background: '#f59e0b', color: '#1c1917', border: 'none',
                            borderRadius: 8, padding: '7px 16px', fontWeight: 700,
                            fontSize: 13, cursor: 'pointer', whiteSpace: 'nowrap'
                        }}
                    >
                        Stay logged in
                    </button>
                </div>
            )}
        </div>
    );
}
