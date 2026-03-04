-- KEYS:
-- 1 stockKey: promo:stock:{promoId}:{skuId}
-- 2 buyKey:   promo:buy:{promoId}:{skuId}:{userId}
-- 3 rlKey:    rl:promo:{promoId}:{window}
--
-- ARGV:
-- 1 nowTsMs
-- 2 startTsMs
-- 3 endTsMs
-- 4 buyTtlSeconds
-- 5 qpsLimit
-- 6 rlTtlSeconds

local now = tonumber(ARGV[1])
local startTs = tonumber(ARGV[2])
local endTs = tonumber(ARGV[3])

if now < startTs then
	return 11 -- NOT_STARTED
end
if now > endTs then
	return 12 -- ENDED
end

-- rate limit
local c = redis.call("INCR", KEYS[3])
if c == 1 then
	redis.call("EXPIRE", KEYS[3], tonumber(ARGV[6]))
end
if c > tonumber(ARGV[5]) then
	return 13 -- RATE_LIMITED
end

-- duplicate buy
if redis.call("EXISTS", KEYS[2]) == 1 then
	return 14 -- DUPLICATE
end

-- stock
local stock = tonumber(redis.call("GET", KEYS[1]) or "-1")
if stock <= 0 then
	return 15 -- SOLD_OUT
end

redis.call("DECR", KEYS[1])
redis.call("SET", KEYS[2], "1", "EX", tonumber(ARGV[4]))
return 0 -- OK