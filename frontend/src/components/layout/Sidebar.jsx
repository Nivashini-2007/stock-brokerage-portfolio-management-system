import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { initials, roleLabel } from '../../utils/format';
import Icon from '../common/Icon';
import { NAV_ITEMS } from './navConfig';

export default function Sidebar() {
  const { user, logout } = useAuth();

  const items = NAV_ITEMS.filter((item) => !item.roles || item.roles.includes(user.role));

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="logo-dot">TF</span>
        <span>TradeFlux</span>
      </div>
      <nav className="sidebar-nav">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-link${isActive ? ' nav-active' : ''}`}
          >
            <span className="nav-icon">
              <Icon name={item.icon} size={15} />
            </span>
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div className="avatar">{initials(user.firstName, user.lastName)}</div>
        <div className="who" style={{ flex: 1 }}>
          <div className="name">
            {user.firstName} {user.lastName}
          </div>
          <div className="sub">{roleLabel(user.role)}</div>
        </div>
        <button className="btn btn-ghost btn-sm" title="Sign out" onClick={logout}>
          <Icon name="logout" size={14} />
        </button>
      </div>
    </aside>
  );
}
