import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import PublicNavbar from '../components/layout/PublicNavbar';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const NAME_RE = /^[A-Za-z ]{2,100}$/;
const PHONE_RE = /^[0-9]{10}$/;

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  function validateStep1() {
    const e = {};
    if (!NAME_RE.test(form.firstName.trim())) e.firstName = 'Alphabetic characters only, 2-100 chars.';
    if (!NAME_RE.test(form.lastName.trim())) e.lastName = 'Alphabetic characters only, 2-100 chars.';
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Enter a valid email address.';
    if (!PHONE_RE.test(form.phone)) e.phone = 'Phone number must be exactly 10 digits.';
    if (form.password.length < 8) e.password = 'Password must be at least 8 characters.';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match.';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  function next(e) {
    e.preventDefault();
    if (validateStep1()) setStep(2);
  }

  async function submit() {
    setSubmitting(true);
    setServerError('');
    try {
      await register({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        phone: form.phone,
        password: form.password,
      });
      toast.success('Registration successful! Please sign in to complete your KYC.');
      navigate('/login');
    } catch (err) {
      setServerError(apiErrorMessage(err, 'Registration failed. Please check your details and try again.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <PublicNavbar />
      <div className="auth-split">
        <div className="auth-visual">
          <h1>
            Open your <span className="grad-text">trading account.</span>
          </h1>
          <p>
            Register in a couple of minutes. You&apos;ll complete PAN, DEMAT and bank verification
            (KYC) right after your first sign-in.
          </p>
          <div className="mini-stats">
            <div className="mini-stat">
              <span className="text-faint">Step 1</span>
              <span>Personal details</span>
            </div>
            <div className="mini-stat">
              <span className="text-faint">Step 2</span>
              <span>Review &amp; submit</span>
            </div>
            <div className="mini-stat">
              <span className="text-faint">After sign-in</span>
              <span>PAN, DEMAT &amp; bank KYC</span>
            </div>
          </div>
        </div>
        <div className="auth-form-col">
          <div className="auth-form-box">
            <h2>Create Your Account</h2>
            <div className="sub">Step {step} of 2 — {step === 1 ? 'Personal Info' : 'Review'}</div>
            <div className="step-indicator">
              <div className={`step-dot ${step >= 1 ? 'step-done' : ''}`} />
              <div className={`step-dot ${step >= 2 ? 'step-active' : ''}`} />
            </div>

            {step === 1 && (
              <form onSubmit={next}>
                <div className="field-row">
                  <div className="field">
                    <label>First Name</label>
                    <input value={form.firstName} onChange={(e) => update('firstName', e.target.value)} placeholder="Alex" />
                    {errors.firstName && <div className="form-error">{errors.firstName}</div>}
                  </div>
                  <div className="field">
                    <label>Last Name</label>
                    <input value={form.lastName} onChange={(e) => update('lastName', e.target.value)} placeholder="Morgan" />
                    {errors.lastName && <div className="form-error">{errors.lastName}</div>}
                  </div>
                </div>
                <div className="field">
                  <label>Email Address</label>
                  <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} placeholder="you@email.com" />
                  {errors.email && <div className="form-error">{errors.email}</div>}
                </div>
                <div className="field">
                  <label>Mobile Number</label>
                  <input value={form.phone} onChange={(e) => update('phone', e.target.value.replace(/\D/g, '').slice(0, 10))} placeholder="10-digit mobile number" />
                  {errors.phone && <div className="form-error">{errors.phone}</div>}
                </div>
                <div className="field-row">
                  <div className="field">
                    <label>Password</label>
                    <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)} placeholder="Min. 8 characters" />
                    {errors.password && <div className="form-error">{errors.password}</div>}
                  </div>
                  <div className="field">
                    <label>Confirm Password</label>
                    <input type="password" value={form.confirmPassword} onChange={(e) => update('confirmPassword', e.target.value)} placeholder="Re-enter password" />
                    {errors.confirmPassword && <div className="form-error">{errors.confirmPassword}</div>}
                  </div>
                </div>
                <button className="btn btn-primary btn-block mt-2" type="submit">
                  Continue
                </button>
              </form>
            )}

            {step === 2 && (
              <div>
                <div className="card" style={{ padding: 14 }}>
                  <table>
                    <tbody>
                      <tr><td className="text-faint">Name</td><td className="text-right">{form.firstName} {form.lastName}</td></tr>
                      <tr><td className="text-faint">Email</td><td className="text-right">{form.email}</td></tr>
                      <tr><td className="text-faint">Mobile</td><td className="text-right">{form.phone}</td></tr>
                    </tbody>
                  </table>
                </div>
                {serverError && <div className="form-error mt-2">{serverError}</div>}
                <div className="flex gap-2 mt-3">
                  <button className="btn btn-ghost" onClick={() => setStep(1)} disabled={submitting}>
                    Back
                  </button>
                  <button className="btn btn-primary btn-block" onClick={submit} disabled={submitting}>
                    {submitting ? 'Submitting...' : 'Submit & Create Account'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
