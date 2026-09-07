import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

const TITLES = {
  '/dashboard': 'Dashboard',
  '/terminal': 'Trading Terminal',
  '/portfolio': 'Portfolio',
  '/holdings': 'Holdings',
  '/orders': 'Orders',
  '/positions': 'Positions',
  '/funds': 'Funds',
  '/watchlist': 'Watchlist',
  '/research': 'Research',
  '/risk-alerts': 'Risk Alerts',
  '/market': 'Market Administration',
  '/compliance': 'Compliance',
  '/notifications': 'Notifications',
  '/profile': 'Profile & Settings',
  '/admin': 'Admin Panel',
};

export default function AppShell() {
  const location = useLocation();
  const title = TITLES[location.pathname] || 'TradeFlux';

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-col">
        <Topbar title={title} />
        <main className="page-body">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
