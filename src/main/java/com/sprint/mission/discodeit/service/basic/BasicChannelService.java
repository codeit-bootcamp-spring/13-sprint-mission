package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  public BasicChannelService(ChannelRepository channelRepository,
      ReadStatusRepository readStatusRepository,
      MessageRepository messageRepository, UserRepository userRepository) {
    this.channelRepository = channelRepository;
    this.readStatusRepository = readStatusRepository;
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
  }

  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(request.name(), request.description(),
        Channel.ChannelType.PUBLIC);
    channelRepository.save(channel);

    return convertToDto(channel);
  }

  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(null, null, Channel.ChannelType.PRIVATE);
    channelRepository.save(channel);

    for (UUID userId : request.participantIds()) {

      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UserNotFoundException(userId));

      ReadStatus readStatus = new ReadStatus(
          user, channel, Instant.now()
      );

      readStatusRepository.save(readStatus);
    }

    return convertToDto(channel);
  }


  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> joinedChannelIds = readStatusRepository.findAll().stream()
        .filter(rs -> rs.getUser().getId().equals(userId))
        .map(rs -> rs.getChannel().getId())
        .toList();

    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType() == ChannelType.PUBLIC || joinedChannelIds.contains(
            channel.getId()))
        .map(channel -> {

          Instant lastMessageAt = messageRepository.findAll().stream()
              .filter(m -> m.getChannel() != null && m.getChannel().getId().equals(channel.getId()))
              .map(Message::getCreatedAt)
              .max(Instant::compareTo)
              .orElse(null);

          List<UUID> participantIds = readStatusRepository.findAll().stream()
              .filter(
                  rs -> rs.getChannel() != null && rs.getChannel().getId().equals(channel.getId()))
              .map(rs -> rs.getUser().getId())
              .toList();

          return new ChannelDto(
              channel.getId(),
              channel.getType(),
              channel.getName(),
              channel.getDescription(),
              Collections.emptyList(), // 임시로 넣어둠
              lastMessageAt
          );
        })
        .toList();
  }

  @Override
  public ChannelDto update(UUID id, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      throw new PrivateChannelUnmodifiableException();
    }
    channel.update(request.newName(), request.newDescription());
    channelRepository.save(channel);

    return convertToDto(channel);
  }

  @Override
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    List<Message> messages = messageRepository.findAll().stream()
        .filter(m -> m.getChannel().getId().equals(id))
        .toList();

    messages.forEach(m -> messageRepository.deleteById(m.getId()));

    List<ReadStatus> readStatuses = readStatusRepository.findAll().stream()
        .filter(rs -> rs.getChannel().getId().equals(id))
        .toList();

    readStatuses.forEach(rs -> readStatusRepository.deleteById(rs.getId()));

    channelRepository.deleteById(channel.getId());
  }

  private ChannelDto convertToDto(Channel channel) {

    return new ChannelDto(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        Collections.emptyList(), // 임시로 넣어둠
        null // 임시로 넣어둠
    );
  }
}
