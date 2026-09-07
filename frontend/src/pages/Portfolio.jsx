import { useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { portfolioApi } from '../api/portfolio';
import Badge from '../components/common/Badge';
import Loader from '../components/common/Loader';
import StatCard from '../components/common/StatCard';
import { useAuth } from '../context/AuthContext';
import { formatCurrency, formatDateTime, formatPercent, formatSigned } from '../utils/format';

const STAFF_ROLES = ['DEALER', 'COMPLIANCE_OFFICER', 'RISK_MANAGER', 'ADMIN'];
const CURRENT_YEAR = new Date().getFullYear();

export default function Portfolio() {
  const { user } = useAuth();
  const isStaff = STAFF_ROLES.includes(user.role);

  const [clientId, setClientId] = useState(user.id);
  const [viewAll, setViewAll] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [performance, setPerformance] = useState(null);
  const [holdings, setHoldings] = useState([]);
  const [allHoldings, setAllHoldings] = useState([]);

  const [year, setYear] = useState(CURRENT_YEAR);
  const [taxReport, setTaxReport] = useState(null);
  const [taxLoading, setTaxLoading] = useState(false);
  const [taxError, setTaxError] = useState('');

  useEffect(() => {
    if (viewAll) {
      setLoading(true);
      portfolioApi.all().then(setAllHoldings).catch((e) => setError(apiErrorMessage(e))).finally(() => setLoading(false));
      return;
    }
    setLoading(true);
    setError('');
    Promise.all([portfolioApi.performance(clientId), portfolioApi.holdings(clientId)])
      .then(([perf, hold]) => {
        setPerformance(perf);
        setHoldings(hold);
      })
      .catch((e) => setError(apiErrorMessage(e, 'Could not load portfolio for this client.')))
      .finally(() => setLoading(false));
  }, [clientId, viewAll]);

  async function generateTaxReport() {
    setTaxLoading(true);
    setTaxError('');
    try {
      const report = await portfolioApi.taxReport(clientId, year);
      setTaxReport(report);
    } catch (e) {
      setTaxError(apiErrorMessage(e, 'Could not generate the tax report.'));
    } finally {
      setTaxLoading(false);
    }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div className="flex gap-2 items-center">
          {isStaff && (
            <>
              <label style={{ margin: 0 }}>Client ID</label>
              <input type="number" style={{ width: 100 }} value={clientId} disabled={viewAll} onChange={(e) => setClientId(Number(e.target.value))} />
              <button className={`btn btn-sm ${viewAll ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setViewAll((v) => !v)}>
                {viewAll ? 'Viewing All Clients' : 'View All Clients'}
              </button>
            </>
          )}
        </div>
      </div>

      {loading ? (
        <Loader full />
      ) : viewAll ? (
        <div className="card">
          <div className="card-title">All Client Holdings</div>
          {allHoldings.length === 0 ? (
            <div className="empty-state">No holdings recorded across any client accounts yet.</div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Client</th><th>Symbol</th><th>Qty</th><th>Avg Cost</th><th>LTP</th><th>Value</th><th>P&amp;L</th></tr>
                </thead>
                <tbody>
                  {allHoldings.map((h) => (
                    <tr key={h.id}>
                      <td>{h.clientName} <span className="text-faint">#{h.clientId}</span></td>
                      <td>{h.stockSymbol}</td>
                      <td>{h.quantity}</td>
                      <td>{formatCurrency(h.averageBuyPrice)}</td>
                      <td>{formatCurrency(h.currentPrice)}</td>
                      <td>{formatCurrency(h.marketValue)}</td>
                      <td className={h.profitLoss >= 0 ? 'up' : 'down'}>{formatSigned(h.profitLoss)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      ) : error ? (
        <div className="card empty-state">{error}</div>
      ) : (
        <>
          <div className="grid grid-cols-4 mb-2">
            <StatCard label="Current Value" value={formatCurrency(performance?.currentValue)} />
            <StatCard
              label="Total Gain/Loss"
              value={formatSigned(performance?.totalProfitLoss)}
              hint={formatPercent(performance?.profitLossPercentage)}
              hintTone={performance?.totalProfitLoss >= 0 ? 'up' : 'down'}
            />
            <StatCard label="Cost Basis" value={formatCurrency(performance?.totalInvestment)} />
            <StatCard label="Holdings" value={performance?.totalStocks ?? 0} />
          </div>

          <div className="card mb-2">
            <div className="card-title">Holdings</div>
            {holdings.length === 0 ? (
              <div className="empty-state">No holdings yet.</div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Symbol</th><th>Qty</th><th>Avg Cost</th><th>LTP</th><th>Value</th><th>P&amp;L</th></tr>
                  </thead>
                  <tbody>
                    {holdings.map((h) => (
                      <tr key={h.id}>
                        <td>
                          <div style={{ fontWeight: 600 }}>{h.stockSymbol}</div>
                          <div className="text-faint" style={{ fontSize: 11.5 }}>{h.companyName}</div>
                        </td>
                        <td>{h.quantity}</td>
                        <td>{formatCurrency(h.averageBuyPrice)}</td>
                        <td>{formatCurrency(h.currentPrice)}</td>
                        <td>{formatCurrency(h.marketValue)}</td>
                        <td className={h.profitLoss >= 0 ? 'up' : 'down'}>{formatSigned(h.profitLoss)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="card">
            <div className="flex justify-between items-center mb-2">
              <div>
                <div className="card-title" style={{ marginBottom: 0 }}>Annual Tax Report</div>
                <div className="card-subtitle" style={{ marginBottom: 0 }}>FIFO capital gains — LTCG/STCG classification</div>
              </div>
              <div className="flex gap-2">
                <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
                  {[CURRENT_YEAR, CURRENT_YEAR - 1, CURRENT_YEAR - 2].map((y) => <option key={y} value={y}>{y}</option>)}
                </select>
                <button className="btn btn-primary btn-sm" onClick={generateTaxReport} disabled={taxLoading}>
                  {taxLoading ? 'Generating...' : 'Generate'}
                </button>
              </div>
            </div>

            {taxError && <div className="form-error mb-2">{taxError}</div>}

            {taxReport && (
              <div>
                <div className="grid grid-cols-4 mb-2">
                  <StatCard label="Realised P&L" value={formatSigned(taxReport.totalRealizedProfit)} />
                  <StatCard label="Short-Term Gain" value={formatSigned(taxReport.shortTermGain)} />
                  <StatCard label="Long-Term Gain" value={formatSigned(taxReport.longTermGain)} />
                  <StatCard label="Est. Total Tax" value={formatCurrency(taxReport.totalEstimatedTax)} />
                </div>
                {taxReport.realizedGains?.length > 0 ? (
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr><th>Symbol</th><th>Qty</th><th>Buy Price</th><th>Sell Price</th><th>Sell Date</th><th>Type</th><th>Gain</th></tr>
                      </thead>
                      <tbody>
                        {taxReport.realizedGains.map((g, i) => (
                          <tr key={i}>
                            <td>{g.symbol}</td>
                            <td>{g.quantity}</td>
                            <td>{formatCurrency(g.buyPrice)}</td>
                            <td>{formatCurrency(g.sellPrice)}</td>
                            <td>{formatDateTime(g.sellDate)}</td>
                            <td><Badge tone={g.gainType === 'LTCG' ? 'blue' : 'amber'}>{g.gainType}</Badge></td>
                            <td className={g.gainAmount >= 0 ? 'up' : 'down'}>{formatSigned(g.gainAmount)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="empty-state">No closed positions for {year}.</div>
                )}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
