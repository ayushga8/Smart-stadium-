import { useState } from 'react';

export default function VolunteerHub() {
  const [activeTab, setActiveTab] = useState('tasks');

  const tasks = [
    { id: 'VT-101', title: 'Guest Wayfinding — Gate A', location: 'Gate A Concourse', priority: 'high', status: 'assigned', time: '10:00 AM – 12:00 PM', notes: 'Guide fans from Gate A to Sections 100-115' },
    { id: 'VT-102', title: 'Water Station Resupply', location: 'Level 2, Fan Zone', priority: 'medium', status: 'in-progress', time: '11:00 AM – 11:30 AM', notes: 'Replenish water coolers at stations 5, 6, and 7' },
    { id: 'VT-103', title: 'Accessibility Escort', location: 'Parking Lot A → Section 105', priority: 'high', status: 'pending', time: '11:30 AM', notes: 'Wheelchair-assisted guest arriving at Lot A, Row 12' },
    { id: 'VT-104', title: 'Merchandise Booth Support', location: 'Fan Store East', priority: 'low', status: 'completed', time: '9:00 AM – 10:00 AM', notes: 'Assist with inventory setup' },
    { id: 'VT-105', title: 'Lost & Found Desk', location: 'Guest Services B', priority: 'medium', status: 'assigned', time: '12:00 PM – 2:00 PM', notes: 'Log incoming items and process claims' },
  ];

  const announcements = [
    { time: '10:12 AM', message: 'Reminder: Halftime volunteer meeting at Staff Room C at 12:45 PM', type: 'info' },
    { time: '9:55 AM', message: 'High crowd density at Gate D — additional wayfinding volunteers needed', type: 'urgent' },
    { time: '9:30 AM', message: 'Welcome! Today\'s event: FIFA World Cup 2026 — Group Stage Day 1', type: 'info' },
  ];

  const schedule = [
    { shift: 'Morning Shift', time: '8:00 AM – 12:00 PM', role: 'Guest Wayfinding', location: 'Gate A', status: 'active' },
    { shift: 'Afternoon Shift', time: '12:00 PM – 4:00 PM', role: 'Accessibility Support', location: 'Level 1', status: 'upcoming' },
    { shift: 'Evening Shift', time: '4:00 PM – 8:00 PM', role: 'Exit Management', location: 'All Gates', status: 'upcoming' },
  ];

  const stats = {
    hoursToday: 3.2,
    tasksCompleted: 4,
    totalHours: 28.5,
    ranking: 12,
  };

  const priColor = p => p === 'high' ? 'text-error' : p === 'medium' ? 'text-tertiary-fixed-dim' : 'text-on-surface-variant';
  const statusStyle = s =>
    s === 'assigned' ? 'bg-primary-fixed/10 text-primary-fixed border-primary-fixed/20' :
    s === 'in-progress' ? 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim border-tertiary-fixed-dim/20' :
    s === 'completed' ? 'bg-secondary/10 text-secondary-fixed border-secondary/20' :
    'bg-surface-variant text-on-surface-variant border-outline-variant/20';

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Volunteer Hub</h1>
          <p className="font-body-md text-on-surface-variant">
            Your mission dashboard — tasks, schedule, and team communications.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Hours Today</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">{stats.hoursToday}h</div>
          </div>
          <div className="h-12 w-px bg-outline-variant" />
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Tasks Done</div>
            <div className="font-data-value text-3xl text-secondary-fixed tracking-tighter glow-sm">{stats.tasksCompleted}</div>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-4 mb-lg" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        {[
          { label: 'TOTAL HOURS', value: `${stats.totalHours}h`, icon: 'schedule', color: 'text-primary-fixed-dim' },
          { label: 'TASKS TODAY', value: tasks.length, icon: 'task_alt', color: 'text-secondary-fixed-dim' },
          { label: 'RANKING', value: `#${stats.ranking}`, icon: 'emoji_events', color: 'text-tertiary-fixed-dim' },
          { label: 'ACTIVE SHIFT', value: 'MORNING', icon: 'badge', color: 'text-secondary-fixed-dim' },
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

      {/* Tab Switcher */}
      <div className="flex gap-xs mb-md">
        {['tasks', 'schedule'].map(tab => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            className={`px-sm py-1 text-data-label font-data-label rounded transition-colors ${
              activeTab === tab
                ? 'bg-surface-variant text-primary-fixed border border-primary-fixed/30'
                : 'border border-outline-variant text-on-surface-variant hover:text-primary-fixed'
            }`}>
            {tab.toUpperCase()}
          </button>
        ))}
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        <div className="flex-[2] min-w-0">
          {activeTab === 'tasks' ? (
            <div className="glass-panel rounded-xl overflow-hidden">
              <div className="p-md border-b border-outline-variant flex items-center gap-sm">
                <span className="material-symbols-outlined text-primary-fixed-dim">checklist</span>
                <span className="font-data-label text-data-label text-on-surface uppercase">MY TASKS</span>
              </div>
              <div className="divide-y divide-outline-variant/30">
                {tasks.map(t => (
                  <div key={t.id} className="p-md hover:bg-surface-variant/20 transition-colors">
                    <div className="flex items-center justify-between mb-xs">
                      <div className="flex items-center gap-sm">
                        <span className="font-data-value text-xs text-primary-fixed-dim">{t.id}</span>
                        <span className="text-body-sm text-on-surface font-medium">{t.title}</span>
                      </div>
                      <span className={`px-2 py-1 text-[10px] rounded border font-data-label ${statusStyle(t.status)}`}>
                        {t.status.toUpperCase().replace('-', ' ')}
                      </span>
                    </div>
                    <div className="flex items-center gap-md text-[10px] text-on-surface-variant font-data-label">
                      <span>📍 {t.location}</span>
                      <span>🕐 {t.time}</span>
                      <span className={priColor(t.priority)}>● {t.priority.toUpperCase()}</span>
                    </div>
                    <p className="text-[11px] text-on-surface-variant mt-1">{t.notes}</p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="glass-panel rounded-xl p-md">
              <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
                <span className="material-symbols-outlined text-[16px]">calendar_today</span>
                TODAY'S SCHEDULE
              </h3>
              <div className="space-y-sm">
                {schedule.map((s, i) => (
                  <div key={i} className={`p-md rounded-lg border ${
                    s.status === 'active' ? 'bg-primary-fixed/5 border-primary-fixed/30' : 'bg-surface-container border-outline-variant/20'
                  }`}>
                    <div className="flex justify-between items-center mb-xs">
                      <span className="text-body-sm text-on-surface font-bold">{s.shift}</span>
                      <span className={`text-[10px] font-data-label px-2 py-0.5 rounded ${
                        s.status === 'active' ? 'bg-secondary/10 text-secondary-fixed' : 'bg-surface-variant text-on-surface-variant'
                      }`}>
                        {s.status.toUpperCase()}
                      </span>
                    </div>
                    <div className="text-[10px] text-on-surface-variant font-data-label">
                      {s.time} · {s.role} · {s.location}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Announcements */}
        <div className="flex-1 min-w-[280px]">
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">campaign</span>
              ANNOUNCEMENTS
            </h3>
            <div className="space-y-sm">
              {announcements.map((a, i) => (
                <div key={i} className={`p-sm rounded-lg border ${
                  a.type === 'urgent' ? 'bg-error/5 border-error/20' : 'bg-surface-container border-outline-variant/20'
                }`}>
                  <div className="flex items-center gap-xs mb-xs">
                    {a.type === 'urgent' && <span className="material-symbols-outlined text-error text-sm status-pulse">priority_high</span>}
                    <span className="text-[10px] font-data-label text-on-surface-variant">{a.time}</span>
                  </div>
                  <p className="text-body-sm text-on-surface leading-snug">{a.message}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
