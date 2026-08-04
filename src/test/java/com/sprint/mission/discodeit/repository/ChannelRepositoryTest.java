package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ChannelRepository 슬라이스 테스트")
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Nested
    @DisplayName("채널 타입별 조회")
    class FindByType {

        @Test
        @DisplayName("PUBLIC 타입으로 조회하면 PUBLIC 채널만 반환한다")
        void success() {
            // given
            Channel publicChannel1 = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "일반 대화 채널"
            );

            Channel publicChannel2 = new Channel(
                    ChannelType.PUBLIC,
                    "개발 채널",
                    "개발 관련 채널"
            );

            Channel privateChannel = new Channel(
                    ChannelType.PRIVATE,
                    null,
                    null
            );

            channelRepository.save(publicChannel1);
            channelRepository.save(publicChannel2);
            channelRepository.save(privateChannel);
            channelRepository.flush();

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.ASC, "name")
            );

            // when
            Page<Channel> result = channelRepository.findByType(
                    ChannelType.PUBLIC,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);

            assertThat(result.getContent())
                    .extracting(Channel::getType)
                    .containsOnly(ChannelType.PUBLIC);

            assertThat(result.getContent())
                    .extracting(Channel::getName)
                    .containsExactly("개발 채널", "일반 채널");

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("해당 타입의 채널이 없으면 빈 페이지를 반환한다")
        void empty() {
            // given
            Channel publicChannel = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "일반 대화 채널"
            );

            channelRepository.saveAndFlush(publicChannel);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Channel> result = channelRepository.findByType(
                    ChannelType.PRIVATE,
                    pageable
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("페이지 크기를 2로 지정하면 최대 2개의 채널만 반환한다")
        void paging() {
            // given
            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "채널 A",
                            "채널 A 설명"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "채널 B",
                            "채널 B 설명"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "채널 C",
                            "채널 C 설명"
                    )
            );

            channelRepository.flush();

            Pageable pageable = PageRequest.of(
                    0,
                    2,
                    Sort.by(Sort.Direction.ASC, "name")
            );

            // when
            Page<Channel> result = channelRepository.findByType(
                    ChannelType.PUBLIC,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);

            assertThat(result.getContent())
                    .extracting(Channel::getName)
                    .containsExactly("채널 A", "채널 B");

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("채널 이름 검색")
    class FindByNameContainingIgnoreCase {

        @Test
        @DisplayName("채널 이름에 검색어가 포함되면 해당 채널을 반환한다")
        void success() {
            // given
            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "Spring Study",
                            "Spring 학습 채널"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "Java Study",
                            "Java 학습 채널"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "일반 채널",
                            "일반 대화 채널"
                    )
            );

            channelRepository.flush();

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.ASC, "name")
            );

            // when
            Page<Channel> result =
                    channelRepository.findByNameContainingIgnoreCase(
                            "study",
                            pageable
                    );

            // then
            assertThat(result.getContent()).hasSize(2);

            assertThat(result.getContent())
                    .extracting(Channel::getName)
                    .containsExactly("Java Study", "Spring Study");
        }

        @Test
        @DisplayName("대소문자가 달라도 채널 이름을 조회할 수 있다")
        void ignoreCase() {
            // given
            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "Spring Boot",
                    "Spring Boot 학습 채널"
            );

            channelRepository.saveAndFlush(channel);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Channel> result =
                    channelRepository.findByNameContainingIgnoreCase(
                            "SPRING",
                            pageable
                    );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName())
                    .isEqualTo("Spring Boot");
        }

        @Test
        @DisplayName("검색어가 포함된 채널이 없으면 빈 페이지를 반환한다")
        void empty() {
            // given
            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "Spring Study",
                    "Spring 학습 채널"
            );

            channelRepository.saveAndFlush(channel);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Channel> result =
                    channelRepository.findByNameContainingIgnoreCase(
                            "kotlin",
                            pageable
                    );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("채널 이름을 내림차순으로 정렬한다")
        void sorting() {
            // given
            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "Study A",
                            "A 채널"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "Study B",
                            "B 채널"
                    )
            );

            channelRepository.save(
                    new Channel(
                            ChannelType.PUBLIC,
                            "Study C",
                            "C 채널"
                    )
            );

            channelRepository.flush();

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "name")
            );

            // when
            Page<Channel> result =
                    channelRepository.findByNameContainingIgnoreCase(
                            "study",
                            pageable
                    );

            // then
            assertThat(result.getContent())
                    .extracting(Channel::getName)
                    .containsExactly(
                            "Study C",
                            "Study B",
                            "Study A"
                    );
        }
    }
}