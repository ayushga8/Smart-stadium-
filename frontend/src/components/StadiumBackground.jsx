export default function StadiumBackground() {
  return (
    <div className="stadium-bg" aria-hidden="true">
      {/* Concentric Rings */}
      <div className="ring" />
      <div className="ring" />
      <div className="ring" />

      {/* Field Outline */}
      <div className="field" />
      <div className="center-circle" />

      {/* Brand */}
      <div className="brand">
        <h2>SMART STADIUM</h2>
        <p>FIFA World Cup 2026 · Mission Control</p>
      </div>
    </div>
  );
}
