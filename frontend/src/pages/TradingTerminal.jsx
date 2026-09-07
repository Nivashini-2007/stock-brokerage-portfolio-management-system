import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { apiErrorMessage } from '../api/client';
import { KNOWN_SYMBOLS, marketApi } from '../api/market';
import { ordersApi } from '../api/orders';
import Badge from '../components/common/Badge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDateTime, formatNumber } from '../utils/format';

const ORDER_TYPES = ['MARKET', 'LIMIT', 'STOP_LOSS', 'BRACKET', 'COVER'];

export default function TradingTerminal() {
  const { user } = useAuth();
  const toast = useToast();
  const [params] = useSearchParams();

  const [symbol, setSymbol] = useState(params.get('symbol') || 'RELIANCE');
  const [symbolInput, setSymbolInput] = useState('');
  const [quote, setQuote] = useState(null);
  const [chart, setChart] = useState([]);
  const [quoteError, setQuoteError] = useState('');

  const [side, setSide] = useState('BUY');
  const [orderType, setOrderType] = useState('MARKET');
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState('');
  const [triggerPrice, setTriggerPrice] = useState('');
  const [targetPrice, setTargetPrice] = useState('');
  const [clientId, setClientId] = useState(user.id);
  const [placing, setPlacing] = useState(false);
  const [formError, setFormError] = useState('');

  const [myOrders, setMyOrders] = useState([]);

  const loadQuote = useCallback(async (sym) => {
    try {
      const [q, h] = await Promise.all([marketApi.quote(sym), marketApi.chart(sym)]);
      setQuote(q);
      setChart(h);
      setQuoteError('');
    } catch (err) {
      setQuote(null);
      setQuoteError(apiErrorMessage(err, `No market data found for ${sym}.`));
    }
  }, []);

  useEffect(() => {
    loadQuote(symbol);
    const id = setInterval(() => loadQuote(symbol), 8000);
    return () => clearInterval(id);
  }, [symbol, loadQuote]);

  const loadMyOrders = useCallback(async () => {
    try {
      const list = await ordersApi.byClient(clientId);
      setMyOrders(list.slice(-8).reverse());
    } catch {
      setMyOrders([]);
    }
  }, [clientId]);

  useEffect(() => {
    loadMyOrders();
  }, [loadMyOrders]);

  function pickSymbol(s) {
    setSymbol(s);
  }

  function onSymbolSubmit(e) {
    e.preventDefault();
    const s = symbolInput.trim().toUpperCase();
    if (s) {
      setSymbol(s);
      setSymbolInput('');
    }
  }

  const estValue = quote ? (Number(price) || quote.currentPrice) * (Number(quantity) || 0) : 0;

  async function placeOrder(e) {
    e.preventDefault();
    setFormError('');

    if (!quote) {
      setFormError('Load a valid quote before placing an order.');
      return;
    }
    if (quote.circuitHalted) {
      setFormError(`${symbol} is currently circuit-halted. New orders are rejected.`);
      return;
    }

    const payload = {
      clientId: Number(clientId),
      stockSymbol: symbol,
      companyName: quote.companyName,
      orderType,
      orderSide: side,
      quantity: Number(quantity),
    };
    if (['LIMIT', 'BRACKET'].includes(orderType)) payload.price = Number(price);
    if (['STOP_LOSS', 'BRACKET', 'COVER'].includes(orderType)) payload.triggerPrice = Number(triggerPrice);
    if (orderType === 'BRACKET') payload.targetPrice = Number(targetPrice);

    setPlacing(true);
    try {
      await ordersApi.place(payload);
      toast.success(`${side} order for ${quantity} ${symbol} placed successfully.`);
      loadMyOrders();
    } catch (err) {
      setFormError(apiErrorMessage(err, 'Order could not be placed.'));
    } finally {
      setPlacing(false);
    }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div className="flex gap-2 items-center">
          {quote?.circuitHalted && <Badge tone="red">Circuit Halted</Badge>}
        </div>
        <form onSubmit={onSymbolSubmit} className="flex gap-1">
          <input placeholder="Jump to symbol..." value={symbolInput} onChange={(e) => setSymbolInput(e.target.value)} style={{ width: 180 }} />
          <button className="btn btn-ghost btn-sm" type="submit">Go</button>
        </form>
      </div>

      <div className="grid" style={{ gridTemplateColumns: '200px 1fr 320px', gap: 16, alignItems: 'start' }}>
        <div className="card" style={{ padding: 10 }}>
          <div className="card-title" style={{ padding: '4px 6px' }}>Symbols</div>
          {KNOWN_SYMBOLS.map((s) => (
            <div
              key={s}
              onClick={() => pickSymbol(s)}
              style={{
                padding: '9px 10px',
                borderRadius: 8,
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: 13,
                background: s === symbol ? 'var(--accent-soft)' : 'transparent',
                color: s === symbol ? 'var(--accent)' : 'var(--text)',
              }}
            >
              {s}
            </div>
          ))}
        </div>

        <div>
          <div className="card mb-2">
            {quoteError ? (
              <div className="empty-state">{quoteError}</div>
            ) : quote ? (
              <>
                <div className="flex justify-between items-center">
                  <div>
                    <h2>{quote.symbol}</h2>
                    <div className="text-faint">{quote.companyName}</div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: 26, fontWeight: 700 }}>{formatCurrency(quote.currentPrice)}</div>
                    <div className="text-faint" style={{ fontSize: 12 }}>Updated {formatDateTime(quote.lastUpdated)}</div>
                  </div>
                </div>
                <div className="flex gap-3 mt-2 text-dim" style={{ fontSize: 12.5 }}>
                  <span>Open {formatCurrency(quote.openPrice)}</span>
                  <span>High {formatCurrency(quote.highPrice)}</span>
                  <span>Low {formatCurrency(quote.lowPrice)}</span>
                  <span>Vol {formatNumber(quote.volume)}</span>
                </div>
                {chart.length > 1 && (
                  <ResponsiveContainer width="100%" height={220} style={{ marginTop: 16 }}>
                    <LineChart data={chart}>
                      <XAxis dataKey="tradingDate" hide />
                      <YAxis domain={['auto', 'auto']} hide />
                      <Tooltip
                        formatter={(v) => formatCurrency(v)}
                        contentStyle={{ background: '#171f33', border: '1px solid #232c42' }}
                      />
                      <Line type="monotone" dataKey="closePrice" stroke="#3b82f6" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </>
            ) : (
              <div className="empty-state">Loading quote...</div>
            )}
          </div>

          <div className="card">
            <div className="card-title">My Recent Orders</div>
            {myOrders.length === 0 ? (
              <div className="empty-state">No orders yet for this client.</div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Symbol</th><th>Side</th><th>Type</th><th>Qty</th><th>Value</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {myOrders.map((o) => (
                      <tr key={o.id}>
                        <td>{o.stockSymbol}</td>
                        <td><Badge tone={o.orderSide === 'BUY' ? 'green' : 'red'}>{o.orderSide}</Badge></td>
                        <td>{o.orderType}</td>
                        <td>{o.quantity}</td>
                        <td>{formatCurrency(o.totalAmount)}</td>
                        <td><Badge>{o.status}</Badge></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="tabs mb-2">
            <button type="button" className={`tab ${side === 'BUY' ? 'tab-active' : ''}`} onClick={() => setSide('BUY')} style={side === 'BUY' ? { background: 'var(--green)' } : {}}>
              BUY
            </button>
            <button type="button" className={`tab ${side === 'SELL' ? 'tab-active' : ''}`} onClick={() => setSide('SELL')} style={side === 'SELL' ? { background: 'var(--red)' } : {}}>
              SELL
            </button>
          </div>

          <form onSubmit={placeOrder}>
            {user.role !== 'CLIENT' && (
              <div className="field">
                <label>Client ID</label>
                <input type="number" value={clientId} onChange={(e) => setClientId(e.target.value)} />
              </div>
            )}

            <div className="field">
              <label>Order Type</label>
              <select value={orderType} onChange={(e) => setOrderType(e.target.value)}>
                {ORDER_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>

            <div className="field">
              <label>Quantity (Shares)</label>
              <input type="number" min={1} value={quantity} onChange={(e) => setQuantity(e.target.value)} />
            </div>

            {['LIMIT', 'BRACKET'].includes(orderType) && (
              <div className="field">
                <label>Limit Price</label>
                <input type="number" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} />
              </div>
            )}
            {['STOP_LOSS', 'BRACKET', 'COVER'].includes(orderType) && (
              <div className="field">
                <label>Trigger Price</label>
                <input type="number" step="0.01" value={triggerPrice} onChange={(e) => setTriggerPrice(e.target.value)} />
              </div>
            )}
            {orderType === 'BRACKET' && (
              <div className="field">
                <label>Target Price</label>
                <input type="number" step="0.01" value={targetPrice} onChange={(e) => setTargetPrice(e.target.value)} />
              </div>
            )}

            <div className="card" style={{ background: 'var(--bg-elevated)', padding: 12, marginBottom: 14 }}>
              <div className="flex justify-between" style={{ fontSize: 12.5 }}>
                <span className="text-faint">Market Price</span>
                <span>{quote ? formatCurrency(quote.currentPrice) : '--'}</span>
              </div>
              <div className="flex justify-between mt-1" style={{ fontSize: 12.5 }}>
                <span className="text-faint">Est. Value</span>
                <span>{formatCurrency(estValue)}</span>
              </div>
            </div>

            {formError && <div className="form-error mb-2">{formError}</div>}

            <button
              type="submit"
              className={`btn btn-block ${side === 'BUY' ? 'btn-success' : 'btn-danger'}`}
              disabled={placing || !quote}
            >
              {placing ? 'Placing...' : `Place ${side} Order`}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
