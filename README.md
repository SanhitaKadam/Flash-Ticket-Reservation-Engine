# Flash Ticket Reservation Engine

High-concurrency ticket reservation system, built incrementally.

## Status
- [x] Phase 1: Spring Boot + PostgreSQL basic booking API
- [x] Phase 2: React frontend (Vite)
- [ ] Phase 3+: Redis, idempotency, Kafka, ledger service, gateway, Docker, Kubernetes, k6

## Run (Windows / PowerShell), two terminals

**Terminal 1: backend**

    $env:DB_USERNAME = "flash_app"
    $env:DB_PASSWORD = "your_password"
    cd reservation-service
    mvn spring-boot:run

**Terminal 2: frontend**

    cd frontend
    npm install
    npm run dev

Open http://localhost:5173

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
| GET | /api/events/{id}/seats | Seat map with status |
| POST | /api/bookings | Book seats `{userId, eventId, seatIds[]}` |
| GET | /api/bookings/{id} | Booking details |
| GET | /api/bookings?userId= | A user's bookings (Phase 2) |

## Double-booking protection
`SELECT ... FOR UPDATE` row locks inside one transaction, plus a unique constraint on `booking_seats.seat_id` as a final safety net.
