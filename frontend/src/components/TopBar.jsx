import { useState, useEffect } from 'react';

export default function TopBar() {
  const [time, setTime] = useState(new Date().toLocaleTimeString('en-US', { hour12: false }));

  useEffect(() => {
    const interval = setInterval(() => {
      setTime(new Date().toLocaleTimeString('en-US', { hour12: false }));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="fixed top-0 right-0 w-[calc(100%-16rem)] h-16 flex justify-between items-center px-grid-margin border-b border-outline-variant backdrop-blur-xl bg-surface/80 z-50">
      <div className="flex items-center gap-lg">
        <span className="font-display-lg text-headline-md text-primary-fixed-dim uppercase tracking-widest">
          STADIUM COMMAND
        </span>
        <div className="hidden md:flex gap-md">
          <span className="font-data-label text-data-label text-primary-fixed border-b-2 border-primary-fixed pb-1">
            Live Feeds
          </span>
          <span className="font-data-label text-data-label text-on-surface-variant">
            Alert Logs
          </span>
        </div>
      </div>

      <div className="flex items-center gap-md">
        <div className="flex items-center gap-xs px-sm py-1 rounded bg-secondary-container/10 border border-secondary-fixed-dim/30">
          <span className="w-2 h-2 rounded-full bg-secondary-fixed-dim animate-pulse" />
          <span className="font-data-label text-data-label text-secondary-fixed-dim">LIVE</span>
        </div>
        <span className="font-data-label text-data-label text-on-surface-variant">{time}</span>
        <div className="flex items-center gap-sm ml-md border-l border-outline-variant pl-md">
          <button className="text-on-surface-variant hover:text-primary-fixed transition-colors">
            <span className="material-symbols-outlined">notifications</span>
          </button>
          <div className="w-8 h-8 rounded-full border border-outline-variant bg-surface-container flex items-center justify-center text-primary-fixed-dim font-bold text-sm">
            A
          </div>
        </div>
      </div>
    </header>
  );
}
