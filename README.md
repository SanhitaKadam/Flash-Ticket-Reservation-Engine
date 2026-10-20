# Flash Ticket Reservation Engine

High-concurrency ticket reservation system, built incrementally.

## Status
- [x] Phase 1: Spring Boot + PostgreSQL basic booking (this zip)
- [ ] Phase 2: Frontend
- [ ] Phase 3+: Redis, idempotency, Kafka, ledger service, gateway, Docker, Kubernetes, k6

## Phase 1 setup (Windows / PowerShell)

1. Create the database (use the full path if psql is not on PATH):

       & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost

       CREATE USER flash_app WITH PASSWORD 'CHOOSE_A_PASSWORD';
       CREATE DATABASE flashticket OWNER flash_app;
       \q

2. Set credentials as environment variables (never commit them):

       $env:DB_USERNAME = "flash_app"
       $env:DB_PASSWORD = "your_password"

3. Run:

       cd reservation-service
       mvn spring-boot:run

4. Test with `reservation-service/requests.http` (VS Code REST Client extension).

## API
| Method | Path | Description |
|---|---|---|
| GET | /api/events | List events |
| GET | /api/events/{id} | Event details |
| GET | /api/events/{id}/seats | Seat map with status |
| POST | /api/bookings | Book seats `{userId, eventId, seatIds[]}` |
| GET | /api/bookings/{id} | Booking details |

## Double-booking protection
`SELECT ... FOR UPDATE` row locks inside one transaction, plus a unique constraint on `booking_seats.seat_id` as a final safety net.
