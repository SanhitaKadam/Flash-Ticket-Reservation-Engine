import { Link, NavLink } from 'react-router-dom'
import { DEMO_USER } from '../config.js'

export default function Header() {
  return (
    <header className="header">
      <div className="container header-inner">
        <Link to="/" className="brand">
          <span className="brand-mark" aria-hidden="true" />
          Flash<span className="brand-accent">Ticket</span>
        </Link>
        <nav className="nav">
          <NavLink to="/" end>Events</NavLink>
          <NavLink to="/bookings">My bookings</NavLink>
        </nav>
        <div className="user-chip" title="No login yet. Authentication comes in Phase 9.">
          <span className="avatar">{DEMO_USER.name[0]}</span>
          {DEMO_USER.name}
        </div>
      </div>
    </header>
  )
}
