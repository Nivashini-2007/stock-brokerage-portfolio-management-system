import { useCallback, useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { ordersApi } from '../api/orders';
import { portfolioApi } from '../api/portfolio';
import Loader from '../components/common/Loader';
import StatCard from '../components/common/StatCard';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatPercent, formatSigned } from '../utils/format';

export default function Positions() {
  const { user } = useAuth();
  const toast = useToast();
  const isStaff = user.role !== 'CLIENT';

  const [clientId, setClientId] = useState(user.id);
  const [holdings, setHoldings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [squaringOff, setSquaringOff] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    portfolioApi
      .holdings(clientId)
      .then((data) => setHoldings(data.filter((h) => h.quantity > 0)))
      .catch(() => setHoldings([]))
      .finally(() => setLoading(false));
  }, [clientId]);

  useEffect(() => {
    load();
  }, [load]);

  async function squareOff(h) {
    if (!window.confirm(`Square off ${h.quantity} shares of ${h.stockSymbol} at market price?`)) return;
    setSquaringOff(h.id);
    try {
      await ordersApi.place({
        clientId: Number(clientId),
        stockSymbol: h.stockSymbol,
        companyName: h.companyName,
        orderType: 'MARKET',
        orderSide: 'SELL',
        quantity: h.quantity,
      });
      toast.success(`Square-off order placed for ${h.stockSymbol}.`);
      load();
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not place square-off order.'));
    } finally {
      setSquaringOff(null);
    }
  }

  const totalPnl = holdings.reduce((s, h) => s + (h.profitLoss || 0), 0);
  const profitCount = holdings.filter((h) => h.profitLoss > 0).length;
  const lossCount = holdings.filter((h) => h.profitLoss < 0).length;

  return (
    <div>
      {isStaff && (
        <div className="flex gap-2 items-center mb-2">
          <label style={{ margin: 0 }}>Client ID</label>
          <input type="number" style={{ width: 100 }} value={clientId} onChange={(e) => setClientId(Number(e.target.value))} />
        </div>
      )}

      <div className="grid grid-cols-4 mb-2">
        <StatCard label="Total P&L" value={formatSigned(totalPnl)} hintTone={totalPnl >= 0 ? 'up' : 'down'} />
        <StatCard label="Open Positions" value={holdings.length} />
        <StatCard label="Profit Positions" value={profitCount} />
        <StatCard label="Loss Positions" value={lossCount} />
      </div>

      <div className="card">
        <div className="card-title">Open Positions</div>
        <div className="card-subtitle">Square off closes the position with a market order.</div>
        {loading ? (
          <Loader inline />
        ) : holdings.length === 0 ? (
          <div className="empty-state">No open positions.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Symbol</th><th>Qty</th><th>Avg Price</th><th>LTP</th><th>P&amp;L</th><th>P&amp;L %</th><th></th></tr>
              </thead>
              <tbody>
                {holdings.map((h) => {
                  const invested = h.averageBuyPrice * h.quantity;
                  const pct = invested ? (h.profitLoss / invested) * 100 : 0;
                  return (
                    <tr key={h.id}>
                      <td style={{ fontWeight: 600 }}>{h.stockSymbol}</td>
                      <td>{h.quantity}</td>
                      <td>{formatCurrency(h.averageBuyPrice)}</td>
                      <td>{formatCurrency(h.currentPrice)}</td>
                      <td className={h.profitLoss >= 0 ? 'up' : 'down'}>{formatSigned(h.profitLoss)}</td>
                      <td className={pct >= 0 ? 'up' : 'down'}>{formatPercent(pct)}</td>
                      <td>
                        <button className="btn btn-outline-danger btn-sm" disabled={squaringOff === h.id} onClick={() => squareOff(h)}>
                          {squaringOff === h.id ? '...' : 'Square Off'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
