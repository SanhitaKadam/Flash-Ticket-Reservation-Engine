-- Atomically hold one or more seats for one reservation.
--
-- KEYS[1..n-1] = seat hold keys      (e.g. seat:hold:1:5)
-- KEYS[n]      = reservation key     (e.g. reservation:<uuid>)
-- ARGV[1]      = reservation id      (stored as the seat key's value = "who holds it")
-- ARGV[2]      = TTL in seconds      (the hold window; Redis deletes the keys itself afterwards)
-- ARGV[3]      = reservation JSON    (so we can find the seats again at confirm time)
--
-- Returns 0 on success, otherwise the 1-based index of the first seat already held.
--
-- Redis runs a script start-to-finish with nothing else interleaved, so the
-- "check" loop and the "set" loop below can never be split by another client.

local seatCount = #KEYS - 1

for i = 1, seatCount do
  if redis.call('EXISTS', KEYS[i]) == 1 then
    return i
  end
end

for i = 1, seatCount do
  redis.call('SET', KEYS[i], ARGV[1], 'EX', ARGV[2])
end

redis.call('SET', KEYS[#KEYS], ARGV[3], 'EX', ARGV[2])
return 0
