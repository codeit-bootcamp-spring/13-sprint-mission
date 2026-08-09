package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.CreatePrivateChannelCommand;
import com.sprint.mission.discodeit.dto.command.CreatePublicChannelCommand;
import com.sprint.mission.discodeit.dto.command.UpdateChannelCommand;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class BasicChannelServiceTest {

    @InjectMocks
    private BasicChannelService channelService;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelMapper channelMapper;

    @Test
    @DisplayName("공개 채널 샐성 성공")
    void createPublicChannel_success() {
        // given
        CreatePublicChannelCommand command = new CreatePublicChannelCommand(
                "공지사항",
                "전체 채널 공지"
        );

        ChannelDto expected = mock(ChannelDto.class);

        given(channelMapper.toDto(any(Channel.class)))
                .willReturn(expected);

        // when
        ChannelDto result = channelService.createPublicChannel(command);

        // then

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);

        then(channelRepository)
                .should()
                .save(captor.capture());

        Channel savedChannel = captor.getValue();

        assertThat(savedChannel.getName())
                .isEqualTo("공지사항");

        assertThat(savedChannel.getDescription())
                .isEqualTo("전체 채널 공지");

        assertThat(savedChannel.getType())
                .isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("비공개 채널 생성 성공")
    void creatPrivateChannel_success() {
        // given
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        User firstUser = new User(
                "군고구마",
                "winter12@test.com",
                "202612"
        );

        User secondUser = new User(
                "호박고구마",
                "rhrnak@test.com",
                "202612"
        );

        CreatePrivateChannelCommand command =
                new CreatePrivateChannelCommand(List.of(firstUserId, secondUserId));

        ChannelDto expected = mock(ChannelDto.class);

        given(userRepository.findById(firstUserId))
                .willReturn(java.util.Optional.of(firstUser));

        given(userRepository.findById(secondUserId))
                .willReturn(java.util.Optional.of(secondUser));

        given(channelMapper.toDto(any(Channel.class)))
                .willReturn(expected);

        // when
        ChannelDto result = channelService.createPrivateChannel(command);

        // then
        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Channel> channelCaptor =
                ArgumentCaptor.forClass(Channel.class);

        then(channelRepository)
                .should()
                .save(channelCaptor.capture());

        assertThat(channelCaptor.getValue().getType())
                .isEqualTo(ChannelType.PRIVATE);

        then(readStatusRepository)
                .should(times(2))
                .save(any(ReadStatus.class));
    }

    @Test
    @DisplayName("비공개 채널 참여자가 존재하지 않으면 생성 실패")
    void createPrivateChannel_fail_participantNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        CreatePrivateChannelCommand command = new CreatePrivateChannelCommand(List.of(userId));

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.createPrivateChannel(command))
                .isInstanceOf(UserNotFoundException.class);

        then(channelRepository)
                .should(never())
                .save(any(Channel.class));

        then(readStatusRepository)
                .should(never())
                .save(any(ReadStatus.class));

        then(channelMapper)
                .shouldHaveNoInteractions();
    }

        @Test
        @DisplayName("공개 채널 이름 수정 성공")
        void update_success() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel = new Channel(
                    "공개채널",
                    "전체 공지 채널",
                    ChannelType.PUBLIC
            );

            UpdateChannelCommand command = new UpdateChannelCommand(
                            "공지 전달 채널",
                            null
                    );

            ChannelDto expected = mock(ChannelDto.class);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(channelMapper.toDto(channel))
                    .willReturn(expected);

            // when
            ChannelDto result = channelService.update(channelId, command);

            // then
            assertThat(result).isSameAs(expected);

            assertThat(channel.getName()).isEqualTo("공지 전달 채널");

            assertThat(channel.getDescription())
                    .isEqualTo("전체 공지 채널");

            then(channelMapper)
                    .should()
                    .toDto(channel);
        }

        @Test
        @DisplayName("비공개 채널은 수정할 수 없음")
        void update_fail_privateChannel() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel = new Channel(
                    null,
                    null,
                    ChannelType.PRIVATE
            );

            UpdateChannelCommand command = new UpdateChannelCommand(
                    "비공개 채널",
                    "비공개 채널 입니다."
            );

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            // when & then
            assertThatThrownBy(()->channelService.update(channelId,command))
                    .isInstanceOf(PrivateChannelUpdateException.class);

            then(channelMapper)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("채널 삭제 성공")
        void delete_success() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel = new Channel(
                    "공지사항",
                    "공지 채널",
                    ChannelType.PUBLIC
            );

            ReadStatus readStatus = mock(ReadStatus.class);
            List<ReadStatus> readStatuses = List.of(readStatus);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(readStatusRepository.findAllByChannelId(channelId))
                    .willReturn(readStatuses);

            // when
            channelService.delete(channelId);

            // then
            then(readStatusRepository)
                    .should()
                    .deleteAll(readStatuses);

            then(channelRepository)
                    .should()
                    .delete(channel);
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 예외 발생")
        void delete_fail_channelNotFound() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    channelService.delete(channelId)
            ).isInstanceOf(ChannelNotFoundException.class);

            then(readStatusRepository)
                    .shouldHaveNoInteractions();

            then(channelRepository)
                    .should(never())
                    .delete(any(Channel.class));
        }

    @Test
    @DisplayName("사용자가 조회할 수 있는 공개 및 비공개 채널을 반환")
    void findAllByUserId_success() {
        // given
        UUID userId = UUID.randomUUID();

        User user = new User(
                "홍길동",
                "hong@test.com",
                "password"
        );

        Channel publicChannel = new Channel(
                "공개 채널",
                "공개 채널 설명",
                ChannelType.PUBLIC
        );

        Channel joinedPrivateChannel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        Channel unjoinedPrivateChannel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        ReadStatus readStatus =
                new ReadStatus(
                        user,
                        joinedPrivateChannel
                );

        ChannelDto publicChannelDto =
                mock(ChannelDto.class);

        ChannelDto privateChannelDto =
                mock(ChannelDto.class);

        given(userRepository.existsById(userId))
                .willReturn(true);

        given(readStatusRepository.findAllByUserId(userId))
                .willReturn(List.of(readStatus));

        given(channelRepository.findAll())
                .willReturn(List.of(
                        publicChannel,
                        joinedPrivateChannel,
                        unjoinedPrivateChannel
                ));

        given(channelMapper.toDto(publicChannel))
                .willReturn(publicChannelDto);

        given(channelMapper.toDto(joinedPrivateChannel))
                .willReturn(privateChannelDto);

        // when
        List<ChannelDto> result =
                channelService.findAllByUserId(userId);

        // then
        assertThat(result)
                .containsExactly(
                        publicChannelDto,
                        privateChannelDto
                );

        then(channelMapper)
                .should(never())
                .toDto(unjoinedPrivateChannel);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 채널을 조회하면 예외가 발생")
    void findAllByUserId_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        given(userRepository.existsById(userId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                channelService.findAllByUserId(userId)
        ).isInstanceOf(UserNotFoundException.class);

        then(readStatusRepository)
                .shouldHaveNoInteractions();

        then(channelRepository)
                .shouldHaveNoInteractions();

        then(channelMapper)
                .shouldHaveNoInteractions();
    }

}
