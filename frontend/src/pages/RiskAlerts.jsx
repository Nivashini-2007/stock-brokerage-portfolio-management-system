import { useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { marginApi } from '../api/margin';
import { KNOWN_SYMBOLS, marketApi } from '../api/market';
import { riskApi } from '../api/risk';
import Badge from '../components/common/Badge';
import Loader from '../components/common/Loader';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDate } from '../utils/format';

export default function RiskAlerts() {
  const toast = useToast();
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const [lookupId, setLookupId] = useState('');
  const [marginInfo, setMarginInfo] = useState(null);
  const [lookupError, setLookupError] = useState('');
  const [lookupLoading, setLookupLoading] = useState(false);

  const [symbol, setSymbol] = useState(KNOWN_SYMBOLS[0]);
  const [halted, setHalted] = useState(false);
  const [haltBusy, setHaltBusy] = useState(false);

  useEffect(() => {
    riskApi.all().then(setAlerts).catch(() => setAlerts([])).finally(() => setLoading(false));
  }, []);

  async function lookupMargin(e) {
    e.preventDefault();
    if (!lookupId) return;
    setLookupLoading(true);
    setLookupError('');
    setMarginInfo(null);
    try {
      const m = await marginApi.get(lookupId);
      setMarginInfo(m);
    } catch (err) {
      setLookupError(apiErrorMessage(err, 'No margin record found for this client.'));
    } finally {
      setLookupLoading(false);
    }
  }

  async function toggleHalt() {
    setHaltBusy(true);
    try {
      const res = await marketApi.setCircuitHalt(symbol, !halted);
      setHalted(res.circuitHalted);
      toast.success(`${symbol} circuit ${res.circuitHalted ? 'halted' : 'resumed'}.`);
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not update circuit status.'));
    } finally {
      setHaltBusy(false);
    }
  }

  return (
    <div>
      <div className="grid grid-cols-2 mb-2">
        <div className="card">
          <div className="card-title">Margin Lookup</div>
          <div className="card-subtitle">Enter a client ID to check margin utilisation.</div>
          <form onSubmit={lookupMargin} className="flex gap-2 mb-2">
            <input type="number" placeholder="Client ID" value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
            <button className="btn btn-primary btn-sm" disabled={lookupLoading}>{lookupLoading ? '...' : 'Lookup'}</button>
          </form>
          {lookupError && <div className="form-error">{lookupError}</div>}
          {marginInfo && (
            <div className="flex justify-between" style={{ fontSize: 13 }}>
              <div>
                <div className="text-faint">Available</div>
                <div style={{ fontWeight: 700 }}>{formatCurrency(marginInfo.availableMargin)}</div>
              </div>
              <div>
                <div className="text-faint">Used</div>
                <div style={{ fontWeight: 700 }}>{formatCurrency(marginInfo.usedMargin)}</div>
              </div>
              <div>
                <div className="text-faint">Utilisation</div>
                <div style={{ fontWeight: 700 }} className={marginInfo.utilizationPercentage >= 80 ? 'down' : ''}>
                  {marginInfo.utilizationPercentage?.toFixed(1)}%
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Circuit Breaker Control</div>
          <div className="card-subtitle">Halt or resume trading on a symbol.</div>
          <div className="flex gap-2 items-center">
            <select value={symbol} onChange={(e) => setSymbol(e.target.value)}>
              {KNOWN_SYMBOLS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <button className={`btn btn-sm ${halted ? 'btn-success' : 'btn-danger'}`} disabled={haltBusy} onClick={toggleHalt}>
              {haltBusy ? '...' : halted ? 'Resume Trading' : 'Halt Trading'}
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Active Risk Alerts</div>
        {loading ? (
          <Loader inline />
        ) : alerts.length === 0 ? (
          <div className="empty-state">No active risk alerts.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Client</th><th>Severity</th><th>Title</th><th>Description</th><th>Date</th></tr>
              </thead>
              <tbody>
                {alerts.map((a) => (
                  <tr key={a.id}>
                    <td>#{a.clientId}</td>
                    <td><Badge tone={a.severity?.toUpperCase() === 'HIGH' ? 'red' : a.severity?.toUpperCase() === 'MEDIUM' ? 'amber' : 'blue'}>{a.severity}</Badge></td>
                    <td style={{ fontWeight: 600 }}>{a.title}</td>
                    <td className="text-dim">{a.description}</td>
                    <td className="text-faint">{formatDate(a.createdDate)}</td>
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
