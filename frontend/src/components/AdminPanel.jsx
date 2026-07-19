import { useState, useEffect } from 'react';

const API_BASE = '';

function getAuthHeaders() {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export default function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionMsg, setActionMsg] = useState('');

  const fetchData = async () => {
    try {
      const [usersRes, statsRes] = await Promise.all([
        fetch(`${API_BASE}/api/admin/users`, { headers: getAuthHeaders() }),
        fetch(`${API_BASE}/api/admin/stats`, { headers: getAuthHeaders() }),
      ]);
      console.log('Admin users response:', usersRes.status, await usersRes.clone().text());
      console.log('Admin stats response:', statsRes.status, await statsRes.clone().text());
      if (usersRes.ok) setUsers(await usersRes.json());
      if (statsRes.ok) setStats(await statsRes.json());
    } catch (e) {
      console.error('Admin fetch error:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const updateRole = async (userId, role) => {
    try {
      const res = await fetch(`${API_BASE}/api/admin/users/${userId}/role`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ role }),
      });
      const data = await res.json();
      if (res.ok) {
        setActionMsg(`✅ ${data.email} → ${data.role}`);
        fetchData();
      } else {
        setActionMsg(`❌ ${data.message}`);
      }
    } catch (e) {
      setActionMsg(`❌ Error: ${e.message}`);
    }
    setTimeout(() => setActionMsg(''), 3000);
  };

  const roleStyle = r =>
    r === 'ADMIN' ? 'bg-primary-fixed/10 text-primary-fixed border-primary-fixed/20' :
    r === 'VOLUNTEER' ? 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim border-tertiary-fixed-dim/20' :
    'bg-surface-variant text-on-surface-variant border-outline-variant/30';

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <span className="material-symbols-outlined text-primary-fixed-dim animate-spin text-4xl">progress_activity</span>
    </div>
  );

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Admin Panel</h1>
          <p className="font-body-md text-on-surface-variant">
            System administration — user management, role assignment, and platform oversight.
          </p>
        </div>
        {actionMsg && (
          <div className="px-md py-sm rounded-xl bg-surface-variant border border-outline-variant text-body-sm text-on-surface">
            {actionMsg}
          </div>
        )}
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid gap-4 mb-lg" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
          {[
            { label: 'TOTAL USERS', value: stats.totalUsers, icon: 'group', color: 'text-primary-fixed-dim' },
            { label: 'VOLUNTEERS', value: stats.volunteers, icon: 'volunteer_activism', color: 'text-tertiary-fixed-dim' },
            { label: 'ADMINS', value: stats.admins, icon: 'admin_panel_settings', color: 'text-error' },
            { label: 'REGULAR USERS', value: stats.regularUsers, icon: 'person', color: 'text-secondary-fixed-dim' },
          ].map((s, i) => (
            <div key={i} className="glass-panel rounded-xl p-md flex items-center gap-md">
              <span className={`material-symbols-outlined text-3xl ${s.color}`}>{s.icon}</span>
              <div>
                <div className="font-data-label text-data-label text-on-surface-variant uppercase">{s.label}</div>
                <div className={`font-data-value text-data-value ${s.color}`}>{s.value}</div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* User Management Table */}
      <div className="glass-panel rounded-xl overflow-hidden">
        <div className="p-md border-b border-outline-variant flex items-center justify-between">
          <div className="flex items-center gap-sm">
            <span className="material-symbols-outlined text-primary-fixed-dim">manage_accounts</span>
            <span className="font-data-label text-data-label text-on-surface uppercase">USER MANAGEMENT</span>
          </div>
          <span className="text-[10px] font-data-label text-on-surface-variant">{users.length} users</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-surface-container text-data-label text-primary-fixed">
              <tr>
                <th className="p-sm font-data-label">ID</th>
                <th className="p-sm font-data-label">EMAIL</th>
                <th className="p-sm font-data-label">NAME</th>
                <th className="p-sm font-data-label">ROLE</th>
                <th className="p-sm font-data-label">PROVIDER</th>
                <th className="p-sm font-data-label">JOINED</th>
                <th className="p-sm font-data-label">ACTIONS</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant/30 font-body-sm">
              {users.map(u => (
                <tr key={u.id} className="hover:bg-surface-variant/30 transition-colors">
                  <td className="p-sm font-data-value text-xs text-primary-fixed-dim">{u.id}</td>
                  <td className="p-sm text-on-surface">{u.email}</td>
                  <td className="p-sm text-on-surface-variant">{u.name || '—'}</td>
                  <td className="p-sm">
                    <span className={`px-2 py-1 text-[10px] rounded border font-data-label ${roleStyle(u.role)}`}>
                      {u.role}
                    </span>
                  </td>
                  <td className="p-sm text-on-surface-variant text-xs">{u.authProvider}</td>
                  <td className="p-sm text-on-surface-variant text-xs">
                    {new Date(u.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                  </td>
                  <td className="p-sm">
                    {u.role !== 'ADMIN' ? (
                      <div className="flex gap-xs">
                        {u.role !== 'VOLUNTEER' && (
                          <button
                            onClick={() => updateRole(u.id, 'VOLUNTEER')}
                            className="px-2 py-1 text-[10px] font-data-label rounded border border-tertiary-fixed-dim/30 text-tertiary-fixed-dim hover:bg-tertiary-fixed-dim/10 transition-colors"
                          >
                            MAKE VOLUNTEER
                          </button>
                        )}
                        {u.role !== 'USER' && (
                          <button
                            onClick={() => updateRole(u.id, 'USER')}
                            className="px-2 py-1 text-[10px] font-data-label rounded border border-outline-variant text-on-surface-variant hover:bg-surface-variant transition-colors"
                          >
                            REVOKE
                          </button>
                        )}
                      </div>
                    ) : (
                      <span className="text-[10px] text-on-surface-variant font-data-label">PROTECTED</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
