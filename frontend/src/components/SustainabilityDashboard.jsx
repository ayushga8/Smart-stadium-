import { useState, useEffect } from 'react';

export default function SustainabilityDashboard() {
  const [tick, setTick] = useState(0);
  useEffect(() => { const t = setInterval(() => setTick(p => p + 1), 5000); return () => clearInterval(t); }, []);

  const energyData = {
    currentKw: 4200 + Math.round(Math.random() * 200),
    peakKw: 6800,
    solarGenKw: 1100 + Math.round(Math.random() * 100),
    gridKw: 3100,
    batteryPct: 72,
  };

  const waterData = {
    usageGallons: 45200 + tick * 12,
    recycledPct: 34,
    greyWaterPct: 22,
    targetGallons: 80000,
  };

  const wasteData = [
    { type: 'Recycling', collected: 2840, target: 5000, color: 'bg-secondary-fixed' },
    { type: 'Compost', collected: 1200, target: 3000, color: 'bg-primary-fixed-dim' },
    { type: 'Landfill', collected: 890, target: 2000, color: 'bg-error' },
    { type: 'E-Waste', collected: 45, target: 100, color: 'bg-tertiary-fixed-dim' },
  ];

  const carbonMetrics = {
    totalTonsCO2: 12.4,
    offsetTons: 8.2,
    netTons: 4.2,
    treesEquivalent: 192,
  };

  const goals = [
    { goal: 'Zero single-use plastics', progress: 87, status: 'on-track' },
    { goal: '50% renewable energy', progress: 62, status: 'on-track' },
    { goal: '90% waste diversion', progress: 71, status: 'at-risk' },
    { goal: 'Carbon neutral operations', progress: 66, status: 'on-track' },
    { goal: 'Water reclamation target', progress: 56, status: 'at-risk' },
  ];

  const diversionRate = Math.round(
    ((wasteData[0].collected + wasteData[1].collected) /
      wasteData.reduce((s, w) => s + w.collected, 0)) * 100
  );

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Sustainability</h1>
          <p className="font-body-md text-on-surface-variant">
            Environmental impact monitoring — energy, water, waste, and carbon footprint tracking.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Diversion Rate</div>
            <div className={`font-data-value text-3xl tracking-tighter glow-sm ${diversionRate >= 80 ? 'text-secondary-fixed' : 'text-tertiary-fixed-dim'}`}>
              {diversionRate}%
            </div>
          </div>
          <div className="h-12 w-px bg-outline-variant" />
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Net Carbon</div>
            <div className="font-data-value text-3xl text-error tracking-tighter glow-sm">{carbonMetrics.netTons}t</div>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-4 mb-lg" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        {[
          { label: 'ENERGY NOW', value: `${(energyData.currentKw / 1000).toFixed(1)} MW`, icon: 'bolt', color: 'text-primary-fixed-dim' },
          { label: 'SOLAR OUTPUT', value: `${(energyData.solarGenKw / 1000).toFixed(1)} MW`, icon: 'solar_power', color: 'text-secondary-fixed-dim' },
          { label: 'WATER USED', value: `${(waterData.usageGallons / 1000).toFixed(1)}K gal`, icon: 'water_drop', color: 'text-tertiary-fixed-dim' },
          { label: 'BATTERY', value: `${energyData.batteryPct}%`, icon: 'battery_charging_full', color: 'text-secondary-fixed-dim' },
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
        {/* Left — Waste & Energy */}
        <div className="flex-[2] min-w-0 space-y-4">
          {/* Waste Streams */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">delete</span>
              WASTE STREAM TRACKING
            </h3>
            <div className="space-y-sm">
              {wasteData.map((w, i) => (
                <div key={i} className="space-y-xs">
                  <div className="flex justify-between font-data-label text-xs">
                    <span className="text-on-surface uppercase">{w.type}</span>
                    <span className="text-on-surface-variant">
                      {w.collected.toLocaleString()} / {w.target.toLocaleString()} lbs
                    </span>
                  </div>
                  <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
                    <div className={`h-full ${w.color} rounded-full transition-all duration-500`}
                         style={{ width: `${(w.collected / w.target) * 100}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Carbon Footprint */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">eco</span>
              CARBON FOOTPRINT
            </h3>
            <div className="grid grid-cols-2 gap-md">
              <div className="p-sm rounded-lg bg-surface-container border border-outline-variant/20 text-center">
                <div className="font-data-label text-[10px] text-on-surface-variant uppercase">Total Emissions</div>
                <div className="font-data-value text-2xl text-error mt-1">{carbonMetrics.totalTonsCO2}t</div>
                <div className="text-[10px] text-on-surface-variant">CO₂ equivalent</div>
              </div>
              <div className="p-sm rounded-lg bg-surface-container border border-outline-variant/20 text-center">
                <div className="font-data-label text-[10px] text-on-surface-variant uppercase">Offsets Applied</div>
                <div className="font-data-value text-2xl text-secondary-fixed mt-1">{carbonMetrics.offsetTons}t</div>
                <div className="text-[10px] text-on-surface-variant">{carbonMetrics.treesEquivalent} trees equiv.</div>
              </div>
            </div>
          </div>
        </div>

        {/* Right — Goals */}
        <div className="flex-1 min-w-[280px] space-y-4">
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">flag</span>
              SUSTAINABILITY GOALS
            </h3>
            <div className="space-y-md">
              {goals.map((g, i) => (
                <div key={i}>
                  <div className="flex justify-between font-data-label text-xs mb-1">
                    <span className="text-on-surface">{g.goal}</span>
                    <span className={g.status === 'on-track' ? 'text-secondary-fixed' : 'text-tertiary-fixed-dim'}>
                      {g.progress}%
                    </span>
                  </div>
                  <div className="h-1 bg-surface-container-highest rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-500 ${
                      g.status === 'on-track' ? 'bg-secondary-fixed' : 'bg-tertiary-fixed-dim'
                    }`} style={{ width: `${g.progress}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Water */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">water_drop</span>
              WATER RECLAMATION
            </h3>
            <div className="space-y-sm">
              <div className="flex justify-between text-xs font-data-label">
                <span className="text-on-surface">Recycled Water</span>
                <span className="text-secondary-fixed">{waterData.recycledPct}%</span>
              </div>
              <div className="h-1 bg-surface-container-highest rounded-full overflow-hidden">
                <div className="h-full bg-secondary-fixed rounded-full" style={{ width: `${waterData.recycledPct}%` }} />
              </div>
              <div className="flex justify-between text-xs font-data-label">
                <span className="text-on-surface">Grey Water Reuse</span>
                <span className="text-primary-fixed-dim">{waterData.greyWaterPct}%</span>
              </div>
              <div className="h-1 bg-surface-container-highest rounded-full overflow-hidden">
                <div className="h-full bg-primary-fixed-dim rounded-full" style={{ width: `${waterData.greyWaterPct}%` }} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
