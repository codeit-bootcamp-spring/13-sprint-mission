package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;
    private final SseService sseService;

    @Override
    @CacheEvict(
            cacheNames = "channels",
            allEntries = true
    )
    public ChannelResponse createPrivateChannel(
            ChannelPrivateRequest dto
    ) {
        int participantCount = dto.channelIds() == null
                ? 0
                : dto.channelIds().size();

        log.debug(
                "PRIVATE 채널 생성 시작: participantCount={}",
                participantCount
        );

        Channel channel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        Channel savedChannel =
                channelRepository.save(channel);

        if (dto.channelIds() != null) {
            for (UUID userId : dto.channelIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        userId
                                )
                        );

                ReadStatus readStatus =
                        new ReadStatus(savedChannel, user);

                readStatusRepository.save(readStatus);
            }
        }

        ChannelResponse response =
                channelMapper.toDto(savedChannel);

        sendChannelEvent(
                "channels.created",
                response
        );

        log.info(
                "PRIVATE 채널 생성 완료: channelId={}, participantCount={}",
                savedChannel.getId(),
                participantCount
        );

        return response;
    }

    @Override
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @CacheEvict(
            cacheNames = "channels",
            allEntries = true
    )
    public ChannelResponse createPublicChannel(
            ChannelPublicRequest dto
    ) {
        log.debug(
                "PUBLIC 채널 생성 시작: name={}",
                dto.name()
        );

        Channel channel = new Channel(
                dto.name(),
                dto.description(),
                ChannelType.PUBLIC
        );

        Channel savedChannel =
                channelRepository.save(channel);

        ChannelResponse response =
                channelMapper.toDto(savedChannel);

        sendChannelEvent(
                "channels.created",
                response
        );

        log.info(
                "PUBLIC 채널 생성 완료: channelId={}",
                savedChannel.getId()
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelResponse> findById(UUID id) {
        return channelRepository.findById(id)
                .map(channelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "channels",
            key = "#userId"
    )
    public List<ChannelResponse> findAll(UUID userId) {
        return findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "channels",
            key = "#userId"
    )
    public List<ChannelResponse> findAllByUserId(
            UUID userId
    ) {
        return channelRepository.findAll()
                .stream()
                .filter(
                        channel ->
                                channel.getType()
                                        == ChannelType.PUBLIC
                                        || readStatusRepository
                                        .existsByChannelIdAndUserId(
                                                channel.getId(),
                                                userId
                                        )
                )
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @CacheEvict(
            cacheNames = "channels",
            allEntries = true
    )
    public ChannelResponse update(
            UUID id,
            ChannelPublicRequest dto
    ) {
        log.debug(
                "채널 수정 시작: channelId={}",
                id
        );

        Channel channel = channelRepository.findById(id)
                .orElseThrow(
                        () -> new ChannelNotFoundException(id)
                );

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new PrivateChannelUpdateException(id);
        }

        channel.updateTitles(
                dto.name(),
                dto.description()
        );

        ChannelResponse response =
                channelMapper.toDto(channel);

        sendChannelEvent(
                "channels.updated",
                response
        );

        log.info(
                "채널 수정 완료: channelId={}",
                id
        );

        return response;
    }

    @Override
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @CacheEvict(
            cacheNames = "channels",
            allEntries = true
    )
    public void delete(UUID id) {
        log.debug(
                "채널 삭제 시작: channelId={}",
                id
        );

        Channel channel = channelRepository.findById(id)
                .orElseThrow(
                        () -> new ChannelNotFoundException(id)
                );

        ChannelResponse response =
                channelMapper.toDto(channel);

        messageRepository.deleteByChannelId(id);
        readStatusRepository.deleteByChannelId(id);
        channelRepository.delete(channel);

        sendChannelEvent(
                "channels.deleted",
                response
        );

        log.info(
                "채널 삭제 완료: channelId={}",
                id
        );
    }

    private void sendChannelEvent(
            String eventName,
            ChannelResponse channel
    ) {
        if (channel.type() == ChannelType.PUBLIC) {
            sseService.broadcast(
                    eventName,
                    channel
            );

            return;
        }

        List<UUID> receiverIds =
                channel.participants()
                        .stream()
                        .map(UserResponse::id)
                        .toList();

        if (!receiverIds.isEmpty()) {
            sseService.send(
                    receiverIds,
                    eventName,
                    channel
            );
        }
    }
}