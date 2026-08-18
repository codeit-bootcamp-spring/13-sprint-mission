package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Nested
    @DisplayName("채널 타입별 조회")
    class FindAllByType {

        @Test
        @DisplayName("지정한 타입의 채널만 조회")
        void get_success() {
            // given
            Channel firstPublicChannel = new Channel(
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널"
            );

            Channel secondPublicChannel = new Channel(
                    ChannelType.PUBLIC,
                    "자유",
                    "자유 대화 채널"
            );

            Channel privateChannel = new Channel(ChannelType.PRIVATE);

            channelRepository.saveAllAndFlush(
                    List.of(
                            firstPublicChannel,
                            secondPublicChannel,
                            privateChannel
                    )
            );

            // when
            List<Channel> result = channelRepository.findAllByType(ChannelType.PUBLIC);

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Channel::getName)
                    .containsExactlyInAnyOrder("공지", "자유");

            assertThat(result)
                    .allMatch(channel -> channel.getType() == ChannelType.PUBLIC);
        }

        @Test
        @DisplayName("지정한 타입의 채널이 없으면 빈 목록을 반환")
        void get_fail() {
            // given
            Channel publicChannel = new Channel(
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널"
            );

            channelRepository.saveAndFlush(publicChannel);

            // when
            List<Channel> result = channelRepository.findAllByType(ChannelType.PRIVATE);

            // then
            assertThat(result).isEmpty();
        }
    }
}