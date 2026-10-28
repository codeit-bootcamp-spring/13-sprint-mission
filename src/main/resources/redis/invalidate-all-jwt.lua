-- KEYS[1]: 사용자의 JWT 저장 키.
-- KEYS[2]: 온라인 추적 사용자 Set의 키.
-- ARGV[1]: 무효화할 사용자 UUID 문자열.
-- 반환값: 1 = 처리 완료. 삭제할 항목이 없어도 정상 처리한다.

local key = KEYS[1]
local trackingKey=  KEYS[2]
local userId = ARGV[1]

-- 전체 세션 무효화와 추적 해제를 원자적으로 묶어 다른 인스턴스의 등록이 중간에 끼지 않게 한다.
redis.call('DEL', key)
redis.call('SREM', trackingKey, userId)

return 1