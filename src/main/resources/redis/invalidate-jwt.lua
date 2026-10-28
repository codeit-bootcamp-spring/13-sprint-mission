-- 요청한 Refresh Token과 일치하는 세션만 로그아웃한다.
-- 이전 로그인/갱신의 토큰으로 뒤늦게 로그아웃해도 현재의 다른 세션은 삭제하지 않는다.
-- 조회·일치 확인·삭제를 한 스크립트에서 처리하여 다른 인스턴스의 변경과 중간에 섞이지 않는다.
--
-- KEYS[1]: 사용자별 키(discodeit:jwt:{userId}). 중괄호 부분은 실제 사용자 UUID를 뜻한다.
-- KEYS[2]: 온라인 추적 사용자 Set의 키.
-- ARGV[1]: 삭제할 Refresh Token 문자열.
-- ARGV[2]: 로그아웃할 사용자 UUID 문자열.
-- 반환값: 1 = 일치하는 유효 세션 삭제 성공, 0 = 키 또는 일치하는 유효 세션 없음.
-- 저장된 JSON은 세션 배열이며 각 원소에는 accessToken, refreshToken,
-- refreshExpiresAt(Refresh Token의 절대 만료 시각, Unix epoch 밀리초)이 있다.
--
-- KEYS/ARGV의 인덱스는 1부터 시작한다. redis/cjson은 Redis 실행 환경에서 제공하며,
-- table/math/string은 별도 선언 없이 사용하는 Lua 표준 라이브러리다.
-- cjson.decode/encode는 JSON과 Lua table을 서로 변환한다.
local key = KEYS[1]
local trackingKey = KEYS[2]
local targetRefreshToken = ARGV[1]
local userId = ARGV[2]

local raw = redis.call('GET', key)

-- 없는 키에 대한 GET은 false다. 이미 로그아웃/자연 만료된 경우 추가 작업 없이 끝낸다.
if not raw then
    return 0
end

-- TIME[1]은 초, TIME[2]는 마이크로초 부분이다. Redis 서버 시각을 밀리초로 환산한다.
local time = redis.call('TIME')
local now = tonumber(time[1]) * 1000
            + math.floor(tonumber(time[2]) / 1000)

local storedTokens = cjson.decode(raw)
-- {}는 새 table이며 여기서는 남길 세션의 배열이다.
local remainingTokens = {}
local removed = false
local expiresAt = 0

-- ipairs로 배열을 처음부터 끝까지 순회한다. 배열은 만료순이 아니므로 조기 종료하지 않는다.
-- 만료 세션은 남기지 않으며 유효한 세션 중에서 요청 토큰과 일치하는 것만 삭제 대상으로 삼는다.
for _, token in ipairs(storedTokens) do
    if token.refreshExpiresAt > now then
        if token.refreshToken == targetRefreshToken then
            -- 삭제 대상은 remainingTokens에 추가하지 않는다.
            removed = true
        else
            -- 다른 세션은 table.insert로 끝에 추가하여 기존 상대 순서를 유지한다.
            table.insert(remainingTokens, token)
            -- 삭제한 세션은 TTL 계산에서 제외한다. 남은 세션의 가장 늦은 만료 시각을 구한다.
            expiresAt = math.max(expiresAt, token.refreshExpiresAt)
        end
    end
end

-- 일치하는 유효 토큰이 없으면 현재 Redis 값을 그대로 둔다.
-- 만료 제외 등 위의 계산은 Lua 메모리에서만 수행했으며 아직 저장하지 않았다.
if not removed then
    return 0
end

-- #은 연속된 배열의 길이다. 마지막 세션까지 삭제되면 키도 제거하여 즉시 오프라인이 된다.
if #remainingTokens == 0 then
    redis.call('DEL', key)
    -- 마지막 세션 삭제와 추적 제거를 함께 수행해 이미 로그아웃한 사용자의 후속 감시를 끝낸다.
    redis.call('SREM', trackingKey, userId)
else
    -- 세션이 남으면 JSON과 TTL을 함께 갱신한다. 가장 오래 유지되던 세션을 삭제했다면
    -- 키 만료도 남은 세션에 맞춰 앞당겨야 존재 여부로 온라인 상태를 정확히 판단할 수 있다.
    -- PXAT는 밀리초 단위 절대 시각이며 %.0f는 지수 표기/소수점 없는 정수 문자열을 만든다.
    redis.call(
        'SET',
        key,
        cjson.encode(remainingTokens),
        'PXAT',
        string.format('%.0f', expiresAt)
    )
end

return 1
