import { useCallback, useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { ledgerApi } from '../api/ledger';
import { marginApi } from '../api/margin';
import Loader from '../components/common/Loader';
import StatCard from '../components/common/StatCard';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDateTime, formatSigned } from '../utils/format';

export default function Funds() {
  const { user } = useAuth();
  const toast = useToast();
  const isStaff = user.role !== 'CLIENT';

  const [clientId, setClientId] = useState(user.id);
  const [margin, setMargin] = useState(null);
  const [ledger, setLedger] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  const [txType, setTxType] = useState('DEPOSIT');
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setLoadError('');
    Promise.all([marginApi.get(clientId), ledgerApi.history(clientId)])
      .then(([m, l]) => {
        setMargin(m);
        setLedger([...l].reverse());
      })
      .catch((e) => setLoadError(apiErrorMessage(e, 'No fund account found for this client.')))
      .finally(() => setLoading(false));
  }, [clientId]);

  useEffect(() => {
    load();
  }, [load]);

  async function submitTransfer(e) {
    e.preventDefault();
    setFormError('');
    const value = Number(amount);
    if (!value || value <= 0) {
      setFormError('Enter an amount greater than zero.');
      return;
    }
    setSubmitting(true);
    try {
      await ledgerApi.transfer({ clientId: Number(clientId), transactionType: txType, amount: value });
      toast.success(`${txType === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'} of ${formatCurrency(value)} recorded.`);
      setAmount('');
      load();
    } catch (err) {
      setFormError(apiErrorMessage(err, 'Transfer could not be processed.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      {isStaff && (
        <div className="flex gap-2 items-center mb-2">
          <label style={{ margin: 0 }}>Client ID</label>
          <input type="number" style={{ width: 100 }} value={clientId} onChange={(e) => setClientId(Number(e.target.value))} />
        </div>
      )}

      {loading ? (
        <Loader full />
      ) : loadError ? (
        <div className="card empty-state">{loadError}</div>
      ) : (
        <>
          <div className="grid grid-cols-3 mb-2">
            <StatCard label="Available Margin" value={formatCurrency(margin?.availableMargin)} />
            <StatCard label="Used Margin" value={formatCurrency(margin?.usedMargin)} />
            <StatCard label="Total Margin" value={formatCurrency(margin?.totalMargin)} hint={`${margin?.utilizationPercentage?.toFixed(1)}% utilised`} hintTone={margin?.utilizationPercentage >= 80 ? 'down' : undefined} />
          </div>

          <div className="grid grid-cols-2">
            <div className="card">
              <div className="tabs mb-2">
                <button type="button" className={`tab ${txType === 'DEPOSIT' ? 'tab-active' : ''}`} onClick={() => setTxType('DEPOSIT')}>Deposit</button>
                <button type="button" className={`tab ${txType === 'WITHDRAWAL' ? 'tab-active' : ''}`} onClick={() => setTxType('WITHDRAWAL')}>Withdraw</button>
              </div>
              <form onSubmit={submitTransfer}>
                <div className="field">
                  <label>Amount (₹)</label>
                  <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0.00" />
                </div>
                <div className="flex gap-1 mb-2">
                  {[5000, 25000, 50000, 100000].map((v) => (
                    <button type="button" key={v} className="btn btn-ghost btn-sm" onClick={() => setAmount(String(v))}>
                      ₹{v.toLocaleString('en-IN')}
                    </button>
                  ))}
                </div>
                {formError && <div className="form-error mb-2">{formError}</div>}
                <button className={`btn btn-block ${txType === 'DEPOSIT' ? 'btn-success' : 'btn-danger'}`} type="submit" disabled={submitting}>
                  {submitting ? 'Processing...' : txType === 'DEPOSIT' ? 'Deposit Funds' : 'Withdraw Funds'}
                </button>
              </form>
            </div>

            <div className="card">
              <div className="card-title">Transfer History</div>
              {ledger.length === 0 ? (
                <div className="empty-state">No ledger entries yet.</div>
              ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr><th>Type</th><th>Amount</th><th>Balance</th><th>Date</th></tr>
                    </thead>
                    <tbody>
                      {ledger.slice(0, 12).map((l) => (
                        <tr key={l.id}>
                          <td>{l.transactionType.replace('_', ' ')}</td>
                          <td className={['DEPOSIT', 'SELL_ORDER'].includes(l.transactionType) ? 'up' : 'down'}>
                            {formatSigned(l.amount)}
                          </td>
                          <td>{formatCurrency(l.balance)}</td>
                          <td className="text-faint">{formatDateTime(l.transactionDate)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
