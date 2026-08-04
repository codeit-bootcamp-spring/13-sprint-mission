package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicChannelService 단위 테스트")
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BasicChannelService channelService;

    @Nested
    @DisplayName("PUBLIC 채널 생성")
    class CreatePublicChannel {

        @Test
        @DisplayName("정상적인 요청이면 PUBLIC 채널을 생성한다")
        void createPublicChannelSuccess() {
            // given
            ChannelCreateRequest request = new ChannelCreateRequest(
                    ChannelType.PUBLIC,
                    "공지사항",
                    "전체 공지 채널입니다."
            );

            given(channelRepository.save(any(Channel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(messageRepository.findLastMessageAtByChannelId(any()))
                    .willReturn(null);

            // when
            ChannelResponse response =
                    channelService.createPublicChannel(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo(ChannelType.PUBLIC);
            assertThat(response.getName()).isEqualTo("공지사항");
            assertThat(response.getDescription())
                    .isEqualTo("전체 공지 채널입니다.");

            then(channelRepository)
                    .should()
                    .save(any(Channel.class));

            then(messageRepository)
                    .should()
                    .findLastMessageAtByChannelId(any());
        }

        @Test
        @DisplayName("PUBLIC 채널 생성 요청이 null이면 예외가 발생한다")
        void createPublicChannelFailsWhenRequestIsNull() {
            // when & then
            assertThatThrownBy(
                    () -> channelService.createPublicChannel(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("채널 생성 요청은 비어 있을 수 없습니다.");

            then(channelRepository)
                    .should(never())
                    .save(any(Channel.class));

            then(messageRepository)
                    .should(never())
                    .findLastMessageAtByChannelId(any());
        }
    }

    @Nested
    @DisplayName("PRIVATE 채널 생성")
    class CreatePrivateChannel {

        @Test
        @DisplayName("존재하는 참여자들로 PRIVATE 채널을 생성한다")
        void createPrivateChannelSuccess() {
            // given
            User user1 = new User(
                    "user1",
                    "user1@example.com",
                    "password1234"
            );

            User user2 = new User(
                    "user2",
                    "user2@example.com",
                    "password1234"
            );

            UUID userId1 = user1.getId();
            UUID userId2 = user2.getId();

            PrivateChannelCreateRequest request =
                    new PrivateChannelCreateRequest(
                            List.of(userId1, userId2)
                    );

            given(userRepository.existsById(userId1))
                    .willReturn(true);

            given(userRepository.existsById(userId2))
                    .willReturn(true);

            given(channelRepository.save(any(Channel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(userRepository.findById(userId1))
                    .willReturn(Optional.of(user1));

            given(userRepository.findById(userId2))
                    .willReturn(Optional.of(user2));

            given(readStatusRepository.save(any(ReadStatus.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(readStatusRepository.findUserIdsByChannelId(any()))
                    .willReturn(List.of(userId1, userId2));

            given(messageRepository.findLastMessageAtByChannelId(any()))
                    .willReturn(null);

            // when
            ChannelResponse response =
                    channelService.createPrivateChannel(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getType())
                    .isEqualTo(ChannelType.PRIVATE);

            assertThat(response.getParticipantUserIds())
                    .containsExactlyInAnyOrder(userId1, userId2);

            assertThat(response.getParticipants())
                    .hasSize(2);

            then(userRepository)
                    .should()
                    .existsById(userId1);

            then(userRepository)
                    .should()
                    .existsById(userId2);

            then(channelRepository)
                    .should()
                    .save(any(Channel.class));

            then(readStatusRepository)
                    .should(times(2))
                    .save(any(ReadStatus.class));
        }

        @Test
        @DisplayName("존재하지 않는 참여자가 있으면 PRIVATE 채널 생성에 실패한다")
        void createPrivateChannelFailsWhenParticipantDoesNotExist() {
            // given
            UUID existingUserId = UUID.randomUUID();
            UUID missingUserId = UUID.randomUUID();

            PrivateChannelCreateRequest request =
                    new PrivateChannelCreateRequest(
                            List.of(existingUserId, missingUserId)
                    );

            given(userRepository.existsById(existingUserId))
                    .willReturn(true);

            given(userRepository.existsById(missingUserId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(
                    () -> channelService.createPrivateChannel(request)
            )
                    .isInstanceOf(UserNotFoundException.class);

            then(channelRepository)
                    .should(never())
                    .save(any(Channel.class));

            then(readStatusRepository)
                    .should(never())
                    .save(any(ReadStatus.class));
        }
    }

    @Nested
    @DisplayName("채널 수정")
    class Update {

        @Test
        @DisplayName("PUBLIC 채널 정보를 정상적으로 수정한다")
        void updatePublicChannelSuccess() {
            // given
            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "기존 채널",
                    "기존 설명"
            );

            UUID channelId = channel.getId();

            ChannelUpdateRequest request = new ChannelUpdateRequest(
                    channelId,
                    ChannelType.PUBLIC,
                    "수정된 채널",
                    "수정된 설명"
            );

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(channelRepository.save(any(Channel.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(messageRepository.findLastMessageAtByChannelId(channelId))
                    .willReturn(null);

            // when
            ChannelResponse response = channelService.update(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getType())
                    .isEqualTo(ChannelType.PUBLIC);
            assertThat(response.getName())
                    .isEqualTo("수정된 채널");
            assertThat(response.getDescription())
                    .isEqualTo("수정된 설명");

            assertThat(channel.getName())
                    .isEqualTo("수정된 채널");
            assertThat(channel.getDescription())
                    .isEqualTo("수정된 설명");

            then(channelRepository)
                    .should()
                    .findById(channelId);

            then(channelRepository)
                    .should()
                    .save(channel);

            then(messageRepository)
                    .should()
                    .findLastMessageAtByChannelId(channelId);
        }

        @Test
        @DisplayName("PRIVATE 채널을 수정하면 예외가 발생한다")
        void updatePrivateChannelFails() {
            // given
            Channel channel = new Channel(
                    ChannelType.PRIVATE,
                    null,
                    null
            );

            UUID channelId = channel.getId();

            ChannelUpdateRequest request = new ChannelUpdateRequest(
                    channelId,
                    ChannelType.PRIVATE,
                    "수정 시도",
                    "수정 시도 설명"
            );

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            // when & then
            assertThatThrownBy(() -> channelService.update(request))
                    .isInstanceOf(PrivateChannelUpdateException.class);

            then(channelRepository)
                    .should()
                    .findById(channelId);

            then(channelRepository)
                    .should(never())
                    .save(any(Channel.class));

            then(messageRepository)
                    .should(never())
                    .findLastMessageAtByChannelId(any());
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class Delete {

        @Test
        @DisplayName("존재하는 채널을 정상적으로 삭제한다")
        void deleteChannelSuccess() {
            // given
            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "삭제할 채널",
                    "삭제 테스트 채널"
            );

            UUID channelId = channel.getId();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(messageRepository.findAllByChannel_Id(channelId))
                    .willReturn(List.of());

            // when
            channelService.delete(channelId);

            // then
            then(channelRepository)
                    .should()
                    .findById(channelId);

            then(messageRepository)
                    .should()
                    .findAllByChannel_Id(channelId);

            then(messageRepository)
                    .should()
                    .deleteByChannel_Id(channelId);

            then(readStatusRepository)
                    .should()
                    .deleteByChannel_Id(channelId);

            then(channelRepository)
                    .should()
                    .deleteById(channelId);

            then(binaryContentService)
                    .should(never())
                    .delete(any(UUID.class));
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 예외가 발생한다")
        void deleteChannelFailsWhenChannelDoesNotExist() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> channelService.delete(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            then(channelRepository)
                    .should()
                    .findById(channelId);

            then(messageRepository)
                    .should(never())
                    .findAllByChannel_Id(any(UUID.class));

            then(messageRepository)
                    .should(never())
                    .deleteByChannel_Id(any(UUID.class));

            then(readStatusRepository)
                    .should(never())
                    .deleteByChannel_Id(any(UUID.class));

            then(channelRepository)
                    .should(never())
                    .deleteById(any(UUID.class));

            then(binaryContentService)
                    .should(never())
                    .delete(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("사용자별 채널 조회")
    class FindAllByUserId {

        @Test
        @DisplayName("사용자가 참여 가능한 PUBLIC 및 PRIVATE 채널을 조회한다")
        void findAllByUserIdSuccess() {
            // given
            User user = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            UUID userId = user.getId();

            Channel publicChannel = new Channel(
                    ChannelType.PUBLIC,
                    "공개 채널",
                    "모든 사용자가 조회할 수 있습니다."
            );

            Channel participatedPrivateChannel = new Channel(
                    ChannelType.PRIVATE,
                    null,
                    null
            );

            Channel nonParticipatedPrivateChannel = new Channel(
                    ChannelType.PRIVATE,
                    null,
                    null
            );

            given(userRepository.existsById(userId))
                    .willReturn(true);

            given(channelRepository.findAll())
                    .willReturn(List.of(
                            publicChannel,
                            participatedPrivateChannel,
                            nonParticipatedPrivateChannel
                    ));

            given(readStatusRepository.findChannelIdsByUserId(userId))
                    .willReturn(List.of(participatedPrivateChannel.getId()));

            given(readStatusRepository.findUserIdsByChannelId(
                    participatedPrivateChannel.getId()
            )).willReturn(List.of(userId));

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(messageRepository.findLastMessageAtByChannelId(any(UUID.class)))
                    .willReturn(null);

            // when
            List<ChannelResponse> responses =
                    channelService.findAllByUserId(userId);

            // then
            assertThat(responses).hasSize(2);

            assertThat(responses)
                    .extracting(ChannelResponse::getType)
                    .containsExactlyInAnyOrder(
                            ChannelType.PUBLIC,
                            ChannelType.PRIVATE
                    );

            assertThat(responses)
                    .extracting(ChannelResponse::getName)
                    .contains("공개 채널");

            then(userRepository)
                    .should()
                    .existsById(userId);

            then(channelRepository)
                    .should()
                    .findAll();

            then(readStatusRepository)
                    .should()
                    .findChannelIdsByUserId(userId);

            then(readStatusRepository)
                    .should(never())
                    .findUserIdsByChannelId(
                            nonParticipatedPrivateChannel.getId()
                    );
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 채널을 조회하면 예외가 발생한다")
        void findAllByUserIdFailsWhenUserDoesNotExist() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(userId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(
                    () -> channelService.findAllByUserId(userId)
            )
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository)
                    .should()
                    .existsById(userId);

            then(channelRepository)
                    .should(never())
                    .findAll();

            then(readStatusRepository)
                    .should(never())
                    .findChannelIdsByUserId(any(UUID.class));
        }
    }
}