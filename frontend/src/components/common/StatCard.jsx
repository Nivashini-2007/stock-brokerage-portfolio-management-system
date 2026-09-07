export default function StatCard({ label, value, hint, hintTone, icon }) {
  return (
    <div className="stat-card">
      <div className="stat-label">
        <span>{label}</span>
        {icon && <span>{icon}</span>}
      </div>
      <div className="stat-value">{value}</div>
      {hint && <div className={`stat-hint ${hintTone === 'up' ? 'up' : hintTone === 'down' ? 'down' : ''}`}>{hint}</div>}
    </div>
  );
}
