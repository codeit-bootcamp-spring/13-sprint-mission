package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.ChannelSummary;
import com.sprint.mission.discodeit.dto.repository.ChannelParticipantIds;
import com.sprint.mission.discodeit.service.basic.ChannelReader;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.events.ChannelCacheClearEvent;
import com.sprint.mission.discodeit.events.ChannelEvent;
import com.sprint.mission.discodeit.events.ChannelEventType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 채널 서비스의 분기와 생성·수정·삭제 이벤트에 담기는 DTO를 검증한다.
 * Repository·Reader·Mapper는 Mock이므로 실제 저장과 필드 매핑을 검증했다고 해석하지 않는다.
 * 삭제 테스트의 순서 검증은 참여자 스냅샷을 확보하고 자식 관계를 먼저 정리하는 계약에 사용한다.
 */
@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ChannelReader channelReader;


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
            assertThat(result).isSameAs(publicChannelDto);

            ArgumentCaptor<ChannelEvent> eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(ChannelEventType.CREATED);
            assertThat(eventCaptor.getValue().channel()).isSameAs(result);

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
            List<UserDto> participants = List.of(
                    new UserDto(participantIds.get(0), "first", "first@test.local",
                            null, false, Role.USER, null, null),
                    new UserDto(participantIds.get(1), "second", "second@test.local",
                            null, false, Role.USER, null, null)
            );
            ChannelDto privateChannelDto = createPrivateChannelDto(channelId, participants);

            // ReadStatus는 이 테스트에서 내부 상태를 검증하지 않고 mapper로 전달만 한다.
            // 따라서 실제 엔티티를 어렵게 구성하지 않고 mock 객체로 충분하다.
            List<ReadStatus> readStatuses = List.of(mock(ReadStatus.class), mock(ReadStatus.class));

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
            assertThat(result).isSameAs(privateChannelDto);

            ArgumentCaptor<ChannelEvent> eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(ChannelEventType.CREATED);
            assertThat(eventCaptor.getValue().channel()).isSameAs(result);

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
        void findById_combinesSummaryAndCurrentParticipants() {
            UUID id = UUID.randomUUID();
            ChannelSummary summary = createChannelSummary(id, ChannelType.PUBLIC);
            ChannelDto expected = createChannelDto(summary);
            UserDto participant = new UserDto(UUID.randomUUID(), "member", "member@test.local",
                    null, false, Role.USER, null, null);
            given(channelReader.findChannelSummary(id)).willReturn(Optional.of(summary));
            given(readStatusService.findParticipantIdsByChannelIds(List.of(id)))
                    .willReturn(List.of(new ChannelParticipantIds(id, participant.id())));
            given(userReader.findUserDtosByIds(List.of(participant.id())))
                    .willReturn(Map.of(participant.id(), participant));
            given(channelMapper.toDtoWithParticipants(summary, List.of(participant))).willReturn(expected);

            assertThat(channelService.findById(id)).isEqualTo(expected);
        }

        @Test
        void findById_stopsWhenChannelDoesNotExist() {
            UUID id = UUID.randomUUID();
            given(channelReader.findChannelSummary(id)).willReturn(Optional.empty());
            assertThatThrownBy(() -> channelService.findById(id)).isInstanceOf(ChannelNotFoundException.class);
            verifyNoInteractions(readStatusService, userReader, channelMapper);
        }
    }

    @Nested
    @DisplayName("사용자별 채널 목록 조회")
    class FindAllChannelTest {
        @AfterEach
        void clearAuthentication() { SecurityContextHolder.clearContext(); }

        private void authenticate(UUID userId, Role role) {
            UserDto user = new UserDto(userId, "requester", "requester@test.local", null,
                    false, role, null, null);
            DiscodeitUserDetails principal = new DiscodeitUserDetails(user, "unused-password");
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        }

        @Test
        void findAllByUserId_groupsParticipantsPreservesOrderAndOmitsMissingData() {
            UUID userId = UUID.randomUUID();
            authenticate(userId, Role.USER);
            ChannelSummary first = createChannelSummary(UUID.randomUUID(), ChannelType.PRIVATE);
            ChannelSummary second = createChannelSummary(UUID.randomUUID(), ChannelType.PUBLIC);
            UUID missingChannel = UUID.randomUUID();
            UUID missingUser = UUID.randomUUID();
            List<UUID> ids = List.of(first.id(), missingChannel, second.id());
            UserDto participant = new UserDto(userId, "member", "member@test.local", null,
                    false, Role.USER, null, null);
            ChannelDto firstDto = createChannelDto(first);
            ChannelDto secondDto = createChannelDto(second);
            Map<UUID, ChannelSummary> summaries = new LinkedHashMap<>();
            summaries.put(second.id(), second);
            summaries.put(first.id(), first);
            given(userReader.isUserExist(userId)).willReturn(true);
            given(channelRepository.findVisibleChannelIds(userId, List.of(ChannelType.PUBLIC))).willReturn(ids);
            given(channelReader.findChannelSummariesByIds(ids)).willReturn(summaries);
            given(readStatusService.findParticipantIdsByChannelIds(ids)).willReturn(List.of(
                    new ChannelParticipantIds(first.id(), userId), new ChannelParticipantIds(second.id(), userId),
                    new ChannelParticipantIds(first.id(), missingUser)));
            given(userReader.findUserDtosByIds(List.of(userId, missingUser))).willReturn(Map.of(userId, participant));
            given(channelMapper.toDtoWithParticipants(first, List.of(participant))).willReturn(firstDto);
            given(channelMapper.toDtoWithParticipants(second, List.of(participant))).willReturn(secondDto);

            assertThat(channelService.findAllByUserId(userId)).containsExactly(firstDto, secondDto);
            verify(userReader).findUserDtosByIds(List.of(userId, missingUser));
        }

        @Test
        void findAllByUserId_returnsEmptyWithoutLoadingParticipants() {
            UUID userId = UUID.randomUUID();
            authenticate(userId, Role.USER);
            given(userReader.isUserExist(userId)).willReturn(true);
            given(channelRepository.findVisibleChannelIds(userId, List.of(ChannelType.PUBLIC))).willReturn(List.of());

            assertThat(channelService.findAllByUserId(userId)).isEmpty();
            verifyNoInteractions(readStatusService, channelMapper);
            verify(userReader, never()).findUserDtosByIds(any());
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"CHANNEL_MANAGER", "ADMIN"})
        void findAllByUserId_includesAllTypes_whenRequesterHasManagementRole(Role role) {
            UUID userId = UUID.randomUUID();
            authenticate(userId, role);
            given(userReader.isUserExist(userId)).willReturn(true);

            assertThat(channelService.findAllByUserId(userId)).isEmpty();
            verify(channelRepository).findVisibleChannelIds(userId, List.of(ChannelType.PUBLIC, ChannelType.PRIVATE));
        }

        @Test
        void findAllByUserId_stopsWhenUserDoesNotExist() {
            UUID id = UUID.randomUUID();
            given(userReader.isUserExist(id)).willReturn(false);
            assertThatThrownBy(() -> channelService.findAllByUserId(id)).isInstanceOf(UserNotFoundException.class);
            verifyNoInteractions(channelRepository, channelReader, readStatusService);
            verify(userReader, never()).findUserDtosByIds(any());
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
            assertThat(result).isSameAs(publicChannelDto);

            ArgumentCaptor<ChannelEvent> eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(ChannelEventType.UPDATED);
            assertThat(eventCaptor.getValue().channel()).isSameAs(result);
            verify(eventPublisher).publishEvent(new ChannelCacheClearEvent(channelId));

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
            assertThat(result).isSameAs(publicChannelDto);

            ArgumentCaptor<ChannelEvent> eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(ChannelEventType.UPDATED);
            assertThat(eventCaptor.getValue().channel()).isSameAs(result);
            verify(eventPublisher).publishEvent(new ChannelCacheClearEvent(channelId));

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

            verifyNoInteractions(eventPublisher);

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

            verifyNoInteractions(eventPublisher);

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

        @ParameterizedTest(name = "{0} 채널 삭제")
        @EnumSource(value = ChannelType.class, names = {"PUBLIC", "PRIVATE"})
        @DisplayName("삭제 전 채널 정보를 보존하고 삭제 이벤트를 발행한다")
        void delete_deletesChannelAndRelatedData_whenChannelExists(ChannelType channelType) {
            // given
            UUID channelId = UUID.randomUUID();
            List<UserDto> participants = List.of(
                    new UserDto(UUID.randomUUID(), "first", "first@test.local",
                            null, false, Role.USER, null, null),
                    new UserDto(UUID.randomUUID(), "second", "second@test.local",
                            null, false, Role.USER, null, null)
            );
            List<UUID> participantIds = participants.stream().map(UserDto::id).toList();
            Channel channel = channelType == ChannelType.PUBLIC
                    ? createPublicChannel(channelId)
                    : createPrivateChannel(channelId,
                            new ChannelCreatePrivateCommand(participantIds, ChannelType.PRIVATE));
            // ReadStatus는 Mapper에 전달만 하므로 Mock을 사용한다.
            List<ReadStatus> readStatuses = List.of(mock(ReadStatus.class), mock(ReadStatus.class));
            ChannelDto channelDto = new ChannelDto(
                    channelId, channelType, channel.getName(), channel.getDescription(), participants, null
            );
            given(channelReader.getChannel(channelId)).willReturn(channel);
            given(readStatusService.findAllByChannelId(channelId)).willReturn(readStatuses);
            given(channelMapper.toDto(channel, readStatuses)).willReturn(channelDto);

            // when
            channelService.delete(channelId);

            // then
            verify(channelReader).getChannel(channelId);
            verify(readStatusService).findAllByChannelId(channelId);

            ArgumentCaptor<ChannelEvent> eventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ChannelEvent deletedEvent = eventCaptor.getValue();
            assertThat(deletedEvent.type()).isEqualTo(ChannelEventType.DELETED);
            assertThat(deletedEvent.channel()).isSameAs(channelDto);
            verify(eventPublisher).publishEvent(new ChannelCacheClearEvent(channelId));

            // 참여자 정보가 사라지기 전에 DTO를 만들고, 자식 데이터와 채널을 삭제한 뒤 이벤트를 발행한다.
            InOrder inOrder = inOrder(channelMapper, readStatusService, messageService,
                    channelRepository, eventPublisher);
            inOrder.verify(channelMapper).toDto(channel, readStatuses);
            inOrder.verify(readStatusService).deleteByChannelId(channelId);
            inOrder.verify(messageService).deleteAllByChannelId(channelId);
            inOrder.verify(channelRepository).deleteById(channelId);
            inOrder.verify(eventPublisher).publishEvent(deletedEvent);

            verifyNoMoreInteractions(channelRepository, readStatusService, messageService);
            verifyNoInteractions(userReader, messageReader);
        }

        @Test
        @DisplayName("채널 삭제 실패 - 존재하지 않는 채널")
        void delete_throwsChannelNotFoundException_whenChannelDoesNotExist() {
            // given
            UUID channelId = UUID.randomUUID();
            given(channelReader.getChannel(channelId)).willThrow(new ChannelNotFoundException(channelId));

            // when & then
            assertThatThrownBy(() -> channelService.delete(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            verify(channelReader).getChannel(channelId);
            // 채널 조회에 실패하면 DTO 생성, 관련 데이터 삭제, 이벤트 발행 모두 진행하지 않는다.
            verifyNoInteractions(channelRepository, readStatusService, messageService,
                    channelMapper, eventPublisher, userReader, messageReader);
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

    private ChannelDto createPrivateChannelDto(UUID channelId, List<UserDto> participants) {
        return new ChannelDto(channelId, ChannelType.PRIVATE, "", "", participants, null);
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
