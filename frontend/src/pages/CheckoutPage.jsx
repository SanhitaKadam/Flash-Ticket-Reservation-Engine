import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import { DEMO_USER } from '../config.js'
import { useFetch } from '../hooks/useFetch.js'
import { Loading, ErrorBox } from '../components/States.jsx'
import { money } from '../format.js'

const mmss = (s) => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

export default function CheckoutPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { data: r, loading, error, reload } = useFetch(() => api.getReservation(id, DEMO_USER.id), [id])

  const [remaining, setRemaining] = useState(null)
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState(null)

  // Countdown. We use the server's secondsRemaining, so a wrong clock on this PC cannot skew it.
  useEffect(() => {
    if (!r) return undefined
    const deadline = Date.now() + r.secondsRemaining * 1000
    const tick = () => setRemaining(Math.max(0, Math.ceil((deadline - Date.now()) / 1000)))
    tick()
    const timer = setInterval(tick, 250)
    return () => clearInterval(timer)
  }, [r])

  if (loading) return <Loading label="Loading your hold…" />
  if (error?.status === 410) return <Expired message={error.message} />
  if (error) return <ErrorBox error={error} onRetry={reload} />
  if (remaining === 0) return <Expired eventId={r.eventId} />

  async function pay() {
    setBusy(true); setActionError(null)
    try {
      const booking = await api.confirmReservation(r.id, DEMO_USER.id)
      navigate(`/bookings/${booking.id}`, { state: { justBooked: true } })
    } catch (e) {
      setActionError(e.message)
      if (e.status === 410) reload()
    } finally { setBusy(false) }
  }

  async function cancel() {
    setBusy(true)
    try { await api.cancelReservation(r.id, DEMO_USER.id) } catch { /* nothing to cancel */ }
    navigate(`/events/${r.eventId}`)
  }

  const urgent = remaining !== null && remaining <= 60
  return (
    <div className="confirm">
      <div className={`timer ${urgent ? 'timer-urgent' : ''}`}>
        <span className="timer-label">Seats held for</span>
        <span className="timer-value">{remaining === null ? '--:--' : mmss(remaining)}</span>
      </div>
      <h1>Complete your booking</h1>
      <p className="muted">Your seats are reserved for you until the timer ends.</p>

      <div className="card receipt">
        <div className="receipt-row"><span>Event</span><strong>{r.eventName}</strong></div>
        <div className="receipt-row">
          <span>Seats</span>
          <span className="chips">{r.seats.map((s) => <span className="chip" key={s}>{s}</span>)}</span>
        </div>
        <div className="receipt-row receipt-total"><span>Total</span><strong>{money(r.totalAmount)}</strong></div>
      </div>

      {actionError && <div className="alert alert-danger">{actionError}</div>}
      <div className="actions">
        <button className="btn btn-primary" disabled={busy} onClick={pay}>
          {busy ? 'Working…' : 'Pay now (simulated)'}
        </button>
        <button className="btn btn-ghost" disabled={busy} onClick={cancel}>Cancel and release seats</button>
      </div>
      <p className="fineprint">No real payment is taken. This button simulates a successful payment.</p>
    </div>
  )
}

function Expired({ eventId, message }) {
  return (
    <div className="confirm">
      <div className="confirm-icon expired-icon" aria-hidden="true">⏱</div>
      <h1>Your hold has expired</h1>
      <p className="muted">{message ?? 'The seats were released so other customers can book them.'}</p>
      <div className="actions">
        <Link className="btn btn-primary" to={eventId ? `/events/${eventId}` : '/'}>
          {eventId ? 'Choose seats again' : 'Browse events'}
        </Link>
      </div>
    </div>
  )
}
