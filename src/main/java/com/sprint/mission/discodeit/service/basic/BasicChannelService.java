package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper channelMapper;
  private final UserMapper userMapper;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ChannelDto createPublic(String name, String description) {
    log.debug("Public Channel 생성 요청 - name: {}", name);
    Channel channel = new Channel(name, ChannelType.PUBLIC, description);
    channelRepository.save(channel);
    log.info("Public Channel 생성 완료 - channelId: {}", channel.getId());
    return convertToDto(channel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivate(List<UUID> participantIds) {
    log.debug("Private Channel 생성 요청 - 참여자 수: {}", participantIds.size());
    Channel channel = new Channel(null, ChannelType.PRIVATE, null);
    channelRepository.save(channel);

    for (UUID userId : participantIds) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn("Private Channel 참여자 추가 실패 - 존재하지 않는 userId: {}", userId);
            return new UserNotFoundException(Map.of("userId", userId));
          });
      ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
      readStatusRepository.save(readStatus);
    }
    log.info("Private Channel 생성 완료 - channelId: {}", channel.getId());
    return convertToDto(channel);
  }

  @Override
  public ChannelDto find(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(Map.of("channelId", id)));
    return convertToDto(channel);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findByUserId(userId).stream()
        .map(ReadStatus::getChannel)
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ChannelDto update(UUID id, String name, String description) {
    log.debug("Channel 수정 요청 - channelId: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Channel 수정 실패 - 존재하지 않는 channelId: {}", id);
          return new ChannelNotFoundException(Map.of("channelId", id));
        });

    if (channel.getType() == ChannelType.PRIVATE) {
      log.warn("Channel 수정 실패 - Private 채널 수정 시도 - channelId: {}", id);
      throw new PrivateChannelUpdateException(Map.of("channelId", id));
    }

    channel.update(name, channel.getType(), description);
    log.info("Channel 수정 완료 - channelId: {}", id);
    return convertToDto(channel);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public void delete(UUID id) {
    log.debug("Channel 삭제 요청 - channelId: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Channel 삭제 실패 - 존재하지 않는 channelId: {}", id);
          return new ChannelNotFoundException(Map.of("channelId", id));
        });

    messageRepository.deleteByChannelId(id);
    readStatusRepository.deleteByChannelId(id);
    channelRepository.delete(channel);
    log.info("Channel 삭제 완료 - channelId: {}", id);
  }

  private ChannelDto convertToDto(Channel channel) {
    Slice<Message> latestMessageSlice = messageRepository.findByChannelIdOrderByCreatedAtDesc(
        channel.getId(), PageRequest.of(0, 1)
    );
    Instant lastMessageAt = latestMessageSlice.hasContent() ?
        latestMessageSlice.getContent().get(0).getCreatedAt() : null;

    List<UserDto> participants = readStatusRepository.findByChannelId(channel.getId()).stream()
        .map(ReadStatus::getUser)
        .map(userMapper::toDto)
        .toList();

    return channelMapper.toDto(channel, lastMessageAt, participants);
  }
}