package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public ChannelResponse createPrivateChannel(ChannelPrivateRequest dto) {
        int participantCount = dto.channelIds() == null ? 0 : dto.channelIds().size();
        log.debug("PRIVATE 채널 생성 시작: participantCount={}", participantCount);
        Channel channel = new Channel(null, null, ChannelType.PRIVATE);

        Channel savedChannel = channelRepository.save(channel);

        if (dto.channelIds() != null) {
            for (UUID userId : dto.channelIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));
                ReadStatus readStatus = new ReadStatus(savedChannel, user);
                readStatusRepository.save(readStatus);
            }
        }

        log.info("PRIVATE 채널 생성 완료: channelId={}, participantCount={}",
                savedChannel.getId(), participantCount);
        return channelMapper.toDto(savedChannel);
    }

    @Override
    public ChannelResponse createPublicChannel(ChannelPublicRequest dto) {
        log.debug("PUBLIC 채널 생성 시작: name={}", dto.name());
        Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
        Channel savedChannel = channelRepository.save(channel);

        log.info("PUBLIC 채널 생성 완료: channelId={}", savedChannel.getId());
        return channelMapper.toDto(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelResponse> findById(UUID id) {
        return channelRepository.findById(id)
                .map(channelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAll(UUID userId) {
        return findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == ChannelType.PUBLIC
                        || readStatusRepository.existsByChannelIdAndUserId(channel.getId(), userId))
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    public ChannelResponse update(UUID id, ChannelPublicRequest dto) {
        log.debug("채널 수정 시작: channelId={}", id);
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new PrivateChannelUpdateException(id);
        }

        channel.updateTitles(dto.name(), dto.description());

        log.info("채널 수정 완료: channelId={}", id);
        return channelMapper.toDto(channel);
    }

    @Override
    public void delete(UUID id) {
        log.debug("채널 삭제 시작: channelId={}", id);
        messageRepository.deleteByChannelId(id);
        readStatusRepository.deleteByChannelId(id);
        channelRepository.deleteById(id);
        log.info("채널 삭제 완료: channelId={}", id);
    }
}
