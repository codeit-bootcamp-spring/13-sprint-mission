package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.events.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.events.ChannelEvent;
import com.sprint.mission.discodeit.events.ChannelEventType;
import com.sprint.mission.discodeit.events.NotificationCreatedEvent;
import com.sprint.mission.discodeit.events.UserEvent;
import com.sprint.mission.discodeit.events.UserEventType;
import com.sprint.mission.discodeit.service.basic.MessageFileReader;
import com.sprint.mission.discodeit.service.basic.ReadStatusReader;
import com.sprint.mission.discodeit.service.basic.UserReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * 실제 업무 이벤트 리스너의 이벤트 이름·DTO·수신자 선택을 검증한다.
 * Reader와 발행자는 Mock으로 분리하며 공개·비공개·프로필 파일의 대상 범위를 확인한다.
 * 메서드를 직접 호출하므로 AFTER_COMMIT 동작은 통합 테스트에서 따로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SSE 이벤트 리스너 단위 테스트")
class SseRequiredEventListenerTest {

    @Mock SseEventPublisher eventPublisher;
    @Mock MessageFileReader messageFileReader;
    @Mock UserReader userReader;
    @Mock ReadStatusReader readStatusReader;

    @InjectMocks SseRequiredEventListener listener;

    private final BinaryContentDto binaryContent = new BinaryContentDto(
            UUID.randomUUID(), "file.txt", 1L, "text/plain", BinaryContentStatus.SUCCESS
    );
    private final BinaryContentUpdatedEvent event = new BinaryContentUpdatedEvent(binaryContent);

