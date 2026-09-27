package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.ChannelSummary;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.MessageReader;
import com.sprint.mission.discodeit.service.basic.ReadStatusService;
import com.sprint.mission.discodeit.service.basic.UserReader;
import com.sprint.mission.discodeit.utils.RequestTimeZoneUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @InjectMocks
    BasicChannelService channelService;

    @Mock
    ChannelRepository channelRepository;

    @Mock
    ReadStatusService readStatusService;

    @Mock
    MessageService messageService;

    @Mock
    ChannelMapper channelMapper;

    @Mock
    UserReader userReader;

    @Mock
    MessageReader messageReader;

    @Nested
    @DisplayName("채널 생성")
    class SaveChannelTest {

        @Test
        @DisplayName("공개 채널 생성 성공")
        void save_returnsPublicChannelDto_whenPublicCommandIsValid() {
            // given
            // 공개 채널 생성 요청 command와 저장 후 부여될 channelId를 준비한다.
            // command는 단순 값 객체이므로 실제 객체를 사용하고,
            // 서비스가 command 값으로 실제 Channel 엔티티를 만드는지 ArgumentCaptor로 검증한다.
            UUID channelId = UUID.randomUUID();
            ChannelCreatePublicCommand publicCreateCommand = createPublicCreateCommand();

            // mapper가 반환할 최종 DTO는 실제 record 객체로 준비한다.
            // 실제 Channel -> ChannelDto 필드 매핑은 ChannelMapper 테스트 책임이므로,
            // 여기서는 mapper 반환값이 서비스 반환값으로 이어지는지만 확인한다.
            ChannelDto publicChannelDto = createPublicChannelDto(channelId, publicCreateCommand);

            // channelRepository는 mock이라 실제 DB 저장이나 id 생성을 하지 않는다.
            // 서비스는 저장된 Channel을 mapper에 전달하므로, save(...)에 들어온 Channel에 id를 넣고 그대로 반환한다.
            given(channelRepository.save(any(Channel.class)))
                    .willAnswer(invocation -> {
                        Channel channel = invocation.getArgument(0);
                        setId(channel, channelId);
                        return channel;
                    });

            given(channelMapper.toDto(any(Channel.class))).willReturn(publicChannelDto);

            // when
            // 공개 채널 생성 로직을 실행한다.
            ChannelDto result = channelService.save(publicCreateCommand);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(publicChannelDto);

            // repository.save(...)에 전달된 실제 Channel을 캡처해서 command 값이 반영됐는지 확인한다.
            ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
            verify(channelRepository).save(captor.capture());

            Channel savedChannel = captor.getValue();
            assertThat(savedChannel.getId()).isEqualTo(channelId);
            assertThat(savedChannel.getType()).isEqualTo(ChannelType.PUBLIC);
            assertThat(savedChannel.getName()).isEqualTo(publicCreateCommand.channelName());
            assertThat(savedChannel.getDescription()).isEqualTo(publicCreateCommand.channelDescription());

            // 공개 채널은 참여자 목록이 없으므로 ReadStatus 생성/조회가 필요 없다.
            // 또한 사용자 존재 확인, 최신 메시지 조회, 메시지 삭제 같은 다른 서비스 책임도 수행하지 않는다.
            verifyNoInteractions(readStatusService, userReader, messageReader, messageService);

            // 공개 채널 DTO 변환은 단일 Channel 인자 mapper를 사용해야 한다.
            // 비공개 채널용 toDto(channel, readStatuses) 오버로드가 호출되면 분기 처리가 잘못된 것이다.
            verify(channelMapper).toDto(savedChannel);
            verify(channelMapper, never()).toDto(any(Channel.class), any());

            verifyNoMoreInteractions(channelRepository, channelMapper);
        }

        @Test
        @DisplayName("비공개 채널 생성 성공 - 참여자 읽음 상태 생성")
        void save_returnsPrivateChannelDto_whenPrivateCommandIsValid() {
            // given
            // 비공개 채널 생성 요청과 참여자 id 목록을 준비한다.
            // 비공개 채널은 저장 직후 참여자별 ReadStatus를 생성해야 하므로 participantIds 전달 여부가 핵심이다.
            UUID channelId = UUID.randomUUID();
            List<UUID> participantIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            ChannelCreatePrivateCommand privateCommand = new ChannelCreatePrivateCommand(participantIds, ChannelType.PRIVATE);

            // mapper가 반환할 최종 DTO는 실제 record 객체로 준비한다.
            ChannelDto privateChannelDto = createPrivateChannelDto(channelId);

            // ReadStatus는 이 테스트에서 내부 상태를 검증하지 않고 mapper로 전달만 한다.
            // 따라서 실제 엔티티를 어렵게 구성하지 않고 mock 객체로 충분하다.
            ReadStatus readStatus = mock(ReadStatus.class);
            List<ReadStatus> readStatuses = List.of(readStatus);

            // mock repository는 실제 id 생성을 하지 않으므로 저장된 Channel에 channelId를 넣어 반환한다.
            // BasicChannelService.save(...)는 savedChannel.getId()로 ReadStatus를 다시 조회한다.
            given(channelRepository.save(any(Channel.class))).willAnswer(invocation -> {
                Channel channel = invocation.getArgument(0);
                setId(channel, channelId);
                return channel;
            });

            given(readStatusService.findAllByChannelId(channelId)).willReturn(readStatuses);
            given(channelMapper.toDto(any(Channel.class), eq(readStatuses))).willReturn(privateChannelDto);

            // when
            // 비공개 채널 생성 로직을 실행한다.
            ChannelDto result = channelService.save(privateCommand);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(privateChannelDto);

            // 비공개 채널 생성 성공 흐름은 순서가 의미 있다.
            // 1. Channel 저장
            // 2. 저장된 Channel과 participantIds로 ReadStatus 생성
            // 3. 저장된 channelId로 ReadStatus 목록 재조회
            // 4. Channel과 ReadStatus 목록을 함께 DTO 변환
            InOrder inOrder = inOrder(channelRepository, readStatusService, channelMapper);

            ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
            inOrder.verify(channelRepository).save(channelCaptor.capture());

            Channel savedChannel = channelCaptor.getValue();
            assertThat(savedChannel.getId()).isEqualTo(channelId);
            assertThat(savedChannel.getType()).isEqualTo(ChannelType.PRIVATE);
            assertThat(savedChannel.getName()).isEmpty();
            assertThat(savedChannel.getDescription()).isEmpty();

            // saveAll(...)의 세 번째 인자는 서비스 내부 Instant.now()이므로 정확한 값 대신 타입과 null 아님을 검증한다.
            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
            inOrder.verify(readStatusService).saveAll(eq(savedChannel), eq(participantIds), instantCaptor.capture());
            assertThat(instantCaptor.getValue()).isNotNull();

            inOrder.verify(readStatusService).findAllByChannelId(channelId);
            inOrder.verify(channelMapper).toDto(savedChannel, readStatuses);

            // 비공개 채널은 참여자 정보를 포함해야 하므로 단일 인자 mapper를 사용하면 안 된다.
            verify(channelMapper, never()).toDto(any(Channel.class));

            // 채널 생성은 사용자 존재 확인, 최신 메시지 조회, 메시지 삭제와 무관하다.
            verifyNoInteractions(userReader, messageReader, messageService);
            verifyNoMoreInteractions(channelRepository, readStatusService, channelMapper);
        }
    }

    @Nested
    @DisplayName("채널 단건 조회")
    class FindChannelTest {

        @Test
        @DisplayName("채널 단건 조회 성공")
        void findById_returnsChannelDto_whenChannelExists() {
            // given
            // findById()는 Channel 엔티티 전체가 아니라 ChannelSummary 조회 결과를 기반으로 DTO를 만든다.
            // 따라서 repository가 반환할 ChannelSummary와 mapper가 반환할 ChannelDto를 준비한다.
            UUID channelId = UUID.randomUUID();
            ChannelSummary channelSummary = createChannelSummary(channelId, ChannelType.PUBLIC);
            ChannelDto channelDto = createChannelDto(channelSummary);

            // 단건 조회 결과에도 참여자 정보가 포함되어야 하므로,
            // 같은 channelId로 조회한 ReadStatus 목록이 mapper에 그대로 전달되는지 확인한다.
            List<ReadStatus> readStatuses = List.of(mock(ReadStatus.class));

            given(channelRepository.findByDetail(channelId)).willReturn(Optional.of(channelSummary));
            given(readStatusService.findAllByChannelId(channelId)).willReturn(readStatuses);

            // any()로 느슨하게 받으면 잘못된 ChannelSummary나 ReadStatus가 전달되어도 테스트가 통과할 수 있다.
            // 그래서 실제로 기대하는 인자 조합에 대해서만 DTO를 반환하도록 설정한다.
            given(channelMapper.toDto(channelSummary, readStatuses)).willReturn(channelDto);

            // when
            // 서비스의 채널 단건 조회 로직을 실행한다.
            ChannelDto result = channelService.findById(channelId);

            // then
            // mapper가 변환한 DTO가 서비스의 최종 반환값인지 검증한다.
            assertThat(result).isEqualTo(channelDto);

            // 조회 순서도 서비스 계약의 일부로 확인한다.
            // 채널이 존재할 때만 ReadStatus를 조회하고, 둘을 합쳐 mapper로 변환해야 한다.
            InOrder inOrder = inOrder(channelRepository, readStatusService, channelMapper);
            inOrder.verify(channelRepository).findByDetail(channelId);
            inOrder.verify(readStatusService).findAllByChannelId(channelId);
            inOrder.verify(channelMapper).toDto(channelSummary, readStatuses);

            // findById()는 조회 전용 로직이므로 다른 협력 객체를 사용하지 않아야 한다.
            verifyNoMoreInteractions(channelRepository, readStatusService, channelMapper);
            verifyNoInteractions(messageService, userReader, messageReader);

        }

        @Test
        @DisplayName("채널 단건 조회 실패 - 존재하지 않는 채널")
        void findById_throwsChannelNotFoundException_whenChannelDoesNotExist() {
            // given
            // 조회할 channelId는 있지만, repository가 ChannelSummary를 찾지 못하는 상황을 만든다.
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findByDetail(channelId)).willReturn(Optional.empty());

            // when & then
            // findByDetail(...) 결과가 비어 있으면 서비스는 ChannelNotFoundException을 던져야 한다.
            assertThatThrownBy(() -> channelService.findById(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            // channelId로 단건 상세 조회를 시도했는지 확인한다.
            verify(channelRepository).findByDetail(channelId);

            // 채널이 없으면 참여자 읽음 상태 조회와 DTO 변환은 실행되면 안 된다.
            verify(readStatusService, never()).findAllByChannelId(any(UUID.class));
            verify(channelMapper, never()).toDto(any(ChannelSummary.class), anyList());

            // 위에서 검증한 호출 외에 findById() 실패 흐름에서 추가 협력이 없었는지 확인한다.
            verifyNoMoreInteractions(channelRepository, readStatusService, channelMapper);
            verifyNoInteractions(messageService, userReader, messageReader);
        }
    }

    @Nested
    @DisplayName("사용자별 채널 목록 조회")
    class FindAllChannelTest {

        @BeforeEach
        void clearPreviousAuthentication() {
            SecurityContextHolder.clearContext();
        }

        @AfterEach
        void clearAuthentication() {
            SecurityContextHolder.clearContext();
        }

        private void authenticate(UUID userId, Role role) {
            UserDto user = new UserDto(userId, "requester", "requester@test.local", null,
                    false, role, null, null);
            DiscodeitUserDetails principal = new DiscodeitUserDetails(user, "unused-password");
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        }

        @Test
        @DisplayName("사용자별 채널 목록 조회 성공")
        void findAllByUserId_returnsChannelDtos_whenUserExists() {
            // given
            // 조회할 사용자가 존재하고, 사용자가 볼 수 있는 채널이 여러 개 있는 상황을 만든다.
            // 공개 채널과 비공개 채널을 섞어 두면 각 채널별 ReadStatus 매핑이 올바른지 함께 확인할 수 있다.
            UUID userId = UUID.randomUUID();
            authenticate(userId, Role.USER);
            UUID firstPublicChannelId = UUID.randomUUID();
            UUID firstPrivateChannelId = UUID.randomUUID();
            UUID secondPrivateChannelId = UUID.randomUUID();
            UUID secondPublicChannelId = UUID.randomUUID();

            ChannelSummary firstPublicChannel = createChannelSummary(firstPublicChannelId, ChannelType.PUBLIC);
            ChannelSummary firstPrivateChannel = createChannelSummary(firstPrivateChannelId, ChannelType.PRIVATE);
            ChannelSummary secondPrivateChannel = createChannelSummary(secondPrivateChannelId, ChannelType.PRIVATE);
            ChannelSummary secondPublicChannel = createChannelSummary(secondPublicChannelId, ChannelType.PUBLIC);

            // repository가 반환할 조회 가능한 채널 목록이다.
            // 서비스는 이 순서를 유지한 채 ChannelDto 목록을 반환해야 한다.
            List<ChannelSummary> channelSummaries = List.of(
                    firstPublicChannel,
                    firstPrivateChannel,
                    secondPrivateChannel,
                    secondPublicChannel
            );

            // 서비스는 ChannelSummary 목록에서 id만 뽑아 ReadStatus 조회에 사용한다.
            // 이 리스트가 readStatusService.findAllByChannelIds(...)에 그대로 전달되는지 검증한다.
            List<UUID> channelIds = List.of(
                    firstPublicChannelId,
                    firstPrivateChannelId,
                    secondPrivateChannelId,
                    secondPublicChannelId
            );

            // findAllByUserId(...)는 ReadStatus를 channelId 기준으로 그룹핑한다.
            // 이 테스트에서는 비공개 채널 2개에만 ReadStatus가 있는 상황으로 둔다.
            ReadStatus firstPrivateReadStatus = createReadStatus(firstPrivateChannelId);
            ReadStatus secondPrivateReadStatus = createReadStatus(secondPrivateChannelId);
            List<ReadStatus> readStatuses = List.of(firstPrivateReadStatus, secondPrivateReadStatus);

            // mapper에 전달되어야 하는 채널별 ReadStatus 목록을 명시적으로 준비한다.
            // 공개 채널에는 ReadStatus가 현재 없다고 가정하고 빈 리스트가 전달되어야 한다.
            List<ReadStatus> firstPublicReadStatuses = List.of();
            List<ReadStatus> firstPrivateReadStatuses = List.of(firstPrivateReadStatus);
            List<ReadStatus> secondPrivateReadStatuses = List.of(secondPrivateReadStatus);
            List<ReadStatus> secondPublicReadStatuses = List.of();

            // mapper가 반환할 DTO를 미리 준비한다.
            // 이 테스트의 관심사는 실제 매핑 결과가 아니라 서비스가 mapper 결과를 순서대로 반환하는지다.
            ChannelDto firstPublicDto = createChannelDto(firstPublicChannel);
            ChannelDto firstPrivateDto = createChannelDto(firstPrivateChannel);
            ChannelDto secondPrivateDto = createChannelDto(secondPrivateChannel);
            ChannelDto secondPublicDto = createChannelDto(secondPublicChannel);
            List<ChannelDto> expectedDtos = List.of(firstPublicDto, firstPrivateDto, secondPrivateDto, secondPublicDto);

            given(userReader.isUserExist(userId)).willReturn(true);
            given(channelRepository.findVisibleChannels(userId, List.of(ChannelType.PUBLIC))).willReturn(channelSummaries);
            given(readStatusService.findAllByChannelIds(channelIds)).willReturn(readStatuses);
            given(channelMapper.toDto(firstPublicChannel, firstPublicReadStatuses)).willReturn(firstPublicDto);
            given(channelMapper.toDto(firstPrivateChannel, firstPrivateReadStatuses)).willReturn(firstPrivateDto);
            given(channelMapper.toDto(secondPrivateChannel, secondPrivateReadStatuses)).willReturn(secondPrivateDto);
            given(channelMapper.toDto(secondPublicChannel, secondPublicReadStatuses)).willReturn(secondPublicDto);

            // when
            // 사용자별 채널 목록 조회 로직을 실행한다.
            List<ChannelDto> result = channelService.findAllByUserId(userId);

            // then
            // 서비스는 mapper가 만들어준 DTO 목록을 repository 조회 순서대로 반환해야 한다.
            assertThat(result).isEqualTo(expectedDtos);

            verify(channelRepository).findVisibleChannels(userId, List.of(ChannelType.PUBLIC));
            verify(readStatusService).findAllByChannelIds(channelIds);
            verify(channelMapper).toDto(firstPublicChannel, firstPublicReadStatuses);
            verify(channelMapper).toDto(firstPrivateChannel, firstPrivateReadStatuses);
            verify(channelMapper).toDto(secondPrivateChannel, secondPrivateReadStatuses);
            verify(channelMapper).toDto(secondPublicChannel, secondPublicReadStatuses);
        }

        @Test
        @DisplayName("사용자별 채널 목록 조회 성공 - 조회 가능한 채널이 없음")
        void findAllByUserId_returnsEmptyList_whenVisibleChannelDoesNotExist() {
            // given
            // 조회할 사용자는 존재하지만, 사용자가 볼 수 있는 채널 목록은 비어 있는 상황을 만든다.
            // 이 케이스는 예외가 아니라 "정상적으로 빈 목록을 반환하는 흐름"을 검증한다.
            UUID userId = UUID.randomUUID();
            authenticate(userId, Role.USER);
            List<ChannelSummary> channelSummaries = List.of();
            List<UUID> channelIds = List.of();

            // findAllByUserId(...)는 가장 먼저 사용자 존재 여부를 확인한다.
            // 여기서는 사용자가 존재하는 성공 흐름이므로 true를 반환하게 설정한다.
            given(userReader.isUserExist(userId)).willReturn(true);

            // 사용자가 존재하면 repository에서 조회 가능한 채널 요약 목록을 가져온다.
            // 이 테스트에서는 조회 가능한 채널이 없으므로 빈 리스트를 반환한다.
            given(channelRepository.findVisibleChannels(userId, List.of(ChannelType.PUBLIC))).willReturn(channelSummaries);

            // 서비스는 조회된 ChannelSummary 목록에서 channelId 목록을 만든 뒤 ReadStatus를 조회한다.
            // channelSummaries가 비어 있으므로 channelIds도 빈 리스트다.
            // anyList()보다 빈 리스트를 명시하면 "채널이 없어서 빈 id 목록으로 조회한다"는 의도가 더 분명하다.
            given(readStatusService.findAllByChannelIds(channelIds)).willReturn(List.of());

            // when
            // 사용자별 채널 목록 조회 로직을 실행한다.
            List<ChannelDto> channelDtos = channelService.findAllByUserId(userId);

            // then
            // 조회 가능한 채널이 없으면 서비스는 null이 아니라 빈 리스트를 반환해야 한다.
            assertThat(channelDtos).isEmpty();

            // 사용자 존재 여부를 확인했는지 검증한다.
            verify(userReader).isUserExist(userId);

            // 존재하는 사용자이므로 조회 가능한 채널 목록 조회까지 실행되어야 한다.
            verify(channelRepository).findVisibleChannels(userId, List.of(ChannelType.PUBLIC));

            // 채널 목록이 비어 있어도 현재 서비스 구현은 빈 channelIds로 ReadStatus 조회를 한 번 수행한다.
            // 이 동작까지 고정하고 싶다면 anyList()보다 channelIds를 직접 검증하는 편이 명확하다.
            verify(readStatusService).findAllByChannelIds(channelIds);

            // ChannelSummary가 하나도 없으면 DTO로 변환할 대상도 없다.
            // verify(channelMapper, never()).toDto(...)처럼 특정 메서드만 막을 수도 있지만,
            // 이 흐름에서는 mapper 자체가 전혀 호출되면 안 되므로 verifyNoInteractions(...)가 더 직접적이다.
            verifyNoInteractions(channelMapper);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"CHANNEL_MANAGER", "ADMIN"})
        @DisplayName("관리 역할은 다른 사용자를 기준으로 조회해도 모든 채널 타입을 요청한다")
        void findAllByUserId_includesAllTypes_whenRequesterHasManagementRole(Role role) {
            UUID targetUserId = UUID.randomUUID();
            authenticate(UUID.randomUUID(), role);
            List<ChannelType> types = List.of(ChannelType.PUBLIC, ChannelType.PRIVATE);
            ChannelSummary summary = createChannelSummary(UUID.randomUUID(), ChannelType.PRIVATE);
            ChannelDto expected = createChannelDto(summary);
            given(userReader.isUserExist(targetUserId)).willReturn(true);
            given(channelRepository.findVisibleChannels(targetUserId, types)).willReturn(List.of(summary));
            given(readStatusService.findAllByChannelIds(List.of(summary.id()))).willReturn(List.of());
            given(channelMapper.toDto(summary, List.of())).willReturn(expected);

            assertThat(channelService.findAllByUserId(targetUserId)).containsExactly(expected);

            verify(channelRepository).findVisibleChannels(targetUserId, types);
        }

        @Test
        @DisplayName("사용자별 채널 목록 조회 실패 - 존재하지 않는 사용자")
        void findAllByUserId_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            // 조회할 userId는 있지만, UserReader가 해당 사용자를 찾지 못하는 상황을 만든다.
            // BasicChannelService.findAllByUserId(...)는 가장 먼저 사용자 존재 여부를 확인한다.
            UUID userId = UUID.randomUUID();

            given(userReader.isUserExist(userId)).willReturn(false);

            // when & then
            // 사용자가 존재하지 않으면 채널 목록 조회를 진행하지 않고 UserNotFoundException을 던져야 한다.
            assertThatThrownBy(() -> channelService.findAllByUserId(userId))
                    .isInstanceOf(UserNotFoundException.class);

            // 사용자 존재 여부를 확인하는 첫 번째 검증이다.
            // 이 호출 결과가 false이므로 서비스 로직은 여기서 예외로 종료된다.
            verify(userReader).isUserExist(userId);

            // 학습 포인트:
            // 이 케이스에서는 사용자가 없으므로 findAllByUserId(...)가 예외를 던지고 즉시 종료되어야 한다.
            // 따라서 아래 후속 협력 메서드들은 모두 호출되면 안 된다.
            // - channelRepository.findVisibleChannels(...): 사용자가 볼 수 있는 채널 목록 조회
            // - readStatusService.findAllByChannelIds(...): 조회된 채널들의 읽음 상태 목록 조회
            // - channelMapper.toDto(...): 조회 결과를 ChannelDto로 변환
            //
            // 이전에 작성했던 방식처럼 각 메서드를 never()로 하나씩 검증할 수도 있다.
            // verify(channelRepository, never()).findVisibleChannels(any(UUID.class), anyList());
            // verify(readStatusService, never()).findAllByChannelIds(anyList());
            // verify(channelMapper, never()).toDto(any(ChannelSummary.class), anyList());
            //
            // 다만 지금처럼 후속 협력 객체 자체가 아예 호출되면 안 되는 흐름에서는
            // verifyNoInteractions(...)로 "이 mock들과는 아무 상호작용도 없었다"를 표현하는 편이 더 간결하고 의도가 분명하다.
            verifyNoInteractions(channelRepository, readStatusService, channelMapper);
        }
    }

    @Nested
    @DisplayName("채널 수정")
    class UpdateChannelTest {

        @Test
        @DisplayName("공개 채널 수정 성공 - 마지막 메시지가 있음")
        void update_returnsChannelDtoWithLastMessageAt_whenPublicChannelExistsAndLatestMessageExists() {
            // given
            // 수정할 채널 id, 수정 요청 command, 기존 공개 채널 엔티티를 준비한다.
            // 이 테스트는 "존재하는 공개 채널"을 수정하는 성공 케이스다.
            UUID channelId = UUID.randomUUID();
            ChannelUpdateCommand updateCommand = createUpdateCommand();
            Channel publicChannel = createPublicChannel(channelId);

            // 마지막 메시지가 존재하는 상황을 만든다.
            // BasicChannelService.update(...)는 messageReader에서 최신 메시지를 조회한 뒤
            // Message::getCreatedAt 으로 마지막 메시지 시간을 꺼내 mapper에 전달한다.
            // Message는 mock이므로 getCreatedAt() 반환값을 직접 stub 해야 한다.
            Instant lastMessageAt = Instant.now();
            Message message = mock(Message.class);
            given(message.getCreatedAt()).willReturn(lastMessageAt);

            // update() 서비스 로직은 ReadStatus 내부 필드에 접근하지 않는다.
            // 조회된 ReadStatus 목록을 mapper에 그대로 넘기는지만 검증하면 되므로 단순 mock으로 충분하다.
            ReadStatus readStatus = mock(ReadStatus.class);
            List<ReadStatus> readStatuses = List.of(readStatus);

            // mapper가 반환할 최종 DTO를 준비한다.
            // lastMessageAt의 OffsetDateTime 변환은 실제 mapper 책임이지만,
            // 여기서는 "마지막 메시지 시간이 있는 DTO가 반환된다"는 기대값을 명확히 만들기 위해 사용한다.
            ChannelDto publicChannelDto = createPublicChannelDto(channelId, updateCommand, RequestTimeZoneUtils.toOffsetDateTime(lastMessageAt));

            // channelRepository.findById(...)가 기존 공개 채널을 찾는 상황을 만든다.
            given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));

            // updateInfo(command)가 적용된 뒤 저장된 채널이 그대로 반환되는 상황을 만든다.
            given(channelRepository.save(publicChannel)).willReturn(publicChannel);

            // 최신 메시지가 존재하므로 Optional.of(message)를 반환하게 한다.
            given(messageReader.getLatestMessageByChannelId(channelId)).willReturn(Optional.of(message));

            // 수정된 채널의 참여자/읽음 상태 정보 조회 결과를 준비한다.
            given(readStatusService.findAllByChannelId(channelId)).willReturn(readStatuses);

            // 서비스는 저장된 채널, 최신 메시지 시간, ReadStatus 목록을 mapper에 넘겨 DTO로 변환한다.
            // any(Instant.class) 대신 lastMessageAt을 정확히 지정하면
            // 최신 메시지 시간이 mapper까지 제대로 전달됐는지 더 분명하게 검증할 수 있다.
            given(channelMapper.toDto(publicChannel, lastMessageAt, readStatuses)).willReturn(publicChannelDto);

            // when
            // 채널 수정 로직을 실행한다.
            ChannelDto result = channelService.update(channelId, updateCommand);

            // then
            // 서비스는 mapper가 만들어준 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(publicChannelDto);

            // updateInfo(command)가 실제 엔티티에 반영됐는지 확인한다.
            // 반환 DTO만 검증하면 채널 이름/설명이 정말 수정됐는지 놓칠 수 있다.
            assertThat(publicChannel.getName()).isEqualTo(updateCommand.channelName());
            assertThat(publicChannel.getDescription()).isEqualTo(updateCommand.channelDescription());

            // 주요 협력 객체들이 서비스 구현 순서대로 호출됐는지 확인한다.
            // 공개 채널 수정 성공 흐름은 다음 순서를 갖는다.
            // 1. 기존 채널 조회
            // 2. 수정된 채널 저장
            // 3. 최신 메시지 조회
            // 4. ReadStatus 목록 조회
            // 5. ChannelDto 변환
            InOrder inOrder = inOrder(channelRepository, messageReader, readStatusService, channelMapper);
            inOrder.verify(channelRepository).findById(channelId);
            inOrder.verify(channelRepository).save(publicChannel);
            inOrder.verify(messageReader).getLatestMessageByChannelId(channelId);
            inOrder.verify(readStatusService).findAllByChannelId(channelId);
            inOrder.verify(channelMapper).toDto(publicChannel, lastMessageAt, readStatuses);

            // update()는 사용자 존재 여부를 확인하지 않고, 메시지를 삭제/수정하지도 않는다.
            // 따라서 userReader와 messageService는 이 흐름에서 호출되면 안 된다.
            verifyNoInteractions(userReader, messageService);

            // 위에서 검증한 호출 외에 추가적인 repository/reader/service/mapper 호출이 없는지 확인한다.
            verifyNoMoreInteractions(channelRepository, messageReader, readStatusService, channelMapper);
        }

        @Test
        @DisplayName("공개 채널 수정 성공 - 마지막 메시지가 없음")
        void update_returnsChannelDtoWithNullLastMessageAt_whenPublicChannelExistsAndLatestMessageDoesNotExist() {
            // given
            // 수정할 채널 id, 수정 요청 command, 기존 공개 채널 엔티티를 준비한다.
            // 이 테스트는 "마지막 메시지가 없는 공개 채널"을 수정하는 성공 케이스다.
            UUID channelId = UUID.randomUUID();
            ChannelUpdateCommand updateCommand = createUpdateCommand();
            Channel publicChannel = createPublicChannel(channelId);

            // update() 서비스 로직은 ReadStatus 내부 필드에 접근하지 않는다.
            // 조회된 ReadStatus 목록을 mapper에 그대로 넘기는지만 검증하면 되므로 단순 mock으로 충분하다.
            ReadStatus readStatus = mock(ReadStatus.class);
            List<ReadStatus> readStatuses = List.of(readStatus);

            // 마지막 메시지가 없으면 mapper에 전달되는 lastMessageAt은 null이어야 한다.
            // RequestTimeZoneUtils.toOffsetDateTime(null)도 null을 반환하지만,
            // 이 케이스에서는 null을 직접 넘기는 편이 "마지막 메시지 없음"이라는 의도를 더 명확히 보여준다.
            ChannelDto publicChannelDto = createPublicChannelDto(channelId, updateCommand, null);

            // channelRepository.findById(...)가 기존 공개 채널을 찾는 상황을 만든다.
            given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));

            // updateInfo(command)가 적용된 뒤 저장된 채널이 그대로 반환되는 상황을 만든다.
            given(channelRepository.save(publicChannel)).willReturn(publicChannel);

            // "마지막 메시지가 없음"은 최신 메시지 조회 결과가 Optional.empty()라는 뜻이다.
            // Message mock을 만들고 getCreatedAt()을 null로 두면
            // "메시지는 있는데 생성 시간이 null"인 다른 상황이 되므로 테스트 이름과 어긋난다.
            given(messageReader.getLatestMessageByChannelId(channelId)).willReturn(Optional.empty());

            // 수정된 채널의 참여자/읽음 상태 정보 조회 결과를 준비한다.
            given(readStatusService.findAllByChannelId(channelId)).willReturn(readStatuses);

            // 서비스는 저장된 채널, null인 마지막 메시지 시간, ReadStatus 목록을 mapper에 넘겨 DTO로 변환한다.
            given(channelMapper.toDto(publicChannel, null, readStatuses)).willReturn(publicChannelDto);

            // when
            // 채널 수정 로직을 실행한다.
            ChannelDto result = channelService.update(channelId, updateCommand);

            // then
            // 서비스는 mapper가 만들어준 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(publicChannelDto);

            // mapper가 반환한 DTO에도 수정 요청 값과 null lastMessageAt이 반영되어 있는지 확인한다.
            assertThat(result.name()).isEqualTo(updateCommand.channelName());
            assertThat(result.description()).isEqualTo(updateCommand.channelDescription());
            assertThat(result.lastMessageAt()).isNull();

            // updateInfo(command)가 실제 엔티티에 반영됐는지 확인한다.
            // 반환 DTO만 검증하면 채널 엔티티 자체가 수정됐는지 놓칠 수 있다.
            assertThat(publicChannel.getName()).isEqualTo(updateCommand.channelName());
            assertThat(publicChannel.getDescription()).isEqualTo(updateCommand.channelDescription());

            // 주요 협력 객체들이 서비스 구현 순서대로 호출됐는지 확인한다.
            // 마지막 메시지가 없어도 최신 메시지 조회 자체는 수행되고,
            // 조회 결과가 비어 있기 때문에 mapper에는 null lastMessageAt이 전달된다.
            InOrder inOrder = inOrder(channelRepository, messageReader, readStatusService, channelMapper);
            inOrder.verify(channelRepository).findById(channelId);
            inOrder.verify(channelRepository).save(publicChannel);
            inOrder.verify(messageReader).getLatestMessageByChannelId(channelId);
            inOrder.verify(readStatusService).findAllByChannelId(channelId);
            inOrder.verify(channelMapper).toDto(publicChannel, null, readStatuses);

            // update()는 사용자 존재 여부를 확인하지 않고, 메시지를 삭제/수정하지도 않는다.
            // 따라서 userReader와 messageService는 이 흐름에서 호출되면 안 된다.
            verifyNoInteractions(userReader, messageService);

            // 위에서 검증한 호출 외에 추가적인 repository/reader/service/mapper 호출이 없는지 확인한다.
            verifyNoMoreInteractions(channelRepository, messageReader, readStatusService, channelMapper);
        }

        @Test
        @DisplayName("채널 수정 실패 - 존재하지 않는 채널")
        void update_throwsChannelNotFoundException_whenChannelDoesNotExist() {
            // given
            // 수정할 channelId와 수정 요청 command를 준비한다.
            // 이 테스트는 channelId에 해당하는 채널이 repository에 없는 실패 케이스다.
            UUID channelId = UUID.randomUUID();
            ChannelUpdateCommand updateCommand = createUpdateCommand();

            // BasicChannelService.update(...)는 가장 먼저 channelRepository.findById(...)로 기존 채널을 조회한다.
            // Optional.empty()를 반환하게 해서 "존재하지 않는 채널" 상황을 만든다.
            given(channelRepository.findById(channelId)).willReturn(Optional.empty());

            // when & then
            // 기존 채널을 찾지 못하면 getChannelRequireThrow(...)에서 ChannelNotFoundException이 발생해야 한다.
            // 이 예외가 발생하면 updateInfo(command), save(...), 최신 메시지 조회, ReadStatus 조회, DTO 변환은 진행되지 않는다.
            assertThatThrownBy(() -> channelService.update(channelId, updateCommand))
                    .isInstanceOf(ChannelNotFoundException.class);

            // 채널 수정은 기존 채널 조회에서 시작하므로 findById(channelId)가 호출됐는지 확인한다.
            verify(channelRepository).findById(channelId);

            // repository에 대해서는 findById(...) 한 번만 호출되어야 한다.
            // 채널이 없으므로 channelRepository.save(...) 같은 저장 작업이 호출되면 안 된다.
            verifyNoMoreInteractions(channelRepository);

            // 채널 조회 단계에서 예외가 발생했으므로 이후 수정 성공 흐름의 협력 객체들은 전혀 호출되면 안 된다.
            // - messageReader: 마지막 메시지 조회에 사용됨
            // - readStatusService: 수정된 채널의 ReadStatus 목록 조회에 사용됨
            // - channelMapper: 저장된 채널을 ChannelDto로 변환하는 데 사용됨
            verifyNoInteractions(messageReader, readStatusService, channelMapper);

            // update()는 사용자 존재 여부를 확인하지 않고, 메시지를 삭제/수정하지도 않는다.
            // 실패 흐름에서도 userReader와 messageService는 호출되면 안 된다.
            verifyNoInteractions(userReader, messageService);
        }

        @Test
        @DisplayName("채널 수정 실패 - 비공개 채널은 수정할 수 없음")
        void update_throwsPrivateChannelUpdateNotAllowedException_whenChannelIsPrivate() {
            // given
            // 수정할 channelId와 수정 요청 command를 준비한다.
            // 이 테스트는 채널은 존재하지만 타입이 PRIVATE이라 수정할 수 없는 실패 케이스다.
            UUID channelId = UUID.randomUUID();
            ChannelUpdateCommand updateCommand = createUpdateCommand();

            // 비공개 채널 엔티티를 만든다.
            // BasicChannelService.update(...)는 채널 조회 후 channel.isPrivate()를 확인하고,
            // true이면 PrivateChannelUpdateNotAllowedException을 던진다.
            Channel privateChannel = createPrivateChannel(
                    channelId,
                    new ChannelCreatePrivateCommand(
                            List.of(UUID.randomUUID(), UUID.randomUUID()),
                            ChannelType.PRIVATE
                    )
            );

            // channelRepository.findById(...)가 존재하는 비공개 채널을 반환하는 상황을 만든다.
            given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

            // when & then
            // 비공개 채널은 이름/설명을 수정할 수 없으므로 PrivateChannelUpdateNotAllowedException이 발생해야 한다.
            // 이 예외는 updateInfo(command)가 호출되기 전에 발생해야 한다.
            assertThatThrownBy(() -> channelService.update(channelId, updateCommand))
                    .isInstanceOf(PrivateChannelUpdateNotAllowedException.class);

            // 기존 채널 조회까지는 수행되어야 한다.
            verify(channelRepository).findById(channelId);

            // 비공개 채널 예외가 발생했으므로 저장은 실행되면 안 된다.
            // repository에 대해서는 findById(...) 외 추가 호출이 없어야 하며,
            // 특히 channelRepository.save(...)가 호출되지 않았음을 함께 보장한다.
            verifyNoMoreInteractions(channelRepository);

            // updateInfo(command)가 실행되지 않았으므로 비공개 채널의 이름/설명은 기존 값 그대로여야 한다.
            // ChannelCreatePrivateCommand는 이름과 설명을 빈 문자열로 만들기 때문에 empty로 확인한다.
            assertThat(privateChannel.getName()).isEmpty();
            assertThat(privateChannel.getDescription()).isEmpty();

            // 채널 타입 확인 단계에서 예외가 발생했으므로 이후 수정 성공 흐름의 협력 객체들은 호출되면 안 된다.
            // - messageReader: 마지막 메시지 조회에 사용됨
            // - readStatusService: 수정된 채널의 ReadStatus 목록 조회에 사용됨
            // - channelMapper: 저장된 채널을 ChannelDto로 변환하는 데 사용됨
            verifyNoInteractions(messageReader, readStatusService, channelMapper);

            // update()는 사용자 존재 여부를 확인하지 않고, 메시지를 삭제/수정하지도 않는다.
            // 비공개 채널 실패 흐름에서도 userReader와 messageService는 호출되면 안 된다.
            verifyNoInteractions(userReader, messageService);
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class DeleteChannelTest {

        @Test
        @DisplayName("채널 삭제 성공")
        void delete_deletesChannelAndRelatedData_whenChannelExists() {
            // given
            // 삭제할 channelId를 준비한다.
            // 이 테스트는 삭제 대상 채널이 존재하는 정상 삭제 케이스다.
            UUID channelId = UUID.randomUUID();

            // BasicChannelService.delete(...)는 가장 먼저 existsById(channelId)로 채널 존재 여부를 확인한다.
            // true를 반환하게 해서 삭제가 가능한 상황을 만든다.
            given(channelRepository.existsById(channelId)).willReturn(true);

            // when
            // 채널 삭제 로직을 실행한다.
            channelService.delete(channelId);

            // then
            // 삭제 성공 흐름은 순서가 중요하다.
            // 1. 채널 존재 여부 확인
            // 2. 채널에 연결된 ReadStatus 삭제
            // 3. 채널에 연결된 메시지 삭제
            // 4. 마지막으로 채널 삭제
            //
            // 관련 데이터를 먼저 삭제한 뒤 채널을 삭제해야 외래키 제약이나 고아 데이터 문제를 피할 수 있다.
            // InOrder에는 같은 mock을 중복해서 넣을 필요가 없으므로 channelRepository는 한 번만 넣는다.
            InOrder inOrder = inOrder(channelRepository, readStatusService, messageService);
            inOrder.verify(channelRepository).existsById(channelId);
            inOrder.verify(readStatusService).deleteByChannelId(channelId);
            inOrder.verify(messageService).deleteAllByChannelId(channelId);
            inOrder.verify(channelRepository).deleteById(channelId);

            // 위에서 검증한 삭제 흐름 외에 추가적인 삭제 관련 호출이 없는지 확인한다.
            verifyNoMoreInteractions(channelRepository, readStatusService, messageService);

            // delete()는 사용자 조회, 최신 메시지 조회, DTO 변환을 하지 않는다.
            // 따라서 아래 조회/변환용 협력 객체들은 이 흐름에서 호출되면 안 된다.
            verifyNoInteractions(userReader, messageReader, channelMapper);
        }

        @Test
        @DisplayName("채널 삭제 실패 - 존재하지 않는 채널")
        void delete_throwsChannelNotFoundException_whenChannelDoesNotExist() {
            // given
            // 삭제할 channelId를 준비한다.
            // 이 테스트는 channelId에 해당하는 채널이 존재하지 않는 실패 케이스다.
            UUID channelId = UUID.randomUUID();

            // BasicChannelService.delete(...)는 가장 먼저 existsById(channelId)로 채널 존재 여부를 확인한다.
            // false를 반환하게 해서 "삭제 대상 채널이 없음" 상황을 만든다.
            given(channelRepository.existsById(channelId)).willReturn(false);

            // when & then
            // 삭제 대상 채널이 없으면 ChannelNotFoundException이 발생해야 한다.
            // 이 예외가 발생하면 ReadStatus 삭제, 메시지 삭제, 채널 삭제는 진행되지 않는다.
            assertThatThrownBy(() -> channelService.delete(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            // 삭제 로직은 채널 존재 여부 확인에서 시작하므로 existsById(channelId)가 호출됐는지 확인한다.
            verify(channelRepository).existsById(channelId);

            // repository에 대해서는 existsById(...) 한 번만 호출되어야 한다.
            // 채널이 없으므로 channelRepository.deleteById(channelId)가 호출되면 안 된다.
            verifyNoMoreInteractions(channelRepository);

            // 채널이 존재하지 않으면 관련 데이터 삭제도 진행되면 안 된다.
            // - readStatusService.deleteByChannelId(...): 채널의 읽음 상태 삭제
            // - messageService.deleteAllByChannelId(...): 채널의 메시지 삭제
            verifyNoInteractions(readStatusService, messageService);

            // delete()는 사용자 조회, 최신 메시지 조회, DTO 변환을 하지 않는다.
            // 실패 흐름에서도 아래 협력 객체들은 호출되면 안 된다.
            verifyNoInteractions(userReader, messageReader, channelMapper);
        }
    }

    private ChannelCreatePublicCommand createPublicCreateCommand() {
        return new ChannelCreatePublicCommand("공지", "전체 공지 채널", ChannelType.PUBLIC);
    }

    private ChannelUpdateCommand createUpdateCommand() {
        return new ChannelUpdateCommand("수정된 공지", "수정된 설명");
    }

    private Channel createPublicChannel(UUID channelId) {
        Channel channel = new Channel(createPublicCreateCommand());
        setId(channel, channelId);
        return channel;
    }

    private Channel createPrivateChannel(UUID channelId, ChannelCreatePrivateCommand command) {
        Channel channel = new Channel(command);
        setId(channel, channelId);
        return channel;
    }

    private ChannelSummary createChannelSummary(UUID channelId, ChannelType type) {
        return new ChannelSummary(channelId, type, type.name().toLowerCase(), type.name().toLowerCase() + " channel", Instant.now());
    }

    private ChannelDto createPublicChannelDto(UUID channelId, ChannelCreatePublicCommand command) {
        return new ChannelDto(channelId, ChannelType.PUBLIC, command.channelName(), command.channelDescription(), List.of(), null);
    }

    private ChannelDto createPublicChannelDto(UUID channelId, ChannelUpdateCommand command, OffsetDateTime lastMessageAt) {
        return new ChannelDto(channelId, ChannelType.PUBLIC, command.channelName(), command.channelDescription(), List.of(), lastMessageAt);
    }

    private ChannelDto createPrivateChannelDto(UUID channelId) {
        return new ChannelDto(channelId, ChannelType.PRIVATE, "", "", List.of(), null);
    }

    private ChannelDto createChannelDto(ChannelSummary channelSummary) {
        return new ChannelDto(
                channelSummary.id(),
                channelSummary.type(),
                channelSummary.name(),
                channelSummary.description(),
                List.of(),
                null
        );
    }

    private ReadStatus createReadStatus(UUID channelId) {
        ReadStatus readStatus = mock(ReadStatus.class);
        given(readStatus.getChannelId()).willReturn(channelId);
        return readStatus;
    }

    private void setId(Object target, UUID id) {
        ReflectionTestUtils.setField(target, "id", id);
    }
}
