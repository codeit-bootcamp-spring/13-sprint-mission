package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelListResponse;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.Collections;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;

  public BasicChannelService(ChannelRepository channelRepository,
      ReadStatusRepository readStatusRepository,
      MessageRepository messageRepository) {
    this.channelRepository = channelRepository;
    this.readStatusRepository = readStatusRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(request.name(), request.description(),
        Channel.ChannelType.PUBLIC);
    channelRepository.save(channel);

    return convertToResponse(channel);
  }

  @Override
  public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(null, null, Channel.ChannelType.PRIVATE);
    channelRepository.save(channel);

    for (UUID userId : request.participantIds()) {
      ReadStatus readStatus = ReadStatus.builder()
          .id(UUID.randomUUID())
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .userId(userId)
          .channelId(channel.getId())
          .readAt(Instant.now())
          .build();

      readStatusRepository.save(readStatus);
    }

    return convertToResponse(channel);
  }


  @Override
  public List<ChannelListResponse> findAllByUserId(UUID userId) {
    List<UUID> joinedChannelIds = readStatusRepository.findAll().stream()
        .filter(rs -> rs.getUserId() != null && rs.getUserId().equals(userId))
        .map(ReadStatus::getChannelId)
        .toList();

    if (joinedChannelIds.isEmpty()) {
      return Collections.emptyList();
    }

    return channelRepository.findAll().stream()
        .filter(channel -> joinedChannelIds.contains(channel.getId()))
        .map(channel -> {

          Instant lastMessageAt = messageRepository.findAll().stream()
              .filter(m -> m.getChannelId() != null && m.getChannelId().equals(channel.getId()))
              .map(Message::getCreatedAt)
              .max(Instant::compareTo)
              .orElse(null);

          List<UUID> participantIds = readStatusRepository.findAll().stream()
              .filter(rs -> rs.getChannelId() != null && rs.getChannelId().equals(channel.getId()))
              .map(ReadStatus::getUserId)
              .toList();

          return new ChannelListResponse(
              channel.getId(),
              channel.getType(),
              channel.getName(),
              channel.getDescription(),
              participantIds,
              lastMessageAt
          );
        })
        .toList();
  }

  @Override
  public ChannelResponse update(UUID id, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      throw new PrivateChannelUnmodifiableException();
    }
    channel.update(request.newName(), request.newDescription());
    channelRepository.save(channel);

    return convertToResponse(channel);
  }

  @Override
  public void delete(UUID id) {
    if (channelRepository.findById(id).isEmpty()) {
      throw new ChannelNotFoundException(id);
    }

    messageRepository.deleteByChannelId(id);
    readStatusRepository.deleteByChannelId(id);

    channelRepository.delete(id);
  }

  private ChannelResponse convertToResponse(Channel channel) {

    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getType(),
        channel.getName(),
        channel.getDescription());
  }
}
