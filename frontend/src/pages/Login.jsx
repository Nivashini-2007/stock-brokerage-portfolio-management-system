import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import PublicNavbar from '../components/layout/PublicNavbar';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(email, password);
      const from = location.state?.from?.pathname || '/dashboard';
      toast.success('Welcome back.');
      navigate(from, { replace: true });
    } catch (err) {
      setError(apiErrorMessage(err, 'Invalid credentials. Please check your email and password.'));
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
            Welcome <span className="grad-text">back.</span>
          </h1>
          <p>Sign in to check today&apos;s P&amp;L, manage orders and review your portfolio.</p>
          <div className="mini-stats">
            <div className="mini-stat">
              <span className="text-faint">Trading terminal</span>
              <span>Live market quotes</span>
            </div>
            <div className="mini-stat">
              <span className="text-faint">Portfolio</span>
              <span>Real-time MTM P&amp;L</span>
            </div>
            <div className="mini-stat">
              <span className="text-faint">Compliance</span>
              <span>SEBI-aligned reporting</span>
            </div>
          </div>
        </div>
        <div className="auth-form-col">
          <div className="auth-form-box">
            <h2>Sign In</h2>
            <div className="sub">Access your brokerage account securely.</div>
            <form onSubmit={onSubmit}>
              <div className="field">
                <label>Email Address</label>
                <input
                  type="email"
                  required
                  placeholder="you@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div className="field">
                <label>Password</label>
                <div className="flex gap-1">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <button type="button" className="btn btn-ghost btn-sm" onClick={() => setShowPassword((s) => !s)}>
                    {showPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
              </div>
              {error && <div className="form-error">{error}</div>}
              <button className="btn btn-primary btn-block mt-2" type="submit" disabled={submitting}>
                {submitting ? 'Signing in...' : 'Sign In to Account'}
              </button>
            </form>
            <div className="form-hint mt-3">
              New to TradeFlux? <Link to="/register" className="btn-link">Create Account</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
