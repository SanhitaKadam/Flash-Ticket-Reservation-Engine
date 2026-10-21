// Fires N simultaneous "hold this seat" requests and counts who wins.
// Usage:  node scripts/race-test.mjs [eventId] [seatId] [requests]
// Example: node scripts/race-test.mjs 1 10 100
//
// All requests use the demo user: the seat contest is per SEAT, not per user,
// so it is a valid contention test. It is a single-machine demonstration,
// not a performance benchmark (that comes with k6 in Phase 12).

const BASE = process.env.BASE_URL ?? 'http://localhost:8080'
const eventId = Number(process.argv[2] ?? 1)
const seatId = Number(process.argv[3] ?? 10)
const count = Number(process.argv[4] ?? 100)

const hold = () =>
  fetch(`${BASE}/api/reservations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: 1, eventId, seatIds: [seatId] }),
  }).then(async (r) => ({ status: r.status, body: await r.json().catch(() => null) }))
    .catch((e) => ({ status: `network error (${e.cause?.code ?? e.message})`, body: null }))

console.log(`Sending ${count} simultaneous requests for seat ${seatId} of event ${eventId}...`)
const started = Date.now()
const results = await Promise.all(Array.from({ length: count }, hold))
const ms = Date.now() - started

const tally = {}
for (const r of results) tally[r.status] = (tally[r.status] ?? 0) + 1
console.log(`Finished in ${ms} ms. Results by HTTP status:`)
for (const [status, n] of Object.entries(tally)) console.log(`  ${status}: ${n}`)

const winners = results.filter((r) => r.status === 201)
console.log(winners.length === 1 ? 'PASS: exactly one request got the seat.'
  : `FAIL: expected exactly 1 winner, got ${winners.length}.`)

// Clean up so the test can be re-run
for (const w of winners) {
  await fetch(`${BASE}/api/reservations/${w.body.id}?userId=1`, { method: 'DELETE' })
}
if (winners.length) console.log('Released the winning hold (cleanup).')
process.exitCode = winners.length === 1 ? 0 : 1
