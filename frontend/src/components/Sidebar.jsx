export default function Sidebar({ activePage, onNavigate, userRole = 'USER' }) {
  const navItems = [
    // Operations
    { id: 'dashboard', icon: 'dashboard', label: 'Control Center', section: 'operations' },
    { id: 'matches', icon: 'calendar_month', label: 'Logistics', section: 'operations' },
    { id: 'map', icon: 'groups', label: 'Crowd Flow', section: 'operations' },
    { id: 'transport', icon: 'directions_bus', label: 'Transportation', section: 'operations' },
    { id: 'security', icon: 'shield', label: 'Security', section: 'operations' },
    // Services
    { id: 'accessibility', icon: 'accessibility_new', label: 'Accessibility', section: 'services' },
    { id: 'sustainability', icon: 'eco', label: 'Sustainability', section: 'services' },
    // Intelligence
    { id: 'analytics', icon: 'insights', label: 'Analytics', section: 'intelligence' },
    // Role-restricted
    { id: 'volunteer', icon: 'volunteer_activism', label: 'Volunteer Hub', section: 'role', roles: ['VOLUNTEER', 'ADMIN'] },
    { id: 'admin', icon: 'admin_panel_settings', label: 'Admin Panel', section: 'role', roles: ['ADMIN'] },
  ];

  const visibleItems = navItems.filter(item => {
    if (!item.roles) return true;
    return item.roles.includes(userRole);
  });

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    window.location.href = '/';
  };

  const sections = [
    { key: 'operations', label: 'OPERATIONS' },
    { key: 'services', label: 'SERVICES' },
    { key: 'intelligence', label: 'INTELLIGENCE' },
    { key: 'role', label: userRole === 'ADMIN' ? 'ADMIN' : 'MY ROLE' },
  ];

  return (
    <aside className="h-screen w-64 fixed left-0 top-0 border-r border-outline-variant bg-surface-container-low backdrop-blur-md flex flex-col py-md px-sm z-[100]">
      {/* Brand */}
      <div className="mb-xl px-xs">
        <h1 className="font-display-lg text-display-lg-mobile text-primary-fixed-dim tracking-tighter">
          Mission Control
        </h1>
        <p className="font-body-sm text-on-surface-variant opacity-70">Stadium Hub Alpha</p>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-xs overflow-y-auto pr-xs -mr-xs">
        {sections.map(section => {
          const sectionItems = visibleItems.filter(i => i.section === section.key);
          if (sectionItems.length === 0) return null;
          return (
            <div key={section.key} className="mb-sm">
              <div className="font-data-label text-[9px] text-on-surface-variant/50 uppercase px-sm mb-xs tracking-widest">
                {section.label}
              </div>
              {sectionItems.map(item => (
                <button
                  key={item.id}
                  onClick={() => onNavigate(item.id)}
                  className={`w-full flex items-center gap-sm px-sm py-xs rounded-lg transition-all duration-200 ${
                    activePage === item.id
                      ? 'bg-surface-variant text-primary-fixed font-bold border-r-2 border-primary-fixed'
                      : 'text-on-surface-variant font-medium hover:bg-surface-variant hover:text-primary'
                  }`}
                >
                  <span
                    className="material-symbols-outlined"
                    style={activePage === item.id ? { fontVariationSettings: "'FILL' 1" } : {}}
                  >
                    {item.icon}
                  </span>
                  <span className="font-body-md">{item.label}</span>
                  {item.id === 'admin' && (
                    <span className="ml-auto w-2 h-2 rounded-full bg-error shadow-[0_0_6px_#ff4444]" />
                  )}
                </button>
              ))}
            </div>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="mt-auto space-y-xs pt-md border-t border-outline-variant/30">
        <div className="px-sm py-xs mb-sm">
          <p className="font-data-label text-data-label text-primary-fixed-dim uppercase">
            System Health: 98%
          </p>
          <div className="w-full bg-surface-container-highest h-1 rounded-full mt-1">
            <div className="bg-primary-fixed-dim h-full rounded-full" style={{ width: '98%' }} />
          </div>
        </div>

        <button
          onClick={() => onNavigate('settings')}
          className="w-full flex items-center gap-sm px-sm py-xs rounded-lg text-on-surface-variant font-medium hover:text-primary transition-colors"
        >
          <span className="material-symbols-outlined">settings</span>
          <span className="font-body-md">Settings</span>
        </button>

        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-sm px-sm py-xs rounded-lg text-on-surface-variant font-medium hover:text-error transition-colors"
        >
          <span className="material-symbols-outlined">logout</span>
          <span className="font-body-md">Sign Out</span>
        </button>
      </div>
    </aside>
  );
}
