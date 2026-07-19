import { useState } from 'react';

export default function TransportHub() {
  const [selectedLot, setSelectedLot] = useState(null);

  const parkingLots = [
    { id: 'A', name: 'Lot A — VIP / Media', capacity: 2000, occupied: 1640, status: 'high' },
    { id: 'B', name: 'Lot B — General North', capacity: 5000, occupied: 3200, status: 'moderate' },
    { id: 'C', name: 'Lot C — General South', capacity: 5000, occupied: 1800, status: 'low' },
    { id: 'D', name: 'Lot D — Staff / Operations', capacity: 1500, occupied: 890, status: 'moderate' },
    { id: 'E', name: 'Lot E — Overflow', capacity: 3000, occupied: 450, status: 'low' },
  ];

  const shuttles = [
    { id: 'SH-1', route: 'Secaucus Junction → Gate A', eta: '4 min', status: 'en-route', load: 82 },
    { id: 'SH-2', route: 'Penn Station → Gate C', eta: '12 min', status: 'en-route', load: 65 },
    { id: 'SH-3', route: 'Gate A → Secaucus Junction', eta: 'Boarding', status: 'boarding', load: 34 },
    { id: 'SH-4', route: 'Newark Airport → Gate B', eta: '22 min', status: 'en-route', load: 91 },
    { id: 'SH-5', route: 'Hoboken Terminal → Gate D', eta: '8 min', status: 'en-route', load: 47 },
  ];

  const transitRoutes = [
    { name: 'NJ Transit Rail', status: 'on-time', frequency: 'Every 8 min', load: 'Heavy' },
    { name: 'PATH Train', status: 'on-time', frequency: 'Every 5 min', load: 'Moderate' },
    { name: 'NY Waterway Ferry', status: 'delayed', frequency: 'Every 20 min', load: 'Light' },
    { name: 'Bus Route 160', status: 'on-time', frequency: 'Every 10 min', load: 'Heavy' },
  ];

  const rideshareZones = [
    { zone: 'Pickup Zone Alpha', waitTime: '3 min', queueLength: 42, surge: 1.2 },
    { zone: 'Pickup Zone Bravo', waitTime: '8 min', queueLength: 89, surge: 1.8 },
    { zone: 'Drop-off Zone', waitTime: '—', queueLength: 0, surge: 1.0 },
  ];

  const getStatusColor = s => s === 'high' ? 'text-error' : s === 'moderate' ? 'text-tertiary-fixed-dim' : 'text-secondary';
  const getBarColor = s => s === 'high' ? 'bg-error' : s === 'moderate' ? 'bg-tertiary-fixed-dim' : 'bg-secondary-fixed';

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Transportation Hub</h1>
          <p className="font-body-md text-on-surface-variant">
            Real-time shuttle tracking, parking capacity, and transit integration for MetLife Stadium.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Active Shuttles</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">{shuttles.length}</div>
          </div>
          <div className="h-12 w-px bg-outline-variant" />
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Total Parked</div>
            <div className="font-data-value text-3xl text-secondary-fixed tracking-tighter glow-sm">
              {parkingLots.reduce((s, l) => s + l.occupied, 0).toLocaleString()}
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Left — Parking & Shuttles */}
        <div className="flex-[2] min-w-0 space-y-4">
          {/* Parking Lots */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">local_parking</span>
              PARKING LOT STATUS
            </h3>
            <div className="space-y-sm">
              {parkingLots.map(lot => (
                <div key={lot.id} className="space-y-xs cursor-pointer hover:bg-surface-variant/20 p-xs rounded transition-colors"
                     onClick={() => setSelectedLot(lot)}>
                  <div className="flex justify-between font-data-label text-xs">
                    <span className="text-on-surface uppercase">{lot.name}</span>
                    <span className={getStatusColor(lot.status)}>
                      {Math.round(lot.occupied / lot.capacity * 100)}% FULL
                    </span>
                  </div>
                  <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
                    <div className={`h-full ${getBarColor(lot.status)} rounded-full transition-all duration-500`}
                         style={{ width: `${(lot.occupied / lot.capacity) * 100}%` }} />
                  </div>
                  <div className="flex justify-between font-data-value text-body-sm text-on-surface-variant">
                    <span>{lot.occupied.toLocaleString()} / {lot.capacity.toLocaleString()}</span>
                    <span>{(lot.capacity - lot.occupied).toLocaleString()} available</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Shuttle Tracker */}
          <div className="glass-panel rounded-xl overflow-hidden">
            <div className="p-md border-b border-outline-variant flex items-center gap-sm">
              <span className="material-symbols-outlined text-primary-fixed-dim">directions_bus</span>
              <span className="font-data-label text-data-label text-on-surface uppercase">LIVE SHUTTLE TRACKER</span>
            </div>
            <table className="w-full text-left">
              <thead className="bg-surface-container text-data-label text-primary-fixed">
                <tr>
                  <th className="p-sm font-data-label">ID</th>
                  <th className="p-sm font-data-label">ROUTE</th>
                  <th className="p-sm font-data-label">ETA</th>
                  <th className="p-sm font-data-label">LOAD</th>
                  <th className="p-sm font-data-label">STATUS</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/30 font-body-sm">
                {shuttles.map(s => (
                  <tr key={s.id} className="hover:bg-surface-variant/30 transition-colors">
                    <td className="p-sm font-data-value text-xs text-primary-fixed-dim">{s.id}</td>
                    <td className="p-sm text-on-surface">{s.route}</td>
                    <td className="p-sm text-on-surface-variant">{s.eta}</td>
                    <td className="p-sm">
                      <div className="flex items-center gap-xs">
                        <div className="w-16 h-1 bg-surface-container-highest rounded-full overflow-hidden">
                          <div className={`h-full rounded-full ${s.load > 80 ? 'bg-error' : s.load > 50 ? 'bg-tertiary-fixed-dim' : 'bg-secondary-fixed'}`}
                               style={{ width: `${s.load}%` }} />
                        </div>
                        <span className="text-xs text-on-surface-variant">{s.load}%</span>
                      </div>
                    </td>
                    <td className="p-sm">
                      <span className={`px-2 py-1 text-[10px] rounded border font-data-label ${
                        s.status === 'boarding'
                          ? 'bg-primary-fixed/10 text-primary-fixed border-primary-fixed/20'
                          : 'bg-secondary/10 text-secondary-fixed border-secondary/20'
                      }`}>
                        {s.status.toUpperCase()}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right — Transit & Rideshare */}
        <div className="flex-1 min-w-[280px] space-y-4">
          {/* Transit Routes */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">train</span>
              TRANSIT CONNECTIONS
            </h3>
            <div className="space-y-sm">
              {transitRoutes.map((t, i) => (
                <div key={i} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="flex justify-between items-center mb-xs">
                    <span className="text-body-sm text-on-surface font-medium">{t.name}</span>
                    <span className={`text-[10px] font-data-label px-2 py-0.5 rounded ${
                      t.status === 'on-time' ? 'bg-secondary/10 text-secondary-fixed' : 'bg-error/10 text-error'
                    }`}>
                      {t.status.toUpperCase()}
                    </span>
                  </div>
                  <div className="flex justify-between text-[10px] text-on-surface-variant font-data-label">
                    <span>{t.frequency}</span>
                    <span>Load: {t.load}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Rideshare Zones */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">local_taxi</span>
              RIDESHARE ZONES
            </h3>
            <div className="space-y-sm">
              {rideshareZones.map((r, i) => (
                <div key={i} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="text-body-sm text-on-surface font-medium mb-xs">{r.zone}</div>
                  <div className="flex justify-between text-[10px] font-data-label text-on-surface-variant">
                    <span>Wait: {r.waitTime}</span>
                    <span>Queue: {r.queueLength}</span>
                    <span className={r.surge > 1.5 ? 'text-error' : 'text-on-surface-variant'}>
                      Surge: {r.surge}x
                    </span>
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
