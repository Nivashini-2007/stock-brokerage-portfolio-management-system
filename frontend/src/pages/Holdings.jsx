import { useEffect, useMemo, useState } from 'react';
import { downloadBlob } from '../api/client';
import { portfolioApi } from '../api/portfolio';
import Loader from '../components/common/Loader';
import { useAuth } from '../context/AuthContext';
import { formatCurrency, formatPercent, formatSigned } from '../utils/format';

const STAFF_ROLES = ['DEALER', 'COMPLIANCE_OFFICER', 'RISK_MANAGER', 'ADMIN'];

export default function Holdings() {
  const { user } = useAuth();
  const isStaff = STAFF_ROLES.includes(user.role);

  const [clientId, setClientId] = useState(user.id);
  const [holdings, setHoldings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [sortKey, setSortKey] = useState('marketValue');
  const [sortDir, setSortDir] = useState('desc');

  useEffect(() => {
    setLoading(true);
    portfolioApi
      .holdings(clientId)
      .then(setHoldings)
      .catch(() => setHoldings([]))
      .finally(() => setLoading(false));
  }, [clientId]);

  const filtered = useMemo(() => {
    let rows = holdings.filter(
      (h) =>
        h.stockSymbol.toLowerCase().includes(search.toLowerCase()) ||
        h.companyName.toLowerCase().includes(search.toLowerCase())
    );
    rows = [...rows].sort((a, b) => {
      const dir = sortDir === 'asc' ? 1 : -1;
      return (a[sortKey] > b[sortKey] ? 1 : -1) * dir;
    });
    return rows;
  }, [holdings, search, sortKey, sortDir]);

  function toggleSort(key) {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('desc');
    }
  }

  function exportCsv() {
    const header = ['Symbol', 'Company', 'Quantity', 'AvgCost', 'LTP', 'Invested', 'Value', 'PnL'];
    const rows = filtered.map((h) => [
      h.stockSymbol,
      h.companyName,
      h.quantity,
      h.averageBuyPrice,
      h.currentPrice,
      (h.averageBuyPrice * h.quantity).toFixed(2),
      h.marketValue,
      h.profitLoss,
    ]);
    const csv = [header, ...rows].map((r) => r.join(',')).join('\n');
    downloadBlob(new Blob([csv], { type: 'text/csv' }), `holdings-client-${clientId}.csv`);
  }

  const totalValue = filtered.reduce((sum, h) => sum + (h.marketValue || 0), 0);
  const totalInvested = filtered.reduce((sum, h) => sum + h.averageBuyPrice * h.quantity, 0);

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div className="flex gap-2 items-center">
          <input placeholder="Search holdings..." value={search} onChange={(e) => setSearch(e.target.value)} style={{ width: 220 }} />
          {isStaff && (
            <>
              <label style={{ margin: 0 }}>Client ID</label>
              <input type="number" style={{ width: 100 }} value={clientId} onChange={(e) => setClientId(Number(e.target.value))} />
            </>
          )}
        </div>
        <button className="btn btn-ghost btn-sm" onClick={exportCsv} disabled={filtered.length === 0}>
          Export CSV
        </button>
      </div>

      <div className="card">
        {loading ? (
          <Loader inline />
        ) : filtered.length === 0 ? (
          <div className="empty-state">No holdings found.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th onClick={() => toggleSort('stockSymbol')} style={{ cursor: 'pointer' }}>Asset</th>
                  <th onClick={() => toggleSort('quantity')} style={{ cursor: 'pointer' }}>Shares</th>
                  <th onClick={() => toggleSort('averageBuyPrice')} style={{ cursor: 'pointer' }}>Avg Cost</th>
                  <th onClick={() => toggleSort('currentPrice')} style={{ cursor: 'pointer' }}>LTP</th>
                  <th>Invested</th>
                  <th onClick={() => toggleSort('marketValue')} style={{ cursor: 'pointer' }}>Value ▾</th>
                  <th onClick={() => toggleSort('profitLoss')} style={{ cursor: 'pointer' }}>Gain/Loss</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((h) => {
                  const invested = h.averageBuyPrice * h.quantity;
                  const pct = invested ? (h.profitLoss / invested) * 100 : 0;
                  return (
                    <tr key={h.id}>
                      <td>
                        <div style={{ fontWeight: 700 }}>{h.stockSymbol}</div>
                        <div className="text-faint" style={{ fontSize: 11.5 }}>{h.companyName}</div>
                      </td>
                      <td>{h.quantity}</td>
                      <td>{formatCurrency(h.averageBuyPrice)}</td>
                      <td>{formatCurrency(h.currentPrice)}</td>
                      <td>{formatCurrency(invested)}</td>
                      <td style={{ fontWeight: 700 }}>{formatCurrency(h.marketValue)}</td>
                      <td className={h.profitLoss >= 0 ? 'up' : 'down'}>
                        {formatSigned(h.profitLoss)}
                        <div style={{ fontSize: 11 }}>{formatPercent(pct)}</div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
              <tfoot>
                <tr>
                  <td colSpan={4}></td>
                  <td style={{ fontWeight: 700 }}>{formatCurrency(totalInvested)}</td>
                  <td style={{ fontWeight: 700 }}>{formatCurrency(totalValue)}</td>
                  <td className={totalValue - totalInvested >= 0 ? 'up' : 'down'} style={{ fontWeight: 700 }}>
                    {formatSigned(totalValue - totalInvested)}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
