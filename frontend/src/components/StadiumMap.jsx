import { useState } from 'react';
import { getStadiumMap } from '../api';
import { useEffect } from 'react';

export default function StadiumMap({ crowd = [] }) {
  const [mapData, setMapData] = useState({ zones: [], amenities: [] });
  const [selectedZone, setSelectedZone] = useState(null);
  const [predictive, setPredictive] = useState(false);

  useEffect(() => { getStadiumMap().then(setMapData); }, []);

  const getStatusColor = (status) =>
    status === 'high' ? 'bg-error' : status === 'moderate' ? 'bg-tertiary-fixed-dim' : 'bg-secondary-fixed';
  const getTextColor = (status) =>
    status === 'high' ? 'text-error' : status === 'moderate' ? 'text-tertiary-fixed-dim' : 'text-secondary';

  return (
    <>
      {/* Header */}
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Crowd Flow</h1>
          <p className="font-body-md text-on-surface-variant">
            Bowl density heatmap and real-time crowd analytics for MetLife Stadium.
          </p>
        </div>
        <div className="flex items-center gap-md shrink-0">
          <div className="text-right">
            <div className="font-data-label text-data-label text-on-surface-variant uppercase">Total Occupancy</div>
            <div className="font-data-value text-3xl text-primary-fixed-dim tracking-tighter glow-sm">
              {crowd.length > 0 ? Math.round(crowd.reduce((s, z) => s + z.percentage, 0) / crowd.length * 100) : 0}%
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Heatmap */}
        <div className="flex-[2] min-w-0">
          <section className="glass-panel rounded-xl overflow-hidden relative" style={{ height: '450px' }}>
            {/* Heatmap Header */}
            <div className="absolute top-sm left-sm z-20 flex flex-col gap-xs">
              <h2 className="font-headline-md text-headline-md text-primary-fixed-dim">BOWL DENSITY HEATMAP</h2>
              <div className="flex items-center gap-md text-data-label font-data-label bg-surface-container-lowest/80 px-sm py-xs rounded border border-outline-variant/20">
                <div className="flex items-center gap-xs"><span className="w-3 h-3 rounded-full bg-secondary-fixed" /> 0-30%</div>
                <div className="flex items-center gap-xs"><span className="w-3 h-3 rounded-full bg-tertiary-fixed-dim" /> 31-70%</div>
                <div className="flex items-center gap-xs"><span className="w-3 h-3 rounded-full bg-error" /> 71-100%</div>
              </div>
            </div>

            {/* Predictive Toggle */}
            <div className="absolute top-sm right-sm z-20">
              <div className="glass-panel p-xs rounded-lg flex items-center gap-sm border border-primary-fixed/20">
                <span className="font-data-label text-data-label text-on-surface-variant px-xs">PREDICTIVE FLOW</span>
                <button
                  onClick={() => setPredictive(!predictive)}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${predictive ? 'bg-primary-container' : 'bg-surface-container-highest'}`}
                >
                  <span className={`inline-block h-4 w-4 transform rounded-full transition-transform ${predictive ? 'translate-x-6 bg-on-primary-container' : 'translate-x-1 bg-outline'}`} />
                </button>
              </div>
            </div>

            <div className="scan-line" />

            {/* Stadium Bowl Visual */}
            <div className="relative w-full h-full flex items-center justify-center bg-surface-container-lowest">
              <div className="relative w-4/5 h-4/5">
                <div className="absolute inset-0 border border-primary-fixed/10 rounded-full scale-110 opacity-20" />
                <div className="absolute inset-0 border border-primary-fixed/10 rounded-full scale-125 opacity-10" />
                <div className={`w-full h-full rounded-[40%] border-4 border-outline-variant/40 flex items-center justify-center p-md bg-surface-container/20 overflow-hidden relative ${predictive ? 'glow-active' : ''}`}>
                  {/* Crowd indicator blobs */}
                  {crowd.filter(z => z.status === 'high').map((z, i) => (
                    <div key={i} className="absolute w-8 h-8 rounded-full bg-error-container animate-ping opacity-20"
                         style={{ top: `${20 + i * 25}%`, left: `${20 + i * 20}%` }} />
                  ))}
                  {crowd.filter(z => z.status === 'low').map((z, i) => (
                    <div key={i} className="absolute w-12 h-12 rounded-full bg-secondary-container animate-pulse opacity-10"
                         style={{ bottom: `${15 + i * 20}%`, right: `${15 + i * 18}%` }} />
                  ))}
                  <div className="relative w-full h-full border border-primary-fixed/30 rounded-[35%] flex items-center justify-center">
                    <div className="w-[60%] h-[50%] border border-primary-fixed/50 rounded-lg bg-surface-container-low/40 flex items-center justify-center">
                      <span className="font-data-label text-data-label text-primary-fixed-dim">FIELD LEVEL</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Tooltip */}
            {selectedZone && (
              <div className="absolute bottom-sm left-sm glass-panel p-sm rounded border border-primary-fixed/30 max-w-xs z-20">
                <div className="flex justify-between items-start mb-xs">
                  <span className="font-data-label text-data-label text-primary-fixed">{selectedZone.zoneName.toUpperCase()}</span>
                  <span className={`font-data-label text-data-label px-1 rounded ${selectedZone.status === 'high' ? 'bg-error/20 text-error' : 'bg-secondary/20 text-secondary'}`}>
                    {selectedZone.status.toUpperCase()}
                  </span>
                </div>
                <div className="font-display-lg text-headline-md text-on-surface">{Math.round(selectedZone.percentage * 100)}% DENSITY</div>
                <p className="font-body-sm text-on-surface-variant text-xs mt-xs">
                  {selectedZone.occupancy.toLocaleString()} / {selectedZone.capacity.toLocaleString()} capacity
                </p>
              </div>
            )}
          </section>
        </div>

        {/* Gate Congestion Sidebar */}
        <aside className="flex-1 min-w-[280px] space-y-4">
          {/* Gate Congestion */}
          <div className="glass-panel rounded-xl p-md flex flex-col gap-sm">
            <div className="flex items-center justify-between">
              <h3 className="font-data-label text-data-label flex items-center gap-xs text-primary-fixed-dim">
                <span className="material-symbols-outlined text-[16px]">sensor_door</span>
                GATE CONGESTION
              </h3>
            </div>
            <div className="space-y-md">
              {crowd.slice(0, 5).map(zone => (
                <div key={zone.zoneId} className="space-y-xs cursor-pointer" onClick={() => setSelectedZone(zone)}>
                  <div className="flex justify-between font-data-label text-xs">
                    <span className="text-on-surface uppercase">{zone.zoneName}</span>
                    <span className={getTextColor(zone.status)}>{zone.status.toUpperCase()}</span>
                  </div>
                  <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
                    <div className={`h-full ${getStatusColor(zone.status)} rounded-full transition-all duration-500`}
                         style={{ width: `${zone.percentage * 100}%` }} />
                  </div>
                  <div className={`flex justify-between font-data-value text-body-sm ${getTextColor(zone.status)}`}>
                    <span>{Math.round(zone.percentage * 100)}%</span>
                    <span>{zone.occupancy.toLocaleString()}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Amenities */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase mb-md flex items-center gap-xs">
              <span className="material-symbols-outlined text-[16px]">place</span>
              KEY AMENITIES
            </h3>
            <div className="space-y-xs max-h-48 overflow-y-auto">
              {mapData.amenities.slice(0, 8).map(a => (
                <div key={a.id} className="flex items-center gap-sm p-xs rounded hover:bg-surface-variant transition-colors">
                  <span className="text-lg">
                    {a.type === 'food' ? '🍔' : a.type === 'restroom' ? '🚻' : a.type === 'medical' ? '🏥' :
                     a.type === 'accessibility' ? '♿' : a.type === 'sustainability' ? '♻️' : a.type === 'merchandise' ? '🛍️' : '⚡'}
                  </span>
                  <div>
                    <div className="text-body-sm text-on-surface font-medium">{a.name}</div>
                    <div className="text-[10px] text-on-surface-variant">{a.description}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </>
  );
}
