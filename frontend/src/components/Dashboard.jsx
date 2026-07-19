import { useState, useEffect } from 'react';
import { getMatches, getCrowdData } from '../api';
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import AiAssistant from './AiAssistant';
import MatchSchedule from './MatchSchedule';
import StadiumMap from './StadiumMap';
import TransportHub from './TransportHub';
import AccessibilityCenter from './AccessibilityCenter';
import SustainabilityDashboard from './SustainabilityDashboard';
import SecurityOps from './SecurityOps';
import AnalyticsInsights from './AnalyticsInsights';
import VolunteerHub from './VolunteerHub';
import AdminPanel from './AdminPanel';

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [page, setPage] = useState('dashboard');
  const [matches, setMatches] = useState([]);
  const [crowd, setCrowd] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (!token) { window.location.href = '/'; return; }

    // Decode the JWT to get user info
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setUser({
        email: payload.sub,
        authProvider: payload.provider || 'EMAIL',
        name: payload.name || '',
        role: payload.role || 'USER',
      });
    } catch (e) {
      console.error('Invalid token:', e);
      localStorage.removeItem('accessToken');
      window.location.href = '/';
      return;
    }

    getMatches().then(m => setMatches(m));
    getCrowdData().then(setCrowd);
    const interval = setInterval(() => getCrowdData().then(setCrowd), 30000);
    return () => clearInterval(interval);
  }, []);

  if (!user) return null;

  const renderPage = () => {
    switch (page) {
      case 'matches': return <MatchSchedule matches={matches} />;
      case 'map': return <StadiumMap crowd={crowd} />;
      case 'transport': return <TransportHub />;
      case 'accessibility': return <AccessibilityCenter />;
      case 'sustainability': return <SustainabilityDashboard />;
      case 'security': return <SecurityOps />;
      case 'analytics': return <AnalyticsInsights />;
      case 'volunteer': return <VolunteerHub />;
      case 'admin': return <AdminPanel />;
      default: return <ControlCenter matches={matches} crowd={crowd} onNavigate={setPage} />;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Sidebar activePage={page} onNavigate={setPage} userRole={user.role} />
      <TopBar />
      <main className="ml-64 pt-16 min-h-screen p-grid-margin">
        {renderPage()}
      </main>
      <AiAssistant />
    </div>
  );
}

