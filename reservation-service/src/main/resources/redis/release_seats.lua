-- Release a reservation's seats (user pressed Cancel).
--
-- KEYS[1..n-1] = seat hold keys, KEYS[n] = reservation key
-- ARGV[1]      = reservation id
--
-- Only deletes a seat key if it still belongs to THIS reservation. If our hold
-- already expired and someone else grabbed the seat, we must not free their hold.
-- Returns the number of seats released.

local released = 0
for i = 1, #KEYS - 1 do
  if redis.call('GET', KEYS[i]) == ARGV[1] then
    redis.call('DEL', KEYS[i])
    released = released + 1
  end
end
redis.call('DEL', KEYS[#KEYS])
return released
