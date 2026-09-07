import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="page-loader" style={{ flexDirection: 'column', gap: 12 }}>
      <h2>404 — Page not found</h2>
      <Link to="/" className="btn btn-primary">
        Back to home
      </Link>
    </div>
  );
}
