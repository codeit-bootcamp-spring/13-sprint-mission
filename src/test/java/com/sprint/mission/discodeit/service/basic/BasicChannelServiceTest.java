package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.command.channel.PrivateChannelCommand;
import com.sprint.mission.discodeit.dto.command.channel.PublicChannelCommand;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock private ChannelRepository channelRepository;
    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널 생성 성공")
    void 공개채널_생성_성공 () {
        // given
        PublicChannelCommand command = new PublicChannelCommand("공지", "공지 채널");
        given(channelRepository.existsByName("공지")).willReturn(false);
        ChannelDto expected = new ChannelDto(null, ChannelType.PUBLIC, "공지", "공지 채널", null, null);
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelDto result = channelService.createPublicChannel(command);

        assertThat(result.name()).isEqualTo("공지");
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("PUBLIC 채널 중복 이름 생성 실패")
    void publicChannel_중복_실패() {
        PublicChannelCommand command = new PublicChannelCommand("공지", "공지 채널");
        given(channelRepository.existsByName("공지")).willReturn(true);

        assertThatThrownBy(() -> channelService.createPublicChannel(command))
                .isInstanceOf(ChannelAlreadyExistsException.class);
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 성공")
    void privateChannel_생성_성공() {
        UUID userId = UUID.randomUUID();
        PrivateChannelCommand command = new PrivateChannelCommand(List.of(userId));
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        ChannelDto expected = new ChannelDto(null, ChannelType.PRIVATE, null, null, null, null);
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelDto result = channelService.createPrivateChannel(command);

        assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 시 존재하지 않는 사용자 실패")
    void privateChannel_사용자없음_실패() {
        UUID userId = UUID.randomUUID();
        PrivateChannelCommand command = new PrivateChannelCommand(List.of(userId));
        given(channelRepository.save(any(Channel.class))).willReturn(null);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.createPrivateChannel(command))
                .isInstanceOf(com.sprint.mission.discodeit.exception.user.UserNotFoundException.class);
    }

    @Test
    @DisplayName("채널 수정 성공")
    void channel_수정_성공() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        ChannelUpdateCommand command = new ChannelUpdateCommand("새이름", "새설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        ChannelDto expected = new ChannelDto(null, ChannelType.PUBLIC, "새이름", "새설명", null, null);
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelDto result = channelService.updateChannel(channelId, command);

        assertThat(result.name()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("PRIVATE 채널 수정 시 실패")
    void channel_수정_private_실패() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        ChannelUpdateCommand command = new ChannelUpdateCommand("새이름", "새설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        assertThatThrownBy(() -> channelService.updateChannel(channelId, command))
                .isInstanceOf(PrivateChannelUpdateException.class);
    }

    @Test
    @DisplayName("채널 삭제 성공")
    void channel_삭제_성공() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        channelService.deleteChannel(channelId);

        then(channelRepository).should().deleteById(channel.getId());
    }

    @Test
    @DisplayName("존재하지 않는 채널 삭제 실패")
    void channel_삭제_실패() {
        UUID channelId = UUID.randomUUID();
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.deleteChannel(channelId))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("사용자별 채널 조회 성공")
    void findByUserId_성공() {
        UUID userId = UUID.randomUUID();
        given(channelRepository.findAll()).willReturn(List.of());
        given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());

        List<ChannelDto> result = channelService.findAllByUserId(userId);

        assertThat(result).isEmpty();
    }



}