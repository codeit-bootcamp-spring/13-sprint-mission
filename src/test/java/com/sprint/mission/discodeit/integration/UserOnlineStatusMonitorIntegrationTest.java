package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.events.UserEvent;
import com.sprint.mission.discodeit.events.UserEventType;
import com.sprint.mission.discodeit.events.UserOnlineStatusMonitor;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.basic.UserReader;
import com.sprint.mission.discodeit.sse.SseEventPublisher;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.sprint.mission.discodeit.support.NotificationTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * 실제 JWT 등록·만료 판단·Spring 이벤트·트랜잭션을 연결해 사용자 상태 변경을 검증한다.
 * 시계를 조절하고 스케줄러를 대체하여 시간 경과를 재현하며 SSE 발행자는 Mock으로 관찰한다.
 * 커밋/롤백에 따른 발행을 확인하되 실제 브라우저 수신이나 Redis 전파까지 검증하지는 않는다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-online-status-monitor;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "discodeit.storage.type=local",
        "discodeit.async.enabled=false",
        "discodeit.security.jwt.registry-type=in-memory",
        "discodeit.security.jwt.access-token-validity=10m",
        "discodeit.security.jwt.refresh-token-validity=1h"
})
@ActiveProfiles("test")
@RecordApplicationEvents
@Import(UserOnlineStatusMonitorIntegrationTest.ClockConfiguration.class)
@DisplayName("사용자 온라인 상태 감지 이벤트 통합 테스트")
class UserOnlineStatusMonitorIntegrationTest {

    private static final Instant INITIAL_TIME = Instant.parse("2026-10-18T00:00:00Z");

    @Autowired UserOnlineStatusMonitor monitor;
    @Autowired UserReader userReader;
    @Autowired UserMapper userMapper;
    @Autowired UserRepository users;
    @Autowired JwtRegistry jwtRegistry;
    @Autowired JwtTokenProvider tokens;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired ApplicationEvents recordedEvents;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired CacheManager cacheManager;
    @Autowired AdjustableClock clock;
    @Autowired UserOnlineTrackingRepository trackingRepository;

    @MockitoBean SseEventPublisher sseEventPublisher;

    // 주기 작업은 실행하지 않고 테스트에서 검사 시점을 직접 결정한다.
    @MockitoBean(name = "taskScheduler") TaskScheduler taskScheduler;

    TransactionTemplate transaction;
    UserDto onlineUser;
    JwtInformation jwtInformation;

