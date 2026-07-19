import { useState } from 'react';

export default function SecurityOps() {
  const [threatLevel] = useState('ELEVATED');

  const incidents = [
    { id: 'INC-401', type: 'Unauthorized Access', location: 'Gate D Perimeter', severity: 'high', time: '10:14 AM', status: 'active', assignee: 'Team Alpha' },
    { id: 'INC-400', type: 'Medical Emergency', location: 'Section 218', severity: 'medium', time: '10:08 AM', status: 'responding', assignee: 'Medical Unit 3' },
    { id: 'INC-399', type: 'Suspicious Package', location: 'Lot B Row 14', severity: 'high', time: '9:52 AM', status: 'cleared', assignee: 'K-9 Unit' },
    { id: 'INC-398', type: 'Fan Altercation', location: 'Section 112', severity: 'low', time: '9:41 AM', status: 'resolved', assignee: 'Team Bravo' },
    { id: 'INC-397', type: 'Lost Child', location: 'Food Court L2', severity: 'medium', time: '9:30 AM', status: 'resolved', assignee: 'Guest Services' },
  ];

  const cameraZones = [
    { zone: 'Gate A Perimeter', cameras: 12, online: 12, alerts: 0 },
    { zone: 'Gate B Perimeter', cameras: 10, online: 10, alerts: 1 },
    { zone: 'Gate C Perimeter', cameras: 8, online: 7, alerts: 0 },
    { zone: 'Gate D Perimeter', cameras: 10, online: 10, alerts: 2 },
    { zone: 'Bowl Level 1', cameras: 24, online: 24, alerts: 0 },
    { zone: 'Bowl Level 2', cameras: 20, online: 19, alerts: 1 },
    { zone: 'Parking Lots', cameras: 16, online: 15, alerts: 0 },
    { zone: 'VIP / Club Level', cameras: 8, online: 8, alerts: 0 },
  ];

  const exitFlows = [
    { exit: 'Gate A (North)', capacity: 'High', flowRate: 420, status: 'normal' },
    { exit: 'Gate B (East)', capacity: 'Medium', flowRate: 280, status: 'normal' },
    { exit: 'Gate C (South)', capacity: 'High', flowRate: 510, status: 'congested' },
    { exit: 'Gate D (West)', capacity: 'Medium', flowRate: 190, status: 'restricted' },
  ];

  const securityStaff = [
    { zone: 'Perimeter Security', deployed: 120, required: 130, gap: 10 },
    { zone: 'Bowl Security', deployed: 85, required: 80, gap: -5 },
    { zone: 'VIP Protection', deployed: 24, required: 24, gap: 0 },
    { zone: 'Parking Operations', deployed: 32, required: 40, gap: 8 },
    { zone: 'Emergency Response', deployed: 18, required: 20, gap: 2 },
  ];

  const threatColors = { LOW: 'text-secondary-fixed', GUARDED: 'text-primary-fixed-dim', ELEVATED: 'text-tertiary-fixed-dim', HIGH: 'text-error', SEVERE: 'text-error' };
  const threatBg = { LOW: 'bg-secondary/10 border-secondary/30', GUARDED: 'bg-primary-fixed/10 border-primary-fixed/30', ELEVATED: 'bg-tertiary-fixed-dim/10 border-tertiary-fixed-dim/30', HIGH: 'bg-error/10 border-error/30', SEVERE: 'bg-error/20 border-error/50' };
  const sevColor = s => s === 'high' ? 'text-error' : s === 'medium' ? 'text-tertiary-fixed-dim' : 'text-on-surface-variant';

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Security Operations</h1>
          <p className="font-body-md text-on-surface-variant">
            Incident monitoring, threat assessment, and emergency response coordination.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className={`px-md py-sm rounded-xl border ${threatBg[threatLevel]} text-center`}>
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Threat Level</div>
            <div className={`font-data-value text-2xl tracking-tighter ${threatColors[threatLevel]}`}>{threatLevel}</div>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-4 mb-lg" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        {[
          { label: 'ACTIVE INCIDENTS', value: incidents.filter(i => i.status === 'active' || i.status === 'responding').length, icon: 'emergency', color: 'text-error' },
          { label: 'CAMERAS ONLINE', value: `${cameraZones.reduce((s, z) => s + z.online, 0)}/${cameraZones.reduce((s, z) => s + z.cameras, 0)}`, icon: 'videocam', color: 'text-primary-fixed-dim' },
          { label: 'STAFF DEPLOYED', value: securityStaff.reduce((s, z) => s + z.deployed, 0), icon: 'shield_person', color: 'text-secondary-fixed-dim' },
          { label: 'EXITS CLEAR', value: `${exitFlows.filter(e => e.status === 'normal').length}/${exitFlows.length}`, icon: 'door_open', color: 'text-tertiary-fixed-dim' },
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

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Left — Incidents & Cameras */}
        <div className="flex-[2] min-w-0 space-y-4">
          {/* Incident Log */}
          <div className="glass-panel rounded-xl overflow-hidden">
            <div className="p-md border-b border-outline-variant flex items-center gap-sm">
              <span className="material-symbols-outlined text-error status-pulse">emergency</span>
              <span className="font-data-label text-data-label text-on-surface uppercase">INCIDENT LOG</span>
            </div>
            <table className="w-full text-left">
              <thead className="bg-surface-container text-data-label text-primary-fixed">
                <tr>
                  <th className="p-sm font-data-label">ID</th>
                  <th className="p-sm font-data-label">TYPE</th>
                  <th className="p-sm font-data-label">LOCATION</th>
                  <th className="p-sm font-data-label">SEVERITY</th>
                  <th className="p-sm font-data-label">STATUS</th>
                  <th className="p-sm font-data-label">ASSIGNED</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/30 font-body-sm">
                {incidents.map(inc => (
                  <tr key={inc.id} className="hover:bg-surface-variant/30 transition-colors">
                    <td className="p-sm font-data-value text-xs text-primary-fixed-dim">{inc.id}</td>
                    <td className="p-sm text-on-surface">{inc.type}</td>
                    <td className="p-sm text-on-surface-variant">{inc.location}</td>
                    <td className="p-sm">
                      <span className={`font-data-label text-[10px] uppercase ${sevColor(inc.severity)}`}>{inc.severity}</span>
                    </td>
                    <td className="p-sm">
                      <span className={`px-2 py-1 text-[10px] rounded border font-data-label ${
                        inc.status === 'active' ? 'bg-error/10 text-error border-error/20' :
                        inc.status === 'responding' ? 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim border-tertiary-fixed-dim/20' :
                        'bg-secondary/10 text-secondary-fixed border-secondary/20'
                      }`}>
                        {inc.status.toUpperCase()}
                      </span>
                    </td>
                    <td className="p-sm text-on-surface-variant text-xs">{inc.assignee}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Camera Grid */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">videocam</span>
              CAMERA SURVEILLANCE GRID
            </h3>
            <div className="grid grid-cols-2 gap-sm">
              {cameraZones.map((z, i) => (
                <div key={i} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="flex justify-between items-center mb-xs">
                    <span className="text-body-sm text-on-surface font-medium">{z.zone}</span>
                    {z.alerts > 0 && (
                      <span className="text-[10px] font-data-label text-error bg-error/10 px-1.5 py-0.5 rounded">
                        {z.alerts} ALERT{z.alerts > 1 ? 'S' : ''}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-xs">
                    <span className={`w-2 h-2 rounded-full ${z.online === z.cameras ? 'bg-secondary-fixed-dim shadow-[0_0_6px_#2ae500]' : 'bg-tertiary-fixed-dim'}`} />
                    <span className="text-[10px] text-on-surface-variant font-data-label">{z.online}/{z.cameras} online</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right — Exits & Staff */}
        <div className="flex-1 min-w-[280px] space-y-4">
          {/* Exit Flow */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">door_open</span>
              EMERGENCY EXIT STATUS
            </h3>
            <div className="space-y-sm">
              {exitFlows.map((e, i) => (
                <div key={i} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="flex justify-between items-center mb-xs">
                    <span className="text-body-sm text-on-surface font-medium">{e.exit}</span>
                    <span className={`text-[10px] font-data-label px-2 py-0.5 rounded ${
                      e.status === 'normal' ? 'bg-secondary/10 text-secondary-fixed' :
                      e.status === 'congested' ? 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim' :
                      'bg-error/10 text-error'
                    }`}>
                      {e.status.toUpperCase()}
                    </span>
                  </div>
                  <div className="text-[10px] text-on-surface-variant font-data-label">
                    Flow: {e.flowRate} ppl/min · Cap: {e.capacity}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Staff Deployment */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">shield_person</span>
              SECURITY DEPLOYMENT
            </h3>
            <div className="space-y-md">
              {securityStaff.map((s, i) => (
                <div key={i}>
                  <div className="flex justify-between text-[10px] font-data-label text-on-surface-variant mb-1 uppercase">
                    <span>{s.zone}</span>
                    <span className={s.gap > 0 ? 'text-error' : 'text-secondary-fixed'}>
                      {s.deployed}/{s.required} {s.gap > 0 ? `(−${s.gap})` : '✓'}
                    </span>
                  </div>
                  <div className="h-1 bg-surface-variant w-full rounded-full overflow-hidden">
                    <div className={`h-full rounded-full ${s.gap > 0 ? 'bg-error' : 'bg-secondary-fixed-dim'}`}
                         style={{ width: `${Math.min((s.deployed / s.required) * 100, 100)}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
