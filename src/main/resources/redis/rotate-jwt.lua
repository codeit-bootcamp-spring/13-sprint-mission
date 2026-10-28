-- 기존 Refresh Token에 해당하는 세션을 새 토큰 쌍으로 교체한다.
-- 기존 토큰의 존재 확인과 교체가 하나의 스크립트 안에서 끝나므로 동일한 기존 토큰으로
-- 동시에 갱신해도 첫 요청만 성공하고, 다음 요청은 기존 토큰을 찾지 못해 실패한다.
--
-- KEYS[1]: 사용자별 키(discodeit:jwt:{userId}). 중괄호 부분은 실제 사용자 UUID를 뜻한다.
-- KEYS[2]: 온라인 추적 사용자 Set의 키.
-- ARGV[1]: 교체할 기존 Refresh Token 문자열.
-- ARGV[2]: 새 세션 JSON(accessToken, refreshToken, refreshExpiresAt).
--          refreshExpiresAt은 Refresh Token의 절대 만료 시각(Unix epoch 밀리초)이다.
-- ARGV[3]: 동시에 유지할 최대 세션 수를 나타내는 문자열.
-- ARGV[4]: 갱신할 사용자 UUID 문자열.
-- 반환값: 1 = 갱신 성공, 0 = 유효한 기존 세션 없음, -1 = 새 Refresh Token 만료.
--         최대 세션 수가 잘못된 경우 숫자 대신 Redis 오류를 반환한다.
--
-- KEYS/ARGV는 Redis가 제공하는 입력 배열이며 첫 인덱스는 1이다.
-- redis/cjson은 Redis 실행 환경에서, table/math/string은 Lua 표준 라이브러리에서 제공한다.
-- cjson.decode/encode는 JSON과 Lua table을 서로 변환한다.
local key = KEYS[1]
local trackingKey = KEYS[2]
local oldRefreshToken = ARGV[1]
local newToken = cjson.decode(ARGV[2])
-- 문자열을 숫자로 변환한 뒤 양의 정수인지 검사한다. 변환 실패 시 tonumber는 nil이다.
local maxCount = tonumber(ARGV[3])
local userId = ARGV[4]

if not maxCount or maxCount < 1 or maxCount ~= math.floor(maxCount) then
    return redis.error_reply('ERR 최대 로그인 개수는 1 이상의 정수여야 합니다.')
end

-- TIME은 [초, 마이크로초 부분]을 반환한다. Redis 시각을 밀리초로 바꿔 만료를 판단한다.
local time = redis.call('TIME')
local now = tonumber(time[1]) * 1000
            + math.floor(tonumber(time[2]) / 1000)

-- Java 검증 이후 만료된 새 토큰으로 기존 세션을 교체하지 않는다.
if newToken.refreshExpiresAt <= now then
    return -1
end

local raw = redis.call('GET', key)

-- GET은 키가 없으면 false를 반환한다. 로그아웃/자연 만료된 사용자를 갱신으로 재등록하지 않는다.
if not raw then
    return 0
end

local storedTokens = cjson.decode(raw)
-- {}는 새 table이다. 여기서는 남길 세션을 연속 인덱스 배열로 모은다.
local activeTokens = {}
local found = false

-- ipairs는 배열을 1부터 순회한다. 만료 세션은 제외하고, 유효한 기존 토큰의 일치를 확인한다.
-- 배열은 만료순이 아니므로 만료 세션을 만나도 끝까지 확인해야 한다.
for _, token in ipairs(storedTokens) do
    if token.refreshExpiresAt > now then
        if token.refreshToken == oldRefreshToken then
            -- 교체 대상은 새 배열에 넣지 않는다. 나중에 새 세션으로 대체한다.
            found = true
        else
            -- table.insert의 인덱스를 생략하면 끝에 추가되어 다른 세션의 상대 순서가 유지된다.
            table.insert(activeTokens, token)
        end
    end
end

-- 이미 갱신되었거나 로그아웃/만료된 토큰이면 실패한다.
-- 지금까지는 Lua table만 바꿨으므로 이 반환 경로에서는 Redis 저장 내용이 변경되지 않는다.
if not found then
    return 0
end

-- #은 배열 길이이며 remove(..., 1)은 가장 오래된 세션을 제거하고 인덱스를 당긴다.
-- 설정된 제한이 줄어든 경우에도 새 세션을 넣은 뒤 최대 개수를 넘지 않도록 자리를 확보한다.
while #activeTokens >= maxCount do
    table.remove(activeTokens, 1)
end

-- 갱신된 세션은 가장 최근 활동으로 취급하여 배열 마지막에 추가한다.
table.insert(activeTokens, newToken)

-- 남은 세션 중 가장 늦은 Refresh Token 만료 시각까지 사용자 키를 유지한다.
local expiresAt = 0

for _, token in ipairs(activeTokens) do
    expiresAt = math.max(expiresAt, token.refreshExpiresAt)
end

-- 검사가 모두 끝난 뒤 JSON과 만료 시각을 SET 한 번으로 저장한다.
-- PXAT는 밀리초 단위의 절대 시각이다. %.0f는 지수 표기/소수점 없는 정수 문자열을 만든다.
redis.call(
    'SET',
    key,
    cjson.encode(activeTokens),
    'PXAT',
    string.format('%.0f', expiresAt)
)

-- JWT 저장과 같은 실행 안에서 추적을 등록한다. 이후 액세스 토큰 만료를 모니터가 감지한다.
redis.call('SADD', trackingKey, userId)

return 1
