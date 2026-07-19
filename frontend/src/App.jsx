import { useState, useEffect } from 'react';
import './index.css';
import StadiumBackground from './components/StadiumBackground';
import LoginCard from './components/LoginCard';
import Dashboard from './components/Dashboard';

export default function App() {
  const [page, setPage] = useState('loading');

  useEffect(() => {
    // Simple client-side routing
    const route = () => {
      const path = window.location.pathname;
      const params = new URLSearchParams(window.location.search);

      // Handle OAuth callback — extract token from URL
      if (path === '/auth/callback') {
        const token = params.get('token');
        if (token) {
          localStorage.setItem('accessToken', token);
          window.history.replaceState(null, '', '/dashboard');
          setPage('dashboard');
          return;
        }
      }

      const hasToken = !!localStorage.getItem('accessToken');

      if (path === '/dashboard' && hasToken) {
        setPage('dashboard');
      } else if (path === '/dashboard' && !hasToken) {
        window.history.replaceState(null, '', '/');
        setPage('login');
      } else {
        if (hasToken) {
          window.history.replaceState(null, '', '/dashboard');
          setPage('dashboard');
        } else {
          setPage('login');
        }
      }
    };

    route();
    window.addEventListener('popstate', route);
    return () => window.removeEventListener('popstate', route);
  }, []);

  if (page === 'loading') return null;

  if (page === 'dashboard') {
    return <Dashboard />;
  }

  return (
    <div className="login-page">
      <div className="app-container">
        <div className="stadium-side">
          <StadiumBackground />
        </div>
        <div className="login-side">
          <LoginCard />
        </div>
      </div>
    </div>
  );
}
