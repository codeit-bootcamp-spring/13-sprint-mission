package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicChannelService implements ChannelService {

  private final ChannelMapper channelMapper;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;  // private channel 생성 시 User 조회용

  @Override
  public Channel create(String name, String description) {
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    return channelRepository.save(channel);
  }

  @Override
  public Channel create(List<UUID> participantIds) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel createdChannel = channelRepository.save(channel);

    // UUID → User 객체로 로드 후 ReadStatus 생성
    List<ReadStatus> readStatuses = participantIds.stream()
            .map(userId -> {
              User user = userRepository.findById(userId)
                      .orElseThrow(
                              () -> new NoSuchElementException("User with id " + userId + " not found"));
              return new ReadStatus(user, createdChannel, Instant.MIN);
            })
            .toList();
    readStatusRepository.saveAll(readStatuses);

    return createdChannel;
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    return channelRepository.findById(channelId)
            .map(channelMapper::toDto)
            .orElseThrow(
                    () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUser_Id(userId).stream()
            .map(rs -> rs.getChannel().getId())
            .toList();

    return channelRepository.findAll().stream()
            .filter(channel ->
                    channel.getType().equals(ChannelType.PUBLIC)
                            || mySubscribedChannelIds.contains(channel.getId())
            )
            .map(channelMapper::toDto)   // toDto() → channelMapper.toDto()
            .toList();
  }

  @Override
  public Channel update(UUID channelId, String newName, String newDescription) {
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(
                    () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new IllegalArgumentException("Private channel cannot be updated");
    }
    channel.update(newName, newDescription);
    return channelRepository.save(channel);
  }

  @Override
  public void delete(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(
                    () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    // Message 삭제 시 cascade로 message_attachments(BinaryContent)도 삭제됨
    messageRepository.deleteAllByChannel_Id(channel.getId());
    readStatusRepository.deleteAllByChannel_Id(channel.getId());
    channelRepository.deleteById(channelId);
  }


}