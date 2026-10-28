package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.events.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.events.ChannelEvent;
import com.sprint.mission.discodeit.events.NotificationCreatedEvent;
import com.sprint.mission.discodeit.events.UserEvent;
import com.sprint.mission.discodeit.service.basic.MessageFileReader;
import com.sprint.mission.discodeit.service.basic.ReadStatusReader;
import com.sprint.mission.discodeit.service.basic.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

/**
 * 커밋된 업무 변경을 SSE 이벤트 이름과 수신자 범위로 변환한다.
 * 알림은 수신자에게, 비공개 채널은 참여자에게 전달하여 다른 사용자에게 노출하지 않는다.
 * 실제 이력 저장과 인스턴스 간 전달은 SseEventPublisher의 공통 경로를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

    private final MessageFileReader messageFileReader;
    private final UserReader userReader;
    private final ReadStatusReader readStatusReader;
    private final SseEventPublisher sseEventPublisher;

    public static final String NOTIFICATION_CREATED = "notifications.created";
    public static final String BINARY_CONTENT_UPDATED = "binaryContents.updated";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event){
        NotificationDto notification = event.notification();

        sseEventPublisher.send(List.of(
                notification.receiverId()),
                NOTIFICATION_CREATED,
                notification
        );
    }

    /**
     * 공개 채널 파일은 전체 방송, 비공개 채널 파일은 참여자에게만 전송한다.
     * 채널 파일이 아니면 프로필 소유자에게만 전송하며 소유 관계가 없으면 발행하지 않는다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBinaryContentUpdated(BinaryContentUpdatedEvent event){
        UUID binaryContentId = event.binaryContent().id();
        messageFileReader.getChannelByBinaryContentId(binaryContentId)
                        .ifPresentOrElse(
                                channel-> {
                                    if (channel.isPublic()){
                                        sseEventPublisher.broadcast(BINARY_CONTENT_UPDATED, event.binaryContent());
                                        return;
                                    }

                                    List<UUID> participantIds = readStatusReader.getUserIdsByChannelId(channel.getId());
                                    sseEventPublisher.send(participantIds, BINARY_CONTENT_UPDATED, event.binaryContent());
                                },
                                ()-> userReader.getUserIdFromProfile(binaryContentId)
                                            .ifPresent(userId -> sseEventPublisher.send(List.of(userId), BINARY_CONTENT_UPDATED, event.binaryContent()))
                        );
    }

    /**
     * 공개 채널은 전체 사용자에게, 비공개 채널은 DTO의 참여자에게 전송한다.
     * 삭제 시에는 삭제 전 스냅샷을 사용하므로 이미 지워진 참여 관계를 다시 조회하지 않는다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChannelEvent(ChannelEvent event){
        ChannelDto channel = event.channel();
        String eventName = event.type().getEventName();
        if (!channel.isPrivate()) {
            sseEventPublisher.broadcast(eventName, channel);
            return;
        }

        List<UUID> participantIds = channel.participants().stream()
                .map(UserDto::id)
                .toList();

        sseEventPublisher.send(participantIds, eventName, channel);
    }

    /**
     * 사용자 변경을 전체 방송한다.
     * 트랜잭션이 있으면 커밋 후 처리하고, 로그인·로그아웃처럼 트랜잭션이 없는 발행도 허용한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleUserEvent(UserEvent event){
        sseEventPublisher.broadcast(event.type().getEventName(), event.user());
    }
}
