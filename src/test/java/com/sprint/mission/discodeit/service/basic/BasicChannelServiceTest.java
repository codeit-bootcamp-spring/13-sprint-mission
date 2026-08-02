package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    void createPublicChannel_성공() {
        ChannelPublicRequest request = new ChannelPublicRequest("공지", "공지 채널입니다.");
        ChannelResponse expected = channelResponse(ChannelType.PUBLIC, "공지");

        given(channelRepository.save(any(Channel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelResponse actual = channelService.createPublicChannel(request);

        assertThat(actual).isEqualTo(expected);
        then(channelRepository).should().save(any(Channel.class));
        then(channelMapper).should().toDto(any(Channel.class));
    }

    @Test
    void createPublicChannel_저장에_실패하면_예외를_전파한다() {
        ChannelPublicRequest request = new ChannelPublicRequest("공지", "공지 채널입니다.");
        given(channelRepository.save(any(Channel.class)))
                .willThrow(new DataIntegrityViolationException("채널 저장 실패"));

        assertThatThrownBy(() -> channelService.createPublicChannel(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("채널 저장 실패");

        then(channelMapper).shouldHaveNoInteractions();
    }

    @Test
    void createPrivateChannel_성공() {
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();
        ChannelPrivateRequest request =
                new ChannelPrivateRequest(List.of(firstUserId, secondUserId));
        User firstUser = new User("first", "first@example.com", "password123");
        User secondUser = new User("second", "second@example.com", "password123");
        ChannelResponse expected = channelResponse(ChannelType.PRIVATE, null);

        given(channelRepository.save(any(Channel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.findById(firstUserId)).willReturn(Optional.of(firstUser));
        given(userRepository.findById(secondUserId)).willReturn(Optional.of(secondUser));
        given(readStatusRepository.save(any(ReadStatus.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(channelMapper.toDto(any(Channel.class))).willReturn(expected);

        ChannelResponse actual = channelService.createPrivateChannel(request);

        assertThat(actual).isEqualTo(expected);
        then(readStatusRepository).should(times(2)).save(any(ReadStatus.class));
        then(channelMapper).should().toDto(any(Channel.class));
    }

    @Test
    void createPrivateChannel_참여자가_없으면_실패() {
        UUID userId = UUID.randomUUID();
        ChannelPrivateRequest request = new ChannelPrivateRequest(List.of(userId));

        given(channelRepository.save(any(Channel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.createPrivateChannel(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(readStatusRepository).should(never()).save(any(ReadStatus.class));
        then(channelMapper).shouldHaveNoInteractions();
    }

    @Test
    void update_PUBLIC_채널_수정_성공() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel("수정 전", "수정 전 설명", ChannelType.PUBLIC);
        ChannelPublicRequest request = new ChannelPublicRequest("수정 후", "수정 후 설명");
        ChannelResponse expected = channelResponse(ChannelType.PUBLIC, "수정 후");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(channelMapper.toDto(channel)).willReturn(expected);

        ChannelResponse actual = channelService.update(channelId, request);

        assertThat(actual).isEqualTo(expected);
        assertThat(channel.getName()).isEqualTo("수정 후");
        assertThat(channel.getDescription()).isEqualTo("수정 후 설명");
    }

    @Test
    void update_PRIVATE_채널이면_실패() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(null, null, ChannelType.PRIVATE);
        ChannelPublicRequest request = new ChannelPublicRequest("수정", "수정 설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(PrivateChannelUpdateException.class)
                .hasMessage("PRIVATE 채널은 수정할 수 없습니다.");

        then(channelMapper).shouldHaveNoInteractions();
    }

    @Test
    void delete_성공() {
        UUID channelId = UUID.randomUUID();

        channelService.delete(channelId);

        then(messageRepository).should().deleteByChannelId(channelId);
        then(readStatusRepository).should().deleteByChannelId(channelId);
        then(channelRepository).should().deleteById(channelId);
    }

    @Test
    void delete_저장소_오류가_발생하면_실패() {
        UUID channelId = UUID.randomUUID();
        willThrow(new DataAccessResourceFailureException("메시지 삭제 실패"))
                .given(messageRepository)
                .deleteByChannelId(channelId);

        assertThatThrownBy(() -> channelService.delete(channelId))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessage("메시지 삭제 실패");

        then(readStatusRepository).shouldHaveNoInteractions();
        then(channelRepository).should(never()).deleteById(channelId);
    }

    @Test
    void findAllByUserId_공개_채널과_참여중인_PRIVATE_채널을_조회한다() {
        UUID userId = UUID.randomUUID();
        Channel publicChannel = new Channel("공지", "공지 채널", ChannelType.PUBLIC);
        Channel privateChannel = new Channel(null, null, ChannelType.PRIVATE);
        ChannelResponse publicResponse = channelResponse(ChannelType.PUBLIC, "공지");
        ChannelResponse privateResponse = channelResponse(ChannelType.PRIVATE, null);

        given(channelRepository.findAll()).willReturn(List.of(publicChannel, privateChannel));
        given(readStatusRepository.existsByChannelIdAndUserId(privateChannel.getId(), userId))
                .willReturn(true);
        given(channelMapper.toDto(publicChannel)).willReturn(publicResponse);
        given(channelMapper.toDto(privateChannel)).willReturn(privateResponse);

        List<ChannelResponse> actual = channelService.findAllByUserId(userId);

        assertThat(actual).containsExactly(publicResponse, privateResponse);
    }

    @Test
    void findAllByUserId_참여중인_채널이_없으면_빈_목록을_반환한다() {
        UUID userId = UUID.randomUUID();
        Channel privateChannel = new Channel(null, null, ChannelType.PRIVATE);

        given(channelRepository.findAll()).willReturn(List.of(privateChannel));
        given(readStatusRepository.existsByChannelIdAndUserId(privateChannel.getId(), userId))
                .willReturn(false);

        List<ChannelResponse> actual = channelService.findAllByUserId(userId);

        assertThat(actual).isEmpty();
        then(channelMapper).shouldHaveNoInteractions();
    }

    private ChannelResponse channelResponse(ChannelType type, String name) {
        return new ChannelResponse(
                UUID.randomUUID(),
                name,
                type == ChannelType.PUBLIC ? "채널 설명" : null,
                type,
                List.of(),
                null
        );
    }
}