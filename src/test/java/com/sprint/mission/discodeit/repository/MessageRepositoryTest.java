package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private BinaryContentRepository binaryContentRepository;

    @Test
    @DisplayName("채널 ID로 메시지 목록을 조회한다")
    void findAllByChannelId_success() {
        User author = saveUser("tester", "tester@example.com");
        Channel channel = saveChannel("general");
        Message message = saveMessage("hello", author, channel);

        List<Message> result = messageRepository.findAllByChannel_Id(channel.getId()).stream().toList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(message.getId());
        assertThat(result.get(0).getContent()).isEqualTo("hello");
    }

    @Test
    @DisplayName("채널 ID에 해당하는 메시지가 없으면 빈 목록을 반환한다")
    void findAllByChannelId_empty() {
        UUID unknownChannelId = UUID.randomUUID();

        List<Message> result = messageRepository.findAllByChannel_Id(unknownChannelId).stream().toList();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채널 ID로 메시지 목록을 페이징 및 정렬 조회한다")
    void findAllByChannelId_withPagingAndSort() throws InterruptedException {
        User author = saveUser("tester", "tester@example.com");
        Channel channel = saveChannel("general");
        Message first = saveMessage("first", author, channel);
        Thread.sleep(5);
        Message second = saveMessage("second", author, channel);
        Thread.sleep(5);
        Message third = saveMessage("third", author, channel);

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        Slice<Message> result = messageRepository.findAllByChannel_Id(channel.getId(), pageRequest);

        assertThat(result.getContent())
                .extracting(Message::getId)
                .containsExactly(third.getId(), second.getId());
        assertThat(result.hasNext()).isTrue();
        assertThat(first.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("커서 이전에 생성된 메시지 목록을 조회한다")
    void findAllByChannelIdAndCreatedAtLessThan_success() throws InterruptedException {
        User author = saveUser("tester", "tester@example.com");
        Channel channel = saveChannel("general");
        Message oldMessage = saveMessage("old", author, channel);
        Thread.sleep(5);
        Message cursorMessage = saveMessage("cursor", author, channel);

        Instant cursor = cursorMessage.getCreateAt();
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Slice<Message> result = messageRepository.findAllByChannel_IdAndCreatedAtLessThan(
                channel.getId(),
                cursor,
                pageRequest
        );

        assertThat(result.getContent())
                .extracting(Message::getId)
                .containsExactly(oldMessage.getId());
    }

    @Test
    @DisplayName("상세 정보와 함께 메시지를 조회한다")
    void findWithDetailsById_success() {
        User author = saveUser("tester", "tester@example.com");
        Channel channel = saveChannel("general");
        BinaryContent attachment = binaryContentRepository.save(
                new BinaryContent("image.png", "image/png", 3L)
        );
        Message message = saveMessage("hello", author, channel, List.of(attachment));

        Optional<Message> result = messageRepository.findWithDetailsById(message.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getAuthor().getName()).isEqualTo("tester");
        assertThat(result.get().getChannel().getId()).isEqualTo(channel.getId());
        assertThat(result.get().getAttachments()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 ID로 상세 조회하면 빈 Optional을 반환한다")
    void findWithDetailsById_notFound() {
        Optional<Message> result = messageRepository.findWithDetailsById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채널의 가장 최근 메시지를 조회한다")
    void findTopByChannelIdOrderByCreatedAtDesc_success() throws InterruptedException {
        User author = saveUser("tester", "tester@example.com");
        Channel channel = saveChannel("general");
        saveMessage("old", author, channel);
        Thread.sleep(5);
        Message latest = saveMessage("latest", author, channel);

        Optional<Message> result = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(channel.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(latest.getId());
    }

    private User saveUser(String username, String email) {
        return userRepository.save(new User(username, email, "password", null));
    }

    private Channel saveChannel(String name) {
        return channelRepository.save(new Channel(name, name + " description", null, ChannelType.PUBLIC));
    }

    private Message saveMessage(String content, User author, Channel channel) {
        return saveMessage(content, author, channel, List.of());
    }

    private Message saveMessage(String content, User author, Channel channel, List<BinaryContent> attachments) {
        return messageRepository.saveAndFlush(new Message(content, author, channel, attachments));
    }
}