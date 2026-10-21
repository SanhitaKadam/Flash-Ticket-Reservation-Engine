// Every call goes to /api/... on the same origin; Vite proxies it to Spring Boot.
async function request(path, options = {}) {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    let detail = `Request failed (${res.status})`
    try {
      const body = await res.json()
      if (body.detail) detail = body.detail // Spring's ProblemDetail message
    } catch { /* body was not JSON */ }
    const err = new Error(detail)
    err.status = res.status
    throw err
  }
  if (res.status === 204) return null // e.g. DELETE has no body
  return res.json()
}

export const api = {
  listEvents: () => request('/events'),
  getEvent: (id) => request(`/events/${id}`),
  getSeats: (id) => request(`/events/${id}/seats`),

  // Phase 3: step 1, hold seats in Redis
  createReservation: (body) => request('/reservations', { method: 'POST', body: JSON.stringify(body) }),
  getReservation: (id, userId) => request(`/reservations/${id}?userId=${userId}`),
  cancelReservation: (id, userId) => request(`/reservations/${id}?userId=${userId}`, { method: 'DELETE' }),

  // Phase 3: step 2, confirm the hold into a permanent booking
  confirmReservation: (reservationId, userId) =>
    request('/bookings', { method: 'POST', body: JSON.stringify({ reservationId, userId }) }),

  getBooking: (id) => request(`/bookings/${id}`),
  listBookings: (userId) => request(`/bookings?userId=${userId}`),
}
