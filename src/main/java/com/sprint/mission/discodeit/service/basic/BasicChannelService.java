package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(request.name(), request.description(),
        Channel.ChannelType.PUBLIC);
    channelRepository.save(channel);

    return channelMapper.toDto(channel);
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

    return channelMapper.toDto(channel);
  }


  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> joinedChannelIds = readStatusRepository.findByUser_Id(userId).stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType() == ChannelType.PUBLIC || joinedChannelIds.contains(
            channel.getId()))
        .map(channel -> channelMapper.toDto(channel))
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

    return channelMapper.toDto(channel);
  }

  @Override
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    messageRepository.findByChannel_Id(id)
        .forEach(messageRepository::delete);

    readStatusRepository.findByChannel_Id(id)
        .forEach(readStatusRepository::delete);

    channelRepository.delete(channel);
  }
}
