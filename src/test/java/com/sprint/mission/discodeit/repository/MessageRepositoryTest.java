package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class MessageRepositoryTest {

    @Autowired MessageRepository messageRepository;
    @Autowired ChannelRepository channelRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserStatusRepository userStatusRepository;

    private Channel channel;
    private User author;

    @BeforeEach
    void setUp() {
        // 채널 생성
        channel = channelRepository.save(
                new Channel(ChannelType.PUBLIC, "테스트채널", "설명")
        );

        // 작성자 생성
        author = new User("author", "author@test.com", "password", null);
        userRepository.save(author);
        userStatusRepository.save(new UserStatus(author, Instant.now()));
    }

    @Test
    @DisplayName("findAllByChannelIdWithAuthor - 채널의 메시지를 최신순으로 반환한다")
    void findAllByChannelIdWithAuthor_메시지반환() {
        // given: 메시지 2개 저장
        messageRepository.save(new Message("첫 번째 메시지", channel, author, List.of()));
        messageRepository.save(new Message("두 번째 메시지", channel, author, List.of()));

        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
                channel.getId(),
                Instant.now().plusSeconds(10),  // 미래 시각으로 설정해 모든 메시지 포함
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("findAllByChannelIdWithAuthor - cursor 이전 메시지만 반환한다 (커서 기반 페이지네이션)")
    void findAllByChannelIdWithAuthor_커서기반페이징() {
        // given: 이전 메시지 저장
        messageRepository.save(new Message("이전 메시지", channel, author, List.of()));

        // 이전 메시지가 저장된 직후 시각을 커서로 지정
        Instant cursor = Instant.now();

        Instant pastCursor = Instant.now().minusSeconds(3600); // 1시간 전

        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when: 1시간 전 이전 메시지 조회 → 없어야 함
        Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
                channel.getId(), pastCursor, pageable
        );

        // then: 1시간 전보다 이전 메시지는 없으므로 빈 결과
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findLastMessageAtByChannelId - 메시지가 있으면 가장 최근 메시지의 시각을 반환한다")
    void findLastMessageAtByChannelId_성공() {
        // given
        messageRepository.save(new Message("첫 번째", channel, author, List.of()));
        messageRepository.save(new Message("두 번째", channel, author, List.of()));

        // when
        Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(channel.getId());

        // then: 가장 최근 메시지의 시각이 반환
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findLastMessageAtByChannelId - 메시지가 없으면 empty를 반환한다")
    void findLastMessageAtByChannelId_메시지없음() {
        // given: 다른 채널 생성
        Channel emptyChannel = channelRepository.save(
                new Channel(ChannelType.PUBLIC, "빈채널", "메시지 없음")
        );

        // when
        Optional<Instant> result = messageRepository.findLastMessageAtByChannelId(
                emptyChannel.getId()
        );

        // then
        assertThat(result).isEmpty();
    }
}