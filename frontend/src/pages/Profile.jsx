import { useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import { kycApi } from '../api/kyc';
import Badge from '../components/common/Badge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { initials, roleLabel } from '../utils/format';

export default function Profile() {
  const { user, logout } = useAuth();
  const toast = useToast();
  const [tab, setTab] = useState('info');

  const [kyc, setKyc] = useState(null);
  const [kycLoading, setKycLoading] = useState(true);
  const [form, setForm] = useState({ panNumber: '', demateId: '', bankAccountNumber: '', bankIfsc: '' });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    kycApi
      .get(user.id)
      .then(setKyc)
      .catch(() => setKyc(null))
      .finally(() => setKycLoading(false));
  }, [user.id]);

  function validate() {
    const e = {};
    if (!/^[A-Z]{5}[0-9]{4}[A-Z]{1}$/.test(form.panNumber)) e.panNumber = 'Format: AAAAA9999A';
    if (!form.demateId.trim()) e.demateId = 'DEMAT ID is required.';
    if (!form.bankAccountNumber.trim()) e.bankAccountNumber = 'Bank account number is required.';
    if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(form.bankIfsc)) e.bankIfsc = 'Format: AAAA0999999';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function submitKyc(e) {
    e.preventDefault();
    setSubmitError('');
    if (!validate()) return;
    setSubmitting(true);
    try {
      const record = await kycApi.submit({ clientId: user.id, ...form });
      setKyc(record);
      toast.success('KYC details submitted for verification.');
    } catch (err) {
      setSubmitError(apiErrorMessage(err, 'Could not submit KYC details.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="grid" style={{ gridTemplateColumns: '220px 1fr', gap: 16, alignItems: 'start' }}>
      <div className="card" style={{ padding: 10 }}>
        {['info', 'kyc'].map((t) => (
          <div
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: '10px 12px',
              borderRadius: 8,
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 13,
              marginBottom: 4,
              background: tab === t ? 'var(--accent-soft)' : 'transparent',
              color: tab === t ? 'var(--accent)' : 'var(--text)',
            }}
          >
            {t === 'info' ? 'Personal Info' : 'KYC & Verification'}
          </div>
        ))}
        <button className="btn btn-outline-danger btn-block mt-2" onClick={logout}>Sign Out</button>
      </div>

      {tab === 'info' ? (
        <div className="card">
          <div className="flex gap-3 items-center mb-3">
            <div className="avatar" style={{ width: 56, height: 56, fontSize: 18 }}>{initials(user.firstName, user.lastName)}</div>
            <div>
              <h2>{user.firstName} {user.lastName}</h2>
              <div className="text-faint">{user.email}</div>
              <div className="flex gap-2 mt-1">
                <Badge tone="blue">{roleLabel(user.role)}</Badge>
                <Badge tone={user.enabled ? 'green' : 'gray'}>{user.enabled ? 'Active' : 'Disabled'}</Badge>
              </div>
            </div>
          </div>
          <div className="field-row">
            <div className="field"><label>First Name</label><input value={user.firstName} disabled /></div>
            <div className="field"><label>Last Name</label><input value={user.lastName} disabled /></div>
          </div>
          <div className="field-row">
            <div className="field"><label>Email</label><input value={user.email} disabled /></div>
            <div className="field"><label>Phone</label><input value={user.phone} disabled /></div>
          </div>
          <div className="form-hint">Profile fields are managed by your account administrator.</div>
        </div>
      ) : (
        <div className="card" style={{ maxWidth: 520 }}>
          {kycLoading ? (
            <div className="empty-state">Loading KYC status...</div>
          ) : (
            <>
              {kyc && (
                <div className="card mb-3" style={{ background: 'var(--bg-elevated)' }}>
                  <div className="flex justify-between mb-1">
                    <span className="text-faint">KYC Status</span>
                    <Badge>{kyc.kycStatus}</Badge>
                  </div>
                  <div className="flex justify-between mb-1">
                    <span className="text-faint">Trading Status</span>
                    <Badge>{kyc.tradingStatus}</Badge>
                  </div>
                  <div className="flex justify-between mb-1">
                    <span className="text-faint">PAN</span>
                    <span>{kyc.maskedPan}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-faint">Risk Profile</span>
                    <span>{kyc.riskProfile || 'Not assessed yet'}</span>
                  </div>
                </div>
              )}

              <div className="card-title">{kyc ? 'Update KYC Details' : 'Complete Your KYC'}</div>
              <div className="card-subtitle">Required before your trading account can be activated.</div>
              <form onSubmit={submitKyc}>
                <div className="field">
                  <label>PAN Number</label>
                  <input
                    value={form.panNumber}
                    onChange={(e) => setForm((f) => ({ ...f, panNumber: e.target.value.toUpperCase() }))}
                    placeholder="ABCDE1234F"
                  />
                  {errors.panNumber && <div className="form-error">{errors.panNumber}</div>}
                </div>
                <div className="field">
                  <label>DEMAT Account ID</label>
                  <input value={form.demateId} onChange={(e) => setForm((f) => ({ ...f, demateId: e.target.value }))} />
                  {errors.demateId && <div className="form-error">{errors.demateId}</div>}
                </div>
                <div className="field">
                  <label>Bank Account Number</label>
                  <input value={form.bankAccountNumber} onChange={(e) => setForm((f) => ({ ...f, bankAccountNumber: e.target.value }))} />
                  {errors.bankAccountNumber && <div className="form-error">{errors.bankAccountNumber}</div>}
                </div>
                <div className="field">
                  <label>Bank IFSC</label>
                  <input
                    value={form.bankIfsc}
                    onChange={(e) => setForm((f) => ({ ...f, bankIfsc: e.target.value.toUpperCase() }))}
                    placeholder="HDFC0001234"
                  />
                  {errors.bankIfsc && <div className="form-error">{errors.bankIfsc}</div>}
                </div>
                {submitError && <div className="form-error mb-2">{submitError}</div>}
                <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
                  {submitting ? 'Submitting...' : 'Submit KYC Details'}
                </button>
              </form>
            </>
          )}
        </div>
      )}
    </div>
  );
}
