package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.enums.ChannelType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadStatusTest {

    @Test
    void PRIVATE_채널은_알림이_활성화된다() {
        Channel channel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        ReadStatus readStatus =
                new ReadStatus(channel, user);

        assertThat(readStatus.isNotificationEnabled())
                .isTrue();
    }

    @Test
    void PUBLIC_채널은_알림이_비활성화된다() {
        Channel channel = new Channel(
                "general",
                "일반 채널",
                ChannelType.PUBLIC
        );

        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        ReadStatus readStatus =
                new ReadStatus(channel, user);

        assertThat(readStatus.isNotificationEnabled())
                .isFalse();
    }

    @Test
    void 알림_설정을_변경한다() {
        Channel channel = new Channel(
                "general",
                "일반 채널",
                ChannelType.PUBLIC
        );

        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        ReadStatus readStatus =
                new ReadStatus(channel, user);

        readStatus.updateNotificationEnabled(true);

        assertThat(readStatus.isNotificationEnabled())
                .isTrue();
    }
}