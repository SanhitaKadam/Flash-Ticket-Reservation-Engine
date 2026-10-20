import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { useFetch } from '../hooks/useFetch.js'
import { Loading, ErrorBox } from '../components/States.jsx'
import { dateTime } from '../format.js'

const gradients = [
  'linear-gradient(135deg, #1d4ed8 0%, #22d3ee 100%)',
  'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)',
  'linear-gradient(135deg, #0e7490 0%, #10b981 100%)',
]

export default function EventsPage() {
  const { data, loading, error, reload } = useFetch(() => api.listEvents())

  if (loading) return <Loading label="Loading events…" />
  if (error) return <ErrorBox error={error} onRetry={reload} />

  return (
    <>
      <section className="hero">
        <h1>Live events, <span className="grad-text">locked in seconds.</span></h1>
        <p className="muted">Pick an event, choose your seats, and book. Every seat is reserved exactly once.</p>
      </section>

      <div className="grid">
        {data.map((e, i) => (
          <article className="card event-card" key={e.id}>
            <div className="event-banner" style={{ background: gradients[i % gradients.length] }}>
              <span className="pill">{e.city}</span>
            </div>
            <div className="event-body">
              <h3>{e.name}</h3>
              <p className="muted">{e.description}</p>
              <div className="meta">
                <span>{e.venueName}</span>
                <span>{dateTime(e.startsAt)}</span>
              </div>
              <Link className="btn btn-primary" to={`/events/${e.id}`}>Select seats</Link>
            </div>
          </article>
        ))}
      </div>
    </>
  )
}
