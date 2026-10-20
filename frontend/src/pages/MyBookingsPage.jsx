import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { DEMO_USER } from '../config.js'
import { useFetch } from '../hooks/useFetch.js'
import { Loading, ErrorBox } from '../components/States.jsx'
import { dateOnly, money } from '../format.js'

export default function MyBookingsPage() {
  const { data, loading, error, reload } = useFetch(() => api.listBookings(DEMO_USER.id))

  if (loading) return <Loading label="Loading your bookings…" />
  if (error) return <ErrorBox error={error} onRetry={reload} />

  return (
    <>
      <h1>My bookings</h1>
      {data.length === 0 ? (
        <div className="state">
          <p>You have no bookings yet.</p>
          <Link className="btn btn-primary" to="/">Browse events</Link>
        </div>
      ) : (
        <div className="stack">
          {data.map((b) => (
            <Link to={`/bookings/${b.id}`} className="card booking-row" key={b.id}>
              <div>
                <h3>{b.eventName}</h3>
                <p className="muted">Booked {dateOnly(b.createdAt)} · #{b.id}</p>
                <span className="chips">{b.seats.map((s) => <span className="chip" key={s}>{s}</span>)}</span>
              </div>
              <div className="booking-side">
                <span className="badge badge-ok">{b.status}</span>
                <strong>{money(b.totalAmount)}</strong>
              </div>
            </Link>
          ))}
        </div>
      )}
    </>
  )
}