    @Test
    @DisplayName("알림 생성 이벤트는 알림의 수신자에게만 전달한다")
    void handleNotificationCreated_sendsOnlyToReceiver() {
        UUID receiverId = UUID.randomUUID();
        NotificationDto notification = new NotificationDto(UUID.randomUUID(),
                OffsetDateTime.parse("2026-10-18T00:00:00Z"), receiverId, "새 알림", "내용");

        listener.handleNotificationCreated(new NotificationCreatedEvent(notification));

        verify(eventPublisher).send(List.of(receiverId), "notifications.created", notification);
        // 알림을 다른 사용자에게 방송하거나 중복 발행하면 안 된다.
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("비공개 채널 수정 이벤트는 해당 참여자에게만 전달한다")
    void handleChannelEvent_sendsUpdatedPrivateChannelOnlyToParticipants() {
        UserDto participant = new UserDto(UUID.randomUUID(), "member", "member@test.local",
                null, false, Role.USER, null, null);
        ChannelDto channel = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE,
                "private", "updated", List.of(participant), null);

        listener.handleChannelEvent(new ChannelEvent(channel, ChannelEventType.UPDATED));

        verify(eventPublisher).send(List.of(participant.id()), "channels.updated", channel);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("공개 채널 첨부파일의 상태 변경을 전체 방송한다")
    void handleBinaryContentUpdated_broadcastsForPublicChannel() {
        Channel channel = new Channel(
                new ChannelCreatePublicCommand("public", "description", ChannelType.PUBLIC)
        );
        given(messageFileReader.getChannelByBinaryContentId(binaryContent.id()))
                .willReturn(Optional.of(channel));

        listener.handleBinaryContentUpdated(event);

        verify(eventPublisher).broadcast("binaryContents.updated", binaryContent);
        // 수신 범위를 벗어나거나 중복되는 추가 전송도 오류로 취급한다.
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("비공개 채널 첨부파일의 상태 변경은 해당 참여자에게만 전송한다")
    void handleBinaryContentUpdated_sendsOnlyToPrivateChannelParticipants() {
        UUID channelId = UUID.randomUUID();
        List<UUID> participantIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        Channel channel = new Channel(
                new ChannelCreatePrivateCommand(participantIds, ChannelType.PRIVATE)
        );
        ReflectionTestUtils.setField(channel, "id", channelId);
        given(messageFileReader.getChannelByBinaryContentId(binaryContent.id()))
                .willReturn(Optional.of(channel));
        given(readStatusReader.getUserIdsByChannelId(channelId)).willReturn(participantIds);

        listener.handleBinaryContentUpdated(event);

        verify(eventPublisher).send(participantIds, "binaryContents.updated", binaryContent);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("프로필 이미지의 상태 변경은 소유자에게만 전송한다")
    void handleBinaryContentUpdated_sendsOnlyToProfileOwner() {
        UUID ownerId = UUID.randomUUID();
        given(messageFileReader.getChannelByBinaryContentId(binaryContent.id()))
                .willReturn(Optional.empty());
        given(userReader.getUserIdFromProfile(binaryContent.id())).willReturn(Optional.of(ownerId));

        listener.handleBinaryContentUpdated(event);

        verify(eventPublisher).send(List.of(ownerId), "binaryContents.updated", binaryContent);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("채널과 프로필 소유자가 모두 없으면 전송하지 않는다")
    void handleBinaryContentUpdated_doesNotSendWhenFileHasNoChannelOrProfileOwner() {
        given(messageFileReader.getChannelByBinaryContentId(binaryContent.id()))
                .willReturn(Optional.empty());
        given(userReader.getUserIdFromProfile(binaryContent.id())).willReturn(Optional.empty());

        listener.handleBinaryContentUpdated(event);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("공개 채널 생성 이벤트를 전체 방송한다")
    void handleChannelEvent_broadcastsCreatedPublicChannel() {
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PUBLIC, "public", "description", List.of(), null
        );
        ChannelEvent createdEvent = new ChannelEvent(channel, ChannelEventType.CREATED);

        listener.handleChannelEvent(createdEvent);

        verify(eventPublisher).broadcast("channels.created", channel);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("비공개 채널 생성 이벤트는 해당 참여자에게만 전송한다")
    void handleChannelEvent_sendsCreatedPrivateChannelOnlyToParticipants() {
        UserDto firstParticipant = new UserDto(UUID.randomUUID(), "first", "first@test.local",
                null, false, Role.USER, null, null);
        UserDto secondParticipant = new UserDto(UUID.randomUUID(), "second", "second@test.local",
                null, false, Role.USER, null, null);
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PRIVATE, "", "",
                List.of(firstParticipant, secondParticipant), null
        );
        ChannelEvent createdEvent = new ChannelEvent(channel, ChannelEventType.CREATED);

        listener.handleChannelEvent(createdEvent);

        verify(eventPublisher).send(
                List.of(firstParticipant.id(), secondParticipant.id()), "channels.created", channel
        );
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("공개 채널 수정 이벤트를 전체 방송한다")
    void handleChannelEvent_broadcastsUpdatedPublicChannel() {
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PUBLIC, "updated", "updated description", List.of(), null
        );
        ChannelEvent updatedEvent = new ChannelEvent(channel, ChannelEventType.UPDATED);

        listener.handleChannelEvent(updatedEvent);

        verify(eventPublisher).broadcast("channels.updated", channel);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("공개 채널 삭제 이벤트를 전체 방송한다")
    void handleChannelEvent_broadcastsDeletedPublicChannel() {
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PUBLIC, "public", "description", List.of(), null
        );
        ChannelEvent deletedEvent = new ChannelEvent(channel, ChannelEventType.DELETED);

        listener.handleChannelEvent(deletedEvent);

        verify(eventPublisher).broadcast("channels.deleted", channel);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("비공개 채널 삭제 이벤트는 삭제 전 DTO의 참여자에게만 전송한다")
    void handleChannelEvent_sendsDeletedPrivateChannelOnlyToParticipants() {
        UserDto firstParticipant = new UserDto(UUID.randomUUID(), "first", "first@test.local",
                null, false, Role.USER, null, null);
        UserDto secondParticipant = new UserDto(UUID.randomUUID(), "second", "second@test.local",
                null, false, Role.USER, null, null);
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PRIVATE, "", "",
                List.of(firstParticipant, secondParticipant), null
        );
        ChannelEvent deletedEvent = new ChannelEvent(channel, ChannelEventType.DELETED);

        listener.handleChannelEvent(deletedEvent);

        verify(eventPublisher).send(
                List.of(firstParticipant.id(), secondParticipant.id()), "channels.deleted", channel
        );
        verifyNoMoreInteractions(eventPublisher);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "CREATED, users.created",
            "UPDATED, users.updated",
            "DELETED, users.deleted"
    })
    @DisplayName("사용자 이벤트의 타입에 맞는 이름과 동일한 DTO로 전체 방송한다")
    void handleUserEvent_broadcastsWithExpectedNameAndSameUser(
            UserEventType type, String expectedEventName
    ) {
        UserDto user = new UserDto(UUID.randomUUID(), "user", "user@test.local",
                null, false, Role.USER, null, null);
        UserEvent userEvent = new UserEvent(user, type);

        listener.handleUserEvent(userEvent);

        verify(eventPublisher).broadcast(eq(expectedEventName), same(user));
        // 중복 방송이나 별도 수신자 지정 전송이 추가되어서는 안 된다.
        verifyNoMoreInteractions(eventPublisher);
    }
}
