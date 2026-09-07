import { Link } from 'react-router-dom';

export default function PublicNavbar() {
  return (
    <nav className="public-nav">
      <Link to="/" className="brand">
        <span className="logo-dot" style={{ width: 30, height: 30, fontSize: 12 }}>
          TF
        </span>
        TradeFlux
      </Link>
      <div className="links">
        <Link to="/login" className="btn btn-ghost btn-sm">
          Sign In
        </Link>
        <Link to="/register" className="btn btn-primary btn-sm">
          Create Account
        </Link>
      </div>
    </nav>
  );
}
