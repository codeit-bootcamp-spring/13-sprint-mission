package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;

  @Override
  public ChannelDto createPublic(String name, String description) {
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    channelRepository.save(channel);
    return find(channel.getId());
  }

  @Override
  public ChannelDto createPrivate(List<UUID> participantIds) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);
    participantIds.forEach(userId ->
        readStatusRepository.save(new ReadStatus(userId, channel.getId(), Instant.now()))
    );
    return find(channel.getId());
  }

  @Override
  public ChannelDto find(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    Instant lastMessageAt = messageRepository.findAll().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .map(Message::getCreatedAt)
        .max(Instant::compareTo)
        .orElse(null);
    List<UUID> participantIds = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      participantIds = readStatusRepository.findAllByChannelId(channelId).stream()
          .map(ReadStatus::getUserId)
          .toList();
    }
    return new ChannelDto(channel.getId(), channel.getType(), channel.getChannelName(),
        channel.getDescription(), participantIds, lastMessageAt);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannelId)
        .toList();
    return channelRepository.findAll().stream()
        .filter(c -> c.getType() == ChannelType.PUBLIC || myChannelIds.contains(c.getId()))
        .map(c -> find(c.getId()))
        .toList();
  }

  @Override
  public ChannelDto update(UUID channelId, String newName, String newDescription) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
    }
    channel.update(newName, newDescription);
    channelRepository.save(channel);
    return find(channelId);
  }

  @Override
  public void delete(UUID channelId) {
    if (!channelRepository.existsById(channelId)) {
      throw new NoSuchElementException("Channel with id " + channelId + " not found");
    }
    messageRepository.findAll().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .forEach(m -> messageRepository.deleteById(m.getId()));
    readStatusRepository.findAllByChannelId(channelId)
        .forEach(r -> readStatusRepository.deleteById(r.getId()));
    channelRepository.deleteById(channelId);
  }
}