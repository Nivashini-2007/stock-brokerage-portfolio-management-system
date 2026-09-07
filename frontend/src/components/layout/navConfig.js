// roles: undefined = visible to every authenticated role
export const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/terminal', label: 'Terminal', icon: 'terminal', roles: ['CLIENT', 'DEALER', 'ADMIN'] },
  { to: '/portfolio', label: 'Portfolio', icon: 'portfolio' },
  { to: '/holdings', label: 'Holdings', icon: 'holdings' },
  { to: '/orders', label: 'Orders', icon: 'orders', roles: ['CLIENT', 'DEALER', 'COMPLIANCE_OFFICER', 'RISK_MANAGER', 'ADMIN'] },
  { to: '/positions', label: 'Positions', icon: 'positions', roles: ['CLIENT', 'DEALER', 'ADMIN'] },
  { to: '/funds', label: 'Funds', icon: 'funds' },
  { to: '/watchlist', label: 'Watchlist', icon: 'watchlist' },
  { to: '/research', label: 'Research', icon: 'research' },
  { to: '/risk-alerts', label: 'Risk Alerts', icon: 'risk', roles: ['DEALER', 'RISK_MANAGER', 'ADMIN'] },
  { to: '/market', label: 'Market Admin', icon: 'market', roles: ['DEALER', 'ADMIN'] },
  { to: '/compliance', label: 'Compliance', icon: 'compliance', roles: ['COMPLIANCE_OFFICER', 'ADMIN'] },
  { to: '/notifications', label: 'Notifications', icon: 'bell' },
  { to: '/profile', label: 'Profile & Settings', icon: 'user' },
  { to: '/admin', label: 'Admin Panel', icon: 'admin', roles: ['ADMIN'] },
];
