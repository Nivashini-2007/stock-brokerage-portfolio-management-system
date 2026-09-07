import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationsApi } from '../../api/notifications';
import { useAuth } from '../../context/AuthContext';
import { initials } from '../../utils/format';
import Icon from '../common/Icon';

export default function Topbar({ title }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function poll() {
      try {
        const list = await notificationsApi.forClient(user.id);
        if (!cancelled) setUnread(list.filter((n) => !n.read).length);
      } catch {
        // non-fatal: notification count is a convenience indicator
      }
    }

    poll();
    const id = setInterval(poll, 20000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [user.id]);

  function onSearch(e) {
    e.preventDefault();
    const symbol = search.trim().toUpperCase();
    if (symbol) navigate(`/terminal?symbol=${symbol}`);
  }

  return (
    <header className="topbar">
      <h1>{title}</h1>
      <form className="topbar-search" onSubmit={onSearch}>
        <input
          placeholder="Search stocks, e.g. RELIANCE..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </form>
      <div className="topbar-right">
        <button className="btn btn-ghost btn-sm" onClick={() => navigate('/notifications')} style={{ position: 'relative' }}>
          <Icon name="bell" size={15} />
          {unread > 0 && (
            <span
              style={{
                position: 'absolute',
                top: -4,
                right: -4,
                background: 'var(--red)',
                color: '#fff',
                borderRadius: '999px',
                fontSize: 10,
                fontWeight: 700,
                minWidth: 16,
                height: 16,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '0 3px',
              }}
            >
              {unread}
            </span>
          )}
        </button>
        <div className="avatar" style={{ width: 30, height: 30, fontSize: 11, cursor: 'pointer' }} onClick={() => navigate('/profile')}>
          {initials(user.firstName, user.lastName)}
        </div>
      </div>
    </header>
  );
}
