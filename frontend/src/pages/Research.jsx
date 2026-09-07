import { useCallback, useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { researchApi } from '../api/research';
import Badge from '../components/common/Badge';
import Loader from '../components/common/Loader';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDate } from '../utils/format';

export default function Research() {
  const { user } = useAuth();
  const toast = useToast();
  const isAnalyst = user.role === 'RESEARCH_ANALYST';
  const isReviewer = ['COMPLIANCE_OFFICER', 'ADMIN'].includes(user.role);

  const [tab, setTab] = useState('published');
  const [published, setPublished] = useState([]);
  const [mine, setMine] = useState([]);
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({ companyName: '', symbol: '', recommendation: 'BUY', targetPrice: '', summary: '' });
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');
  const [actingId, setActingId] = useState(null);

  const loadAll = useCallback(async () => {
    setLoading(true);
    const tasks = [researchApi.published().then(setPublished).catch(() => [])];
    if (isAnalyst) tasks.push(researchApi.mine().then(setMine).catch(() => []));
    if (isReviewer) tasks.push(researchApi.pending().then(setPending).catch(() => []));
    await Promise.all(tasks);
    setLoading(false);
  }, [isAnalyst, isReviewer]);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  async function createReport(e) {
    e.preventDefault();
    setCreateError('');
    if (!form.companyName || !form.symbol || !form.recommendation || !form.targetPrice || !form.summary) {
      setCreateError('All fields are required.');
      return;
    }
    setCreating(true);
    try {
      await researchApi.create({ ...form, symbol: form.symbol.toUpperCase(), targetPrice: Number(form.targetPrice) });
      toast.success('Report submitted for compliance review.');
      setForm({ companyName: '', symbol: '', recommendation: 'BUY', targetPrice: '', summary: '' });
      loadAll();
    } catch (err) {
      setCreateError(apiErrorMessage(err, 'Could not submit the report.'));
    } finally {
      setCreating(false);
    }
  }

  async function act(id, type) {
    setActingId(id);
    try {
      if (type === 'approve') await researchApi.approve(id);
      else await researchApi.reject(id);
      toast.success(`Report ${type === 'approve' ? 'published' : 'rejected'}.`);
      loadAll();
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Action failed.'));
    } finally {
      setActingId(null);
    }
  }

  const tabs = [
    { key: 'published', label: 'Published' },
    ...(isAnalyst ? [{ key: 'mine', label: 'My Reports' }, { key: 'create', label: 'New Report' }] : []),
    ...(isReviewer ? [{ key: 'pending', label: `Pending Review (${pending.length})` }] : []),
  ];

  function ReportCard({ r, showActions }) {
    return (
      <div className="card">
        <div className="flex justify-between items-start">
          <div>
            <div style={{ fontWeight: 700 }}>
              {r.symbol} <span className="text-faint" style={{ fontWeight: 400 }}>{r.companyName}</span>
            </div>
            <Badge tone={r.recommendation?.toUpperCase().includes('SELL') ? 'red' : r.recommendation?.toUpperCase().includes('HOLD') ? 'amber' : 'green'}>
              {r.recommendation}
            </Badge>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div className="text-faint" style={{ fontSize: 11 }}>Target Price</div>
            <div style={{ fontWeight: 700 }}>{formatCurrency(r.targetPrice)}</div>
          </div>
        </div>
        <p className="text-dim mt-2" style={{ fontSize: 13 }}>{r.summary}</p>
        <div className="flex justify-between items-center mt-2">
          <span className="text-faint" style={{ fontSize: 11.5 }}>
            {r.analystName} · {r.publishedDate ? formatDate(r.publishedDate) : 'Unpublished'}
          </span>
          <div className="flex gap-2 items-center">
            <Badge>{r.status}</Badge>
            {showActions && (
              <>
                <button className="btn btn-success btn-sm" disabled={actingId === r.id} onClick={() => act(r.id, 'approve')}>Approve</button>
                <button className="btn btn-danger btn-sm" disabled={actingId === r.id} onClick={() => act(r.id, 'reject')}>Reject</button>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="tabs mb-2">
        {tabs.map((t) => (
          <button key={t.key} className={`tab ${tab === t.key ? 'tab-active' : ''}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <Loader inline />
      ) : tab === 'published' ? (
        published.length === 0 ? (
          <div className="card empty-state">No published research reports yet.</div>
        ) : (
          <div className="grid grid-cols-2">
            {published.map((r) => <ReportCard key={r.id} r={r} />)}
          </div>
        )
      ) : tab === 'mine' ? (
        mine.length === 0 ? (
          <div className="card empty-state">You haven&apos;t submitted any reports yet.</div>
        ) : (
          <div className="grid grid-cols-2">
            {mine.map((r) => <ReportCard key={r.id} r={r} />)}
          </div>
        )
      ) : tab === 'pending' ? (
        pending.length === 0 ? (
          <div className="card empty-state">No reports awaiting review.</div>
        ) : (
          <div className="grid grid-cols-2">
            {pending.map((r) => <ReportCard key={r.id} r={r} showActions />)}
          </div>
        )
      ) : (
        <div className="card" style={{ maxWidth: 480 }}>
          <form onSubmit={createReport}>
            <div className="field-row">
              <div className="field">
                <label>Company Name</label>
                <input value={form.companyName} onChange={(e) => setForm((f) => ({ ...f, companyName: e.target.value }))} />
              </div>
              <div className="field">
                <label>Symbol</label>
                <input value={form.symbol} onChange={(e) => setForm((f) => ({ ...f, symbol: e.target.value }))} />
              </div>
            </div>
            <div className="field-row">
              <div className="field">
                <label>Recommendation</label>
                <select value={form.recommendation} onChange={(e) => setForm((f) => ({ ...f, recommendation: e.target.value }))}>
                  <option value="BUY">Buy</option>
                  <option value="HOLD">Hold</option>
                  <option value="SELL">Sell</option>
                </select>
              </div>
              <div className="field">
                <label>Target Price</label>
                <input type="number" step="0.01" value={form.targetPrice} onChange={(e) => setForm((f) => ({ ...f, targetPrice: e.target.value }))} />
              </div>
            </div>
            <div className="field">
              <label>Investment Thesis</label>
              <textarea rows={4} value={form.summary} onChange={(e) => setForm((f) => ({ ...f, summary: e.target.value }))} style={{ width: '100%', resize: 'vertical' }} />
            </div>
            {createError && <div className="form-error mb-2">{createError}</div>}
            <button className="btn btn-primary btn-block" type="submit" disabled={creating}>
              {creating ? 'Submitting...' : 'Submit for Review'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
