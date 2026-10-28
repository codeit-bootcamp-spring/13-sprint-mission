package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.MessageDto;

import java.util.UUID;

/**
 * 알림 생성에 필요한 정보와 채팅 구독자에게 전달할 MessageDto를 함께 담는다.
 * 트랜잭션 안에서 완성한 DTO를 전달하여 커밋 후 엔티티 재조회·지연 로딩에 의존하지 않는다.
 */
public record MessageCreatedEvent(
        UUID channelId,
        UUID authorId,
        String channelName,
        String authorName,
        String message,
        MessageDto messageDto
) implements NotificationContent {

    public static MessageCreatedEvent of(UUID channelId, UUID authorId, String channelName, String authorName, String content, MessageDto messageDto) {
        return new MessageCreatedEvent(
                channelId,
                authorId,
                channelName,
                authorName,
                content,
                messageDto
        );
    }

    @Override
    public String title() {
        return String.format("%s (#%s)", authorName, channelName);
    }

    @Override
    public String content() {
        return message;
    }

}
