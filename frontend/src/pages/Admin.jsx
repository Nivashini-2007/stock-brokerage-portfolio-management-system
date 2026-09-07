import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { apiErrorMessage } from '../api/client';
import { useToast } from '../context/ToastContext';
import { roleLabel } from '../utils/format';

const NAME_RE = /^[A-Za-z ]{2,100}$/;
const PHONE_RE = /^[0-9]{10}$/;

export default function Admin() {
  const toast = useToast();
  const [roles, setRoles] = useState([]);
  const [rolesError, setRolesError] = useState('');

  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', password: '', roleId: '' });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  useEffect(() => {
    authApi
      .roles()
      .then((data) => {
        setRoles(data);
        setForm((f) => ({ ...f, roleId: data.find((r) => r.name !== 'CLIENT')?.id || data[0]?.id || '' }));
      })
      .catch((err) => setRolesError(apiErrorMessage(err, 'Could not load roles.')));
  }, []);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  function validate() {
    const e = {};
    if (!NAME_RE.test(form.firstName.trim())) e.firstName = 'Alphabetic characters only.';
    if (!NAME_RE.test(form.lastName.trim())) e.lastName = 'Alphabetic characters only.';
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Enter a valid email address.';
    if (!PHONE_RE.test(form.phone)) e.phone = 'Phone number must be exactly 10 digits.';
    if (form.password.length < 8) e.password = 'Password must be at least 8 characters.';
    if (!form.roleId) e.roleId = 'Select a role.';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function submit(e) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSubmitting(true);
    try {
      const created = await authApi.registerStaff({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        phone: form.phone,
        password: form.password,
        roleId: Number(form.roleId),
      });
      toast.success(`${roleLabel(created.role)} account created for ${created.email}.`);
      setForm((f) => ({ ...f, firstName: '', lastName: '', email: '', phone: '', password: '' }));
    } catch (err) {
      setServerError(apiErrorMessage(err, 'Could not create this staff account.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="grid" style={{ gridTemplateColumns: '1fr 320px', gap: 16, alignItems: 'start' }}>
      <div className="card">
        <div className="card-title">Provision Staff Account</div>
        <div className="card-subtitle">Create Dealer, Research Analyst, Compliance Officer, Risk Manager or Admin accounts.</div>
        {rolesError && <div className="form-error mb-2">{rolesError}</div>}
        <form onSubmit={submit}>
          <div className="field-row">
            <div className="field">
              <label>First Name</label>
              <input value={form.firstName} onChange={(e) => update('firstName', e.target.value)} />
              {errors.firstName && <div className="form-error">{errors.firstName}</div>}
            </div>
            <div className="field">
              <label>Last Name</label>
              <input value={form.lastName} onChange={(e) => update('lastName', e.target.value)} />
              {errors.lastName && <div className="form-error">{errors.lastName}</div>}
            </div>
          </div>
          <div className="field-row">
            <div className="field">
              <label>Email</label>
              <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} />
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>
            <div className="field">
              <label>Phone</label>
              <input value={form.phone} onChange={(e) => update('phone', e.target.value.replace(/\D/g, '').slice(0, 10))} />
              {errors.phone && <div className="form-error">{errors.phone}</div>}
            </div>
          </div>
          <div className="field-row">
            <div className="field">
              <label>Temporary Password</label>
              <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)} />
              {errors.password && <div className="form-error">{errors.password}</div>}
            </div>
            <div className="field">
              <label>Role</label>
              <select value={form.roleId} onChange={(e) => update('roleId', e.target.value)}>
                {roles.map((r) => (
                  <option key={r.id} value={r.id}>{roleLabel(r.name)}</option>
                ))}
              </select>
              {errors.roleId && <div className="form-error">{errors.roleId}</div>}
            </div>
          </div>
          {serverError && <div className="form-error mb-2">{serverError}</div>}
          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? 'Creating...' : 'Create Staff Account'}
          </button>
        </form>
      </div>

      <div className="card">
        <div className="card-title">Quick Links</div>
        <div className="flex" style={{ flexDirection: 'column', gap: 8 }}>
          <Link to="/compliance" className="btn btn-ghost btn-block">Compliance Reports & KYC</Link>
          <Link to="/risk-alerts" className="btn btn-ghost btn-block">Risk Alerts & Margin</Link>
          <Link to="/market" className="btn btn-ghost btn-block">Market Administration</Link>
          <Link to="/orders" className="btn btn-ghost btn-block">Order Book</Link>
          <Link to="/portfolio" className="btn btn-ghost btn-block">All Client Portfolios</Link>
        </div>
      </div>
    </div>
  );
}
