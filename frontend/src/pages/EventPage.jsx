import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import { DEMO_USER } from '../config.js'
import { useFetch } from '../hooks/useFetch.js'
import { Loading, ErrorBox } from '../components/States.jsx'
import SeatMap from '../components/SeatMap.jsx'
import { dateTime, money } from '../format.js'

export default function EventPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const event = useFetch(() => api.getEvent(id), [id])
  // Poll every 5 s so seats held or released by other customers show up live.
  const seats = useFetch(() => api.getSeats(id), [id], { pollMs: 5000 })

  const [selected, setSelected] = useState(new Set())
  const [submitting, setSubmitting] = useState(false)
  const [holdError, setHoldError] = useState(null)

  if (event.loading || seats.loading) return <Loading label="Loading seats…" />
  if (event.error) return <ErrorBox error={event.error} onRetry={event.reload} />
  if (seats.error) return <ErrorBox error={seats.error} onRetry={seats.reload} />

  const byId = new Map(seats.data.map((s) => [s.id, s]))
  // A seat someone else grabbed since we selected it silently drops out of our selection.
  const chosen = [...selected].map((sid) => byId.get(sid)).filter((s) => s && s.status === 'AVAILABLE')
  const total = chosen.reduce((sum, s) => sum + Number(s.price), 0)
  const available = seats.data.filter((s) => s.status === 'AVAILABLE').length

  function toggle(seat) {
    setHoldError(null)
    setSelected((prev) => {
      const next = new Set(prev)
      next.has(seat.id) ? next.delete(seat.id) : next.add(seat.id)
      return next
    })
  }

  async function holdSeats() {
    setSubmitting(true)
    setHoldError(null)
    try {
      const r = await api.createReservation({
        userId: DEMO_USER.id,
        eventId: event.data.id,
        seatIds: chosen.map((s) => s.id),
      })
      navigate(`/checkout/${r.id}`)
    } catch (e) {
      // Typical case: 409, someone else got there first. Show it and refresh the map.
      setHoldError(e.message)
      setSelected(new Set())
      seats.reload()
    } finally {
      setSubmitting(false)
    }
  }

  const e = event.data
  return (
    <>
      <Link to="/" className="back">← All events</Link>
      <div className="event-head">
        <h1>{e.name}</h1>
        <p className="muted">{e.venueName}, {e.city} · {dateTime(e.startsAt)}</p>
      </div>

      <div className="layout">
        <section className="card seat-card">
          <div className="card-title">
            <h2>Choose your seats</h2>
            <span className="muted">{available} available</span>
          </div>
          <SeatMap seats={seats.data} selected={selected} onToggle={toggle} />
        </section>

        <aside className="card summary">
          <h2>Your selection</h2>
          {chosen.length === 0 ? (
            <p className="muted">Pick up to 6 seats to continue.</p>
          ) : (
            <ul className="summary-list">
              {chosen.map((s) => (
                <li key={s.id}><span>Seat {s.label}</span><span>{money(s.price)}</span></li>
              ))}
            </ul>
          )}
          <div className="total"><span>Total</span><strong>{money(total)}</strong></div>
          {holdError && <div className="alert alert-danger">{holdError}</div>}
          <button className="btn btn-primary btn-block"
                  disabled={chosen.length === 0 || chosen.length > 6 || submitting}
                  onClick={holdSeats}>
            {submitting ? 'Holding…' : `Hold ${chosen.length || ''} seat${chosen.length === 1 ? '' : 's'}`}
          </button>
          <p className="fineprint">
            Holding reserves your seats for a limited time while you check out. Payment is a clearly labelled simulation.
          </p>
        </aside>
      </div>
    </>
  )
}
