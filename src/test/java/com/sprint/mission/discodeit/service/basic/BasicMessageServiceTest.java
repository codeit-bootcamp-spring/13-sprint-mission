package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicMessageService 단위 테스트")
class BasicMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService messageService;

    @Nested
    @DisplayName("메시지 생성")
    class Create {

        @Test
        @DisplayName("정상적인 요청이면 메시지를 생성한다")
        void createSuccess() {
            // given
            User author = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "테스트 채널"
            );

            UUID authorId = author.getId();
            UUID channelId = channel.getId();

            MessageCreateRequest request = new MessageCreateRequest(
                    "테스트 메시지입니다.",
                    authorId,
                    channelId
            );

            given(userRepository.findById(authorId))
                    .willReturn(Optional.of(author));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(messageRepository.save(any(Message.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            MessageResponse response = messageService.create(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent())
                    .isEqualTo("테스트 메시지입니다.");
            assertThat(response.getAuthorId())
                    .isEqualTo(authorId);
            assertThat(response.getChannelId())
                    .isEqualTo(channelId);

            then(userRepository)
                    .should()
                    .findById(authorId);

            then(channelRepository)
                    .should()
                    .findById(channelId);

            then(messageRepository)
                    .should()
                    .save(any(Message.class));

            then(binaryContentService)
                    .shouldHaveNoInteractions();

            then(binaryContentRepository)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 작성자로 메시지를 생성하면 예외가 발생한다")
        void createFailsWhenAuthorDoesNotExist() {
            // given
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            MessageCreateRequest request = new MessageCreateRequest(
                    "테스트 메시지입니다.",
                    authorId,
                    channelId
            );

            given(userRepository.findById(authorId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.create(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository)
                    .should()
                    .findById(authorId);

            then(channelRepository)
                    .should(never())
                    .findById(any(UUID.class));

            then(messageRepository)
                    .should(never())
                    .save(any(Message.class));

            then(binaryContentService)
                    .shouldHaveNoInteractions();

            then(binaryContentRepository)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class Update {

        @Test
        @DisplayName("존재하는 메시지의 내용을 정상적으로 수정한다")
        void updateSuccess() {
            // given
            User author = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "테스트 채널"
            );

            Message message = new Message(
                    "기존 메시지",
                    author,
                    channel
            );

            UUID messageId = message.getId();

            MessageUpdateRequest request = new MessageUpdateRequest(
                    messageId,
                    "수정된 메시지"
            );

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            given(messageRepository.save(any(Message.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            MessageResponse response = messageService.update(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent())
                    .isEqualTo("수정된 메시지");

            assertThat(message.getContent())
                    .isEqualTo("수정된 메시지");

            then(messageRepository)
                    .should()
                    .findById(messageId);

            then(messageRepository)
                    .should()
                    .save(message);
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 수정하면 예외가 발생한다")
        void updateFailsWhenMessageDoesNotExist() {
            // given
            UUID messageId = UUID.randomUUID();

            MessageUpdateRequest request = new MessageUpdateRequest(
                    messageId,
                    "수정된 메시지"
            );

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.update(request))
                    .isInstanceOf(MessageNotFoundException.class);

            then(messageRepository)
                    .should()
                    .findById(messageId);

            then(messageRepository)
                    .should(never())
                    .save(any(Message.class));
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class Delete {

        @Test
        @DisplayName("존재하는 메시지를 정상적으로 삭제한다")
        void deleteSuccess() {
            // given
            User author = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "테스트 채널"
            );

            Message message = new Message(
                    "삭제할 메시지",
                    author,
                    channel
            );

            UUID messageId = message.getId();

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            // when
            messageService.delete(messageId);

            // then
            then(messageRepository)
                    .should()
                    .findById(messageId);

            then(messageRepository)
                    .should()
                    .deleteById(messageId);

            then(binaryContentService)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 삭제하면 예외가 발생한다")
        void deleteFailsWhenMessageDoesNotExist() {
            // given
            UUID messageId = UUID.randomUUID();

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.delete(messageId))
                    .isInstanceOf(MessageNotFoundException.class);

            then(messageRepository)
                    .should()
                    .findById(messageId);

            then(messageRepository)
                    .should(never())
                    .deleteById(any(UUID.class));

            then(binaryContentService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널별 메시지 조회")
    class FindAllByChannelId {

        @Test
        @DisplayName("존재하는 채널의 메시지 목록을 조회한다")
        void findAllByChannelIdSuccess() {
            // given
            User author = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "일반 채널",
                    "테스트 채널"
            );

            Message message1 = new Message(
                    "첫 번째 메시지",
                    author,
                    channel
            );

            Message message2 = new Message(
                    "두 번째 메시지",
                    author,
                    channel
            );

            UUID channelId = channel.getId();

            given(channelRepository.existsById(channelId))
                    .willReturn(true);

            given(messageRepository.findByChannel_IdOrderByCreatedAtDesc(
                    eq(channelId),
                    any(Pageable.class)
            )).willReturn(List.of(message2, message1));

            // when
            List<MessageResponse> responses =
                    messageService.findAllByChannelId(channelId);

            // then
            assertThat(responses).hasSize(2);

            assertThat(responses)
                    .extracting(MessageResponse::getContent)
                    .containsExactly(
                            "두 번째 메시지",
                            "첫 번째 메시지"
                    );

            then(channelRepository)
                    .should()
                    .existsById(channelId);

            then(messageRepository)
                    .should()
                    .findByChannel_IdOrderByCreatedAtDesc(
                            eq(channelId),
                            any(Pageable.class)
                    );
        }

        @Test
        @DisplayName("존재하지 않는 채널의 메시지를 조회하면 예외가 발생한다")
        void findAllByChannelIdFailsWhenChannelDoesNotExist() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.existsById(channelId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(
                    () -> messageService.findAllByChannelId(channelId)
            )
                    .isInstanceOf(ChannelNotFoundException.class);

            then(channelRepository)
                    .should()
                    .existsById(channelId);

            then(messageRepository)
                    .should(never())
                    .findByChannel_IdOrderByCreatedAtDesc(
                            any(UUID.class),
                            any(Pageable.class)
                    );
        }
    }
}