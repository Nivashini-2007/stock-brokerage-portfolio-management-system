import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { KNOWN_SYMBOLS, marketApi } from '../api/market';
import Loader from '../components/common/Loader';
import { formatCurrency, formatNumber } from '../utils/format';

const STORAGE_KEY = 'tf_watchlist';

function loadSymbols() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : KNOWN_SYMBOLS;
  } catch {
    return KNOWN_SYMBOLS;
  }
}

export default function Watchlist() {
  const navigate = useNavigate();
  const [symbols, setSymbols] = useState(loadSymbols);
  const [newSymbol, setNewSymbol] = useState('');
  const [quotes, setQuotes] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(symbols));
  }, [symbols]);

  useEffect(() => {
    let cancelled = false;

    async function refresh() {
      const results = await Promise.all(
        symbols.map((s) =>
          marketApi
            .quote(s)
            .then((q) => [s, q])
            .catch(() => [s, null])
        )
      );
      if (!cancelled) {
        setQuotes(Object.fromEntries(results));
        setLoading(false);
      }
    }

    refresh();
    const id = setInterval(refresh, 10000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [symbols]);

  function addSymbol(e) {
    e.preventDefault();
    const s = newSymbol.trim().toUpperCase();
    if (s && !symbols.includes(s)) setSymbols((prev) => [...prev, s]);
    setNewSymbol('');
  }

  function removeSymbol(s) {
    setSymbols((prev) => prev.filter((x) => x !== s));
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div className="text-faint">Saved locally on this device.</div>
        <form onSubmit={addSymbol} className="flex gap-1">
          <input placeholder="Add symbol e.g. TCS" value={newSymbol} onChange={(e) => setNewSymbol(e.target.value)} style={{ width: 180 }} />
          <button className="btn btn-primary btn-sm" type="submit">Add</button>
        </form>
      </div>

      <div className="card">
        {loading ? (
          <Loader inline />
        ) : symbols.length === 0 ? (
          <div className="empty-state">Your watchlist is empty. Add a symbol above.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Symbol</th><th>LTP</th><th>Open</th><th>High</th><th>Low</th><th>Volume</th><th></th></tr>
              </thead>
              <tbody>
                {symbols.map((s) => {
                  const q = quotes[s];
                  return (
                    <tr key={s} style={{ cursor: 'pointer' }}>
                      <td onClick={() => navigate(`/terminal?symbol=${s}`)} style={{ fontWeight: 700 }}>
                        {s}
                        {q?.circuitHalted && <span className="badge badge-red" style={{ marginLeft: 8 }}>HALT</span>}
                      </td>
                      <td>{q ? formatCurrency(q.currentPrice) : '--'}</td>
                      <td>{q ? formatCurrency(q.openPrice) : '--'}</td>
                      <td>{q ? formatCurrency(q.highPrice) : '--'}</td>
                      <td>{q ? formatCurrency(q.lowPrice) : '--'}</td>
                      <td>{q ? formatNumber(q.volume) : '--'}</td>
                      <td>
                        <button className="btn btn-ghost btn-sm" onClick={() => removeSymbol(s)}>Remove</button>
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
