import { useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { KNOWN_SYMBOLS, marketApi } from '../api/market';
import Badge from '../components/common/Badge';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDateTime } from '../utils/format';

export default function MarketAdmin() {
  const toast = useToast();
  const [quotes, setQuotes] = useState([]);
  const [knownSymbols, setKnownSymbols] = useState(KNOWN_SYMBOLS);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({ symbol: '', companyName: '', price: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  function loadQuotes(symbols) {
    setLoading(true);
    Promise.all(symbols.map((s) => marketApi.quote(s).catch(() => null)))
      .then((results) => setQuotes(results.filter(Boolean)))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadQuotes(knownSymbols);
  }, [knownSymbols]);

  async function submit(e) {
    e.preventDefault();
    setError('');
    if (!form.symbol || !form.companyName || !form.price) {
      setError('All fields are required.');
      return;
    }
    setSubmitting(true);
    try {
      const symbol = form.symbol.toUpperCase();
      await marketApi.createOrUpdateStock({ symbol, companyName: form.companyName, price: Number(form.price) });
      toast.success(`${symbol} saved.`);
      setKnownSymbols((prev) => (prev.includes(symbol) ? prev : [...prev, symbol]));
      loadQuotes(knownSymbols.includes(symbol) ? knownSymbols : [...knownSymbols, symbol]);
      setForm({ symbol: '', companyName: '', price: '' });
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not save this stock.'));
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleHalt(symbol, currentlyHalted) {
    try {
      await marketApi.setCircuitHalt(symbol, !currentlyHalted);
      toast.success(`${symbol} ${!currentlyHalted ? 'halted' : 'resumed'}.`);
      loadQuotes(knownSymbols);
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not update circuit status.'));
    }
  }

  return (
    <div className="grid" style={{ gridTemplateColumns: '1fr 340px', gap: 16, alignItems: 'start' }}>
      <div className="card">
        <div className="card-title">Tradable Instruments</div>
        <div className="card-subtitle">Live quotes for stocks currently listed on the platform.</div>
        {loading ? (
          <div className="empty-state">Loading...</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Symbol</th><th>Company</th><th>LTP</th><th>Open</th><th>High</th><th>Low</th><th>Status</th><th>Updated</th><th></th></tr>
              </thead>
              <tbody>
                {quotes.map((q) => (
                  <tr key={q.symbol}>
                    <td style={{ fontWeight: 700 }}>{q.symbol}</td>
                    <td className="text-dim">{q.companyName}</td>
                    <td>{formatCurrency(q.currentPrice)}</td>
                    <td>{formatCurrency(q.openPrice)}</td>
                    <td>{formatCurrency(q.highPrice)}</td>
                    <td>{formatCurrency(q.lowPrice)}</td>
                    <td>{q.circuitHalted ? <Badge tone="red">Halted</Badge> : <Badge tone="green">Active</Badge>}</td>
                    <td className="text-faint">{formatDateTime(q.lastUpdated)}</td>
                    <td>
                      <button className="btn btn-ghost btn-sm" onClick={() => toggleHalt(q.symbol, q.circuitHalted)}>
                        {q.circuitHalted ? 'Resume' : 'Halt'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card">
        <div className="card-title">Create / Update Stock</div>
        <div className="card-subtitle">No live NSE/BSE feed is connected — prices are set manually.</div>
        <form onSubmit={submit}>
          <div className="field">
            <label>Symbol</label>
            <input value={form.symbol} onChange={(e) => setForm((f) => ({ ...f, symbol: e.target.value }))} placeholder="e.g. WIPRO" />
          </div>
          <div className="field">
            <label>Company Name</label>
            <input value={form.companyName} onChange={(e) => setForm((f) => ({ ...f, companyName: e.target.value }))} placeholder="e.g. Wipro Ltd" />
          </div>
          <div className="field">
            <label>Price</label>
            <input type="number" step="0.01" value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} placeholder="0.00" />
          </div>
          {error && <div className="form-error mb-2">{error}</div>}
          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? 'Saving...' : 'Save Stock'}
          </button>
        </form>
      </div>
    </div>
  );
}
