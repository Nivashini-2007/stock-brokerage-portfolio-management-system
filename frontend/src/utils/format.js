export function formatCurrency(value, { decimals = 2 } = {}) {
  const n = Number(value);
  if (Number.isNaN(n)) return '--';
  return '₹' + n.toLocaleString('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export function formatNumber(value, decimals = 0) {
  const n = Number(value);
  if (Number.isNaN(n)) return '--';
  return n.toLocaleString('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export function formatPercent(value, decimals = 2) {
  const n = Number(value);
  if (Number.isNaN(n)) return '--';
  return (n > 0 ? '+' : '') + n.toFixed(decimals) + '%';
}

export function formatSigned(value, opts) {
  const n = Number(value);
  if (Number.isNaN(n)) return '--';
  const formatted = formatCurrency(Math.abs(n), opts);
  return (n >= 0 ? '+' : '-') + formatted;
}

export function formatDateTime(value) {
  if (!value) return '--';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '--';
  return d.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatDate(value) {
  if (!value) return '--';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '--';
  return d.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export function initials(firstName, lastName) {
  return `${(firstName || '?')[0] || ''}${(lastName || '')[0] || ''}`.toUpperCase();
}

export function roleLabel(role) {
  const map = {
    ADMIN: 'Administrator',
    CLIENT: 'Client',
    DEALER: 'Dealer',
    RESEARCH_ANALYST: 'Research Analyst',
    COMPLIANCE_OFFICER: 'Compliance Officer',
    RISK_MANAGER: 'Risk Manager',
  };
  return map[role] || role;
}

export function statusTone(status) {
  const map = {
    EXECUTED: 'green',
    PUBLISHED: 'green',
    VERIFIED: 'green',
    ACTIVE: 'green',
    APPROVED: 'green',
    PENDING: 'amber',
    PENDING_REVIEW: 'amber',
    SUSPENDED: 'amber',
    CANCELLED: 'gray',
    CLOSED: 'gray',
    DRAFT: 'gray',
    REJECTED: 'red',
  };
  return map[status] || 'gray';
}
