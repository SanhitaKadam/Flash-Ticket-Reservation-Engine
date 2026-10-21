# Flash Ticket Reservation Engine

High-concurrency ticket reservation system, built incrementally.

## Status
- [x] Phase 1: Spring Boot + PostgreSQL basic booking API
- [x] Phase 2: React frontend (Vite)
- [x] Phase 3: Redis seat holds (atomic Lua script, 10-minute expiry)
- [ ] Phase 4+: Idempotency, Kafka, ledger service, reliability, gateway, Docker, Kubernetes, k6

## Booking flow
1. **Hold**: `POST /api/reservations` puts the seats in Redis for 10 minutes (one winner per seat).
2. **Confirm**: `POST /api/bookings` with the reservation id writes the permanent booking to PostgreSQL.
3. If the user does nothing, Redis expires the hold and the seats become available again.

## Run (Windows / PowerShell), three things running

**Redis (Docker, once)**

    $env:REDIS_PASSWORD = "choose_a_redis_password"
    docker run -d --name flash-redis -p 127.0.0.1:6379:6379 redis:7-alpine redis-server --requirepass $env:REDIS_PASSWORD

Later: `docker start flash-redis` / `docker stop flash-redis`

**Backend**

    $env:DB_USERNAME = "flash_app"
    $env:DB_PASSWORD = "your_db_password"
    $env:REDIS_PASSWORD = "choose_a_redis_password"
    cd reservation-service
    mvn spring-boot:run

**Frontend**

    cd frontend
    npm install
    npm run dev

Open http://localhost:5173

Optional: `$env:RESERVATION_HOLD_SECONDS = "30"` before starting the backend to test expiry quickly.

## Race test (100 simultaneous requests for one seat)

    node scripts/race-test.mjs 1 10 100

## Database setup (once)

    & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost

    CREATE USER flash_app WITH PASSWORD 'CHOOSE_A_PASSWORD';
    CREATE DATABASE flashticket OWNER flash_app;
    \q

## API
| Method | Path | Description |
|---|---|---|
| GET | /api/events | List events |
| GET | /api/events/{id} | Event details |
| GET | /api/events/{id}/seats | Seat map: AVAILABLE / HELD / BOOKED |
| POST | /api/reservations | Hold seats `{userId, eventId, seatIds[]}` (201, or 409) |
| GET | /api/reservations/{id}?userId= | Current hold + seconds remaining (410 if expired) |
| DELETE | /api/reservations/{id}?userId= | Cancel a hold (204) |
| POST | /api/bookings | Confirm a hold `{reservationId, userId}` |
| GET | /api/bookings/{id} | Booking details |
| GET | /api/bookings?userId= | A user's bookings |

## Concurrency design
- **Redis** decides who gets a seat: a Lua script checks and sets all requested seat keys atomically.
- **PostgreSQL** remains the final authority: `SELECT ... FOR UPDATE` plus a unique constraint on `booking_seats.seat_id`.
