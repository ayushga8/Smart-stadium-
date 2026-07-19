import { useState } from 'react';

export default function MatchSchedule({ matches = [] }) {
  const [filter, setFilter] = useState('all');

  const filtered = filter === 'all' ? matches
    : filter === 'metlife' ? matches.filter(m => m.venue?.includes('MetLife'))
    : matches.filter(m => m.stage === filter);

  return (
    <>
      {/* Header */}
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Operations Logistics</h1>
          <p className="font-body-md text-on-surface-variant">
            Real-time scheduling of matches, team movements, and tactical staff deployments.
          </p>
        </div>
        <div className="flex items-center shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Total Events</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">{matches.length}</div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-xs mb-md flex-wrap">
        {[
          ['all', 'ALL EVENTS'],
          ['metlife', 'METLIFE ONLY'],
          ['Group Stage', 'GROUP STAGE'],
          ['Quarter-Final', 'KNOCKOUTS'],
        ].map(([val, label]) => (
          <button
            key={val}
            onClick={() => setFilter(val)}
            className={`px-sm py-1 text-data-label font-data-label rounded transition-colors ${
              filter === val
                ? 'bg-surface-variant text-primary-fixed border border-primary-fixed/30'
                : 'border border-outline-variant text-on-surface-variant hover:text-primary-fixed'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Tournament Manifest Table */}
        <div className="flex-[3] min-w-0">
          <div className="glass-panel rounded-xl overflow-hidden">
            <div className="p-md border-b border-outline-variant flex justify-between items-center">
              <div className="flex items-center gap-sm">
                <span className="material-symbols-outlined text-primary-fixed-dim">table_chart</span>
                <span className="font-data-label text-data-label text-on-surface uppercase">Tournament Manifest</span>
              </div>
              <div className="flex gap-sm">
                <span className="flex items-center gap-xs text-[10px] text-on-surface-variant bg-surface-container-highest px-2 py-1 rounded">
                  <span className="w-2 h-2 rounded-full bg-secondary" /> PRIORITY
                </span>
                <span className="flex items-center gap-xs text-[10px] text-on-surface-variant bg-surface-container-highest px-2 py-1 rounded">
                  <span className="w-2 h-2 rounded-full bg-primary-fixed-dim" /> BROADCAST
                </span>
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-surface-container text-data-label text-primary-fixed">
                  <tr>
                    <th className="p-sm font-data-label">EVENT ID</th>
                    <th className="p-sm font-data-label">COMPETITION</th>
                    <th className="p-sm font-data-label">LOCATION</th>
                    <th className="p-sm font-data-label">WINDOW</th>
                    <th className="p-sm font-data-label">STATUS</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/30 font-body-sm">
                  {filtered.map(m => (
                    <tr key={m.id} className="hover:bg-surface-variant/30 transition-colors">
                      <td className="p-sm font-data-value text-xs text-primary-fixed-dim">EV-{m.id}</td>
                      <td className="p-sm">
                        <div className="font-medium text-on-surface">{m.flagA} {m.teamA} vs {m.teamB} {m.flagB}</div>
                        <div className="text-[10px] text-on-surface-variant">{m.stage}{m.group ? ` · ${m.group}` : ''}</div>
                      </td>
                      <td className="p-sm text-on-surface-variant">{m.venue}</td>
                      <td className="p-sm text-on-surface-variant">
                        {new Date(m.kickoff).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}{' '}
                        {new Date(m.kickoff).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td className="p-sm">
                        <span className="px-2 py-1 bg-secondary/10 text-secondary-fixed text-[10px] rounded border border-secondary/20 font-data-label">
                          READY
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Sidebar */}
        <div className="flex-1 min-w-[260px] space-y-4">
          {/* Staff Deployment */}
          <div className="glass-panel rounded-xl p-md">
            <div className="flex items-center gap-sm mb-md">
              <span className="material-symbols-outlined text-secondary-fixed-dim">badge</span>
              <span className="font-data-label text-data-label text-on-surface uppercase">Staff Deployment</span>
            </div>
            <div className="space-y-md">
              {[
                { label: 'Security (Tier 1)', deployed: 412, total: 450, color: 'bg-secondary-fixed-dim', textColor: 'text-secondary' },
                { label: 'Medical / First Aid', deployed: 88, total: 120, color: 'bg-primary-fixed-dim', textColor: 'text-primary-fixed' },
                { label: 'Logistics Crew', deployed: 156, total: 200, color: 'bg-on-surface', textColor: 'text-on-surface' },
              ].map((s, i) => (
                <div key={i}>
                  <div className="flex justify-between text-[10px] font-data-label text-on-surface-variant mb-1 uppercase">
                    <span>{s.label}</span>
                    <span className={s.textColor}>{s.deployed} / {s.total}</span>
                  </div>
                  <div className="h-1 bg-surface-variant w-full rounded-full overflow-hidden">
                    <div className={`h-full ${s.color} rounded-full`} style={{ width: `${(s.deployed / s.total) * 100}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Alert */}
          <div className="bg-error-container/10 border border-error-container/30 rounded-xl p-sm">
            <div className="flex items-center gap-xs text-error mb-sm">
              <span className="material-symbols-outlined text-sm status-pulse">warning</span>
              <span className="font-data-label text-[10px] uppercase">Active Logistics Alert</span>
            </div>
            <p className="text-[11px] text-on-error-container leading-tight">
              Congestion detected at Gate 4. Team transport delayed by 12 minutes.
            </p>
            <button className="w-full mt-sm py-1 border border-error-container/50 text-[10px] text-on-error-container hover:bg-error-container/20 rounded uppercase font-data-label">
              Re-route Staff
            </button>
          </div>
        </div>
      </div>

      {/* Footer Bar */}
      <div className="fixed bottom-0 right-0 w-[calc(100%-16rem)] px-grid-margin py-xs bg-surface-container-low/90 backdrop-blur-md border-t border-outline-variant flex items-center justify-between z-40">
        <div className="flex items-center gap-md">
          <div className="flex items-center gap-xs">
            <div className="w-2 h-2 rounded-full bg-secondary-fixed-dim shadow-[0_0_8px_#2ae500]" />
            <span className="text-[10px] font-data-label text-on-surface uppercase">Live Sync: Active</span>
          </div>
          <div className="h-4 w-px bg-outline-variant" />
          <div className="text-[10px] font-data-label text-on-surface-variant">
            {new Date().toLocaleTimeString('en-US', { hour12: false })}
          </div>
        </div>
        <button className="text-on-surface-variant hover:text-primary-fixed transition-colors flex items-center gap-xs">
          <span className="material-symbols-outlined text-sm">download</span>
          <span className="text-[10px] font-data-label">EXPORT MANIFEST</span>
        </button>
      </div>
    </>
  );
}
