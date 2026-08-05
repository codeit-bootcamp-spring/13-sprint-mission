package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    void findAll_이름_오름차순으로_채널을_조회한다() {
        channelRepository.save(new Channel("general", "일반 채널", ChannelType.PUBLIC));
        channelRepository.save(new Channel("announcement", "공지 채널", ChannelType.PUBLIC));
        channelRepository.flush();

        List<Channel> result = channelRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        assertThat(result)
                .extracting(Channel::getName)
                .containsExactly("announcement", "general");
    }

    @Test
    void findById_채널이_없으면_빈_값을_반환한다() {
        Optional<Channel> result = channelRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}