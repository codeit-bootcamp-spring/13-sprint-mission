package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.aspect.LogAction;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.ChannelSummary;
import com.sprint.mission.discodeit.dto.repository.ChannelParticipantIds;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.events.*;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusService readStatusService;
    private final MessageService messageService;
    private final ChannelMapper channelMapper;
    private final ChannelReader channelReader;
    private final UserReader userReader;
    private final MessageReader messageReader;
    private final ApplicationEventPublisher eventPublisher;

    @LogAction(value = "채널 생성")
    @PreAuthorize("hasRole('CHANNEL_MANAGER') or @channelGuard.isUserInPrivateChannelParticipants(#command, authentication.principal.userDto.id)")
    @Override
    public ChannelDto save(ChannelCreateCommand command) {

        Channel channel = new Channel(command);
        Channel savedChannel = channelRepository.save(channel);

        // 참여자 정보가 준비된 생성 DTO를 응답과 SSE 이벤트에 함께 사용한다.
        ChannelDto channelDto = getChannelDto(command, savedChannel);
        eventPublisher.publishEvent(ChannelEvent.createEvent(channelDto));

        return channelDto;
    }

    @Transactional(readOnly = true)
    @Override
    public ChannelDto findById(UUID channelId) {

        ChannelSummary summary = channelReader.findChannelSummary(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
        return channelMapper.toDtoWithParticipants(summary,
                findParticipantsByChannelIds(List.of(channelId)).getOrDefault(channelId, List.of()));
    }

    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('CHANNEL_MANAGER') or #userId == authentication.principal.userDto.id")
    public List<ChannelDto> findAllByUserId(UUID userId) {
        if (!userReader.isUserExist(userId)) throw new UserNotFoundException(userId);

        DiscodeitUserDetails principal = (DiscodeitUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Role role = principal.getUserDto().role();
        List<ChannelType> types = List.of(ChannelType.PUBLIC);

        if (role == Role.ADMIN || role == Role.CHANNEL_MANAGER) {
            types = List.of(ChannelType.PUBLIC, ChannelType.PRIVATE);
        }

        List<UUID> channelIds = channelRepository.findVisibleChannelIds(userId, types);
        Map<UUID, ChannelSummary> summaries = channelReader.findChannelSummariesByIds(channelIds);
        Map<UUID, List<UserDto>> participants = findParticipantsByChannelIds(channelIds);

        return channelIds.stream()
                .map(summaries::get)
                .filter(Objects::nonNull)
                .map(summary -> channelMapper.toDtoWithParticipants(summary,
                        participants.getOrDefault(summary.id(), List.of())))
                .toList();
    }

    @LogAction(value = "채널 수정")
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Override
    public ChannelDto update(UUID channelId, ChannelUpdateCommand command) {
        Channel channel = getChannelRequireThrow(channelId);

        if (channel.isPrivate()) throw new PrivateChannelUpdateNotAllowedException(channelId);

        channel.updateInfo(command);

        eventPublisher.publishEvent(new ChannelCacheClearEvent(channelId));
        ChannelDto channelDto = channelMapper.toDto(
                channelRepository.save(channel),
                getLastMessageAt(channelId),
                readStatusService.findAllByChannelId(channelId)
        );

        // 변경 후 DTO를 발행하고 리스너가 커밋 뒤 공개/비공개 수신 범위를 결정한다.
        eventPublisher.publishEvent(ChannelEvent.updateEvent(channelDto));
        return channelDto;
    }

    @LogAction(value = "채널 삭제", idName = "channelId", idParamIndex = 0)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Override
    public void delete(UUID channelId) {
        // 참여 관계를 지우기 전에 DTO를 만든다. 삭제 후에도 비공개 채널의 기존 참여자에게 알려야 한다.
        ChannelDto channelDto = channelMapper.toDto( channelReader.getChannel(channelId), readStatusService.findAllByChannelId(channelId));

        readStatusService.deleteByChannelId(channelId);
        messageService.deleteAllByChannelId(channelId);
        channelRepository.deleteById(channelId);

        eventPublisher.publishEvent(new ChannelCacheClearEvent(channelId));
        eventPublisher.publishEvent(ChannelEvent.deleteEvent(channelDto));

    }

    private Channel getChannelRequireThrow(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(()-> new ChannelNotFoundException(channelId));
    }

    private @Nullable Instant getLastMessageAt(UUID channelId) {
        return messageReader.getLatestMessageByChannelId(channelId)
                .map(Message::getCreatedAt)
                .orElse(null);
    }

    private Map<UUID, List<UserDto>> findParticipantsByChannelIds(List<UUID> channelIds) {
        if (channelIds.isEmpty()) return Map.of();

        List<ChannelParticipantIds> memberships = readStatusService.findParticipantIdsByChannelIds(channelIds);
        List<UUID> userIds = memberships.stream().map(ChannelParticipantIds::userId).distinct().toList();
        Map<UUID, UserDto> users = userReader.findUserDtosByIds(userIds);
        return memberships.stream()
                .filter(membership -> users.containsKey(membership.userId()))
                .collect(Collectors.groupingBy(ChannelParticipantIds::channelId,
                        Collectors.mapping(membership -> users.get(membership.userId()), Collectors.toList())));
    }

    private ChannelDto getChannelDto(ChannelCreateCommand command, Channel savedChannel) {
        if (command.isPrivate() && command instanceof ChannelCreatePrivateCommand privateCommand) {
            readStatusService.saveAll(savedChannel, privateCommand.participantIds(), Instant.now());

            return channelMapper.toDto(
                    savedChannel,
                    readStatusService.findAllByChannelId(savedChannel.getId())
            );
        }

        return channelMapper.toDto(savedChannel);
    }

}
