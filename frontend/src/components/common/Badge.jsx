import { statusTone } from '../../utils/format';

export default function Badge({ children, tone }) {
  const resolvedTone = tone || statusTone(children);
  return <span className={`badge badge-${resolvedTone}`}>{children}</span>;
}
