import { Route, Routes } from 'react-router-dom'
import Header from './components/Header.jsx'
import EventsPage from './pages/EventsPage.jsx'
import EventPage from './pages/EventPage.jsx'
import CheckoutPage from './pages/CheckoutPage.jsx'
import BookingPage from './pages/BookingPage.jsx'
import MyBookingsPage from './pages/MyBookingsPage.jsx'

export default function App() {
  return (
    <>
      <Header />
      <main className="container">
        <Routes>
          <Route path="/" element={<EventsPage />} />
          <Route path="/events/:id" element={<EventPage />} />
          <Route path="/checkout/:id" element={<CheckoutPage />} />
          <Route path="/bookings" element={<MyBookingsPage />} />
          <Route path="/bookings/:id" element={<BookingPage />} />
          <Route path="*" element={<p className="state">Page not found.</p>} />
        </Routes>
      </main>
    </>
  )
}
