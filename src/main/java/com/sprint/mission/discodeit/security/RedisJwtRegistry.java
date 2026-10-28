package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.redis.UserOnlineRedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisJwtRegistry implements JwtRegistry{

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private static final RedisScript<Long> REGISTER_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/register-jwt.lua"),
            Long.class
    );

    private static final RedisScript<Long> ROTATE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/rotate-jwt.lua"),
            Long.class
    );

    private static final RedisScript<Long> INVALIDATE_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/invalidate-jwt.lua"),
            Long.class
    );

    private static final RedisScript<Long> INVALIDATE_ALL_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/invalidate-all-jwt.lua"),
            Long.class
    );

    private static final RedisScript<Long> REMOVE_OFFLINE_TRACKING_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/remove-offline-tracking.lua"),
            Long.class
    );

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userDto().id();

        long result = executeJwtScript(
                REGISTER_SCRIPT,
                userId,
                serializeJwtInformation(jwtInformation),
                jwtTokenProvider.getMaxActiveJwtCount().toString(),
                userId.toString()
        );

        if(result == -1L){
            throw new IllegalArgumentException("저장 전에 Refresh Token이 만료되었습니다.");
        }

        if(result != 1L){
            throw new IllegalStateException("JWT 등록 결과가 올바르지 않습니다. userId="+userId);
        }

    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        long result = executeJwtScript(
                INVALIDATE_ALL_SCRIPT,
                userId,
                userId.toString()
        );

        if(result != 1L){
            throw new IllegalStateException("JWT 전체 무효화 결과가 올바르지 않스니다. userId=" + userId);
        }
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(UUID userId, String refreshToken) {
        long result = executeJwtScript(
                INVALIDATE_SCRIPT,
                userId,
                refreshToken,
                userId.toString()
        );

        if (result != 0L && result != 1L){
            throw new IllegalStateException("JWT 로그아웃 결과가 올바르지 않습니다. userId="+ userId);
        }
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(jwtKey(userId)));
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return hasActiveToken(accessToken, JwtTokenProvider.TokenType.ACCESS);
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return hasActiveToken(refreshToken, JwtTokenProvider.TokenType.REFRESH);
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userDto().id();

        long result = executeJwtScript(
                ROTATE_SCRIPT,
                userId,
                refreshToken,
                serializeJwtInformation(newJwtInformation),
                jwtTokenProvider.getMaxActiveJwtCount().toString(),
                userId.toString()
        );

        if (result == 0L || result == -1L){
            throw new TokenRenewalFailedException();
        }

        if(result != 1L){
            throw new IllegalStateException("JWT 갱신 결과가 올바르지 않습니다. userId="+userId);
        }
    }

    @Override
    public Set<UUID> findActiveUserIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()){
            return Set.of();
        }

        List<UUID> distinctUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        Set<UUID> activeUserIds = new HashSet<>();

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) con -> {
            distinctUserIds.forEach(userId -> {
                byte[] key = redisTemplate.getStringSerializer().serialize(jwtKey(userId));

                con.keyCommands().exists(new byte[][]{key});
            });
            return null;
        });

        for (int i = 0; i < distinctUserIds.size(); i++) {
            if (Long.valueOf(1L).equals(results.get(i))) {
                activeUserIds.add(distinctUserIds.get(i));
            }
        }

        return activeUserIds;
    }

    @Override
    public Set<UUID> findUserIdsWithValidAccessToken(Collection<UUID> userIds) {
        if (userIds.isEmpty()) return Set.of();
        long now = clock.millis();

        List<UUID> distinctUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        // GET을 파이프라인으로 묶어 사용자마다 Redis 왕복 통신이 발생하지 않도록 한다.
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            distinctUserIds.forEach(userId -> {
                byte[] key = redisTemplate.getStringSerializer().serialize(jwtKey(userId));
                connection.stringCommands().get(key);
            });
            return null;
        });

        return IntStream.rangeClosed(0, distinctUserIds.size() - 1)
                .filter(i -> hasValidAccessToken(distinctUserIds.get(i), (String) results.get(i), now))
                .mapToObj(distinctUserIds::get)
                .collect(Collectors.toUnmodifiableSet());

    }

    /**
     * 조회한 JWT 스냅샷이 오프라인이면 Lua에서 현재 값과 다시 비교하여 추적을 제거한다.
     * 조회 후 로그인·갱신으로 값이 바뀌면 제거하지 않아 새 온라인 세션을 보호한다.
     * 동일 스냅샷을 여러 서버가 처리해도 SREM에 실제 성공한 호출만 true를 받는다.
     */
    @Override
    public boolean tryRemoveOfflineTracking(UUID userId){
        String json = redisTemplate.opsForValue().get(jwtKey(userId));

        if (hasValidAccessToken(userId, json, clock.millis())){
            return false;
        }

        // 없던 키에 새 JWT가 생기는 경우도 감지하도록 존재 여부와 JSON 원문을 함께 비교한다.
        long result = executeJwtScript(REMOVE_OFFLINE_TRACKING_SCRIPT,
                userId,
                json == null ? "0" : "1",
                json == null ? "" : json,
                userId.toString()
        );

        if(result != 0L && result != 1L){
            throw new IllegalStateException("오프라인 사용자 추적 제거 결과가 올바르지 않습니다. userId=" + userId);
        }

        return result == 1L;
    }

    private boolean hasValidAccessToken(UUID userId, String json, long now) {
        return deserializeStoredTokens(userId, json).stream()
                .filter(info -> info.refreshExpiresAt() > now)
                .flatMap(info -> jwtTokenProvider.validateToken(info.accessToken()).stream())
                .filter(claims -> JwtTokenProvider.TokenType.ACCESS.name()
                        .equals(jwtTokenProvider.getTokenType(claims)))
                .filter(claims -> userId.equals(jwtTokenProvider.getUserId(claims)))
                .anyMatch(claims -> claims.getExpiration() != null
                        && claims.getExpiration().getTime() > now);
    }

    private String jwtKey(UUID userId) {
        return "discodeit:jwt:"+userId;
    }

    private boolean hasActiveToken(String token, JwtTokenProvider.TokenType tokenType){
        return jwtTokenProvider.validateToken(token)
                .filter(claims -> tokenType.name().equals(jwtTokenProvider.getTokenType(claims)))
                .map(claims -> {
                    UUID userId = jwtTokenProvider.getUserId(claims);
                    long now = clock.millis();

                    return readStoredTokens(userId).stream()
                            .filter(info -> info.refreshExpiresAt() > now)
                            .anyMatch(info -> switch (tokenType){
                                case ACCESS -> token.equals(info.accessToken());
                                case REFRESH -> token.equals(info.refreshToken());
                            });
                })
                .orElse(false);
    }

    private List<StoredJwtInformation> readStoredTokens(UUID userId){
        String json = redisTemplate.opsForValue().get(jwtKey(userId));
        return deserializeStoredTokens(userId, json);
    }

    private List<StoredJwtInformation> deserializeStoredTokens(UUID userId, String json) {
        if (json == null) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<StoredJwtInformation>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "JWT 등록 정보를 읽을 수 없습니다. userId=" + userId,
                    e
            );
        }
    }

    private String serializeJwtInformation(JwtInformation information){
        return jwtTokenProvider.validateToken(information.refreshToken())
                .filter(claims -> JwtTokenProvider.TokenType.REFRESH.name()
                        .equals(jwtTokenProvider.getTokenType(claims)))
                .filter(claims -> information.userDto().id()
                        .equals(jwtTokenProvider.getUserId(claims)))
                .filter(claims -> claims.getExpiration() != null)
                .map(claims -> new StoredJwtInformation(
                        information.accessToken(),
                        information.refreshToken(),
                        claims.getExpiration().getTime()
                ))
                .map(stored -> convertStoredJwtToJson(information.userDto().id(), stored))
                .orElseThrow(() -> new IllegalArgumentException("등록할 Refresh Token이 올바르지 않습니다."))
                ;
    }

    private String convertStoredJwtToJson(UUID userId, StoredJwtInformation stored) {
        try {
            return objectMapper.writeValueAsString(stored);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "JWT 등록 정보를 변환할 수 없습니다. userId="
                            + userId,
                    e
            );
        }
    }

    /**
     * 사용자 JWT 키와 공통 추적 Set 키를 같은 순서로 Lua에 전달한다.
     * 각 스크립트는 JWT 변경과 추적 목록 변경을 하나의 Redis 실행 안에서 처리한다.
     */
    private long executeJwtScript(RedisScript<Long> script, UUID userId, Object... arguments){
        Long result = redisTemplate.execute(
                script,
                List.of(jwtKey(userId) , UserOnlineRedisKeys.TRACKED_USERS_KEY),
                arguments
        );

        if(result == null){
            throw new IllegalArgumentException("JWT 처리 결과를 확인할 수 없습니다. userId=" + userId);
        }

        return result;
    }

    private record StoredJwtInformation(
            String accessToken,
            String refreshToken,
            long refreshExpiresAt
    ) {
    }
}
