export default function Footer() {
  return (
    <footer className="footer">
      <span>© {new Date().getFullYear()} TradeFlux Stock Brokerage and Portfolio Management System. All rights reserved.</span>
      <span className="flex gap-3">
        <span>Privacy Policy</span>
        <span>Terms of Service</span>
        <span>Support</span>
      </span>
    </footer>
  );
}
