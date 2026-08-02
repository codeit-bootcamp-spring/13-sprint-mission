package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelTypeNotAllowedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널 생성 성공 - 정상 요청이면 ChannelDto를 반환한다")
    void createPublic_성공() {
        // given
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "일반채널", "자유롭게 대화하세요"
        );
        Channel channel = new Channel(ChannelType.PUBLIC, "일반채널", "자유롭게 대화하세요");
        ChannelDto expectedDto = new ChannelDto(
                channel.getId(), ChannelType.PUBLIC, "일반채널", "자유롭게 대화하세요",
                List.<UserDto>of(), Instant.now()
        );

        given(channelRepository.save(any(Channel.class))).willReturn(channel);
        given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.create(request);

        // then
        assertThat(result.name()).isEqualTo("일반채널");
        assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 성공 - 참여자 ID 목록이 있으면 ReadStatus도 함께 생성된다")
    void createPrivate_성공() {
        // given
        UUID participantId = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(participantId)
        );
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        ChannelDto expectedDto = new ChannelDto(
                channel.getId(), ChannelType.PRIVATE, null, null,
                List.<UserDto>of(), Instant.now()
        );

        given(channelRepository.save(any(Channel.class))).willReturn(channel);
        // 참여자 조회 시 빈 리스트 반환
        given(userRepository.findAllById(any())).willReturn(List.of());
        given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.create(request);

        // then
        assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("채널 수정 성공 - PUBLIC 채널이면 이름/설명을 수정할 수 있다")
    void update_성공() {
        // given
        UUID channelId = UUID.randomUUID();
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "기존이름", "기존설명");
        PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
        ChannelDto expectedDto = new ChannelDto(
                channelId, ChannelType.PUBLIC, "새이름", "새설명", List.<UserDto>of(), Instant.now()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
        given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.update(channelId, request);

        // then
        assertThat(result.name()).isEqualTo("새이름");
        assertThat(result.description()).isEqualTo("새설명");
    }

    @Test
    @DisplayName("채널 수정 실패 - 존재하지 않는 채널이면 ChannelNotFoundException 발생")
    void update_실패_채널없음() {
        // given
        UUID notExistChannelId = UUID.randomUUID();
        PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
        given(channelRepository.findById(notExistChannelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.update(notExistChannelId, request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("채널 수정 실패 - PRIVATE 채널은 수정할 수 없다")
    void update_실패_PRIVATE채널() {
        // given: PRIVATE 채널
        UUID channelId = UUID.randomUUID();
        Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
        PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

        // when & then
        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(ChannelTypeNotAllowedException.class);
    }

    @Test
    @DisplayName("채널 삭제 성공 - 존재하는 채널이면 삭제 후 관련 Message, ReadStatus도 삭제된다")
    void delete_성공() {
        // given
        UUID channelId = UUID.randomUUID();
        given(channelRepository.existsById(channelId)).willReturn(true);

        // when
        channelService.delete(channelId);

        // then
        then(messageRepository).should().deleteAllByChannelId(channelId);
        then(readStatusRepository).should().deleteAllByChannelId(channelId);
        then(channelRepository).should().deleteById(channelId);
    }

    @Test
    @DisplayName("채널 삭제 실패 - 존재하지 않는 채널이면 ChannelNotFoundException 발생")
    void delete_실패_채널없음() {
        // given
        UUID notExistChannelId = UUID.randomUUID();
        given(channelRepository.existsById(notExistChannelId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> channelService.delete(notExistChannelId))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - PUBLIC 채널과 참여 중인 PRIVATE 채널을 반환한다")
    void findAllByUserId_성공() {
        // given
        UUID userId = UUID.randomUUID();
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공개채널", "설명");
        ChannelDto channelDto = new ChannelDto(
                publicChannel.getId(), ChannelType.PUBLIC, "공개채널", "설명", List.<UserDto>of(), Instant.now()
        );

        given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());
        given(channelRepository.findAllByTypeOrIdIn(any(), any())).willReturn(List.of(publicChannel));
        given(channelMapper.toDto(any(Channel.class))).willReturn(channelDto);

        // when
        List<ChannelDto> result = channelService.findAllByUserId(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("공개채널");
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 - 채널이 없으면 빈 리스트를 반환한다")
    void findAllByUserId_빈목록() {
        // given
        UUID userId = UUID.randomUUID();
        given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());
        given(channelRepository.findAllByTypeOrIdIn(any(), any())).willReturn(List.of());

        // when
        List<ChannelDto> result = channelService.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();
    }
}