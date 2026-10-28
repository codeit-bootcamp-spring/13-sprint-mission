-- KEYS[1]: 이벤트 순번 카운터
-- KEYS[2]: 이벤트 순서를 저장하는 Sorted Set
-- KEYS[3]: 이벤트 내용을 저장하는 Hash
-- ARGV[1]: eventId
-- ARGV[2]: SseMessage 전체 JSON
-- ARGV[3]: 발행할 Pub/Sub 토픽

local eventId = ARGV[1]
local messageJson = ARGV[2]
local topic = ARGV[3]

-- 동일 이벤트는 다시 저장하거나 발행하지 않습니다.
if redis.call('HEXISTS', KEYS[3], eventId) == 1 then
    return 0
end

local sequence = redis.call('INCR', KEYS[1])

-- 구독자가 알림을 받자마자 순번과 본문을 조회할 수 있도록 저장을 먼저 완료한다.
-- Redis 안의 처리 순서만 보장하며 DB 트랜잭션과의 원자성이나 구독 중단 중 전달은 보장하지 않는다.
redis.call('HSET', KEYS[3], eventId, messageJson)
redis.call('ZADD', KEYS[2], sequence, eventId)
redis.call('PUBLISH', topic, messageJson)

return 1