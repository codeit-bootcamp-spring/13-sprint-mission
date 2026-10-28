-- KEYS[1]: 사용자의 JWT 저장 키.
-- KEYS[2]: 온라인 추적 사용자 Set의 키.
-- ARGV[1]: 조회 당시 JWT 키 존재 여부. "1" = 존재, "0" = 없음.
-- ARGV[2]: 조회 당시 JSON 원문. 키가 없었다면 빈 문자열.
-- ARGV[3]: 추적을 제거할 사용자 UUID 문자열.
-- 반환값: 1 = 실제 추적 삭제, 0 = 상태 변경 또는 추적 항목 없음.

local key = KEYS[1]
local trackingKey = KEYS[2]
local expectedExists = ARGV[1]
local expectedJson = ARGV[2]
local userId = ARGV[3]

local currentJson = redis.call('GET', key)

-- Java에서 오프라인으로 판단한 스냅샷이 아직 유효한지 비교한다.
-- 판단 이후 다른 서버가 로그인/갱신했다면 추적을 유지한다.
if expectedExists == '1' then
    if currentJson ~= expectedJson then
        return 0
    end
elseif expectedExists == '0' then
    if currentJson then
        return 0
    end
else
    return redis.error_reply('ERR JWT 존재 여부는 0 또는 1이어야 합니다.')
end

-- SREM 결과 1을 받은 호출만 오프라인 이벤트 발행을 시도한다. 발행 자체는 이 Lua 밖에서 수행한다.
return redis.call('SREM', trackingKey, userId)