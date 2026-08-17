package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

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
    @DisplayName("채널을 저장하고 ID로 조회할 수 있다")
    void saveAndFindByIdSuccess() {
        // given
        Channel channel = createChannel(
                "일반 채널",
                "일반 대화용 채널"
        );

        Channel savedChannel =
                channelRepository.saveAndFlush(channel);

        // when
        Optional<Channel> result =
                channelRepository.findById(savedChannel.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("일반 채널");
        assertThat(result.get().getDescription())
                .isEqualTo("일반 대화용 채널");
        assertThat(result.get().getType())
                .isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 조회하면 빈 Optional을 반환한다")
    void findByIdFailWhenChannelNotFound() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when
        Optional<Channel> result =
                channelRepository.findById(unknownId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채널 이름을 오름차순으로 정렬하여 조회한다")
    void findAllWithNameAscendingSort() {
        // given
        channelRepository.save(createChannel(
                "Charlie",
                "세 번째 채널"
        ));

        channelRepository.save(createChannel(
                "Alice",
                "첫 번째 채널"
        ));

        channelRepository.save(createChannel(
                "Bravo",
                "두 번째 채널"
        ));

        channelRepository.flush();

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "name"
        );

        // when
        var channels = channelRepository.findAll(sort);

        // then
        assertThat(channels)
                .extracting(Channel::getName)
                .containsExactly(
                        "Alice",
                        "Bravo",
                        "Charlie"
                );
    }

    @Test
    @DisplayName("채널 목록을 페이지 단위로 조회한다")
    void findAllWithPagination() {
        // given
        channelRepository.save(createChannel(
                "channel01",
                "첫 번째 채널"
        ));

        channelRepository.save(createChannel(
                "channel02",
                "두 번째 채널"
        ));

        channelRepository.save(createChannel(
                "channel03",
                "세 번째 채널"
        ));

        channelRepository.flush();

        PageRequest pageRequest = PageRequest.of(
                0,
                2,
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                )
        );

        // when
        Page<Channel> result =
                channelRepository.findAll(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();

        assertThat(result.getContent())
                .extracting(Channel::getName)
                .containsExactly(
                        "channel01",
                        "channel02"
                );
    }

    private Channel createChannel(
            String name,
            String description
    ) {
        return new Channel(
                ChannelType.PUBLIC,
                name,
                description
        );
    }
}