    @BeforeEach
    void setUp() {
        clock.set(INITIAL_TIME);
        transaction = new TransactionTemplate(transactionManager);
        User savedUser = users.saveAndFlush(user());
        onlineUser = userMapper.toDto(savedUser, true);
        jwtInformation = new JwtInformation(
                onlineUser,
                tokens.generateAccessToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole()),
                tokens.generateRefreshToken(savedUser.getId(), savedUser.getUsername())
        );
        jwtRegistry.registerJwtInformation(jwtInformation);
        assertThat(trackingRepository.findAll()).contains(onlineUser.id());
        assertThat(userReader.findUserDto(onlineUser.id()).orElseThrow().online()).isTrue();
        recordedEvents.clear();
        clearInvocations(sseEventPublisher);
    }

    @AfterEach
    void cleanUp() {
        if (onlineUser == null) return;

        publisher.publishEvent(UserEvent.deleteEvent(onlineUser));
        jwtRegistry.invalidateJwtInformationByUserId(onlineUser.id());
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.USERS)).evict(onlineUser.id());
        users.deleteById(onlineUser.id());
    }

    @Test
    @DisplayName("트랜잭션 없는 온라인 이벤트도 추적하고 Access Token 만료 시 SSE에 한 번만 오프라인을 전달한다")
    void accessTokenExpiry_broadcastsOfflineUpdateOnceWithoutTransaction() {
        publisher.publishEvent(UserEvent.updateEvent(onlineUser));
        monitor.checkOnlineStatus();
        assertThat(offlineEvents()).isEmpty();
        verify(sseEventPublisher).broadcast("users.updated", onlineUser);

        expireAccessTokenOnly();
        UserDto offlineUser = userReader.findUserDto(onlineUser.id()).orElseThrow();
        assertThat(offlineUser.online()).isFalse();

        monitor.checkOnlineStatus();
        monitor.checkOnlineStatus();

        assertThat(offlineEvents()).containsExactly(UserEvent.updateEvent(offlineUser));
        verify(sseEventPublisher).broadcast("users.updated", offlineUser);
        verifyNoMoreInteractions(sseEventPublisher);
    }

    @Test
    @DisplayName("온라인 이벤트가 없어도 JWT 등록으로 추적하고 만료 갱신을 한 번만 전달한다")
    void registration_tracksExpiryWithoutOnlineEvent() {
        expireAccessTokenOnly();
        UserDto offlineUser = userReader.findUserDto(onlineUser.id()).orElseThrow();

        monitor.checkOnlineStatus();
        monitor.checkOnlineStatus();

        assertThat(offlineEvents()).containsExactly(UserEvent.updateEvent(offlineUser));
        assertThat(trackingRepository.findAll()).doesNotContain(onlineUser.id());
        verify(sseEventPublisher).broadcast("users.updated", offlineUser);
        verifyNoMoreInteractions(sseEventPublisher);
    }

    @Test
    @DisplayName("트랜잭션 안의 사용자 이벤트는 커밋 후 SSE 발행자에게 전달한다")
    void userEvent_broadcastsOnlyAfterCommit() {
        transaction.executeWithoutResult(status -> {
            publisher.publishEvent(UserEvent.updateEvent(onlineUser));
            // 추적 등록은 이미 JWT 등록 시 끝났으며 이벤트 커밋과 독립적이다.
            assertThat(trackingRepository.findAll()).contains(onlineUser.id());
            verifyNoInteractions(sseEventPublisher);
        });

        verify(sseEventPublisher).broadcast("users.updated", onlineUser);
        verifyNoMoreInteractions(sseEventPublisher);
    }

    @Test
    @DisplayName("롤백된 사용자 이벤트는 SSE로 전달하지 않고 기존 JWT 추적은 유지한다")
    void userEvent_rollbackSuppressesBroadcastButPreservesJwtTracking() {
        transaction.executeWithoutResult(status -> {
            publisher.publishEvent(UserEvent.updateEvent(onlineUser));
            status.setRollbackOnly();
        });

        verifyNoInteractions(sseEventPublisher);
        assertThat(trackingRepository.findAll()).contains(onlineUser.id());
        expireAccessTokenOnly();
        UserDto offlineUser = userReader.findUserDto(onlineUser.id()).orElseThrow();
        monitor.checkOnlineStatus();

        assertThat(offlineEvents()).containsExactly(UserEvent.updateEvent(offlineUser));
        verify(sseEventPublisher).broadcast("users.updated", offlineUser);
        verifyNoMoreInteractions(sseEventPublisher);
    }

    private void expireAccessTokenOnly() {
        clock.advance(Duration.ofMinutes(11));
        assertThat(tokens.validateToken(jwtInformation.accessToken())).isEmpty();
        assertThat(tokens.validateToken(jwtInformation.refreshToken())).isPresent();
        assertThat(jwtRegistry.hasActiveJwtInformationByRefreshToken(jwtInformation.refreshToken())).isTrue();
    }

    private List<UserEvent> offlineEvents() {
        return recordedEvents.stream(UserEvent.class)
                .filter(event -> event.type() == UserEventType.UPDATED)
                .filter(event -> event.user().id().equals(onlineUser.id()))
                .filter(event -> !event.user().online())
                .toList();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ClockConfiguration {
        @Bean
        @Primary
        AdjustableClock adjustableClock() {
            return new AdjustableClock(new AtomicReference<>(INITIAL_TIME), ZoneOffset.UTC);
        }
    }

    static class AdjustableClock extends Clock {
        private final AtomicReference<Instant> current;
        private final ZoneId zone;

        AdjustableClock(AtomicReference<Instant> current, ZoneId zone) {
            this.current = current;
            this.zone = zone;
        }

        void set(Instant instant) {
            current.set(instant);
        }

        void advance(Duration duration) {
            current.updateAndGet(instant -> instant.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new AdjustableClock(current, zone);
        }

        @Override
        public Instant instant() {
            return current.get();
        }
    }
}
