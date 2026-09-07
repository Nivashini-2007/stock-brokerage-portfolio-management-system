import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from 'recharts';
import { ledgerApi } from '../api/ledger';
import { marginApi } from '../api/margin';
import { notificationsApi } from '../api/notifications';
import { ordersApi } from '../api/orders';
import { portfolioApi } from '../api/portfolio';
import { researchApi } from '../api/research';
import { riskApi } from '../api/risk';
import Badge from '../components/common/Badge';
import Loader from '../components/common/Loader';
import StatCard from '../components/common/StatCard';
import { useAuth } from '../context/AuthContext';
import { formatCurrency, formatDateTime, formatPercent, formatSigned, roleLabel } from '../utils/format';

const COLORS = ['#3b82f6', '#22c55e', '#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4'];

const CAN_TRADE = ['CLIENT', 'DEALER', 'ADMIN'];

export default function Dashboard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [performance, setPerformance] = useState(null);
  const [holdings, setHoldings] = useState([]);
  const [margin, setMargin] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [recentNotifications, setRecentNotifications] = useState([]);
  const [riskCount, setRiskCount] = useState(null);
  const [pendingResearch, setPendingResearch] = useState(null);
  const [myReports, setMyReports] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      const tasks = [];

      if (CAN_TRADE.includes(user.role)) {
        tasks.push(
          portfolioApi.performance(user.id).then((d) => !cancelled && setPerformance(d)).catch(() => {}),
          portfolioApi.holdings(user.id).then((d) => !cancelled && setHoldings(d)).catch(() => {}),
          marginApi.get(user.id).then((d) => !cancelled && setMargin(d)).catch(() => {}),
          ordersApi.byClient(user.id).then((d) => !cancelled && setRecentOrders(d.slice(-5).reverse())).catch(() => {}),
          ledgerApi.history(user.id).catch(() => [])
        );
      }

      tasks.push(
        notificationsApi.forClient(user.id).then((d) => !cancelled && setRecentNotifications(d.slice(0, 5))).catch(() => {})
      );

      if (['DEALER', 'RISK_MANAGER', 'ADMIN'].includes(user.role)) {
        tasks.push(riskApi.all().then((d) => !cancelled && setRiskCount(d.length)).catch(() => {}));
      }

      if (['COMPLIANCE_OFFICER', 'ADMIN'].includes(user.role)) {
        tasks.push(researchApi.pending().then((d) => !cancelled && setPendingResearch(d.length)).catch(() => {}));
      }

      if (user.role === 'RESEARCH_ANALYST') {
        tasks.push(researchApi.mine().then((d) => !cancelled && setMyReports(d)).catch(() => {}));
      }

      await Promise.all(tasks);
      if (!cancelled) setLoading(false);
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [user.id, user.role]);

  if (loading) return <Loader full />;

  const allocation = holdings
    .filter((h) => h.marketValue > 0)
    .map((h) => ({ name: h.stockSymbol, value: h.marketValue }));

  return (
    <div>
      <div className="mb-2">
        <h2>Welcome, {user.firstName}</h2>
        <div className="text-faint">{roleLabel(user.role)} · {new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</div>
      </div>

      {CAN_TRADE.includes(user.role) && (
        <div className="grid grid-cols-4 mb-2">
          <StatCard
            label="Portfolio Value"
            value={performance ? formatCurrency(performance.currentValue) : '--'}
            hint={performance ? `${formatSigned(performance.totalProfitLoss)} (${formatPercent(performance.profitLossPercentage)})` : 'No holdings yet'}
            hintTone={performance && performance.totalProfitLoss >= 0 ? 'up' : 'down'}
          />
          <StatCard
            label="Total Investment"
            value={performance ? formatCurrency(performance.totalInvestment) : '--'}
            hint={performance ? `${performance.totalStocks} holding(s)` : ''}
          />
          <StatCard
            label="Available Margin"
            value={margin ? formatCurrency(margin.availableMargin) : '--'}
            hint={margin ? `of ${formatCurrency(margin.totalMargin)} total` : ''}
          />
          <StatCard
            label="Margin Utilised"
            value={margin ? `${margin.utilizationPercentage?.toFixed(1)}%` : '--'}
            hint={margin ? formatCurrency(margin.usedMargin) + ' used' : ''}
            hintTone={margin && margin.utilizationPercentage >= 80 ? 'down' : undefined}
          />
        </div>
      )}

      {(riskCount !== null || pendingResearch !== null || myReports !== null) && (
        <div className="grid grid-cols-4 mb-2">
          {riskCount !== null && <StatCard label="Active Risk Alerts" value={riskCount} />}
          {pendingResearch !== null && <StatCard label="Research Pending Review" value={pendingResearch} />}
          {myReports !== null && (
            <>
              <StatCard label="My Reports" value={myReports.length} />
              <StatCard label="Published" value={myReports.filter((r) => r.status === 'PUBLISHED').length} />
            </>
          )}
        </div>
      )}

      <div className="grid grid-cols-2 mb-2">
        {CAN_TRADE.includes(user.role) && (
          <div className="card">
            <div className="card-title">Allocation by Holding</div>
            <div className="card-subtitle">Market value distribution</div>
            {allocation.length === 0 ? (
              <div className="empty-state">No holdings yet. Place your first trade from the Terminal.</div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie data={allocation} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={2}>
                    {allocation.map((entry, i) => (
                      <Cell key={entry.name} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v) => formatCurrency(v)} contentStyle={{ background: '#171f33', border: '1px solid #232c42' }} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        )}

        <div className="card">
          <div className="card-title">Recent Notifications</div>
          <div className="card-subtitle">Latest account activity</div>
          {recentNotifications.length === 0 ? (
            <div className="empty-state">No notifications yet.</div>
          ) : (
            <div>
              {recentNotifications.map((n) => (
                <div key={n.id} className="flex justify-between" style={{ padding: '10px 0', borderBottom: '1px solid var(--border-soft)' }}>
                  <div>
                    <div style={{ fontSize: 13 }}>{n.message}</div>
                    <div className="text-faint" style={{ fontSize: 11 }}>{formatDateTime(n.createdAt)}</div>
                  </div>
                  {!n.read && <span className="badge badge-blue">New</span>}
                </div>
              ))}
            </div>
          )}
          <Link to="/notifications" className="btn-link mt-2" style={{ display: 'inline-block' }}>
            View all →
          </Link>
        </div>
      </div>

      {CAN_TRADE.includes(user.role) && (
        <div className="card">
          <div className="card-title">Recent Orders</div>
          {recentOrders.length === 0 ? (
            <div className="empty-state">No orders placed yet.</div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Symbol</th>
                    <th>Side</th>
                    <th>Type</th>
                    <th>Qty</th>
                    <th>Value</th>
                    <th>Status</th>
                    <th>Placed</th>
                  </tr>
                </thead>
                <tbody>
                  {recentOrders.map((o) => (
                    <tr key={o.id}>
                      <td>{o.stockSymbol}</td>
                      <td><Badge tone={o.orderSide === 'BUY' ? 'green' : 'red'}>{o.orderSide}</Badge></td>
                      <td>{o.orderType}</td>
                      <td>{o.quantity}</td>
                      <td>{formatCurrency(o.totalAmount)}</td>
                      <td><Badge>{o.status}</Badge></td>
                      <td>{formatDateTime(o.placedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <Link to="/orders" className="btn-link mt-2" style={{ display: 'inline-block' }}>
            View all orders →
          </Link>
        </div>
      )}
    </div>
  );
}
