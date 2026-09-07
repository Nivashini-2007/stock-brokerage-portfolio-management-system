const PATHS = {
  dashboard: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
  terminal: 'M4 4h16v16H4V4zm3 5l3 3-3 3m5 0h5',
  portfolio: 'M3 12l4-6 4 4 4-8 4 6M3 20h18',
  holdings: 'M4 4h16v4H4V4zm0 6h16v10H4V10zm4 3h8',
  orders: 'M6 3h9l3 3v15H6V3zm3 6h6m-6 4h6m-6 4h4',
  positions: 'M4 19l5-5 4 4 7-9M4 19h16',
  funds: 'M4 6h16v12H4V6zm0 4h16M8 14h4',
  watchlist: 'M12 3l2.6 5.6 6.1.6-4.6 4 1.3 6-5.4-3.2L6.6 19l1.3-6-4.6-4 6.1-.6L12 3z',
  research: 'M9 3h9v14l-4.5 4-4.5-4V3zm-3 3H3v14l3 3',
  risk: 'M12 3l9 16H3l9-16zm0 6v4m0 3h.01',
  market: 'M4 20V10m6 10V4m6 16v-7m6 7V8',
  compliance: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
  bell: 'M6 8a6 6 0 1112 0c0 5 2 6 2 6H4s2-1 2-6zm4.3 10a1.8 1.8 0 003.4 0',
  user: 'M12 12a4 4 0 100-8 4 4 0 000 8zm-7 9a7 7 0 0114 0',
  admin: 'M12 3l7 3v6c0 4.5-3 7.7-7 9-4-1.3-7-4.5-7-9V6l7-3z',
  logout: 'M9 4H5v16h4M16 16l4-4-4-4m4 4H9',
  search: 'M11 4a7 7 0 105 12l5 5-5-5a7 7 0 00-5-12z',
  kyc: 'M8 3h8l3 3v15H5V6l3-3zm1 7h6m-6 4h6m-6 4h3',
  plus: 'M12 5v14M5 12h14',
  close: 'M6 6l12 12M6 18L18 6',
  chevronDown: 'M6 9l6 6 6-6',
  download: 'M12 3v12m0 0l-4-4m4 4l4-4M5 21h14',
  bank: 'M3 10l9-6 9 6M5 10v9h4v-6h6v6h4v-9',
};

export default function Icon({ name, size = 16, className = '' }) {
  const d = PATHS[name];
  if (!d) return null;
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d={d} />
    </svg>
  );
}
