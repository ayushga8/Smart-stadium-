import { useState, useEffect } from 'react';

export default function AnalyticsInsights() {
  const [timeRange, setTimeRange] = useState('live');
  const [tick, setTick] = useState(0);
  useEffect(() => { const t = setInterval(() => setTick(p => p + 1), 8000); return () => clearInterval(t); }, []);

  const sentimentData = {
    positive: 68 + (tick % 3),
    neutral: 22 - (tick % 2),
    negative: 10,
    topPositive: ['Atmosphere 🔥', 'Food quality 🍔', 'Security staff 👍'],
    topNegative: ['Queue wait times', 'WiFi speed', 'Parking congestion'],
  };

  const crowdPredictions = [
    { time: '+30 min', occupancy: 64, trend: 'up', delta: '+6%' },
    { time: '+60 min', occupancy: 78, trend: 'up', delta: '+14%' },
    { time: '+90 min', occupancy: 85, trend: 'up', delta: '+7%' },
    { time: '+120 min (Halftime)', occupancy: 72, trend: 'down', delta: '-13%' },
  ];

  const revenueMetrics = [
    { category: 'Food & Beverage', revenue: 284500, target: 350000, icon: '🍔' },
    { category: 'Merchandise', revenue: 156200, target: 200000, icon: '👕' },
    { category: 'Premium Seating', revenue: 420000, target: 450000, icon: '🎟️' },
    { category: 'Sponsorship Activations', revenue: 95000, target: 100000, icon: '📢' },
  ];

  const aiRecommendations = [
    { priority: 'high', action: 'Deploy 6 additional food vendors to Level 2 — predicted 40% demand surge in 30 min', category: 'Operations' },
    { priority: 'high', action: 'Open Gate E overflow — Section 300 approaching 90% capacity', category: 'Crowd' },
    { priority: 'medium', action: 'Increase shuttle frequency on Secaucus route — 82% load on current shuttles', category: 'Transport' },
    { priority: 'medium', action: 'Schedule maintenance crew for Restroom Block C — sentiment flagged in last 15 min', category: 'Facilities' },
    { priority: 'low', action: 'Promotional push for merchandise at Gate B kiosk — low foot traffic vs. potential', category: 'Revenue' },
  ];

  const historicalComparison = [
    { metric: 'Peak Attendance', current: '82,400', previous: '78,200', change: '+5.4%' },
    { metric: 'Avg. Dwell Time', current: '4h 22m', previous: '3h 58m', change: '+10.1%' },
    { metric: 'Fan Satisfaction', current: '4.3/5', previous: '4.1/5', change: '+4.9%' },
    { metric: 'Incident Response', current: '2.1 min', previous: '3.4 min', change: '-38.2%' },
    { metric: 'Revenue / Head', current: '$48.20', previous: '$42.80', change: '+12.6%' },
  ];

  return (
    <>
      <div className="flex flex-wrap justify-between items-start mb-lg gap-4">
        <div className="flex-1 min-w-0">
          <h1 className="font-display-lg text-4xl text-primary-fixed-dim mb-2">Analytics & Insights</h1>
          <p className="font-body-md text-on-surface-variant">
            AI-powered operational intelligence, fan sentiment, and predictive analytics.
          </p>
        </div>
        <div className="flex items-center gap-sm shrink-0">
          {['live', '1h', '4h', 'event'].map(r => (
            <button key={r} onClick={() => setTimeRange(r)}
              className={`px-sm py-1 text-data-label font-data-label rounded transition-colors ${
                timeRange === r
                  ? 'bg-surface-variant text-primary-fixed border border-primary-fixed/30'
                  : 'border border-outline-variant text-on-surface-variant hover:text-primary-fixed'
              }`}>
              {r === 'live' ? '● LIVE' : r.toUpperCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Left */}
        <div className="flex-[2] min-w-0 space-y-4">
          {/* AI Recommendations */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">smart_toy</span>
              AI RECOMMENDATIONS
            </h3>
            <div className="space-y-sm">
              {aiRecommendations.map((rec, i) => (
                <div key={i} className={`p-sm rounded-lg border ${
                  rec.priority === 'high' ? 'bg-error/5 border-error/20' :
                  rec.priority === 'medium' ? 'bg-tertiary-fixed-dim/5 border-tertiary-fixed-dim/20' :
                  'bg-surface-container border-outline-variant/20'
                }`}>
                  <div className="flex items-start gap-sm">
                    <span className={`text-[10px] font-data-label px-1.5 py-0.5 rounded mt-0.5 ${
                      rec.priority === 'high' ? 'bg-error/10 text-error' :
                      rec.priority === 'medium' ? 'bg-tertiary-fixed-dim/10 text-tertiary-fixed-dim' :
                      'bg-surface-variant text-on-surface-variant'
                    }`}>
                      {rec.priority.toUpperCase()}
                    </span>
                    <div className="flex-1">
                      <p className="text-body-sm text-on-surface leading-snug">{rec.action}</p>
                      <span className="text-[10px] text-on-surface-variant font-data-label mt-1 inline-block">{rec.category}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Crowd Predictions */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">trending_up</span>
              CROWD DENSITY FORECAST
            </h3>
            <div className="grid grid-cols-4 gap-sm">
              {crowdPredictions.map((p, i) => (
                <div key={i} className="text-center p-sm rounded-lg bg-surface-container border border-outline-variant/20">
                  <div className="font-data-label text-[10px] text-on-surface-variant uppercase mb-2">{p.time}</div>
                  <div className={`font-data-value text-2xl ${
                    p.occupancy > 80 ? 'text-error' : p.occupancy > 60 ? 'text-tertiary-fixed-dim' : 'text-secondary-fixed'
                  }`}>{p.occupancy}%</div>
                  <div className={`text-[10px] font-data-label mt-1 ${
                    p.trend === 'up' ? 'text-error' : 'text-secondary-fixed'
                  }`}>
                    {p.trend === 'up' ? '↑' : '↓'} {p.delta}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Historical Comparison */}
          <div className="glass-panel rounded-xl overflow-hidden">
            <div className="p-md border-b border-outline-variant flex items-center gap-sm">
              <span className="material-symbols-outlined text-primary-fixed-dim">compare_arrows</span>
              <span className="font-data-label text-data-label text-on-surface uppercase">EVENT VS. PREVIOUS</span>
            </div>
            <table className="w-full text-left">
              <thead className="bg-surface-container text-data-label text-primary-fixed">
                <tr>
                  <th className="p-sm font-data-label">METRIC</th>
                  <th className="p-sm font-data-label">CURRENT</th>
                  <th className="p-sm font-data-label">PREVIOUS</th>
                  <th className="p-sm font-data-label">CHANGE</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/30 font-body-sm">
                {historicalComparison.map((h, i) => (
                  <tr key={i} className="hover:bg-surface-variant/30 transition-colors">
                    <td className="p-sm text-on-surface">{h.metric}</td>
                    <td className="p-sm font-data-value text-primary-fixed-dim">{h.current}</td>
                    <td className="p-sm text-on-surface-variant">{h.previous}</td>
                    <td className="p-sm">
                      <span className={`font-data-label ${h.change.startsWith('+') ? 'text-secondary-fixed' : h.change.startsWith('-') && h.metric === 'Incident Response' ? 'text-secondary-fixed' : 'text-error'}`}>
                        {h.change}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right — Sentiment & Revenue */}
        <div className="flex-1 min-w-[280px] space-y-4">
          {/* Fan Sentiment */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-primary-fixed-dim uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">sentiment_satisfied</span>
              FAN SENTIMENT
            </h3>
            <div className="flex gap-sm mb-md">
              {[
                { label: '😊 Positive', value: sentimentData.positive, color: 'bg-secondary-fixed' },
                { label: '😐 Neutral', value: sentimentData.neutral, color: 'bg-outline' },
                { label: '😤 Negative', value: sentimentData.negative, color: 'bg-error' },
              ].map((s, i) => (
                <div key={i} className="flex-1 text-center">
                  <div className={`font-data-value text-xl ${i === 0 ? 'text-secondary-fixed' : i === 2 ? 'text-error' : 'text-on-surface-variant'}`}>
                    {s.value}%
                  </div>
                  <div className="text-[10px] text-on-surface-variant font-data-label">{s.label}</div>
                </div>
              ))}
            </div>
            <div className="h-2 rounded-full overflow-hidden flex mb-md">
              <div className="bg-secondary-fixed h-full" style={{ width: `${sentimentData.positive}%` }} />
              <div className="bg-outline h-full" style={{ width: `${sentimentData.neutral}%` }} />
              <div className="bg-error h-full" style={{ width: `${sentimentData.negative}%` }} />
            </div>
            <div className="grid grid-cols-2 gap-sm">
              <div>
                <div className="text-[10px] text-secondary-fixed font-data-label uppercase mb-1">Top Positive</div>
                {sentimentData.topPositive.map((t, i) => (
                  <div key={i} className="text-[11px] text-on-surface-variant">{t}</div>
                ))}
              </div>
              <div>
                <div className="text-[10px] text-error font-data-label uppercase mb-1">Top Negative</div>
                {sentimentData.topNegative.map((t, i) => (
                  <div key={i} className="text-[11px] text-on-surface-variant">{t}</div>
                ))}
              </div>
            </div>
          </div>

          {/* Revenue */}
          <div className="glass-panel rounded-xl p-md">
            <h3 className="font-data-label text-data-label text-on-surface-variant uppercase flex items-center gap-xs mb-md">
              <span className="material-symbols-outlined text-[16px]">payments</span>
              REVENUE TRACKING
            </h3>
            <div className="space-y-md">
              {revenueMetrics.map((r, i) => (
                <div key={i}>
                  <div className="flex justify-between text-[10px] font-data-label text-on-surface-variant mb-1 uppercase">
                    <span>{r.icon} {r.category}</span>
                    <span className="text-primary-fixed-dim">
                      ${(r.revenue / 1000).toFixed(0)}K / ${(r.target / 1000).toFixed(0)}K
                    </span>
                  </div>
                  <div className="h-1 bg-surface-variant rounded-full overflow-hidden">
                    <div className="h-full bg-primary-fixed-dim rounded-full" style={{ width: `${(r.revenue / r.target) * 100}%` }} />
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
