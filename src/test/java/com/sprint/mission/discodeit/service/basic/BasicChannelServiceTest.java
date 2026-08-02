package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ChannelMapper channelMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private BasicChannelService basicChannelService;

    @Test
    @DisplayName("공개 채널 생성 성공")
    void createPublic_success() {
        // given
        PublicChannelCreateRequest request = new PublicChannelCreateRequest("공지", "설명");
        given(channelMapper.toDto(any(Channel.class)))
                .willReturn(new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "공지", "설명", List.of(), null));

        // when
        ChannelDto result = basicChannelService.createPublic(request);

        // then
        assertThat(result.name()).isEqualTo("공지");
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("비공개 채널 생성 성공")
    void createPrivate_success() {
        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId1, userId2));

        given(userRepository.findById(userId1)).willReturn(Optional.of(mock(User.class)));
        given(userRepository.findById(userId2)).willReturn(Optional.of(mock(User.class)));
        given(channelMapper.toDto(any(Channel.class)))
                .willReturn(new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, null, null, List.of(), null));

        // when
        ChannelDto result = basicChannelService.createPrivate(request);

        // then
        assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
        then(channelRepository).should().save(any(Channel.class));
        then(readStatusRepository).should(times(2)).save(any());
    }

    @Test
    @DisplayName("비공개 채널 생성 실패 - 존재하지 않는 사용자")
    void createPrivate_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicChannelService.createPrivate(request))
                .isInstanceOf(UserNotFoundException.class);

        then(readStatusRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("채널 수정 성공")
    void update_success() {
        // given
        Channel channel = new Channel(ChannelType.PUBLIC, "이전이름", "이전설명");
        UUID channelId = channel.getId();
        ChannelUpdateRequest request = new ChannelUpdateRequest("새이름", "새설명");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(channelMapper.toDto(any(Channel.class)))
                .willReturn(new ChannelDto(channelId, ChannelType.PUBLIC, "새이름", "새설명", List.of(), null));

        // when
        ChannelDto result = basicChannelService.update(channelId, request);

        // then
        assertThat(channel.getName()).isEqualTo("새이름");
        assertThat(result.name()).isEqualTo("새이름");
        then(channelRepository).should().save(channel);
    }

    @Test
    @DisplayName("채널 수정 실패 - 존재하지 않는 채널")
    void update_fail_channelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        ChannelUpdateRequest request = new ChannelUpdateRequest("새이름", "새설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicChannelService.update(channelId, request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("채널 수정 실패 - 비공개 채널은 수정 불가")
    void update_fail_privateChannelUpdate() {
        // given
        Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
        UUID channelId = privateChannel.getId();
        ChannelUpdateRequest request = new ChannelUpdateRequest("새이름", "새설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

        // when & then
        assertThatThrownBy(() -> basicChannelService.update(channelId, request))
                .isInstanceOf(PrivateChannelUpdateException.class);

        then(channelRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("채널 삭제 성공")
    void delete_success() {
        // given
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        UUID channelId = channel.getId();
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        // when
        basicChannelService.delete(channelId);

        // then
        then(channelRepository).should().delete(channel);
    }

    @Test
    @DisplayName("채널 삭제 실패 - 존재하지 않는 채널")
    void delete_fail_channelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicChannelService.delete(channelId))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("findAllByUserId - 공개 채널은 참여 여부와 상관없이 포함된다")
    void findAllByUserId_includesPublicChannel() {
        // given
        UUID userId = UUID.randomUUID();
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지", "공지 채널 입니다.");

        given(channelRepository.findAll()).willReturn(List.of(publicChannel));
        given(readStatusRepository.findAllByUser_Id(userId)).willReturn(List.of());
        given(channelMapper.toDto(any(Channel.class), anyList(), any()))
                .willReturn(new ChannelDto(publicChannel.getId(), ChannelType.PUBLIC, "공지", "공지 채널 입니다.", List.of(), null));

        // when
        List<ChannelDto> result = basicChannelService.findAllByUserId(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("공지");
    }

    @Test
    @DisplayName("findAllByUserId - 참여하지 않은 비공개 채널은 제외된다")
    void findAllByUserId_excludesNotJoinedPrivateChannel() {
        // given
        UUID userId = UUID.randomUUID();
        Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);

        given(channelRepository.findAll()).willReturn(List.of(privateChannel));
        given(readStatusRepository.findAllByUser_Id(userId)).willReturn(List.of());

        // when
        List<ChannelDto> result = basicChannelService.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();
        then(channelMapper).should(never()).toDto(any(Channel.class), anyList(), any());
    }
}
