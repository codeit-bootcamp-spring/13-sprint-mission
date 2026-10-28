package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.basic.UserReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 모니터의 오프라인 분기와 이벤트 발행 조건을 검증한다.
 * Reader·Registry·추적 저장소는 Mock이며 원자적 제거 성공/실패를 명시해 발행 여부를 확인한다.
 * 이 테스트의 호출 횟수 검증만으로 Redis의 분산 원자성을 증명하지는 않는다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 온라인 상태 감지 컴포넌트 단위 테스트")
class UserOnlineStatusMonitorTest {
    @Mock UserReader userReader;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JwtRegistry jwtRegistry;
    @Mock UserOnlineTrackingRepository trackingRepository;
    @InjectMocks UserOnlineStatusMonitor monitor;

    @Test
    @DisplayName("오프라인 사용자의 추적 제거에 성공한 경우만 갱신 이벤트를 발행한다")
    void checkOnlineStatus_publishesWhenOfflineClaimSucceeds() {
        UUID id = UUID.randomUUID();
        UserDto user = user(id, false);
        given(trackingRepository.findAll()).willReturn(Set.of(id));
        given(userReader.findUserDtosByIds(Set.of(id))).willReturn(Map.of(id, user));
        given(jwtRegistry.tryRemoveOfflineTracking(id)).willReturn(true);

        monitor.checkOnlineStatus();

        verify(eventPublisher).publishEvent(UserEvent.updateEvent(user));
    }

    @Test
    @DisplayName("다른 검사에서 먼저 처리했거나 다시 온라인이면 오프라인 이벤트를 발행하지 않는다")
    void checkOnlineStatus_doesNotPublishWhenOfflineClaimFails() {
        UUID id = UUID.randomUUID();
        given(trackingRepository.findAll()).willReturn(Set.of(id));
        given(userReader.findUserDtosByIds(Set.of(id))).willReturn(Map.of(id, user(id, false)));
        given(jwtRegistry.tryRemoveOfflineTracking(id)).willReturn(false);

        monitor.checkOnlineStatus();

        verify(jwtRegistry).tryRemoveOfflineTracking(id);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("같은 추적 스냅샷을 재검사해도 제거에 성공한 한 번만 이벤트를 발행한다")
    void checkOnlineStatus_doesNotRepublishAfterClaimWasConsumed() {
        UUID id = UUID.randomUUID();
        UserDto user = user(id, false);
        given(trackingRepository.findAll()).willReturn(Set.of(id));
        given(userReader.findUserDtosByIds(Set.of(id))).willReturn(Map.of(id, user));
        given(jwtRegistry.tryRemoveOfflineTracking(id)).willReturn(true, false);

        monitor.checkOnlineStatus();
        monitor.checkOnlineStatus();

        verify(eventPublisher).publishEvent(UserEvent.updateEvent(user));
    }

    @Test
    @DisplayName("온라인 사용자는 추적 제거를 시도하거나 이벤트를 발행하지 않는다")
    void checkOnlineStatus_preservesOnlineUser() {
        UUID id = UUID.randomUUID();
        given(trackingRepository.findAll()).willReturn(Set.of(id));
        given(userReader.findUserDtosByIds(Set.of(id))).willReturn(Map.of(id, user(id, true)));

        monitor.checkOnlineStatus();

        verifyNoInteractions(jwtRegistry, eventPublisher);
        verify(trackingRepository, never()).remove(any());
    }

    @Test
    @DisplayName("추적 대상이 없으면 사용자 조회와 토큰 확인을 생략한다")
    void checkOnlineStatus_skipsEmptyTrackingSet() {
        given(trackingRepository.findAll()).willReturn(Set.of());

        monitor.checkOnlineStatus();

        verifyNoInteractions(userReader, jwtRegistry, eventPublisher);
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 추적 저장소에서 제거한다")
    void checkOnlineStatus_removesMissingUser() {
        UUID id = UUID.randomUUID();
        given(trackingRepository.findAll()).willReturn(Set.of(id));
        given(userReader.findUserDtosByIds(Set.of(id))).willReturn(Map.of());

        monitor.checkOnlineStatus();

        verify(trackingRepository).remove(id);
        verifyNoInteractions(jwtRegistry, eventPublisher);
    }

    @Test
    @DisplayName("일괄 조회한 사용자 중 제거에 성공한 오프라인 사용자만 발행한다")
    void checkOnlineStatus_batchesLookupAndFiltersClaimedOfflineUsers() {
        UUID onlineId = UUID.randomUUID();
        UUID offlineId = UUID.randomUUID();
        UUID alreadyClaimedId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Set<UUID> ids = Set.of(onlineId, offlineId, alreadyClaimedId, missingId);
        UserDto offlineUser = user(offlineId, false);
        given(trackingRepository.findAll()).willReturn(ids);
        given(userReader.findUserDtosByIds(ids)).willReturn(Map.of(
                onlineId, user(onlineId, true), offlineId, offlineUser,
                alreadyClaimedId, user(alreadyClaimedId, false)));
        given(jwtRegistry.tryRemoveOfflineTracking(offlineId)).willReturn(true);
        given(jwtRegistry.tryRemoveOfflineTracking(alreadyClaimedId)).willReturn(false);

        monitor.checkOnlineStatus();

        verify(userReader).findUserDtosByIds(ids);
        // 사용자별 추가 조회가 생기면 일괄 조회 계약을 위반한다.
        verifyNoMoreInteractions(userReader);
        verify(trackingRepository).remove(missingId);
        verify(jwtRegistry, never()).tryRemoveOfflineTracking(onlineId);
        verify(eventPublisher).publishEvent(UserEvent.updateEvent(offlineUser));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("오프라인 이벤트는 최신 토큰 상태를 확인하는 원자적 추적 제거에 위임한다")
    void handleUserEvent_delegatesOfflineRemovalToRegistry() {
        UUID id = UUID.randomUUID();

        monitor.handleUserEvent(UserEvent.updateEvent(user(id, false)));

        verify(jwtRegistry).tryRemoveOfflineTracking(id);
        verifyNoInteractions(trackingRepository, userReader, eventPublisher);
    }

    @Test
    @DisplayName("삭제 이벤트는 사용자의 추적 정보를 제거한다")
    void handleUserEvent_removesDeletedUser() {
        UUID id = UUID.randomUUID();

        monitor.handleUserEvent(UserEvent.deleteEvent(user(id, true)));

        verify(trackingRepository).remove(id);
        verifyNoInteractions(jwtRegistry, userReader, eventPublisher);
    }

    @Test
    @DisplayName("온라인 이벤트는 추적 대상을 다시 등록하지 않는다")
    void handleUserEvent_doesNotRegisterOnlineUser() {
        monitor.handleUserEvent(UserEvent.updateEvent(user(UUID.randomUUID(), true)));

        // 등록 책임은 JWT 등록·갱신에 있다. 늦게 도착한 이벤트가 추적 상태를 되돌리면 안 된다.
        verifyNoInteractions(trackingRepository, jwtRegistry, userReader, eventPublisher);
    }

    private UserDto user(UUID id, boolean online) {
        return new UserDto(id, "user", "user@test.local", null, online, Role.USER, null, null);
    }
}
