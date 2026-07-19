export default function AccessibilityCenter() {
  const routes = [
    { id: 1, from: 'Gate A', to: 'Section 100', type: 'Wheelchair', elevators: 2, ramps: 3, estTime: '6 min' },
    { id: 2, from: 'Gate B', to: 'Section 200', type: 'Wheelchair', elevators: 1, ramps: 2, estTime: '8 min' },
    { id: 3, from: 'Lot A', to: 'Gate C', type: 'Mobility Scooter', elevators: 0, ramps: 4, estTime: '5 min' },
  ];

  const sensoryRooms = [
    { id: 'SR-1', name: 'Quiet Room Alpha', location: 'Level 1, Section 110', status: 'available', capacity: 8, current: 2 },
    { id: 'SR-2', name: 'Quiet Room Bravo', location: 'Level 2, Section 215', status: 'in-use', capacity: 6, current: 5 },
    { id: 'SR-3', name: 'Sensory Suite', location: 'Level 3, Club Level', status: 'available', capacity: 10, current: 0 },
  ];

  const interpreters = [
    { lang: 'ASL', name: 'Maria Torres', location: 'Guest Services Desk A', available: true, nextSlot: '—' },
    { lang: 'BSL', name: 'James Wright', location: 'Section 120 Booth', available: false, nextSlot: '2:30 PM' },
    { lang: 'ISL', name: 'Priya Sharma', location: 'Guest Services Desk B', available: true, nextSlot: '—' },
  ];

  const assistiveDevices = [
    { device: 'Wheelchair', total: 50, checkedOut: 32, available: 18 },
    { device: 'Hearing Loop Receiver', total: 100, checkedOut: 45, available: 55 },
    { device: 'Mobility Scooter', total: 20, checkedOut: 14, available: 6 },
    { device: 'Visual Aid Tablet', total: 30, checkedOut: 8, available: 22 },
  ];

  const multilingualKiosks = [
    { id: 'MK-1', location: 'Gate A Entrance', languages: 12, status: 'online', usageToday: 234 },
    { id: 'MK-2', location: 'Food Court Level 2', languages: 12, status: 'online', usageToday: 189 },
    { id: 'MK-3', location: 'Gate C Entrance', languages: 12, status: 'maintenance', usageToday: 0 },
    { id: 'MK-4', location: 'Fan Zone East', languages: 12, status: 'online', usageToday: 312 },
  ];

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Accessibility Center</h1>
          <p className="font-body-md text-on-surface-variant">
            Inclusive services, assistive technology, and multilingual support for all stadium guests.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Devices Available</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">
              {assistiveDevices.reduce((s, d) => s + d.available, 0)}
            </div>
          </div>
          <div className="h-12 w-px bg-outline-variant" />
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Kiosks Online</div>
            <div className="font-data-value text-3xl text-secondary-fixed tracking-tighter glow-sm">
              {multilingualKiosks.filter(k => k.status === 'online').length}/{multilingualKiosks.length}
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        <div className="flex-[2] min-w-0 space-y-4">
          {/* Accessible Routes */}
          <div className="glass-panel rounded-xl overflow-hidden">
            <div className="p-md border-b border-outline-variant flex items-center gap-sm">
              <span className="material-symbols-outlined text-primary-fixed-dim">accessible_forward</span>
              <span className="font-data-label text-data-label text-on-surface uppercase">ACCESSIBLE ROUTE PLANNER</span>
            </div>
            <table className="w-full text-left">
              <thead className="bg-surface-container text-data-label text-primary-fixed">
                <tr>
                  <th className="p-sm font-data-label">FROM</th>
                  <th className="p-sm font-data-label">TO</th>
                  <th className="p-sm font-data-label">TYPE</th>
                  <th className="p-sm font-data-label">ELEVATORS</th>
                  <th className="p-sm font-data-label">RAMPS</th>
                  <th className="p-sm font-data-label">EST. TIME</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/30 font-body-sm">
                {routes.map(r => (
                  <tr key={r.id} className="hover:bg-surface-variant/30 transition-colors">
                    <td className="p-sm text-on-surface">{r.from}</td>
                    <td className="p-sm text-on-surface">{r.to}</td>
                    <td className="p-sm text-on-surface-variant">{r.type}</td>
                    <td className="p-sm text-primary-fixed-dim font-data-value">{r.elevators}</td>
                    <td className="p-sm text-secondary-fixed font-data-value">{r.ramps}</td>
                    <td className="p-sm text-on-surface-variant">{r.estTime}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Assistive Device Checkout */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">hearing</span>
              ASSISTIVE DEVICE INVENTORY
            </h3>
            <div className="space-y-sm">
              {assistiveDevices.map((d, i) => (
                <div key={i} className="space-y-xs">
                  <div className="flex justify-between font-data-label text-xs">
                    <span className="text-on-surface uppercase">{d.device}</span>
                    <span className={d.available < 10 ? 'text-error' : 'text-secondary'}>
                      {d.available} AVAILABLE
                    </span>
                  </div>
                  <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-500 ${
                      d.available < 10 ? 'bg-error' : 'bg-secondary-fixed'
                    }`} style={{ width: `${(d.available / d.total) * 100}%` }} />
                  </div>
                  <div className="flex justify-between font-data-value text-body-sm text-on-surface-variant">
                    <span>{d.checkedOut} checked out</span>
                    <span>{d.total} total</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Sidebar */}
        <div className="flex-1 min-w-[280px] space-y-4">
          {/* Sensory Rooms */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">spa</span>
              SENSORY ROOMS
            </h3>
            <div className="space-y-sm">
              {sensoryRooms.map(room => (
                <div key={room.id} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="flex justify-between items-center mb-xs">
                    <span className="text-body-sm text-on-surface font-medium">{room.name}</span>
                    <span className={`text-[10px] font-data-label px-2 py-0.5 rounded ${
                      room.status === 'available' ? 'bg-secondary/10 text-secondary-fixed' : 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim'
                    }`}>
                      {room.status.toUpperCase()}
                    </span>
                  </div>
                  <div className="text-[10px] text-on-surface-variant font-data-label">{room.location}</div>
                  <div className="text-[10px] text-on-surface-variant mt-1">
                    Occupancy: {room.current}/{room.capacity}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Sign Language Interpreters */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">sign_language</span>
              INTERPRETERS
            </h3>
            <div className="space-y-sm">
              {interpreters.map((interp, i) => (
                <div key={i} className="p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="flex justify-between items-center mb-xs">
                    <span className="text-body-sm text-on-surface font-medium">{interp.lang} — {interp.name}</span>
                    <span className={`w-2 h-2 rounded-full ${interp.available ? 'bg-secondary-fixed-dim shadow-[0_0_6px_#2ae500]' : 'bg-outline'}`} />
                  </div>
                  <div className="text-[10px] text-on-surface-variant font-data-label">{interp.location}</div>
                  {!interp.available && (
                    <div className="text-[10px] text-tertiary-fixed-dim mt-1">Next available: {interp.nextSlot}</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Multilingual Kiosks */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">translate</span>
              MULTILINGUAL KIOSKS
            </h3>
            <div className="space-y-sm">
              {multilingualKiosks.map(k => (
                <div key={k.id} className="flex items-center justify-between p-xs rounded hover:bg-surface-variant/20 transition-colors">
                  <div>
                    <div className="text-body-sm text-on-surface font-medium">{k.id} — {k.location}</div>
                    <div className="text-[10px] text-on-surface-variant">{k.languages} languages · {k.usageToday} uses today</div>
                  </div>
                  <span className={`w-2 h-2 rounded-full ${k.status === 'online' ? 'bg-secondary-fixed-dim shadow-[0_0_6px_#2ae500]' : 'bg-error'}`} />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
