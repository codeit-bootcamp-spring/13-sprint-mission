package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService basicChannelService;

    @Nested
    @DisplayName("(생성) Public 채널 생성")
    class CreatePublicChannel {

        @Test
        @DisplayName("Public 채널 생성에 성공")
        void 생성_성공() {
            // given
            PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                    "테스트 채널",
                    "테스트 채널 설명"
            );

            Channel savedChannel = new Channel(
                    ChannelType.PUBLIC,
                    request.name(),
                    request.description()
            );

            ChannelDto expectedDto = new ChannelDto(
                    savedChannel.getId(),
                    savedChannel.getType(),
                    savedChannel.getName(),
                    savedChannel.getDescription(),
                    null,
                    null
            );

            given(channelRepository.save(any(Channel.class)))
                    .willReturn(savedChannel);
            given(channelMapper.toDto(savedChannel))
                    .willReturn(expectedDto);

            // when
            ChannelDto result = basicChannelService.createPublicChannel(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedDto);

            then(channelRepository).should()
                    .save(any(Channel.class));
            then(channelMapper).should()
                    .toDto(savedChannel);
        }
    }

    @Nested
    @DisplayName("(생성) Private 채널 생성")
    class CreatePrivateChannel {

        @Test
        @DisplayName("참여자들과 함께 Private 채널을 생성")
        void 생성_성공() {
            // given
            UUID firstUserId = UUID.randomUUID();
            UUID secondUserId = UUID.randomUUID();

            PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                    List.of(firstUserId, secondUserId)
            );

            User firstUser = mock(User.class);
            User secondUser = mock(User.class);

            Channel savedChannel = new Channel(ChannelType.PRIVATE);

            ChannelDto expectedDto = mock(ChannelDto.class);

            given(channelRepository.save(any(Channel.class)))
                    .willReturn(savedChannel);

            given(userRepository.findById(firstUserId))
                    .willReturn(Optional.of(firstUser));
            given(userRepository.findById(secondUserId))
                    .willReturn(Optional.of(secondUser));

            // ReadStatus 저장 시 전달받은 객체를 그대로 반환하도록 설정
            given(readStatusRepository.save(any(ReadStatus.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(channelMapper.toDto(savedChannel))
                    .willReturn(expectedDto);

            // when
            ChannelDto result = basicChannelService.createPrivateChannel(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedDto);

            then(channelRepository).should()
                    .save(any(Channel.class));

            then(userRepository).should()
                    .findById(firstUserId);
            then(userRepository).should()
                    .findById(secondUserId);

            then(readStatusRepository).should(times(2))
                    .save(any(ReadStatus.class));

            then(channelMapper).should()
                    .toDto(savedChannel);
        }

        @Test
        @DisplayName("참여자가 존재하지 않으면 예외 발생")
        void 생성_실패_참여자_없음() {
            // given
            UUID userId = UUID.randomUUID();

            PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));

            Channel savedChannel = new Channel(ChannelType.PRIVATE);

            given(channelRepository.save(any(Channel.class)))
                    .willReturn(savedChannel);

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicChannelService.createPrivateChannel(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findById(userId);

            then(readStatusRepository).should(never())
                    .save(any(ReadStatus.class));
        }
    }

    @Nested
    @DisplayName("(수정) update")
    class UpdateChannel {

        @Test
        @DisplayName("Public 채널의 이름과 설명을 수정")
        void 수정_성공() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "기존 이름",
                    "기존 설명"
            );

            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(
                    "새 이름",
                    "새 설명"
            );

            ChannelDto expectedDto = mock(ChannelDto.class);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(channelMapper.toDto(channel))
                    .willReturn(expectedDto);

            // when
            ChannelDto result = basicChannelService.updateChannel(channelId, request);

            // then
            assertThat(channel.getName())
                    .isEqualTo(request.newName());
            assertThat(channel.getDescription())
                    .isEqualTo(request.newDescription());

            assertThat(result).isEqualTo(expectedDto);

            then(channelRepository).should()
                    .findById(channelId);
            then(channelMapper).should()
                    .toDto(channel);
        }

        @Test
        @DisplayName("Private 채널은 수정 불가")
        void 수정_실패_Private_채널() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel privateChannel = new Channel(ChannelType.PRIVATE);

            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(
                    "새 이름",
                    "새 설명"
            );

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(privateChannel));

            // when & then
            assertThatThrownBy(() -> basicChannelService.updateChannel(channelId, request))
                    .isInstanceOf(PrivateChannelUpdateException.class);

            then(channelRepository).should()
                    .findById(channelId);
        }
    }

    @Nested
    @DisplayName("(삭제) delete")
    class DeleteChannel {

        @Test
        @DisplayName("채널이 존재하면 관련 데이터와 채널을 삭제")
        void 삭제_성공() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel = mock(Channel.class);

            given(channel.getId())
                    .willReturn(channelId);
            given(channel.getName())
                    .willReturn("공지 채널");

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(messageRepository.findAllByChannelId(channelId))
                    .willReturn(List.of());

            // when
            basicChannelService.deleteChannel(channelId);

            // then
            then(channelRepository).should()
                    .findById(channelId);

            then(messageRepository).should()
                    .findAllByChannelId(channelId);

            then(readStatusRepository).should()
                    .deleteAllByChannelId(channelId);

            then(channelRepository).should()
                    .deleteById(channelId);

        }

        @Test
        @DisplayName("삭제할 채널이 존재하지 않으면 예외 발생")
        void 삭제_실패_채널_없음() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicChannelService.deleteChannel(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            then(channelRepository).should()
                    .findById(channelId);

            then(channelRepository).should(never())
                    .deleteById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("(조회) 사용자별 채널 조회")
    class GetChannelsByUserId {

        @Test
        @DisplayName("사용자의 Private 채널과 전체 Public 채널을 조회")
        void 조회_성공() {
            // given
            UUID userId = UUID.randomUUID();
            UUID privateChannelId = UUID.randomUUID();

            Channel privateChannel = mock(Channel.class);
            Channel publicChannel = mock(Channel.class);
            ReadStatus readStatus = mock(ReadStatus.class);

            ChannelDto privateDto = mock(ChannelDto.class);
            ChannelDto publicDto = mock(ChannelDto.class);

            given(userRepository.existsById(userId))
                    .willReturn(true);

            given(readStatus.getChannel())
                    .willReturn(privateChannel);

            given(privateChannel.getId())
                    .willReturn(privateChannelId);
            given(privateChannel.getType())
                    .willReturn(ChannelType.PRIVATE);

            given(readStatusRepository.findAllByUserId(userId))
                    .willReturn(List.of(readStatus));

            given(channelRepository.findById(privateChannelId))
                    .willReturn(Optional.of(privateChannel));

            given(channelRepository.findAllByType(ChannelType.PUBLIC))
                    .willReturn(List.of(publicChannel));

            given(channelMapper.toDto(privateChannel))
                    .willReturn(privateDto);
            given(channelMapper.toDto(publicChannel))
                    .willReturn(publicDto);

            // when
            List<ChannelDto> result = basicChannelService.getChannelsByUserId(userId);

            // then
            assertThat(result)
                    .containsExactly(privateDto, publicDto);

            then(userRepository).should()
                    .existsById(userId);
            then(readStatusRepository).should()
                    .findAllByUserId(userId);
            then(channelRepository).should()
                    .findAllByType(ChannelType.PUBLIC);

            then(channelMapper).should()
                    .toDto(privateChannel);
            then(channelMapper).should()
                    .toDto(publicChannel);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생")
        void 조회_실패_사용자_없음() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(userId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> basicChannelService.getChannelsByUserId(userId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .existsById(userId);
        }
    }

}