import { useState } from 'react';
import { apiErrorMessage, downloadBlob } from '../api/client';
import { complianceApi } from '../api/compliance';
import { kycApi } from '../api/kyc';
import Badge from '../components/common/Badge';
import { useToast } from '../context/ToastContext';

const today = () => new Date().toISOString().slice(0, 10);

export default function Compliance() {
  const toast = useToast();
  const [date, setDate] = useState(today());
  const [downloadingDaily, setDownloadingDaily] = useState(false);
  const [downloadingUcc, setDownloadingUcc] = useState(false);

  const [kycClientId, setKycClientId] = useState('');
  const [kycRecord, setKycRecord] = useState(null);
  const [kycError, setKycError] = useState('');
  const [kycLoading, setKycLoading] = useState(false);
  const [remarks, setRemarks] = useState('');
  const [reviewing, setReviewing] = useState(false);

  async function downloadDaily() {
    setDownloadingDaily(true);
    try {
      const blob = await complianceApi.dailyActivityReport(date);
      downloadBlob(blob, `daily-activity-report-${date}.csv`);
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not generate the report.'));
    } finally {
      setDownloadingDaily(false);
    }
  }

  async function downloadUcc() {
    setDownloadingUcc(true);
    try {
      const blob = await complianceApi.uccFile();
      downloadBlob(blob, 'ucc-file.csv');
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not generate the UCC file.'));
    } finally {
      setDownloadingUcc(false);
    }
  }

  async function lookupKyc(e) {
    e.preventDefault();
    if (!kycClientId) return;
    setKycLoading(true);
    setKycError('');
    setKycRecord(null);
    try {
      const record = await kycApi.get(kycClientId);
      setKycRecord(record);
    } catch (err) {
      setKycError(apiErrorMessage(err, 'No KYC record found for this client.'));
    } finally {
      setKycLoading(false);
    }
  }

  async function review(approved) {
    setReviewing(true);
    try {
      const record = await kycApi.review(kycClientId, { approved, remarks });
      setKycRecord(record);
      toast.success(`KYC ${approved ? 'approved' : 'rejected'} for client #${kycClientId}.`);
      setRemarks('');
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Could not submit the KYC review.'));
    } finally {
      setReviewing(false);
    }
  }

  return (
    <div className="grid grid-cols-2">
      <div className="card">
        <div className="card-title">Regulatory Reports</div>
        <div className="card-subtitle">Exchange-prescribed CSV exports.</div>

        <div className="field">
          <label>Daily Activity Report — Date</label>
          <div className="flex gap-2">
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
            <button className="btn btn-primary btn-sm" onClick={downloadDaily} disabled={downloadingDaily}>
              {downloadingDaily ? 'Preparing...' : 'Download CSV'}
            </button>
          </div>
        </div>

        <div className="field mt-3">
          <label>Unique Client Code (UCC) File</label>
          <button className="btn btn-ghost btn-sm" onClick={downloadUcc} disabled={downloadingUcc}>
            {downloadingUcc ? 'Preparing...' : 'Download UCC File'}
          </button>
        </div>
      </div>

      <div className="card">
        <div className="card-title">KYC Review</div>
        <div className="card-subtitle">Look up a client&apos;s KYC status and approve or reject it.</div>
        <form onSubmit={lookupKyc} className="flex gap-2 mb-2">
          <input type="number" placeholder="Client ID" value={kycClientId} onChange={(e) => setKycClientId(e.target.value)} />
          <button className="btn btn-primary btn-sm" disabled={kycLoading}>{kycLoading ? '...' : 'Lookup'}</button>
        </form>

        {kycError && <div className="form-error mb-2">{kycError}</div>}

        {kycRecord && (
          <div>
            <table className="mb-2">
              <tbody>
                <tr><td className="text-faint">Client</td><td className="text-right">{kycRecord.clientName} (#{kycRecord.clientId})</td></tr>
                <tr><td className="text-faint">PAN</td><td className="text-right">{kycRecord.maskedPan}</td></tr>
                <tr><td className="text-faint">KYC Status</td><td className="text-right"><Badge>{kycRecord.kycStatus}</Badge></td></tr>
                <tr><td className="text-faint">Trading Status</td><td className="text-right"><Badge>{kycRecord.tradingStatus}</Badge></td></tr>
                <tr><td className="text-faint">Risk Profile</td><td className="text-right">{kycRecord.riskProfile || '--'}</td></tr>
              </tbody>
            </table>
            <div className="field">
              <label>Remarks</label>
              <input value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Optional review remarks" />
            </div>
            <div className="flex gap-2">
              <button className="btn btn-success btn-block" disabled={reviewing} onClick={() => review(true)}>Approve KYC</button>
              <button className="btn btn-danger btn-block" disabled={reviewing} onClick={() => review(false)}>Reject KYC</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
