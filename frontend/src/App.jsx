import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import ProtectedRoute from './routes/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';

import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import TradingTerminal from './pages/TradingTerminal';
import Portfolio from './pages/Portfolio';
import Holdings from './pages/Holdings';
import Orders from './pages/Orders';
import Positions from './pages/Positions';
import Funds from './pages/Funds';
import Watchlist from './pages/Watchlist';
import Research from './pages/Research';
import RiskAlerts from './pages/RiskAlerts';
import MarketAdmin from './pages/MarketAdmin';
import Compliance from './pages/Compliance';
import Notifications from './pages/Notifications';
import Profile from './pages/Profile';
import Admin from './pages/Admin';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<AppShell />}>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/portfolio" element={<Portfolio />} />
                <Route path="/holdings" element={<Holdings />} />
                <Route path="/funds" element={<Funds />} />
                <Route path="/watchlist" element={<Watchlist />} />
                <Route path="/research" element={<Research />} />
                <Route path="/notifications" element={<Notifications />} />
                <Route path="/profile" element={<Profile />} />

                <Route element={<ProtectedRoute roles={['CLIENT', 'DEALER', 'ADMIN']} />}>
                  <Route path="/terminal" element={<TradingTerminal />} />
                </Route>
                <Route element={<ProtectedRoute roles={['CLIENT', 'DEALER', 'COMPLIANCE_OFFICER', 'RISK_MANAGER', 'ADMIN']} />}>
                  <Route path="/orders" element={<Orders />} />
                </Route>
                <Route element={<ProtectedRoute roles={['CLIENT', 'DEALER', 'ADMIN']} />}>
                  <Route path="/positions" element={<Positions />} />
                </Route>
                <Route element={<ProtectedRoute roles={['DEALER', 'RISK_MANAGER', 'ADMIN']} />}>
                  <Route path="/risk-alerts" element={<RiskAlerts />} />
                </Route>
                <Route element={<ProtectedRoute roles={['DEALER', 'ADMIN']} />}>
                  <Route path="/market" element={<MarketAdmin />} />
                </Route>
                <Route element={<ProtectedRoute roles={['COMPLIANCE_OFFICER', 'ADMIN']} />}>
                  <Route path="/compliance" element={<Compliance />} />
                </Route>
                <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                  <Route path="/admin" element={<Admin />} />
                </Route>
              </Route>
            </Route>

            <Route path="*" element={<NotFound />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