/* ========= Control Center (Dashboard Home) ========= */
function ControlCenter({ matches, crowd, onNavigate }) {
  const totalOcc = crowd.reduce((s, z) => s + z.occupancy, 0);
  const totalCap = crowd.reduce((s, z) => s + z.capacity, 0);
  const overallPct = totalCap > 0 ? Math.round((totalOcc / totalCap) * 100) : 0;
  const highZones = crowd.filter(z => z.status === 'high').length;
  const metlifeMatches = matches.filter(m => m.venue?.includes('MetLife'));

  const stats = [
    { label: 'BOWL CAPACITY', value: `${overallPct}%`, icon: 'groups', color: overallPct > 80 ? 'text-error' : 'text-primary-fixed-dim' },
    { label: 'CRITICAL ZONES', value: highZones, icon: 'warning', color: highZones > 2 ? 'text-error' : 'text-tertiary-fixed-dim' },
    { label: 'ACTIVE MATCHES', value: metlifeMatches.length, icon: 'sports_soccer', color: 'text-secondary-fixed-dim' },
    { label: 'AI ASSISTANT', value: 'ONLINE', icon: 'smart_toy', color: 'text-secondary-fixed-dim' },
  ];

  return (
    <>
      {/* Header */}
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Control Center</h1>
          <p className="font-body-md text-on-surface-variant">
            Real-time overview of MetLife Stadium operations for FIFA World Cup 2026.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Stadium Capacity</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">{overallPct}%</div>
          </div>
          <div className="h-12 w-px bg-outline-variant" />
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Active Staff</div>
            <div className="font-data-value text-3xl text-secondary-fixed tracking-tighter glow-sm">1,240</div>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-4 mb-lg" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        {stats.map((s, i) => (
          <div key={i} className="glass-panel rounded-xl p-md flex items-center gap-md">
            <span className={`material-symbols-outlined text-3xl ${s.color}`}>{s.icon}</span>
            <div>
              <div className="font-data-label text-data-label text-on-surface-variant uppercase">{s.label}</div>
              <div className={`font-data-value text-data-value ${s.color}`}>{s.value}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Crowd Density Overview */}
        <div className="flex-[2] min-w-0">
          <div className="glass-panel rounded-xl p-md mb-grid-gutter">
            <div className="flex items-center justify-between mb-md">
              <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs">
                <span className="material-symbols-outlined text-[16px]">sensor_door</span>
                ZONE DENSITY OVERVIEW
              </h3>
              <button onClick={() => onNavigate('map')} className="font-data-label text-data-label text-primary-fixed hover:underline">
                OPEN FULL MAP →
              </button>
            </div>
            <div className="space-y-sm">
              {crowd.slice(0, 6).map(zone => (
                <div key={zone.zoneId} className="space-y-xs">
                  <div className="flex justify-between font-data-label text-xs">
                    <span className="text-on-surface uppercase">{zone.zoneName}</span>
                    <span className={zone.status === 'high' ? 'text-error' : zone.status === 'moderate' ? 'text-tertiary-fixed-dim' : 'text-secondary'}>
                      {zone.status.toUpperCase()}
                    </span>
                  </div>
                  <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        zone.status === 'high' ? 'bg-error' : zone.status === 'moderate' ? 'bg-tertiary-fixed-dim' : 'bg-secondary-fixed'
                      }`}
                      style={{ width: `${zone.percentage * 100}%` }}
                    />
                  </div>
                  <div className="flex justify-between font-data-value text-body-sm">
                    <span className={zone.status === 'high' ? 'text-error' : 'text-on-surface-variant'}>
                      {Math.round(zone.percentage * 100)}% full
                    </span>
                    <span className="text-on-surface-variant">{zone.occupancy.toLocaleString()} / {zone.capacity.toLocaleString()}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Sidebar - Matches + Quick Actions */}
        <div className="flex-1 min-w-[280px] space-y-4">
          {/* Upcoming Matches */}
          <div className="glass-panel rounded-xl p-md">
            <div className="flex items-center justify-between mb-md">
              <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs">
                <span className="material-symbols-outlined text-[16px]">sports_soccer</span>
                UPCOMING AT METLIFE
              </h3>
              <button onClick={() => onNavigate('matches')} className="font-data-label text-data-label text-primary-fixed hover:underline">
                ALL →
              </button>
            </div>
            <div className="space-y-sm">
              {metlifeMatches.slice(0, 3).map(m => (
                <div key={m.id} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20 hover:border-primary-fixed/30 transition-all">
                  <div className="font-data-label text-[10px] text-tertiary-fixed-dim uppercase mb-xs">
                    {m.stage}{m.group ? ` · ${m.group}` : ''}
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-body-sm text-on-surface">{m.flagA} {m.teamA}</span>
                    <span className="font-data-label text-[10px] text-primary-fixed">VS</span>
                    <span className="text-body-sm text-on-surface">{m.teamB} {m.flagB}</span>
                  </div>
                  <div className="font-data-label text-[10px] text-on-surface-variant mt-xs">
                    {new Date(m.kickoff).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase mb-md flex items-center gap-xs">
              <span className="material-symbols-outlined text-[16px]">bolt</span>
              QUICK ACTIONS
            </h3>
            <div className="space-y-xs">
              {[
                { icon: 'map', label: 'Open Crowd Heatmap', page: 'map' },
                { icon: 'calendar_month', label: 'Match Schedule', page: 'matches' },
                { icon: 'directions_bus', label: 'Transportation Hub', page: 'transport' },
                { icon: 'smart_toy', label: 'Launch AI Assistant', page: null },
              ].map((a, i) => (
                <button
                  key={i}
                  onClick={() => a.page ? onNavigate(a.page) : document.querySelector('.ai-fab')?.click()}
                  className="w-full flex items-center gap-sm p-sm rounded-lg bg-surface-container border border-outline-variant/20 hover:border-primary-fixed/30 hover:bg-surface-variant transition-all text-left"
                >
                  <span className="material-symbols-outlined text-primary-fixed-dim">{a.icon}</span>
                  <span className="text-body-sm text-on-surface">{a.label}</span>
                  <span className="material-symbols-outlined text-on-surface-variant ml-auto text-[16px]">arrow_forward</span>
                </button>
              ))}
            </div>
          </div>

          {/* System Alert */}
          <div className="bg-error-container/10 border border-error-container/30 rounded-xl p-sm">
            <div className="flex items-center gap-xs text-error mb-sm">
              <span className="material-symbols-outlined text-sm status-pulse">warning</span>
              <span className="font-data-label text-[10px] uppercase">Active Alert</span>
            </div>
            <p className="text-[11px] text-on-error-container leading-tight">
              High density detected in Lower North Stand. Recommended: Deploy 4 additional staff units to Gate A.
            </p>
          </div>
        </div>
      </div>
    </>
  );
}
