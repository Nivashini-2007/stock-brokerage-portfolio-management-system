import { useEffect, useState } from 'react';
import { notificationsApi } from '../api/notifications';
import Loader from '../components/common/Loader';
import { useAuth } from '../context/AuthContext';
import { formatDateTime } from '../utils/format';

const ICON_TONE = {
  ORDER_EXECUTED: 'green',
  ORDER_REJECTED: 'red',
  MARGIN_CALL: 'red',
  POSITION_SQUARE_OFF: 'amber',
  RISK_ALERT: 'red',
  FUND_TRANSFER: 'blue',
  RESEARCH_PUBLISHED: 'blue',
  KYC_UPDATE: 'amber',
};

export default function Notifications() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    notificationsApi
      .forClient(user.id)
      .then((data) => setNotifications([...data].reverse()))
      .catch(() => setNotifications([]))
      .finally(() => setLoading(false));
  }, [user.id]);

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="card">
      <div className="flex justify-between items-center mb-2">
        <div className="card-title" style={{ marginBottom: 0 }}>
          {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount === 1 ? '' : 's'}` : 'All caught up'}
        </div>
      </div>
      {loading ? (
        <Loader inline />
      ) : notifications.length === 0 ? (
        <div className="empty-state">No notifications yet.</div>
      ) : (
        <div>
          {notifications.map((n) => (
            <div
              key={n.id}
              className="flex justify-between items-center"
              style={{ padding: '14px 4px', borderBottom: '1px solid var(--border-soft)' }}
            >
              <div className="flex gap-2 items-center">
                <span className={`badge badge-${ICON_TONE[n.type] || 'gray'}`} style={{ borderRadius: 8 }}>
                  {n.type.replace(/_/g, ' ')}
                </span>
                <span style={{ fontSize: 13.5 }}>{n.message}</span>
              </div>
              <div className="flex gap-2 items-center">
                <span className="text-faint" style={{ fontSize: 11.5 }}>{formatDateTime(n.createdAt)}</span>
                {!n.read && <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--accent)' }} />}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
