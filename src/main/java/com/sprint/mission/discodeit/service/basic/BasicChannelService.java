package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.aspect.LogAction;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.ChannelSummary;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private final UserReader userReader;
    private final MessageReader messageReader;

    @LogAction(value = "채널 생성")
    @PreAuthorize("hasRole('CHANNEL_MANAGER') or #command.isPrivate()")
    @Override
    public ChannelDto save(ChannelCreateCommand command) {

        Channel channel = new Channel(command);
        Channel savedChannel = channelRepository.save(channel);

        if (command.isPrivate() && command instanceof ChannelCreatePrivateCommand privateCommand) {
            readStatusService.saveAll(savedChannel, privateCommand.participantIds(), Instant.now());

            return channelMapper.toDto(
                    savedChannel,
                    readStatusService.findAllByChannelId(savedChannel.getId())
            );
        }

        return channelMapper.toDto(savedChannel);
    }

    @Transactional(readOnly = true)
    @Override
    public ChannelDto findById(UUID channelId) {

        ChannelSummary channelSummary = channelRepository.findByDetail(channelId)
                .orElseThrow(()-> new ChannelNotFoundException(channelId));

        return channelMapper.toDto(channelSummary, readStatusService.findAllByChannelId(channelId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        if (!userReader.isUserExist(userId)) throw new UserNotFoundException(userId);

        List<ChannelSummary> channelSummaries = channelRepository.findVisibleChannels(userId, ChannelType.PUBLIC);

        List<UUID> channelIds = channelSummaries
                .stream()
                .map(ChannelSummary::id)
                .toList();

        Map<UUID, List<ReadStatus>> channelIdToReadStatusMap = retrieveReadStatusMapByChannelIds(channelIds);

        return channelSummaries
                .stream()
                .map(cs -> channelMapper.toDto(
                                cs,
                                channelIdToReadStatusMap.getOrDefault(cs.id(), List.of())
                        )
                )
                .toList();
    }

    @LogAction(value = "채널 수정")
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Override
    public ChannelDto update(UUID channelId, ChannelUpdateCommand command) {
        Channel channel = getChannelRequireThrow(channelId);

        if (channel.isPrivate()) throw new PrivateChannelUpdateNotAllowedException(channelId);

        channel.updateInfo(command);

        return channelMapper.toDto(
                channelRepository.save(channel),
                getLastMessageAt(channelId),
                readStatusService.findAllByChannelId(channelId)
        );
    }

    @LogAction(value = "채널 삭제", idName = "channelId", idParamIndex = 0)
    @PreAuthorize("hasRole('CHANNEL_MANAGER') or @channelGuard.isChannelPrivate(#channelId)")
    @Override
    public void delete(UUID channelId) {

        if (!channelRepository.existsById(channelId)) throw new ChannelNotFoundException(channelId);

        readStatusService.deleteByChannelId(channelId);

        messageService.deleteAllByChannelId(channelId);

        channelRepository.deleteById(channelId);

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

    private @NonNull Map<UUID, List<ReadStatus>> retrieveReadStatusMapByChannelIds(List<UUID> channelIds) {
        return readStatusService.findAllByChannelIds(channelIds)
                .stream()
                .collect(Collectors.groupingBy(ReadStatus::getChannelId));
    }

}
