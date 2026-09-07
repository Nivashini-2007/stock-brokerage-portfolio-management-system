import { Link } from 'react-router-dom';
import Footer from '../components/layout/Footer';
import PublicNavbar from '../components/layout/PublicNavbar';

const FEATURES = [
  { title: 'Trading Terminal', desc: 'Market, limit, stop-loss and bracket orders with live quotes and margin checks.' },
  { title: 'Portfolio Analytics', desc: 'Real-time MTM valuation, unrealised/realised P&L and performance tracking.' },
  { title: 'Margin & Risk', desc: 'Live margin utilisation, automated alerts and risk manager oversight.' },
  { title: 'Tax & Ledger', desc: 'FIFO capital-gains reporting with LTCG/STCG classification and brokerage ledger.' },
  { title: 'Research Desk', desc: 'Analyst recommendations reviewed by compliance before client publication.' },
  { title: 'SEBI Compliance', desc: 'Daily activity reports, UCC files and KYC verification workflows.' },
];

export default function Landing() {
  return (
    <div>
      <PublicNavbar />
      <div className="landing-hero">
        <h1>
          Trade, invest and manage <span className="grad-text">your entire portfolio</span> in one place.
        </h1>
        <p>
          TradeFlux is a full-stack stock brokerage and portfolio management platform built on a
          React front end and a Spring Boot + MySQL back end, with role-based access for clients,
          dealers, analysts, compliance and risk teams.
        </p>
        <div className="landing-cta">
          <Link to="/register" className="btn btn-primary">
            Create Your Account
          </Link>
          <Link to="/login" className="btn btn-ghost">
            Sign In
          </Link>
        </div>
      </div>
      <div className="landing-features">
        <div className="grid grid-cols-3">
          {FEATURES.map((f) => (
            <div className="card" key={f.title}>
              <div className="card-title">{f.title}</div>
              <div className="text-dim" style={{ fontSize: 13 }}>
                {f.desc}
              </div>
            </div>
          ))}
        </div>
      </div>
      <Footer />
    </div>
  );
}
