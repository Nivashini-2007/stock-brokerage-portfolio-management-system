export default function Loader({ full = false, inline = false }) {
  if (full) {
    return (
      <div className="page-loader">
        <div className="spinner" />
      </div>
    );
  }
  if (inline) {
    return (
      <div className="card-loader">
        <div className="spinner" />
      </div>
    );
  }
  return <div className="spinner" />;
}
