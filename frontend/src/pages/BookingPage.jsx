import { Link, useLocation, useParams } from 'react-router-dom'
import { api } from '../api.js'
import { useFetch } from '../hooks/useFetch.js'
import { Loading, ErrorBox } from '../components/States.jsx'
import { dateTime, money } from '../format.js'

export default function BookingPage() {
  const { id } = useParams()
  const { state } = useLocation()
  const { data: b, loading, error, reload } = useFetch(() => api.getBooking(id), [id])

  if (loading) return <Loading label="Loading booking…" />
  if (error) return <ErrorBox error={error} onRetry={reload} />

  return (
    <div className="confirm">
      <div className="confirm-icon" aria-hidden="true">✓</div>
      <h1>{state?.justBooked ? 'Booking confirmed' : `Booking #${b.id}`}</h1>
      <p className="muted">{state?.justBooked ? 'Your seats are locked in.' : 'Booking details'}</p>

      <div className="card receipt">
        <div className="receipt-row"><span>Booking ID</span><strong>#{b.id}</strong></div>
        <div className="receipt-row"><span>Event</span><strong>{b.eventName}</strong></div>
        <div className="receipt-row">
          <span>Seats</span>
          <span className="chips">{b.seats.map((s) => <span className="chip" key={s}>{s}</span>)}</span>
        </div>
        <div className="receipt-row"><span>Status</span><span className="badge badge-ok">{b.status}</span></div>
        <div className="receipt-row"><span>Booked at</span><strong>{dateTime(b.createdAt)}</strong></div>
        <div className="receipt-row receipt-total"><span>Total</span><strong>{money(b.totalAmount)}</strong></div>
      </div>

      <div className="actions">
        <Link className="btn btn-primary" to="/bookings">My bookings</Link>
        <Link className="btn btn-ghost" to="/">Browse events</Link>
      </div>
    </div>
  )
}
