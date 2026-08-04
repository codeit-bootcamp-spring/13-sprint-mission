package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널을 생성한다")
    void createPublic_success() {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "general",
                "general channel"
        );
        ChannelDto expected = new ChannelDto(
                UUID.randomUUID(),
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelDto result = channelService.createPublic(request);

        assertThat(result).isEqualTo(expected);
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("PRIVATE 채널을 생성한다")
    void createPrivate_success() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user1 = new User("user1", "user1@example.com", "password", null);
        User user2 = new User("user2", "user2@example.com", "password", null);
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(userId1, userId2)
        );
        ChannelDto expected = new ChannelDto(
                UUID.randomUUID(),
                ChannelType.PRIVATE,
                null,
                null,
                List.of(),
                null
        );

        given(userRepository.findById(userId1)).willReturn(Optional.of(user1));
        given(userRepository.findById(userId2)).willReturn(Optional.of(user2));
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelDto result = channelService.createPrivate(request);

        assertThat(result).isEqualTo(expected);
        then(channelRepository).should().save(any(Channel.class));
        then(readStatusRepository).should(times(2)).save(any(ReadStatus.class));
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 시 참여자를 찾지 못하면 실패한다")
    void createPrivate_fail_userNotFound() {
        UUID userId = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.createPrivate(request))
                .isInstanceOf(UserNotFoundException.class);

        then(channelRepository).should(never()).save(any(Channel.class));
        then(readStatusRepository).should(never()).save(any(ReadStatus.class));
    }

    @Test
    @DisplayName("PUBLIC 채널을 수정한다")
    void update_success() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel("general", "old description", null, ChannelType.PUBLIC);
        ChannelUpdateRequest request = new ChannelUpdateRequest(
                channelId,
                "notice",
                "new description"
        );
        ChannelDto expected = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "notice",
                "new description",
                List.of(),
                null
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(channelMapper.toDto(channel)).willReturn(expected);

        ChannelDto result = channelService.update(channelId, request);

        assertThat(result).isEqualTo(expected);
        assertThat(channel.getName()).isEqualTo("notice");
        assertThat(channel.getNameDescription()).isEqualTo("new description");
        then(channelRepository).should().save(channel);
    }

    @Test
    @DisplayName("PRIVATE 채널은 수정할 수 없다")
    void update_fail_privateChannel() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(null, null, null, ChannelType.PRIVATE);
        ChannelUpdateRequest request = new ChannelUpdateRequest(
                channelId,
                "private",
                "cannot update"
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(PrivateChannelUpdateException.class);

        then(channelRepository).should(never()).save(any(Channel.class));
    }

    @Test
    @DisplayName("존재하지 않는 채널은 수정에 실패한다")
    void update_fail_channelNotFound() {
        UUID channelId = UUID.randomUUID();
        ChannelUpdateRequest request = new ChannelUpdateRequest(
                channelId,
                "notice",
                "description"
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("사용자가 볼 수 있는 채널 목록을 조회한다")
    void findAllByUserId_success() {
        UUID userId = UUID.randomUUID();
        User user = new User("tester", "tester@example.com", "password", null);
        Channel publicChannel = new Channel("general", "public", null, ChannelType.PUBLIC);
        Channel privateChannel = new Channel(null, null, null, ChannelType.PRIVATE);
        ReadStatus readStatus = new ReadStatus(user, privateChannel, java.time.Instant.now());

        ChannelDto publicDto = new ChannelDto(publicChannel.getId(), ChannelType.PUBLIC, "general", "public", List.of(), null);
        ChannelDto privateDto = new ChannelDto(privateChannel.getId(), ChannelType.PRIVATE, null, null, List.of(), null);

        given(channelRepository.findAllByType(ChannelType.PUBLIC)).willReturn(List.of(publicChannel));
        given(readStatusRepository.findAllByUser_Id(userId)).willReturn(List.of(readStatus));
        given(channelMapper.toDto(publicChannel)).willReturn(publicDto);
        given(channelMapper.toDto(privateChannel)).willReturn(privateDto);

        List<ChannelDto> result = channelService.findAllByUserId(userId).stream().toList();

        assertThat(result).containsExactly(publicDto, privateDto);
    }

    @Test
    @DisplayName("채널을 삭제한다")
    void delete_success() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        List<Message> messages = List.of();
        List<ReadStatus> readStatuses = List.of();

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(messageRepository.findAllByChannel_Id(channelId)).willReturn(messages);
        given(readStatusRepository.findAllByChannel_Id(channelId)).willReturn(readStatuses);

        channelService.delete(channelId);

        then(messageRepository).should().deleteAll(messages);
        then(readStatusRepository).should().deleteAll(readStatuses);
        then(channelRepository).should().delete(channel);
    }

    @Test
    @DisplayName("존재하지 않는 채널은 삭제에 실패한다")
    void delete_fail_channelNotFound() {
        UUID channelId = UUID.randomUUID();

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.delete(channelId))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should(never()).delete(any(Channel.class));
    }
}