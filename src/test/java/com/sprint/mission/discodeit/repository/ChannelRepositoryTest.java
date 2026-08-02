package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    private Channel publicChannel1;
    private Channel publicChannel2;
    private Channel privateChannel;

    @BeforeEach
    void setUp() {
        publicChannel1 = channelRepository.save(
                new Channel(ChannelType.PUBLIC, "공개채널1", "첫 번째 공개 채널")
        );
        publicChannel2 = channelRepository.save(
                new Channel(ChannelType.PUBLIC, "공개채널2", "두 번째 공개 채널")
        );
        privateChannel = channelRepository.save(
                new Channel(ChannelType.PRIVATE, null, null)
        );
    }


    @Test
    @DisplayName("findAllByTypeOrIdIn - PUBLIC 타입이면 모든 PUBLIC 채널을 반환한다")
    void findAllByTypeOrIdIn_PUBLIC채널반환() {
        // when: PUBLIC 타입으로 조회, 별도 ID 목록 없음
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(
                ChannelType.PUBLIC, List.of()
        );

        // then: PUBLIC 채널 2개 반환
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getType() == ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("findAllByTypeOrIdIn - PRIVATE 채널 ID를 포함하면 해당 PRIVATE 채널도 반환한다")
    void findAllByTypeOrIdIn_특정PRIVATE채널포함() {
        // when
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(
                ChannelType.PUBLIC,
                List.of(privateChannel.getId())
        );

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("findAllByTypeOrIdIn - 존재하지 않는 ID는 무시된다")
    void findAllByTypeOrIdIn_존재하지않는ID무시() {
        // when: 존재하지 않는 UUID로 조회
        List<Channel> result = channelRepository.findAllByTypeOrIdIn(
                ChannelType.PUBLIC,
                List.of(UUID.randomUUID())  // 존재하지 않는 UUID
        );

        // then
        assertThat(result).hasSize(2);
    }


    @Test
    @DisplayName("findAll(Pageable) - 페이지 크기를 지정하면 해당 크기만큼만 반환한다")
    void findAll_페이지크기제한() {
        // when: 페이지 크기 2로 조회
        Page<Channel> result = channelRepository.findAll(
                PageRequest.of(0, 2, Sort.by("createdAt"))
        );

        // then: 최대 2개만 반환
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findAll(Pageable) - 두 번째 페이지에는 나머지 채널이 반환된다")
    void findAll_두번째페이지() {
        // when: 두 번째 페이지
        Page<Channel> result = channelRepository.findAll(
                PageRequest.of(1, 2, Sort.by("createdAt"))
        );

        // then: 나머지 1개만 반환
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
    }
}