import { useState } from 'react';

export default function Navbar({ user, activePage, onNavigate }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    window.location.href = '/';
  };

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: '🏠' },
    { id: 'matches', label: 'Matches', icon: '⚽' },
    { id: 'map', label: 'Stadium Map', icon: '🗺️' },
  ];

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <span className="nav-logo">🏟️</span>
        <div>
          <span className="nav-title">Smart Stadium</span>
          <span className="nav-subtitle">FIFA World Cup 2026</span>
        </div>
      </div>

      <div className={`nav-links${menuOpen ? ' open' : ''}`}>
        {navItems.map(item => (
          <button
            key={item.id}
            className={`nav-link${activePage === item.id ? ' active' : ''}`}
            onClick={() => { onNavigate(item.id); setMenuOpen(false); }}
          >
            <span className="nav-icon">{item.icon}</span>
            {item.label}
          </button>
        ))}
      </div>

      <div className="nav-user">
        <div className="nav-avatar" onClick={() => setMenuOpen(!menuOpen)}>
          {(user?.email || 'U').charAt(0).toUpperCase()}
        </div>
        <button className="nav-logout" onClick={handleLogout} title="Sign out">
          ↪ 
        </button>
      </div>

      <button className="nav-hamburger" onClick={() => setMenuOpen(!menuOpen)}>
        ☰
      </button>
    </nav>
  );
}
