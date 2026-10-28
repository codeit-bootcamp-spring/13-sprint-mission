package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.events.UserCacheClearEvent;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "in-memory",
        matchIfMissing = true
)
public class InMemoryJwtRegistry implements JwtRegistry{

    private final Map<UUID , Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

    private final JwtTokenProvider jwtTokenProvider;

    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final UserOnlineTrackingRepository userOnlineTrackingRepository;

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userDto().id();
        Integer maxActiveJwtCount = jwtTokenProvider.getMaxActiveJwtCount();
        origin.compute(userId, (key, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }

            while (queue.size() >= maxActiveJwtCount) {
                queue.poll();
            }
            queue.add(jwtInformation);
            userOnlineTrackingRepository.add(userId);
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.compute(userId , (key, queue) ->{
            userOnlineTrackingRepository.remove(userId);
            // compute()에서 null을 반환하면 해당 사용자의 JWT 항목이 제거됩니다.
            return null;
        });
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(UUID userId, String refreshToken) {
        // 등록·로테이션과 같은 사용자별 잠금 안에서 비교와 삭제를 완료한다.
        origin.computeIfPresent(userId, (key, queue) -> {
            boolean removed = queue.removeIf(info -> info.refreshToken().equals(refreshToken));
            if (removed && queue.isEmpty()) {
                userOnlineTrackingRepository.remove(userId);
                return null;
            }
            return queue;
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> jwtInformations = origin.get(userId);
        if (jwtInformations == null) return false;

       return jwtInformations.stream()
               .anyMatch(jwtInformation -> jwtTokenProvider.validateToken(jwtInformation.refreshToken()).isPresent());
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return origin.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(info -> info.accessToken().equals(accessToken));
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return origin.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(info -> info.refreshToken().equals(refreshToken));

    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userDto().id();
        Integer maxActiveJwtCount = jwtTokenProvider.getMaxActiveJwtCount();
        origin.compute(userId, (key, queue) -> {
            if (queue == null || queue.stream().noneMatch(info -> info.refreshToken().equals(refreshToken))) {
                throw new TokenRenewalFailedException();
            }
            // 검증과 교체를 사용자별로 원자적으로 처리하여 같은 토큰의 중복 갱신을 막는다.
            queue.removeIf(info -> info.refreshToken().equals(refreshToken));
            while (queue.size() >= maxActiveJwtCount) {
                queue.poll();
            }
            queue.add(newJwtInformation);
            userOnlineTrackingRepository.add(userId);
            return queue;
        });
    }

    @Override
    public Set<UUID> findActiveUserIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) return Set.of();

       return userIds.stream()
               .distinct()
               .filter(this::hasActiveJwtInformationByUserId)
               .collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> findUserIdsWithValidAccessToken(Collection<UUID> userIds) {
       Instant now = clock.instant();
       return userIds.stream()
                .filter(userId -> hasValidAccessToken(userId, origin.get(userId), now))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpiredJwtInformation() {
        // 등록·로테이션과 같은 사용자별 잠금 안에서 검사와 삭제를 완료한다.
        Set<UUID> changedUserIds = new HashSet<>();
        origin.keySet().forEach(userId -> origin.computeIfPresent(userId, (key, queue) -> {
            boolean isRemoved = queue.removeIf(info -> jwtTokenProvider.validateToken(info.refreshToken()).isEmpty());
            if (isRemoved) {
                changedUserIds.add(userId);
            }
            return queue.isEmpty() ? null : queue;
        }));

        changedUserIds.forEach(userId -> eventPublisher.publishEvent(new UserCacheClearEvent(userId)));
    }

    /**
     * 등록·갱신과 동일한 사용자별 compute 안에서 유효 액세스 토큰을 검사한다.
     * 오프라인이고 추적 항목을 실제 제거했을 때만 true를 반환한다.
     * 보호 범위는 이 인스턴스의 Map이며 여러 서버의 상태를 동기화하지 않는다.
     */
    @Override
    public boolean tryRemoveOfflineTracking(UUID userId){
        // 람다 안의 제거 결과를 반환하기 위한 값이다. 동시 변경 보호 자체는 compute가 담당한다.
        AtomicBoolean removed = new AtomicBoolean(false);
        origin.compute(userId, (key, tokens)->{
            if(!hasValidAccessToken(userId, tokens, clock.instant())){
                removed.set(userOnlineTrackingRepository.remove(userId));
            }
            return tokens;
        });

        return removed.get();
    }

    private boolean hasValidAccessToken(UUID userId, Collection<JwtInformation> tokens, Instant now){
        if (tokens == null) {
            return false;
        }

        return tokens.stream()
                .filter(info -> jwtTokenProvider.validateToken(info.refreshToken()).isPresent())
                .flatMap(info -> jwtTokenProvider.validateToken(info.accessToken()).stream())
                .filter(claims -> JwtTokenProvider.TokenType.ACCESS.name().equals(jwtTokenProvider.getTokenType(claims)))
                .filter(claims ->  userId.equals(jwtTokenProvider.getUserId(claims)))
                .anyMatch(claims -> claims.getExpiration() != null && claims.getExpiration().toInstant().isAfter(now));
    }

}
