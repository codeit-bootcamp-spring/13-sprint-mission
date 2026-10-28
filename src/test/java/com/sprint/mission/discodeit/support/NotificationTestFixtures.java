package com.sprint.mission.discodeit.support;

import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.events.MessageCreatedEvent;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 알림 관련 테스트의 실제 엔티티·DTO·이벤트 생성 코드를 모은다.
 * 메시지 이벤트는 알림 정보와 WebSocket 응답 DTO를 함께 담는 현재 계약에 맞춰 생성한다.
 */
public final class NotificationTestFixtures {
    private NotificationTestFixtures() {}

    public static User user() {
        String suffix = UUID.randomUUID().toString();
        return new User(new UserCreateCommand("user-" + suffix, "encoded-password", suffix + "@test.local"), null);
    }

    public static Channel channel(ChannelType type) {
        return type == ChannelType.PRIVATE
                ? new Channel(new ChannelCreatePrivateCommand(List.of(), type))
                : new Channel(new ChannelCreatePublicCommand("test-channel", "description", type));
    }

    public static MessageCreatedEvent messageCreatedEvent(UUID channelId, UUID authorId,
                                                         String channelName, String authorName, String content) {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-02T00:00:00Z");
        UserDto author = new UserDto(authorId, authorName, authorId + "@test.local",
                null, false, Role.USER, createdAt, createdAt);
        MessageDto message = new MessageDto(UUID.randomUUID(), createdAt, null,
                content, channelId, author, List.of());
        return new MessageCreatedEvent(channelId, authorId, channelName, authorName, content, message);
    }

    // 단위 테스트용 식별자이며 실제 DB 저장을 의미하지 않는다.
    public static <T> T identified(T entity) {
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        return entity;
    }

    public static DiscodeitUserDetails principal(User user) {
        OffsetDateTime now = OffsetDateTime.parse("2026-10-02T00:00:00Z");
        return new DiscodeitUserDetails(new UserDto(user.getId(), user.getUsername(), user.getEmail(),
                null, false, user.getRole(), now, now), user.getPassword());
    }
}
