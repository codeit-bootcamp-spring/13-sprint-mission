-- 사용자에게 새 로그인 세션을 등록한다. 같은 사용자 키에 대한 조회·개수 제한·저장을
-- 하나의 스크립트에서 처리하므로 다른 인스턴스의 로그인/갱신/로그아웃과 중간에 섞이지 않는다.
--
-- KEYS[1]: 사용자별 키(discodeit:jwt:{userId}). 중괄호 부분은 실제 사용자 UUID를 뜻한다.
-- KEYS[2]: 온라인 추적 사용자 Set의 키.
-- ARGV[1]: 새 세션의 JSON. accessToken/refreshToken은 토큰 문자열이며,
--          refreshExpiresAt은 Refresh Token의 절대 만료 시각(Unix epoch 밀리초)이다.
-- ARGV[2]: 동시에 유지할 최대 세션 수. Java에서 문자열로 전달한다.
-- ARGV[3]: 등록할 사용자 UUID 문자열.
-- 반환값: 1 = 등록 성공, -1 = 저장 전에 새 Refresh Token이 만료됨.
--         최대 세션 수가 잘못된 경우 숫자 대신 Redis 오류를 반환한다.
--
-- KEYS/ARGV는 Redis가 제공하는 입력 배열이며 인덱스는 1부터 시작한다.
-- redis/cjson은 Redis 실행 환경에서 제공하고, table/math/string은 Lua 표준 라이브러리다.
-- 별도로 table을 선언하지 않아도 table.insert/remove를 사용할 수 있다.
-- cjson.decode는 JSON을 Lua table로, cjson.encode는 Lua table을 JSON으로 변환한다.
local key = KEYS[1]
local trackingKey = KEYS[2]

local newToken = cjson.decode(ARGV[1])
-- ARGV는 문자열이므로 계산 전에 숫자로 변환한다. 변환할 수 없으면 nil이다.
local maxCount = tonumber(ARGV[2])
local userId = ARGV[3]

-- 0/음수/소수는 허용하지 않는다. math.floor와 비교하여 정수 여부를 확인한다.
if not maxCount or maxCount < 1 or maxCount ~= math.floor(maxCount) then
    return redis.error_reply('ERR 최대 로그인 개수는 1 이상의 정수여야 합니다.')
end

-- 인스턴스별 애플리케이션 시계 대신 Redis 서버 시각으로 저장 시점의 만료를 판단한다.
-- TIME[1]은 초, TIME[2]는 해당 초의 마이크로초 부분이다. 두 값을 밀리초로 합친다.
local time = redis.call('TIME')
local now = tonumber(time[1]) * 1000
                + math.floor(tonumber(time[2]) / 1000)

-- Java 검증 후 Redis에 도착하는 사이 만료될 수도 있으므로 쓰기 전에 다시 확인한다.
if newToken.refreshExpiresAt <= now then
    return -1
end

-- 없는 키에 대한 GET 결과는 false다. {}는 비어 있는 Lua table을 새로 만든다.
-- 여기서는 연속된 숫자 인덱스에 세션을 담는 배열 용도로 사용한다.
local raw = redis.call('GET', key)
local storedTokens = {}

if raw then
    storedTokens = cjson.decode(raw)
end

-- 저장된 배열은 등록·갱신 순서다. 만료된 세션은 로그인 개수에 포함하지 않는다.
local activeTokens = {}

-- ipairs는 인덱스 1부터 순서대로 순회한다. 만료 시각으로 정렬된 배열이 아니므로
-- 만료 세션을 만나도 return/break로 끝내면 안 된다. 뒤에 유효한 세션이 있을 수 있다.
for _, token in ipairs(storedTokens) do
    if token.refreshExpiresAt > now then
        -- table.insert에 인덱스를 생략하면 배열 마지막에 추가한다.
        table.insert(activeTokens, token)
    end
end

-- #은 연속된 배열의 길이다. 새 세션을 추가할 한 자리까지 확보한다.
-- table.remove(..., 1)은 가장 오래된 세션을 제거하고 뒤 원소의 인덱스를 당긴다.
while #activeTokens >= maxCount do
    table.remove(activeTokens, 1)
end

-- 새 로그인은 가장 최근 세션이므로 배열 마지막에 둔다.
table.insert(activeTokens, newToken)

-- 사용자 키는 마지막 유효 세션이 만료될 때까지 살아 있어야 한다.
-- 가장 빠른 만료 시각을 사용하면 다른 유효 세션까지 함께 사라진다.
local expiresAt = 0

for _, token in ipairs(activeTokens) do
    expiresAt = math.max(expiresAt, token.refreshExpiresAt)
end

-- 위 계산은 Lua 메모리에서만 수행하고 최종 JSON과 만료 시각을 SET 한 번으로 저장한다.
-- PXAT는 TTL 길이가 아니라 절대 만료 시각(밀리초)을 받는다.
-- %.0f는 큰 숫자가 지수 표기나 소수점 없이 정수 문자열로 전달되도록 한다.
redis.call(
    'SET',
    key,
    cjson.encode(activeTokens),
    'PXAT',
    string.format('%.0f',expiresAt)
)

-- JWT 저장과 같은 실행 안에서 추적을 등록한다. 이후 액세스 토큰 만료를 모니터가 감지한다.
redis.call('SADD', trackingKey, userId)

return 1
