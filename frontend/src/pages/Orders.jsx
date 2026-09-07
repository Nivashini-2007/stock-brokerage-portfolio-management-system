import { useCallback, useEffect, useState } from 'react';
import { apiErrorMessage, downloadBlob } from '../api/client';
import { ordersApi } from '../api/orders';
import Badge from '../components/common/Badge';
import Loader from '../components/common/Loader';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDateTime } from '../utils/format';

const BOOK_ROLES = ['DEALER', 'COMPLIANCE_OFFICER', 'RISK_MANAGER', 'ADMIN'];
const TABS = ['ALL', 'PENDING', 'EXECUTED', 'CANCELLED', 'REJECTED'];

export default function Orders() {
  const { user } = useAuth();
  const toast = useToast();
  const canSeeBook = BOOK_ROLES.includes(user.role);

  const [mode, setMode] = useState(canSeeBook ? 'book' : 'mine');
  const [clientId, setClientId] = useState(user.id);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('ALL');
  const [cancellingId, setCancellingId] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    const req = mode === 'book' ? ordersApi.book() : ordersApi.byClient(clientId);
    req
      .then((data) => setOrders([...data].reverse()))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [mode, clientId]);

  useEffect(() => {
    load();
  }, [load]);

  async function cancelOrder(id) {
    setCancellingId(id);
    try {
      await ordersApi.cancel(id);
      toast.success('Order cancelled.');
      load();
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not cancel this order.'));
    } finally {
      setCancellingId(null);
    }
  }

  function exportCsv() {
    const header = ['ID', 'Client', 'Symbol', 'Side', 'Type', 'Qty', 'Price', 'Value', 'Status', 'PlacedAt'];
    const rows = orders.map((o) => [o.id, o.clientName || o.clientId, o.stockSymbol, o.orderSide, o.orderType, o.quantity, o.price ?? '', o.totalAmount, o.status, o.placedAt]);
    const csv = [header, ...rows].map((r) => r.join(',')).join('\n');
    downloadBlob(new Blob([csv], { type: 'text/csv' }), 'orders.csv');
  }

  const filtered = tab === 'ALL' ? orders : orders.filter((o) => o.status === tab);
  const counts = TABS.reduce((acc, t) => {
    acc[t] = t === 'ALL' ? orders.length : orders.filter((o) => o.status === t).length;
    return acc;
  }, {});

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div className="tabs">
          {TABS.map((t) => (
            <button key={t} className={`tab ${tab === t ? 'tab-active' : ''}`} onClick={() => setTab(t)}>
              {t} {counts[t] > 0 && `(${counts[t]})`}
            </button>
          ))}
        </div>
        <div className="flex gap-2 items-center">
          {canSeeBook && (
            <>
              <button className={`btn btn-sm ${mode === 'book' ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setMode('book')}>
                Order Book
              </button>
              <button className={`btn btn-sm ${mode === 'mine' ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setMode('mine')}>
                By Client
              </button>
            </>
          )}
          {mode === 'mine' && canSeeBook && (
            <input type="number" style={{ width: 90 }} value={clientId} onChange={(e) => setClientId(Number(e.target.value))} />
          )}
          <button className="btn btn-ghost btn-sm" onClick={load}>Refresh</button>
          <button className="btn btn-ghost btn-sm" onClick={exportCsv} disabled={orders.length === 0}>Export</button>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <Loader inline />
        ) : filtered.length === 0 ? (
          <div className="empty-state">No orders found.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  {mode === 'book' && <th>Client</th>}
                  <th>Symbol</th>
                  <th>Side</th>
                  <th>Type</th>
                  <th>Qty</th>
                  <th>Price</th>
                  <th>Value</th>
                  <th>Status</th>
                  <th>Placed</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((o) => (
                  <tr key={o.id}>
                    <td className="text-faint">#{o.id}</td>
                    {mode === 'book' && <td>{o.clientName} <span className="text-faint">#{o.clientId}</span></td>}
                    <td style={{ fontWeight: 600 }}>{o.stockSymbol}</td>
                    <td><Badge tone={o.orderSide === 'BUY' ? 'green' : 'red'}>{o.orderSide}</Badge></td>
                    <td>{o.orderType.replace('_', ' ')}</td>
                    <td>{o.quantity}</td>
                    <td>{o.price ? formatCurrency(o.price) : 'Market'}</td>
                    <td>{formatCurrency(o.totalAmount)}</td>
                    <td><Badge>{o.status}</Badge></td>
                    <td className="text-faint">{formatDateTime(o.placedAt)}</td>
                    <td>
                      {o.status === 'PENDING' && (
                        <button className="btn btn-outline-danger btn-sm" disabled={cancellingId === o.id} onClick={() => cancelOrder(o.id)}>
                          {cancellingId === o.id ? '...' : 'Cancel'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
